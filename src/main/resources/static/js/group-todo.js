const API_BASE = "";
const queryParams = new URLSearchParams(window.location.search);

const PRESET_CATEGORIES = ["발표 준비", "보고서", "기능 구현", "자료 조사"];
const CATEGORY_META = {
    "발표 준비": { css: "presentation", icon: "P" },
    "보고서": { css: "report", icon: "R" },
    "기능 구현": { css: "dev", icon: "D" },
    "자료 조사": { css: "research", icon: "S" }
};
const PRIORITY_LABELS = {
    HIGH: "높음",
    MEDIUM: "보통",
    LOW: "낮음"
};
const PRIORITY_CSS = {
    HIGH: "high",
    MEDIUM: "medium",
    LOW: "low"
};

let currentGroupId = Number(queryParams.get("groupId") || localStorage.getItem("currentGroupId") || 1);
const currentDetailTab = (queryParams.get("tab") || "in-progress").toLowerCase();
let currentUser = null;
// --- 할 일 상세 모달 (팀원용 등) ---
function formatDeadlineFull(deadline) {
    if (!deadline) return "-";
    const d = new Date(deadline);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    const dow = days[d.getDay()];
    return year + ". " + month + ". " + day + " (" + dow + ")";
}

async function openTodoModal(todoId) {
    try {
        console.log("openTodoModal called", todoId);
        const data = await api("/todos/" + todoId);
        console.log("openTodoModal data", data);
        if (!data) {
            alert("API로부터 할 일 데이터를 받지 못했습니다.");
            return;
        }
        try {
            showTodoDetailModal(data);
        } catch (innerE) {
            console.error("showTodoDetailModal error:", innerE);
            alert("모달 화면 구성 중 에러가 발생했습니다: " + innerE.message);
        }
    } catch (e) {
        console.error(e);
        alert("할 일 정보를 불러오지 못했습니다. 에러: " + e.message);
    }
}

function showTodoDetailModal(todo) {
    const overlay = document.getElementById("mytodoModalOverlay");
    if (!overlay) {
        alert("모달 HTML 요소를 찾을 수 없습니다.");
        return;
    }

    const deadlineFull = formatDeadlineFull(todo.deadline);

    overlay.querySelector(".mtm-title").textContent = todo.todo_name;
    overlay.querySelector(".mtm-category").textContent = todo.category || "";
    overlay.querySelector(".mtm-deadline-val").textContent = deadlineFull;

    const descInput = overlay.querySelector(".mtm-desc-input");
    if (descInput) {
        descInput.value = todo.description || "";
        
        // 내용 변경 시 자동 저장 (PATCH)
        descInput.onchange = async () => {
            try {
                await api(`/todos/${todo.todo_id}`, {
                    method: "PATCH",
                    body: JSON.stringify({
                        todo_name: todo.todo_name,
                        description: descInput.value
                    })
                });
            } catch (e) {
                console.error("설명 업데이트 실패", e);
            }
        };
    }

    const avatarWrap = overlay.querySelector(".mtm-assignee-avatars");
    if (avatarWrap && todo.assignees && todo.assignees.length) {
        avatarWrap.innerHTML = todo.assignees.map(a =>
            '<div class="mtm-assignee-avatar" title="' + a.nickname + '">' + getAvatarInitial(a.nickname) + '</div>'
        ).join('');
    }

    const pct = getProgressPercent(todo) || 0;
    overlay.querySelector(".mtm-progress-fill").style.width = pct + "%";
    overlay.querySelector(".mtm-progress-pct").textContent = Math.floor(pct) + "%";
    overlay.querySelector(".mtm-progress-pct-right").textContent = Math.floor(pct) + "%";

    const assigneeList = overlay.querySelector(".mtm-assignee-list");
    if (assigneeList && todo.assignees && todo.assignees.length) {
        assigneeList.innerHTML = todo.assignees.map(a => {
            const isCompleted = a.status === 'COMPLETED' || todo.status === 'COMPLETED';
            const statusTxt = isCompleted ? '완료' : '진행 중';
            const statusClass = isCompleted ? 'mtm-status--done' : 'mtm-status--progress';
            return [
                '<div class="mtm-assignee-row">',
                '  <div class="mtm-assignee-avatar">' + getAvatarInitial(a.nickname) + '</div>',
                '  <span class="mtm-assignee-name">' + a.nickname + '</span>',
                '  <span class="mtm-assignee-status ' + statusClass + '">' + statusTxt + '</span>',
                '</div>'
            ].join('');
        }).join('');
    }

    const completeBtn = overlay.querySelector(".mtm-complete-btn");
    
    // 본인이 담당자인지 여부와 완료 상태 확인
    const isAssignee = todo.assignees && todo.assignees.some(a => a.user_id === currentUser?.user_id);
    const myAssigneeInfo = todo.assignees && todo.assignees.find(a => a.user_id === currentUser?.user_id);
    const isCompleted = myAssigneeInfo?.status === 'COMPLETED' || todo.status === 'COMPLETED';

    if (isAssignee && !isCompleted) {
        completeBtn.style.display = "block";
    } else {
        completeBtn.style.display = "none";
    }

    completeBtn.onclick = async () => {
        try {
            await api("/todos/" + todo.todo_id + "/complete", {
                method: "PATCH"
            });
            overlay.classList.remove("active");
            await loadTodos(); // 완료 후 목록 갱신
        } catch (e) {
            console.error(e);
            alert("할 일 완료 처리에 실패했습니다.");
        }
    };

    overlay.classList.add("active");
}

