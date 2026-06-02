/* ==========================================
 *  todo-completed.js
 *  완료된 그룹 프로젝트 목록 + 팀장 마늘 분배 모달
 * ========================================== */

let completedGroups = [];
let currentUser = null;

// 현재 마늘 분배 모달에서 다루는 그룹 정보
let activeGroup = null;
let garlicTotal = 0;
let garlicDistMode = "equal";       // "equal" | "custom"
let garlicCustomDist = {};           // { userId: amount }
let garlicMembers = [];              // [{ user_id, nickname }]

/* ---- 유틸 ---- */
function getAvatarInitial(name) {
    return name && name.trim() ? name.trim()[0] : "?";
}

function formatEndDate(deadline) {
    if (!deadline) return "";
    const d = new Date(deadline);
    if (isNaN(d.getTime())) return "";
    return `종료일: ${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

function buildMemberAvatars(group) {
    const members = Array.isArray(group.members) ? group.members : [];
    const visible = members.slice(0, 3).map((m) => {
        const initial = (m.nickname || "?").trim().charAt(0);
        if (m.profile_image) {
            return `<div class="member-avatar"><img src="${m.profile_image}" alt="${m.nickname || ""}"></div>`;
        }
        return `<div class="member-avatar member-avatar--empty">${initial}</div>`;
    }).join("");
    const extra = Math.max((group.member_count || 0) - Math.min(members.length, 3), 0);
    return visible + (extra > 0 ? `<div class="member-more">+${extra}</div>` : "");
}

function getGroupIconStyle(group) {
    if (group.group_color) return `background:${group.group_color};`;
    if (!group.group_icon_url) return "";
    try {
        const parsed = JSON.parse(group.group_icon_url);
        if (parsed.color) return `background:${parsed.color};`;
    } catch (_) { /* ignore */ }
    return "";
}

/* ---- 프로젝트 카드 렌더 ---- */
function createCompletedCard(group) {
    const endDate = formatEndDate(group.deadline);
    const isLeader = currentUser && group.leader_id === currentUser.user_id;

    return `
    <div class="project-card completed" data-group-id="${group.group_id}">
        <div class="project-icon" style="${getGroupIconStyle(group)}">${getAvatarInitial(group.group_name || "?")}</div>
        <div class="project-name-block">
            <div>
                <span class="project-name">${group.group_name || "이름 없는 프로젝트"}</span>
            </div>
            ${endDate ? `<div class="project-end-date">${endDate}</div>` : ""}
        </div>
        <div class="project-progress-block">
            <div class="progress-bar"><div class="progress-fill" style="width:100%"></div></div>
            <span class="project-percent">100 %</span>
        </div>
        <div class="member-avatars">${buildMemberAvatars(group)}</div>
        <div class="project-priority"><span class="project-priority-flag">&#9873;</span></div>
        ${isLeader
            ? `<button class="project-more" type="button" data-group-id="${group.group_id}" aria-label="마늘 분배">&#8942;</button>`
            : `<div></div>`
        }
    </div>`;
}

function renderCompletedProjects(groups) {
    const list = document.getElementById("completedProjectList");
    if (!list) return;

    if (!groups.length) {
        list.innerHTML = '<div class="todo-empty-state">완료된 프로젝트가 없습니다.</div>';
        return;
    }
    list.innerHTML = groups.map(createCompletedCard).join("");
}

/* ---- 완료 프로젝트 로드 ---- */
async function loadCurrentUser() {
    try {
        const data = await fetchTodoJson("/users");
        if (data && data.user_id) {
            currentUser = data;
        }
    } catch (e) {
        console.error("사용자 정보 로드 실패", e);
    }
}

async function loadCompletedGroups() {
    try {
        const data = await fetchTodoJson("/todo-groups?status=COMPLETED");
        completedGroups = data?.groups || [];
    } catch (_) {
        // COMPLETED 필터 미지원 시 전체에서 필터링
        try {
            const data = await fetchTodoJson("/todo-groups");
            const all = data?.groups || [];
            // progress 100% 또는 remaining_garlic_reward == 0 인 것
            completedGroups = all.filter((g) => {
                const total = Number(g.total_garlic_reward || 0);
                const remain = Number(g.remaining_garlic_reward || 0);
                if (total <= 0) return false;
                return Math.round(((total - remain) / total) * 100) >= 100;
            });
        } catch (e) {
            console.error("프로젝트 목록 로드 실패", e);
            completedGroups = [];
        }
    }
    renderCompletedProjects(completedGroups);
}

/* ==========================================
 *  마늘 분배 모달 로직
 * ========================================== */

/* --- 분배 계산 --- */
function buildEqualRewardMap() {
    if (!garlicMembers.length) return {};
    const base = Math.floor(garlicTotal / garlicMembers.length);
    const rem = garlicTotal % garlicMembers.length;
    const map = {};
    garlicMembers.forEach((m, i) => {
        map[m.user_id] = base + (i < rem ? 1 : 0);
    });
    return map;
}

function allocateAmounts(total, userIds, weightMap = {}) {
    if (!userIds.length) return {};
    if (total <= 0) {
        const r = {};
        userIds.forEach((id) => { r[id] = 0; });
        return r;
    }
    const weights = userIds.map((id) => Math.max(0, Number(weightMap[id] ?? 0)));
    const totalW = weights.reduce((s, w) => s + w, 0);
    if (totalW <= 0) {
        const base = Math.floor(total / userIds.length);
        let rem = total % userIds.length;
        const r = {};
        userIds.forEach((id) => {
            r[id] = base + (rem > 0 ? 1 : 0);
            rem = Math.max(0, rem - 1);
        });
        return r;
    }
    const raw = userIds.map((id, i) => {
        const v = (total * weights[i]) / totalW;
        return { id, amount: Math.floor(v), frac: v - Math.floor(v) };
    });
    let rem = total - raw.reduce((s, x) => s + x.amount, 0);
    raw.sort((a, b) => b.frac - a.frac).forEach((x) => {
        if (rem <= 0) return;
        x.amount += 1;
        rem -= 1;
    });
    const result = {};
    raw.forEach((x) => { result[x.id] = x.amount; });
    return result;
}

function redistributeCustom(changedId, requested) {
    if (!garlicMembers.length) { garlicCustomDist = {}; return; }
    if (garlicMembers.length === 1) {
        garlicCustomDist = { [garlicMembers[0].user_id]: garlicTotal };
        return;
    }
    const next = Math.max(0, Math.min(garlicTotal, Number(requested)));
    const others = garlicMembers.map((m) => m.user_id).filter((id) => id !== changedId);
    const wm = {};
    others.forEach((id) => { wm[id] = garlicCustomDist[id] ?? 0; });
    garlicCustomDist = {
        ...allocateAmounts(garlicTotal - next, others, wm),
        [changedId]: next
    };
}

/* --- 렌더 --- */
function renderGarlicModal() {
    const rewardMap = garlicDistMode === "equal" ? buildEqualRewardMap() : garlicCustomDist;

    /* 균등 미리보기 */
    const equalPreview = document.getElementById("garlicEqualPreview");
    if (equalPreview) {
        equalPreview.innerHTML = garlicMembers.map((m) => `
            <div class="dist-preview-item">
                <div class="dist-preview-avatar">${getAvatarInitial(m.nickname)}</div>
                <span class="dist-preview-count">${rewardMap[m.user_id] ?? 0}개</span>
            </div>
        `).join("");
    }

    /* 직접 조정 슬라이더 */
    const slidersEl = document.getElementById("garlicSliders");
    if (slidersEl) {
        slidersEl.innerHTML = garlicMembers.map((m) => {
            const amount = garlicCustomDist[m.user_id] ?? 0;
            const pct = garlicTotal > 0 ? Math.round((amount / garlicTotal) * 100) : 0;
            return `
            <div class="slider-row">
                <div class="slider-avatar">${getAvatarInitial(m.nickname)}</div>
                <span class="slider-name">${m.nickname}</span>
                <div class="slider-track" style="--val:${pct}%">
                    <input type="range" class="slider-input" min="0" max="${garlicTotal}"
                           value="${amount}" data-uid="${m.user_id}" style="--val:${pct}%">
                </div>
                <span class="slider-percent" data-uid-pct="${m.user_id}">${pct}%</span>
                <span class="slider-count" data-uid-cnt="${m.user_id}">${amount}개</span>
            </div>`;
        }).join("");

        slidersEl.querySelectorAll(".slider-input").forEach((input) => {
            input.addEventListener("input", (e) => {
                redistributeCustom(Number(e.target.dataset.uid), Number(e.target.value));
                renderGarlicModal();
            });
        });
    }

    /* 요약 */
    const totalUsed = Object.values(rewardMap).reduce((s, v) => s + v, 0);
    const sumPct = garlicTotal > 0 ? Math.round((totalUsed / garlicTotal) * 100) : 0;
    const sumPctEl = document.getElementById("garlicSumPercent");
    const sumCntEl = document.getElementById("garlicSumCount");
    if (sumPctEl) sumPctEl.textContent = String(sumPct);
    if (sumCntEl) sumCntEl.textContent = String(totalUsed);
}

/* --- 모달 열기 / 닫기 --- */
function openGarlicModal(group) {
    activeGroup = group;
    garlicTotal = Number(group.total_garlic_reward || 0);
    garlicMembers = Array.isArray(group.members)
        ? group.members.map((m) => ({ user_id: m.user_id, nickname: m.nickname || `멤버${m.user_id}` }))
        : [];
    garlicDistMode = "equal";
    garlicCustomDist = {};

    // 커스텀 분배 초기값: 균등으로 초기화
    const equalMap = buildEqualRewardMap();
    garlicMembers.forEach((m) => { garlicCustomDist[m.user_id] = equalMap[m.user_id] ?? 0; });

    // 모달 제목
    const titleEl = document.getElementById("garlicModalProjectName");
    if (titleEl) titleEl.textContent = group.group_name || "프로젝트 완료!";

    // 총 마늘 표시
    const totalEl = document.getElementById("garlicTotalCount");
    if (totalEl) totalEl.textContent = String(garlicTotal);

    // 탭 상태 초기화
    setDistTab("equal");

    document.getElementById("garlicModalOverlay")?.classList.add("open");
}

function closeGarlicModal() {
    document.getElementById("garlicModalOverlay")?.classList.remove("open");
    activeGroup = null;
}

function setDistTab(mode) {
    garlicDistMode = mode;

    document.getElementById("tabEqual")?.classList.toggle("active", mode === "equal");
    document.getElementById("tabCustom")?.classList.toggle("active", mode === "custom");

    const descEl = document.getElementById("garlicDistDesc");
    if (descEl) {
        descEl.textContent = mode === "equal"
            ? "모든 담당자에게 균등하게 분배됩니다."
            : "담당자별로 마늘을 직접 분배할 수 있습니다.";
    }

    document.getElementById("garlicEqualSection")?.classList.toggle("hidden", mode !== "equal");
    document.getElementById("garlicCustomSection")?.classList.toggle("hidden", mode !== "custom");

    renderGarlicModal();
}

/* --- 분배 제출 --- */
async function submitGarlicDistribution() {
    if (!activeGroup) return;

    const rewardMap = garlicDistMode === "equal" ? buildEqualRewardMap() : garlicCustomDist;
    const payload = garlicMembers.map((m) => ({
        user_id: m.user_id,
        reward_amount: rewardMap[m.user_id] ?? 0
    }));

    try {
        await fetchTodoJson(`/todo-groups/${activeGroup.group_id}/garlic-distribution`, {
            method: "POST",
            body: JSON.stringify({ distributions: payload })
        });
        alert("마늘이 성공적으로 분배되었습니다! 🧄");
        closeGarlicModal();
    } catch (e) {
        console.error("마늘 분배 실패", e);
        alert("마늘 분배에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }
}

/* ---- 컨텍스트 메뉴 (⋮ 버튼 → 마늘 분배) ---- */
let activeMenuGroupId = null;

function showCompletedContextMenu(button) {
    const menu = document.getElementById("completedProjectContextMenu");
    if (!menu || !button) return;
    const rect = button.getBoundingClientRect();
    activeMenuGroupId = Number(button.dataset.groupId);
    menu.style.left = `${Math.max(16, rect.right - 148)}px`;
    menu.style.top = `${rect.bottom + 6}px`;
    menu.classList.remove("hidden");
}

function hideCompletedContextMenu() {
    document.getElementById("completedProjectContextMenu")?.classList.add("hidden");
    activeMenuGroupId = null;
}

/* ---- 이벤트 바인딩 ---- */
function bindEvents() {
    /* 프로젝트 카드 클릭 */
    document.getElementById("completedProjectList")?.addEventListener("click", (e) => {
        const moreBtn = e.target.closest(".project-more");
        if (moreBtn) {
            e.stopPropagation();
            showCompletedContextMenu(moreBtn);
            return;
        }
        // 카드 자체 클릭 → 그룹 상세 이동
        const card = e.target.closest(".project-card.completed");
        if (card && card.dataset.groupId) {
            window.location.href = `/todos/detail?groupId=${card.dataset.groupId}&tab=completed`;
        }
    });

    /* 컨텍스트 메뉴: 마늘 분배 */
    document.getElementById("completedDistributeAction")?.addEventListener("click", () => {
        const groupId = activeMenuGroupId;
        hideCompletedContextMenu();
        const group = completedGroups.find((g) => g.group_id === groupId);
        if (group) openGarlicModal(group);
    });

    /* 외부 클릭 → 메뉴 닫기 */
    document.addEventListener("click", (e) => {
        if (!e.target.closest("#completedProjectContextMenu") && !e.target.closest(".project-more")) {
            hideCompletedContextMenu();
        }
    });

    /* 모달 닫기 */
    document.getElementById("garlicModalClose")?.addEventListener("click", closeGarlicModal);
    document.getElementById("garlicModalCancel")?.addEventListener("click", closeGarlicModal);
    document.getElementById("garlicModalOverlay")?.addEventListener("click", (e) => {
        if (e.target.id === "garlicModalOverlay") closeGarlicModal();
    });

    /* 분배 탭 전환 */
    document.getElementById("tabEqual")?.addEventListener("click", () => setDistTab("equal"));
    document.getElementById("tabCustom")?.addEventListener("click", () => setDistTab("custom"));

    /* 분배 제출 */
    document.getElementById("garlicModalSubmit")?.addEventListener("click", submitGarlicDistribution);
}

/* ---- 초기화 ---- */
document.addEventListener("DOMContentLoaded", async () => {
    bindEvents();
    await loadCurrentUser();
    await loadCompletedGroups();
});