let todos = [];
let friendList = [];
let currentProjectMembers = [];
let tempProjectMembers = [];

let isLeader = false;
let editingTodoId = null;
let selectedAssignees = [];
let selectedCategory = "";
let customCategory = "";
let selectedPriority = "MEDIUM";
let garlicReward = 10;
let distributionMode = "equal";
let customDistribution = {};

function getCurrentTodoStatus() {
    return currentDetailTab === "completed" ? "COMPLETED" : "IN_PROGRESS";
}

function getDetailBackUrl() {
    return `/todos/detail?groupId=${currentGroupId}`;
}

function getAccessToken() {
    return localStorage.getItem("accessToken") || "";
}

function getAuthHeaders(extraHeaders = {}) {
    const headers = { ...extraHeaders };
    const token = getAccessToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    if (!(headers instanceof FormData) && !headers["Content-Type"]) {
        headers["Content-Type"] = "application/json";
    }
    return headers;
}

async function api(url, options = {}) {
    const response = await fetch(API_BASE + url, {
        ...options,
        headers: getAuthHeaders(options.headers || {})
    });

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        throw new Error("로그인이 필요합니다.");
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    if (!response.ok) {
        throw new Error(text || `요청에 실패했습니다. (${response.status})`);
    }

    return text ? JSON.parse(text) : null;
}

function getAvatarInitial(name) {
    return name && name.trim() ? name.trim()[0] : "?";
}

function resolveCategoryMeta(category) {
    const label = category || "기타";
    const presetMeta = CATEGORY_META[label];
    if (presetMeta) {
        return presetMeta;
    }

    return {
        css: "research",
        icon: getAvatarInitial(label)
    };
}

function formatDeadline(deadline) {
    if (!deadline) {
        return "";
    }

    const parsed = new Date(deadline);
    if (Number.isNaN(parsed.getTime())) {
        return deadline;
    }

    const dayLabels = ["일", "월", "화", "수", "목", "금", "토"];
    return `마감 ${parsed.getMonth() + 1}/${String(parsed.getDate()).padStart(2, "0")}(${dayLabels[parsed.getDay()]})`;
}

function calculateDday(deadline) {
    if (!deadline) {
        return "-";
    }

    const target = new Date(deadline);
    target.setHours(0, 0, 0, 0);

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const diff = Math.ceil((target.getTime() - today.getTime()) / 86400000);
    if (diff > 0) {
        return `D-${diff}`;
    }
    if (diff === 0) {
        return "D-Day";
    }
    return `D+${Math.abs(diff)}`;
}

function getProjectMember(userId) {
    return currentProjectMembers.find((member) => member.user_id === userId) || null;
}

function getFriend(userId) {
    return friendList.find((friend) => friend.user_id === userId) || null;
}

function upsertProjectMember(member) {
    if (!member || !member.user_id) {
        return;
    }

    const index = currentProjectMembers.findIndex((item) => item.user_id === member.user_id);
    if (index === -1) {
        currentProjectMembers.push(member);
        return;
    }

    currentProjectMembers[index] = {
        ...currentProjectMembers[index],
        ...member
    };
}

function buildDisplayMember(userId, nickname, extra = {}) {
    const knownMember = getProjectMember(userId) || getFriend(userId);
    const displayName = nickname
        || knownMember?.nickname
        || (currentUser && currentUser.user_id === userId ? currentUser.nickname : `멤버 ${userId}`);

    return {
        user_id: userId,
        nickname: displayName,
        avatar: getAvatarInitial(displayName),
        isMe: currentUser ? currentUser.user_id === userId : false,
        ...extra
    };
}

function syncProjectMembersFromTodos() {
    todos.forEach((todo) => {
        (todo.assignees || []).forEach((assignee) => {
            upsertProjectMember(buildDisplayMember(assignee.user_id, assignee.nickname));
        });
    });
}

function getProgressPercent(todo) {
    if (todo.status === "COMPLETED") {
        return 100;
    }
    return Number.isFinite(todo.progress) ? todo.progress : 0;
}

function buildRewardMap() {
    const rewardMap = {};
    if (!selectedAssignees.length) {
        return rewardMap;
    }

    if (distributionMode === "custom") {
        syncCustomDistribution();
        selectedAssignees.forEach((userId) => {
            rewardMap[userId] = customDistribution[userId] ?? 0;
        });
        return rewardMap;
    }

    const base = Math.floor(garlicReward / selectedAssignees.length);
    const remainder = garlicReward % selectedAssignees.length;
    selectedAssignees.forEach((userId, index) => {
        rewardMap[userId] = base + (index < remainder ? 1 : 0);
    });
    return rewardMap;
}

function allocateAmounts(total, userIds, weightMap = {}) {
    const allocation = {};
    if (!userIds.length) {
        return allocation;
    }

    if (total <= 0) {
        userIds.forEach((userId) => {
            allocation[userId] = 0;
        });
        return allocation;
    }

    const weights = userIds.map((userId) => Math.max(0, Number(weightMap[userId] ?? 0)));
    const totalWeight = weights.reduce((sum, weight) => sum + weight, 0);

    if (totalWeight <= 0) {
        const base = Math.floor(total / userIds.length);
        let remainder = total % userIds.length;
        userIds.forEach((userId) => {
            allocation[userId] = base + (remainder > 0 ? 1 : 0);
            remainder = Math.max(0, remainder - 1);
        });
        return allocation;
    }

    const rawAllocations = userIds.map((userId, index) => {
        const rawAmount = (total * weights[index]) / totalWeight;
        const flooredAmount = Math.floor(rawAmount);
        return {
            userId,
            amount: flooredAmount,
            fraction: rawAmount - flooredAmount
        };
    });

    let remainder = total - rawAllocations.reduce((sum, item) => sum + item.amount, 0);
    rawAllocations
        .sort((left, right) => right.fraction - left.fraction)
        .forEach((item) => {
            if (remainder <= 0) {
                return;
            }
            item.amount += 1;
            remainder -= 1;
        });

    rawAllocations.forEach((item) => {
        allocation[item.userId] = item.amount;
    });

    return allocation;
}

function syncCustomDistribution() {
    if (distributionMode !== "custom") {
        return;
    }

    if (!selectedAssignees.length) {
        customDistribution = {};
        return;
    }

    if (selectedAssignees.length === 1) {
        customDistribution = {
            [selectedAssignees[0]]: garlicReward
        };
        return;
    }

    const sanitized = {};
    selectedAssignees.forEach((userId) => {
        sanitized[userId] = Math.max(0, Number(customDistribution[userId] ?? 0));
    });

    customDistribution = allocateAmounts(garlicReward, selectedAssignees, sanitized);
}

function redistributeRewards(changedUserId, requestedAmount) {
    if (!selectedAssignees.length) {
        customDistribution = {};
        return;
    }

    if (selectedAssignees.length === 1) {
        customDistribution = {
            [selectedAssignees[0]]: garlicReward
        };
        return;
    }

    const nextAmount = Math.max(0, Math.min(garlicReward, Number(requestedAmount)));
    const otherUserIds = selectedAssignees.filter((userId) => userId !== changedUserId);
    const remainingReward = garlicReward - nextAmount;

    const weightMap = {};
    otherUserIds.forEach((userId) => {
        weightMap[userId] = customDistribution[userId] ?? 0;
    });

    customDistribution = {
        ...allocateAmounts(remainingReward, otherUserIds, weightMap),
        [changedUserId]: nextAmount
    };
}

function buildAssigneePayload() {
    const rewardMap = buildRewardMap();
    return selectedAssignees.map((userId) => ({
        user_id: userId,
        reward_amount: rewardMap[userId] ?? 0
    }));
}

function renderGroupMemberAvatars() {
    const container = document.getElementById("groupMemberAvatars");
    if (!container) {
        return;
    }

    container.innerHTML = "";
    currentProjectMembers.slice(0, 4).forEach((member) => {
        const avatar = document.createElement("div");
        avatar.className = "group-member-mini";
        avatar.textContent = getAvatarInitial(member.nickname);
        avatar.title = member.nickname;
        container.appendChild(avatar);
    });

    if (currentProjectMembers.length > 4) {
        const more = document.createElement("div");
        more.className = "group-member-mini";
        more.textContent = `+${currentProjectMembers.length - 4}`;
        container.appendChild(more);
    }
}

function renderEmptyTodoState(message) {
    const list = document.getElementById("todoList");
    if (!list) {
        return;
    }
    list.innerHTML = `<div style="text-align:center;padding:60px;color:#8b8276;">${message}</div>`;
}

function renderTodoList() {
    const list = document.getElementById("todoList");
    const count = document.getElementById("inProgressCount");
    if (!list) {
        return;
    }

    list.innerHTML = "";
    if (count) {
        count.textContent = String(todos.length);
    }

    if (!todos.length) {
        renderEmptyTodoState("등록된 할 일이 없습니다.");
        return;
    }

    todos.forEach((todo) => {
        const progress = getProgressPercent(todo);
        const assignees = Array.isArray(todo.assignees) ? todo.assignees : [];
        const garlicTotal = todo.garlic_reward ?? 0;

        const assigneeHtml = assignees.slice(0, 3).map((assignee) => (
            `<div class="assignee-avatar" title="${assignee.nickname || ""}">${getAvatarInitial(assignee.nickname)}</div>`
        )).join("");

        const moreCount = assignees.length - 3;
        const moreHtml = moreCount > 0 ? `<div class="assignee-avatar assignee-more">+${moreCount}</div>` : "";
        const moreBtnHtml = isLeader ? `<button class="todo-more-btn" data-todo-id="${todo.todo_id}" type="button">⋮</button>` : "";

        const item = document.createElement("div");
        item.className = "todo-item";
        item.dataset.todoId = String(todo.todo_id);
        item.innerHTML = `
            <div class="todo-info">
                <span class="todo-name">${todo.todo_name || "이름 없는 할 일"}</span>
                <span class="todo-deadline">${formatDeadline(todo.deadline)}</span>
            </div>
            <div class="todo-progress">
                <div class="progress-bar"><div class="progress-fill" style="width:${progress}%"></div></div>
                <span class="progress-text">${progress}%</span>
            </div>
            <div class="todo-assignees">${assigneeHtml}${moreHtml}</div>
            ${moreBtnHtml}
        `;

        item.addEventListener("click", (e) => {
            if (e.target.closest('.todo-more-btn')) {
                return;
            }
            if (typeof openTodoModal === "function") {
                openTodoModal(todo.todo_id);
            }
        });
        item.style.cursor = "pointer";

        list.appendChild(item);
    });
}

function showContextMenu(event, todoId) {
    const menu = document.getElementById("contextMenu");
    if (!menu) {
        return;
    }

    event.stopPropagation();
    menu.classList.remove("hidden");
    menu.style.left = `${event.clientX - 60}px`;
    menu.style.top = `${event.clientY + 4}px`;
    menu.dataset.todoId = String(todoId);
}

function hideContextMenu() {
    document.getElementById("contextMenu")?.classList.add("hidden");
}

async function loadCurrentUser() {
    const data = await api("/users");
    if (!data || !data.user_id) {
        return;
    }

    currentUser = {
        user_id: data.user_id,
        nickname: data.nickname || "나",
        avatar: getAvatarInitial(data.nickname || "나"),
        isMe: true
    };
    upsertProjectMember(currentUser);
}

async function loadFriends() {
    const data = await api("/friends");
    friendList = Array.isArray(data?.friends)
        ? data.friends.map((friend) => ({
            user_id: friend.user_id,
            nickname: friend.nickname || `친구 ${friend.user_id}`,
            avatar: getAvatarInitial(friend.nickname || "친")
        }))
        : [];
}

async function loadGroupContext() {
    const data = await api("/todo-groups");
    if (!Array.isArray(data?.groups)) {
        return;
    }

    const group = data.groups.find((item) => item.group_id === currentGroupId);
    if (!group) {
        return;
    }

    const groupName = document.getElementById("groupName");
    const ddayBadge = document.getElementById("ddayBadge");
    const projectGarlicTotal = document.getElementById("projectGarlicTotal");
    const titleEl = document.querySelector(".todo-title");

    isLeader = (group.leader_id === currentUser?.user_id);

    const btnAddTodo = document.getElementById("btnAddTodo");
    if (btnAddTodo) {
        btnAddTodo.style.display = isLeader ? "flex" : "none";
    }

    if (titleEl) {
        titleEl.textContent = group.group_name || "프로젝트";
    }

    if (groupName) {
        groupName.textContent = group.group_name || "프로젝트";
    }
    if (ddayBadge) {
        ddayBadge.textContent = calculateDday(group.deadline);
    }
    if (projectGarlicTotal) {
        projectGarlicTotal.textContent = String(group.total_garlic_reward ?? 0);
    }

    (group.members || []).forEach((member) => {
        upsertProjectMember(buildDisplayMember(member.user_id, member.nickname));
    });

    renderGroupMemberAvatars();
}

async function loadTodos() {
    try {
        const data = await api(`/todo-groups/${currentGroupId}/todos?status=${getCurrentTodoStatus()}`);
        todos = Array.isArray(data?.todos) ? data.todos : [];
        syncProjectMembersFromTodos();
        renderGroupMemberAvatars();
        renderTodoList();
    } catch (error) {
        console.error(error);
        todos = [];
        renderEmptyTodoState("할 일 목록을 불러오지 못했습니다.");
        alert("할 일 목록을 불러오지 못했습니다.");
    }
}

async function loadMemo() {
    try {
        const data = await api(`/todo-groups/${currentGroupId}/note`);
        const textarea = document.getElementById("memoTextarea");
        if (textarea) {
            textarea.textContent = data?.content || "";
        }
    } catch (error) {
        console.error(error);
    }
}

let memoTimer = null;

function autoSaveMemo() {
    clearTimeout(memoTimer);
    memoTimer = window.setTimeout(async () => {
        try {
            const textarea = document.getElementById("memoTextarea");
            await api(`/todo-groups/${currentGroupId}/note`, {
                method: "PUT",
                body: JSON.stringify({ content: textarea?.textContent || "" })
            });

            const footer = document.getElementById("memoFooter");
            const now = new Date();
            if (footer) {
                footer.textContent = `자동 저장됨 · 오늘 ${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
            }
        } catch (error) {
            console.error(error);
        }
    }, 800);
}

function updateCharCounts() {
    document.getElementById("titleCharCount").textContent = String(document.getElementById("todoTitleInput").value.length);
}

function ensureCustomCategoryChip() {
    const container = document.getElementById("categoryChips");
    const addButton = document.getElementById("addCategoryBtn");
    const existing = container.querySelector(".chip-custom");

    if (!customCategory) {
        existing?.remove();
        return;
    }

    if (existing) {
        existing.dataset.cat = customCategory;
        existing.textContent = customCategory;
        return;
    }

    const chip = document.createElement("button");
    chip.className = "chip chip-custom";
    chip.type = "button";
    chip.dataset.cat = customCategory;
    chip.textContent = customCategory;
    container.insertBefore(chip, addButton);
}

function updateCategoryChips() {
    ensureCustomCategoryChip();
    document.querySelectorAll("#categoryChips .chip").forEach((chip) => {
        chip.classList.toggle("active", chip.dataset.cat === selectedCategory);
    });
}

function promptCustomCategory() {
    const entered = window.prompt("카테고리 이름을 입력하세요.", customCategory || "");
    if (!entered) {
        return;
    }

    const trimmed = entered.trim();
    if (!trimmed) {
        return;
    }

    customCategory = trimmed;
    selectedCategory = trimmed;
    updateCategoryChips();
}

function updatePriorityDisplay() {
    document.getElementById("priorityText").textContent = PRIORITY_LABELS[selectedPriority] || "보통";
}

function updateGarlicDisplay() {
    const el = document.getElementById("garlicCount");
    if (el) {
        el.textContent = String(garlicReward);
    }
}

function updateDatePlaceholder() {
    const value = document.getElementById("todoDeadlineInput").value;
    document.getElementById("datePlaceholder").style.display = value ? "none" : "block";
}

function updateAssigneeDisplay() {
    const area = document.getElementById("assigneeArea");
    const addButton = document.getElementById("assigneeAddBtn");
    area.querySelectorAll(".assignee-selected").forEach((node) => node.remove());

    selectedAssignees.forEach((userId) => {
        const member = getProjectMember(userId);
        if (!member) {
            return;
        }

        const avatar = document.createElement("div");
        avatar.className = "assignee-selected";
        avatar.textContent = getAvatarInitial(member.nickname);
        avatar.title = member.nickname;
        area.insertBefore(avatar, addButton);
    });
}

function recalculateDistributionSummary() {
    const rewardMap = buildRewardMap();
    const totalReward = selectedAssignees.reduce((sum, userId) => sum + (rewardMap[userId] ?? 0), 0);
    const totalPercent = garlicReward > 0
        ? Math.round((totalReward / garlicReward) * 100)
        : 0;
    document.getElementById("distSumPercent").textContent = String(totalPercent);
    document.getElementById("distSumCount").textContent = String(totalReward);
}

function onSliderChange(event) {
    const userId = Number(event.target.dataset.uid);
    const value = Number(event.target.value);
    redistributeRewards(userId, value);
    updateDistribution();
}

function updateDistribution() {
    const equalPreview = document.getElementById("distEqualPreview");
    const sliders = document.getElementById("distSliders");
    syncCustomDistribution();
    const rewardMap = buildRewardMap();

    equalPreview.innerHTML = "";
    sliders.innerHTML = "";

    selectedAssignees.forEach((userId) => {
        const member = getProjectMember(userId);
        if (!member) {
            return;
        }

        equalPreview.innerHTML += `
            <div class="dist-preview-item">
                <div class="dist-preview-avatar">${getAvatarInitial(member.nickname)}</div>
                <span class="dist-preview-count">${rewardMap[userId] ?? 0}개</span>
            </div>
        `;
    });

    selectedAssignees.forEach((userId) => {
        const member = getProjectMember(userId);
        if (!member) {
            return;
        }

        const rewardAmount = rewardMap[userId] ?? 0;
        const percent = garlicReward > 0
            ? Math.round((rewardAmount / garlicReward) * 100)
            : 0;
        sliders.innerHTML += `
            <div class="slider-row">
                <div class="slider-avatar">${getAvatarInitial(member.nickname)}</div>
                <span class="slider-name">${member.nickname}</span>
                <div class="slider-track" style="--val:${percent}%">
                    <input type="range" class="slider-input" min="0" max="${garlicReward}" value="${rewardAmount}" data-uid="${userId}" style="--val:${percent}%">
                </div>
                <span class="slider-percent" data-uid-pct="${userId}">${percent}%</span>
                <span class="slider-count" data-uid-cnt="${userId}">${rewardAmount}개</span>
            </div>
        `;
    });

    sliders.querySelectorAll(".slider-input").forEach((input) => {
        input.addEventListener("input", onSliderChange);
    });

    recalculateDistributionSummary();
}

function showAssigneeDropdown() {
    const dropdown = document.getElementById("assigneeDropdown");
    const button = document.getElementById("assigneeAddBtn");
    const rect = button.getBoundingClientRect();

    dropdown.style.left = `${rect.left}px`;
    dropdown.style.top = `${rect.bottom + 4}px`;
    dropdown.classList.remove("hidden");
    renderAssigneeOptions();
}

function hideAssigneeDropdown() {
    document.getElementById("assigneeDropdown").classList.add("hidden");
}

function renderAssigneeOptions() {
    const container = document.getElementById("assigneeOptions");
    const keyword = document.getElementById("assigneeSearch").value.toLowerCase();
    container.innerHTML = "";

    currentProjectMembers
        .filter((member) => member.nickname.toLowerCase().includes(keyword))
        .forEach((member) => {
            const isSelected = selectedAssignees.includes(member.user_id);
            const option = document.createElement("div");
            option.className = `assignee-opt ${isSelected ? "selected" : ""}`;
            option.innerHTML = `
                <div class="assignee-opt-avatar">${getAvatarInitial(member.nickname)}</div>
                <span class="assignee-opt-name">${member.nickname}</span>
                ${isSelected ? '<span class="assignee-opt-check">✓</span>' : ""}
            `;

            option.addEventListener("click", () => {
                if (isSelected) {
                    selectedAssignees = selectedAssignees.filter((id) => id !== member.user_id);
                } else {
                    selectedAssignees.push(member.user_id);
                }
                updateAssigneeDisplay();
                updateDistribution();
                renderAssigneeOptions();
            });

            container.appendChild(option);
        });
}

function applyTodoDataToForm(todoData) {
    document.getElementById("todoTitleInput").value = todoData?.todo_name || "";
    document.getElementById("todoDeadlineInput").value = todoData?.deadline || "";

    const category = todoData?.category || "";
    selectedCategory = category;
    customCategory = category && !PRESET_CATEGORIES.includes(category) ? category : "";
    selectedPriority = todoData?.priority || "MEDIUM";
    garlicReward = todoData?.garlic_reward || 10;
    distributionMode = todoData?.distribution_type === "CUSTOM" ? "custom" : "equal";
    selectedAssignees = Array.isArray(todoData?.assignees)
        ? todoData.assignees.map((assignee) => assignee.user_id)
        : [];

    customDistribution = {};
    (todoData?.assignees || []).forEach((assignee) => {
        if (assignee.reward_amount != null) {
            customDistribution[assignee.user_id] = assignee.reward_amount;
        }
    });
}

function openModal(mode, todoData = null) {
    editingTodoId = mode === "edit" ? todoData?.todo_id : null;
    document.getElementById("todoModal").classList.add("show");

    document.getElementById("modalTitle").textContent = mode === "edit" ? "할 일 수정하기" : "할 일 추가하기";
    document.getElementById("modalSubmitText").textContent = mode === "edit" ? "수정하기" : "추가하기";

    applyTodoDataToForm(todoData);

    updateCharCounts();
    updateGarlicDisplay();
    updateDatePlaceholder();
    updateAssigneeDisplay();
}

function closeModal() {
    document.getElementById("todoModal").classList.remove("show");
    editingTodoId = null;
}

async function submitTodo() {
    const todoName = document.getElementById("todoTitleInput").value.trim();
    if (!todoName) {
        alert("할 일 제목을 입력해주세요.");
        return;
    }

    if (!selectedAssignees.length) {
        alert("담당자를 최소 1명 선택해주세요.");
        return;
    }

    const assignees = buildAssigneePayload();

    const body = {
        todo_name: todoName,
        deadline: document.getElementById("todoDeadlineInput").value || null,
        garlic_reward: 0,
        priority: selectedPriority,
        category: selectedCategory || null,
        distribution_type: "EVEN",
        assignees
    };

    try {
        if (editingTodoId) {
            body.status = "IN_PROGRESS";
            await api(`/todos/${editingTodoId}`, {
                method: "PATCH",
                body: JSON.stringify(body)
            });
        } else {
            await api(`/todo-groups/${currentGroupId}/todos`, {
                method: "POST",
                body: JSON.stringify(body)
            });
        }

        closeModal();
        await loadTodos();
    } catch (error) {
        console.error(error);
        alert(error.message || "할 일을 저장하지 못했습니다.");
    }
}

async function deleteTodo(todoId) {
    if (!confirm("정말 삭제하시겠습니까?")) {
        return;
    }

    try {
        await api(`/todos/${todoId}`, { method: "DELETE" });
        await loadTodos();
    } catch (error) {
        console.error(error);
        alert(error.message || "할 일을 삭제하지 못했습니다.");
    }
}

function openMemberModal() {
    tempProjectMembers = currentProjectMembers.map((member) => ({ ...member }));
    document.getElementById("memberModal").classList.add("show");
    renderMemberModal();
}

function closeMemberModal() {
    document.getElementById("memberModal").classList.remove("show");
}

function renderMemberModal() {
    const friendListArea = document.getElementById("friendListArea");
    const projectMemberListArea = document.getElementById("projectMemberListArea");
    const keyword = document.getElementById("memberSearchInput").value.toLowerCase();

    friendListArea.innerHTML = "";
    projectMemberListArea.innerHTML = "";

    const filteredFriends = friendList.filter((friend) => friend.nickname.toLowerCase().includes(keyword));
    document.getElementById("friendCountLabel").textContent = `내 친구 (${friendList.length})`;
    document.getElementById("projectMemberCountLabel").textContent = `프로젝트 멤버 (${tempProjectMembers.length}/10)`;
    document.getElementById("memberConfirmCount").textContent = String(tempProjectMembers.length);

    filteredFriends.forEach((friend) => {
        const isAdded = tempProjectMembers.some((member) => member.user_id === friend.user_id);
        const row = document.createElement("div");
        row.className = "member-row";
        row.innerHTML = `
            <div class="member-row-avatar">${getAvatarInitial(friend.nickname)}</div>
            <span class="member-row-name">${friend.nickname}</span>
            <button class="member-row-action ${isAdded ? "check" : "add"}" type="button">${isAdded ? "✓" : "+"}</button>
        `;
        row.addEventListener("click", () => {
            if (isAdded) {
                tempProjectMembers = tempProjectMembers.filter((member) => member.user_id !== friend.user_id);
            } else if (tempProjectMembers.length < 10) {
                tempProjectMembers.push(buildDisplayMember(friend.user_id, friend.nickname));
            }
            renderMemberModal();
        });
        friendListArea.appendChild(row);
    });

    tempProjectMembers.forEach((member) => {
        const row = document.createElement("div");
        row.className = "member-row";
        row.innerHTML = `
            <div class="member-row-avatar ${member.isMe ? "me" : ""}">${getAvatarInitial(member.nickname)}</div>
            <span class="member-row-name">${member.nickname}</span>
            ${member.isMe ? '<span class="member-row-badge">나</span>' : ""}
            ${member.isMe ? "" : `<button class="member-row-action remove" data-user-id="${member.user_id}" type="button">−</button>`}
        `;

        const removeButton = row.querySelector(".member-row-action.remove");
        if (removeButton) {
            removeButton.addEventListener("click", (event) => {
                event.stopPropagation();
                tempProjectMembers = tempProjectMembers.filter((item) => item.user_id !== member.user_id);
                renderMemberModal();
            });
        }
        projectMemberListArea.appendChild(row);
    });
}

function confirmMemberChanges() {
    alert("멤버 변경 API는 아직 연결되지 않았습니다. 현재는 조회용 화면입니다.");
    closeMemberModal();
}

function removeAllProjectMembers() {
    tempProjectMembers = tempProjectMembers.filter((member) => member.isMe);
    renderMemberModal();
}

function syncDetailTabState() {
    document.querySelectorAll("#tabBar .tab-item").forEach((tab) => {
        const isActive = (currentDetailTab === "completed" && tab.dataset.tab === "completed")
            || (currentDetailTab !== "completed" && tab.dataset.tab === "in-progress");
        tab.classList.toggle("active", isActive);
    });

    const addTodoButton = document.getElementById("addTodoBtn");
    if (addTodoButton) {
        addTodoButton.style.display = currentDetailTab === "completed" ? "none" : "";
    }
}

function initDetailTabs() {
    const tabBar = document.getElementById("tabBar");
    if (!tabBar) {
        return;
    }

    syncDetailTabState();

    tabBar.addEventListener("click", (event) => {
        const tab = event.target.closest(".tab-item");
        if (!tab) {
            return;
        }

        if (tab.dataset.tab === "in-progress") {
            window.location.href = `/todos/detail?groupId=${currentGroupId}&tab=in-progress`;
            return;
        }
        if (tab.dataset.tab === "completed") {
            window.location.href = `/todos/detail?groupId=${currentGroupId}&tab=completed`;
            return;
        }
    });
}

async function bootstrapGroupTodoPage() {
    localStorage.setItem("currentGroupId", String(currentGroupId));

    try {
        await loadCurrentUser();
    } catch (error) {
        console.error(error);
    }

    try {
        await loadFriends();
    } catch (error) {
        console.error(error);
        friendList = [];
    }

    try {
        await loadGroupContext();
    } catch (error) {
        console.error(error);
    }

    await loadTodos();
    await loadMemo();
}

document.addEventListener("DOMContentLoaded", () => {
    initDetailTabs();
    bootstrapGroupTodoPage();

    document.getElementById("memoTextarea")?.addEventListener("input", autoSaveMemo);
    document.getElementById("addTodoBtn")?.addEventListener("click", () => openModal("add"));

    document.getElementById("modalCloseBtn")?.addEventListener("click", closeModal);
    document.getElementById("modalCancelBtn")?.addEventListener("click", closeModal);
    document.getElementById("todoModal")?.addEventListener("click", (event) => {
        if (event.target.id === "todoModal") {
            closeModal();
        }
    });
    document.getElementById("modalSubmitBtn")?.addEventListener("click", submitTodo);

    document.getElementById("todoTitleInput")?.addEventListener("input", updateCharCounts);
    document.getElementById("todoDeadlineInput")?.addEventListener("change", updateDatePlaceholder);

    document.getElementById("garlicMinus")?.addEventListener("click", () => {
        if (garlicReward <= 1) {
            return;
        }
        garlicReward -= 1;
        updateGarlicDisplay();
    });

    document.getElementById("garlicPlus")?.addEventListener("click", () => {
        garlicReward += 1;
        updateGarlicDisplay();
    });

    document.getElementById("assigneeAddBtn")?.addEventListener("click", (event) => {
        event.stopPropagation();
        showAssigneeDropdown();
    });
    document.getElementById("assigneeSearch")?.addEventListener("input", renderAssigneeOptions);

    document.getElementById("todoList")?.addEventListener("click", (event) => {
        const moreButton = event.target.closest(".todo-more-btn");
        if (!moreButton) {
            return;
        }
        showContextMenu(event, moreButton.dataset.todoId);
    });

    document.getElementById("contextEdit")?.addEventListener("click", async () => {
        const todoId = document.getElementById("contextMenu")?.dataset.todoId;
        hideContextMenu();

        try {
            const detail = await api(`/todos/${todoId}`);
            openModal("edit", detail);
        } catch (error) {
            console.error(error);
            alert(error.message || "할 일 상세를 불러오지 못했습니다.");
        }
    });

    document.getElementById("contextDelete")?.addEventListener("click", () => {
        const todoId = document.getElementById("contextMenu")?.dataset.todoId;
        hideContextMenu();
        deleteTodo(todoId);
    });

    document.addEventListener("click", (event) => {
        if (!event.target.closest(".context-menu") && !event.target.closest(".todo-more-btn")) {
            hideContextMenu();
        }
        if (!event.target.closest(".assignee-dropdown") && !event.target.closest(".assignee-add-btn")) {
            hideAssigneeDropdown();
        }
    });

    document.getElementById("openMemberModal")?.addEventListener("click", openMemberModal);
    document.getElementById("memberModalClose")?.addEventListener("click", closeMemberModal);
    document.getElementById("memberModalCancel")?.addEventListener("click", closeMemberModal);
    document.getElementById("memberRemoveAll")?.addEventListener("click", removeAllProjectMembers);
    document.getElementById("memberSearchInput")?.addEventListener("input", renderMemberModal);
    document.getElementById("memberModalConfirm")?.addEventListener("click", confirmMemberChanges);
    document.getElementById("memberModal")?.addEventListener("click", (event) => {
        if (event.target.id === "memberModal") {
            closeMemberModal();
        }
    });

    // 개인 할 일 모달 닫기 이벤트 추가
    const overlay = document.getElementById("mytodoModalOverlay");
    if (overlay) {
        overlay.querySelector(".mtm-close")?.addEventListener("click", () => {
            overlay.classList.remove("active");
        });
        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) overlay.classList.remove("active");
        });
    }
});


