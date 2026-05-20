const API_BASE = "";
const queryParams = new URLSearchParams(window.location.search);

let currentGroupId = Number(queryParams.get("groupId") || localStorage.getItem("currentGroupId") || 1);
let currentUser = null;
let todos = [];
let friendList = [];
let currentProjectMembers = [];
let tempProjectMembers = [];

let editingTodoId = null;
let selectedAssignees = [];
let selectedCategory = "";
let selectedPriority = "MEDIUM";
let garlicReward = 10;
let distributionMode = "equal";
let customDistribution = {};

const CATEGORY_META = {
    "학교": { css: "report", icon: "S" },
    "대외활동": { css: "presentation", icon: "A" },
    "스터디": { css: "dev", icon: "T" },
    "개인": { css: "research", icon: "P" },
    "기타": { css: "research", icon: "E" }
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

function getAccessToken() {
    return localStorage.getItem("accessToken") || "";
}

function getAuthHeaders(extraHeaders = {}) {
    const headers = { ...extraHeaders };
    const token = getAccessToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    return headers;
}

async function api(url, options = {}) {
    const headers = getAuthHeaders(options.headers || {});
    if (!(options.body instanceof FormData) && !headers["Content-Type"]) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(API_BASE + url, {
        ...options,
        headers
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

function formatDeadline(deadline) {
    if (!deadline) {
        return "";
    }

    const date = new Date(deadline);
    if (Number.isNaN(date.getTime())) {
        return deadline;
    }

    const labels = ["일", "월", "화", "수", "목", "금", "토"];
    return `마감 ${date.getMonth() + 1}/${String(date.getDate()).padStart(2, "0")}(${labels[date.getDay()]})`;
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
        selectedAssignees.forEach((userId) => {
            const percent = customDistribution[userId] ?? 0;
            rewardMap[userId] = Math.round(garlicReward * percent / 100);
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
        count.textContent = String(todos.filter((todo) => todo.status !== "COMPLETED").length);
    }

    if (!todos.length) {
        renderEmptyTodoState("등록된 할 일이 없습니다.");
        return;
    }

    todos.forEach((todo) => {
        const categoryLabel = todo.category || "기타";
        const categoryMeta = CATEGORY_META[categoryLabel] || CATEGORY_META["기타"];
        const priorityCss = PRIORITY_CSS[todo.priority] || "medium";
        const priorityLabel = PRIORITY_LABELS[todo.priority] || "보통";
        const progress = getProgressPercent(todo);
        const assignees = Array.isArray(todo.assignees) ? todo.assignees : [];

        const assigneeHtml = assignees.slice(0, 3).map((assignee) => (
            `<div class="assignee-avatar" title="${assignee.nickname || ""}">${getAvatarInitial(assignee.nickname)}</div>`
        )).join("");

        const moreCount = assignees.length - 3;
        const moreHtml = moreCount > 0 ? `<div class="assignee-avatar assignee-more">+${moreCount}</div>` : "";

        const item = document.createElement("div");
        item.className = "todo-item";
        item.dataset.todoId = String(todo.todo_id);
        item.innerHTML = `
            <div class="todo-status-icon ${categoryMeta.css}">${categoryMeta.icon}</div>
            <div class="todo-info">
                <span class="todo-name">${todo.todo_name || "이름 없는 할 일"}</span>
                <span class="todo-deadline">${formatDeadline(todo.deadline)}</span>
            </div>
            <span class="todo-category ${categoryMeta.css}">${categoryLabel}</span>
            <div class="todo-progress">
                <div class="progress-bar"><div class="progress-fill ${priorityCss}" style="width:${progress}%"></div></div>
                <span class="progress-text">${progress}%</span>
            </div>
            <div class="todo-assignees">${assigneeHtml}${moreHtml}</div>
            <div class="todo-priority">
                <span class="priority-flag ${priorityCss}">⚑</span>
                <span class="priority-label ${priorityCss}">${priorityLabel}</span>
            </div>
            <button class="todo-more-btn" data-todo-id="${todo.todo_id}" type="button">⋮</button>
        `;
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
        upsertProjectMember(buildDisplayMember(member.user_id));
    });

    renderGroupMemberAvatars();
}

async function loadTodos() {
    try {
        const data = await api(`/todo-groups/${currentGroupId}/todos?status=IN_PROGRESS`);
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
    document.getElementById("descCharCount").textContent = String(document.getElementById("todoDescInput").value.length);
}

function updateCategoryChips() {
    document.querySelectorAll("#categoryChips .chip").forEach((chip) => {
        chip.classList.toggle("active", chip.dataset.cat === selectedCategory);
    });
}

function updatePriorityDisplay() {
    document.getElementById("priorityText").textContent = PRIORITY_LABELS[selectedPriority] || "보통";
}

function updateGarlicDisplay() {
    document.getElementById("garlicCount").textContent = String(garlicReward);
    document.getElementById("distTotalGarlic").textContent = String(garlicReward);
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
    const totalPercent = distributionMode === "custom"
        ? selectedAssignees.reduce((sum, userId) => sum + (customDistribution[userId] ?? 0), 0)
        : (selectedAssignees.length ? 100 : 0);

    const totalReward = selectedAssignees.reduce((sum, userId) => sum + (rewardMap[userId] ?? 0), 0);
    document.getElementById("distSumPercent").textContent = String(totalPercent);
    document.getElementById("distSumCount").textContent = String(totalReward);
}

function onSliderChange(event) {
    const userId = Number(event.target.dataset.uid);
    const value = Number(event.target.value);
    customDistribution[userId] = value;
    event.target.style.setProperty("--val", `${value}%`);
    event.target.closest(".slider-track").style.setProperty("--val", `${value}%`);

    const rewardMap = buildRewardMap();
    const percent = document.querySelector(`[data-uid-pct="${userId}"]`);
    const count = document.querySelector(`[data-uid-cnt="${userId}"]`);
    if (percent) {
        percent.textContent = `${value}%`;
    }
    if (count) {
        count.textContent = `${rewardMap[userId] ?? 0}개`;
    }
    recalculateDistributionSummary();
}

function updateDistribution() {
    const equalPreview = document.getElementById("distEqualPreview");
    const sliders = document.getElementById("distSliders");
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

    const defaultPercent = selectedAssignees.length ? Math.floor(100 / selectedAssignees.length) : 0;
    selectedAssignees.forEach((userId) => {
        const member = getProjectMember(userId);
        if (!member) {
            return;
        }

        const percent = customDistribution[userId] ?? defaultPercent;
        sliders.innerHTML += `
            <div class="slider-row">
                <div class="slider-avatar">${getAvatarInitial(member.nickname)}</div>
                <span class="slider-name">${member.nickname}</span>
                <div class="slider-track" style="--val:${percent}%">
                    <input type="range" class="slider-input" min="0" max="100" value="${percent}" data-uid="${userId}" style="--val:${percent}%">
                </div>
                <span class="slider-percent" data-uid-pct="${userId}">${percent}%</span>
                <span class="slider-count" data-uid-cnt="${userId}">${rewardMap[userId] ?? 0}개</span>
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

function openModal(mode, todoData = null) {
    editingTodoId = mode === "edit" ? todoData?.todo_id : null;
    document.getElementById("todoModal").classList.add("show");

    document.getElementById("modalTitle").textContent = mode === "edit" ? "할 일 수정하기" : "할 일 추가하기";
    document.getElementById("modalSubmitText").textContent = mode === "edit" ? "수정하기" : "추가하기";

    document.getElementById("todoTitleInput").value = todoData?.todo_name || "";
    document.getElementById("todoDescInput").value = todoData?.description || "";
    document.getElementById("todoDeadlineInput").value = todoData?.deadline || "";

    selectedCategory = todoData?.category || "";
    selectedPriority = todoData?.priority || "MEDIUM";
    garlicReward = todoData?.garlic_reward || 10;
    distributionMode = todoData?.distribution_type === "CUSTOM" ? "custom" : "equal";
    selectedAssignees = Array.isArray(todoData?.assignees)
        ? todoData.assignees.map((assignee) => assignee.user_id)
        : [];

    customDistribution = {};
    (todoData?.assignees || []).forEach((assignee) => {
        if (assignee.reward_amount != null && garlicReward > 0) {
            customDistribution[assignee.user_id] = Math.round((assignee.reward_amount / garlicReward) * 100);
        }
    });

    document.getElementById("distEqualTab").classList.toggle("active", distributionMode === "equal");
    document.getElementById("distCustomTab").classList.toggle("active", distributionMode === "custom");
    document.getElementById("distEqualView").classList.toggle("hidden", distributionMode !== "equal");
    document.getElementById("distCustomView").classList.toggle("hidden", distributionMode !== "custom");

    updateCharCounts();
    updateCategoryChips();
    updatePriorityDisplay();
    updateGarlicDisplay();
    updateDatePlaceholder();
    updateAssigneeDisplay();
    updateDistribution();
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
    if (distributionMode === "custom") {
        const totalReward = assignees.reduce((sum, assignee) => sum + (assignee.reward_amount || 0), 0);
        if (totalReward !== garlicReward) {
            alert("직접 조절 분배의 총합은 마늘 보상과 같아야 합니다.");
            return;
        }
    }

    const body = {
        todo_name: todoName,
        description: document.getElementById("todoDescInput").value.trim(),
        deadline: document.getElementById("todoDeadlineInput").value || null,
        garlic_reward: garlicReward,
        priority: selectedPriority,
        category: selectedCategory || null,
        distribution_type: distributionMode === "custom" ? "CUSTOM" : "EVEN",
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
    alert("멤버 변경 API는 아직 연결되지 않았습니다. 현재 화면에서는 조회만 실데이터를 사용합니다.");
    closeMemberModal();
}

function removeAllProjectMembers() {
    tempProjectMembers = tempProjectMembers.filter((member) => member.isMe);
    renderMemberModal();
}

function initDetailTabs() {
    const tabBar = document.getElementById("tabBar");
    if (!tabBar) {
        return;
    }

    tabBar.addEventListener("click", (event) => {
        const tab = event.target.closest(".tab-item");
        if (!tab) {
            return;
        }

        if (tab.dataset.tab === "in-progress") {
            window.location.href = `/todos/detail?groupId=${currentGroupId}`;
            return;
        }
        if (tab.dataset.tab === "completed") {
            window.location.href = "/todos/completed";
            return;
        }
        if (tab.dataset.tab === "calendar") {
            window.location.href = "/todos/calendar";
            return;
        }
        if (tab.dataset.tab === "files") {
            window.location.href = "/todos/files";
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
    document.getElementById("todoDescInput")?.addEventListener("input", updateCharCounts);
    document.getElementById("todoDeadlineInput")?.addEventListener("change", updateDatePlaceholder);

    document.getElementById("categoryChips")?.addEventListener("click", (event) => {
        const chip = event.target.closest(".chip");
        if (!chip) {
            return;
        }

        selectedCategory = selectedCategory === chip.dataset.cat ? "" : chip.dataset.cat;
        updateCategoryChips();
    });

    document.getElementById("prioritySelect")?.addEventListener("click", () => {
        document.getElementById("priorityDropdown")?.classList.toggle("hidden");
    });

    document.querySelectorAll(".priority-option").forEach((button) => {
        button.addEventListener("click", () => {
            selectedPriority = button.dataset.priority;
            updatePriorityDisplay();
            document.getElementById("priorityDropdown")?.classList.add("hidden");
        });
    });

    document.getElementById("garlicMinus")?.addEventListener("click", () => {
        if (garlicReward <= 1) {
            return;
        }
        garlicReward -= 1;
        updateGarlicDisplay();
        updateDistribution();
    });

    document.getElementById("garlicPlus")?.addEventListener("click", () => {
        garlicReward += 1;
        updateGarlicDisplay();
        updateDistribution();
    });

    document.getElementById("distEqualTab")?.addEventListener("click", () => {
        distributionMode = "equal";
        document.getElementById("distEqualTab").classList.add("active");
        document.getElementById("distCustomTab").classList.remove("active");
        document.getElementById("distEqualView").classList.remove("hidden");
        document.getElementById("distCustomView").classList.add("hidden");
        updateDistribution();
    });

    document.getElementById("distCustomTab")?.addEventListener("click", () => {
        distributionMode = "custom";
        document.getElementById("distCustomTab").classList.add("active");
        document.getElementById("distEqualTab").classList.remove("active");
        document.getElementById("distCustomView").classList.remove("hidden");
        document.getElementById("distEqualView").classList.add("hidden");
        updateDistribution();
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
        if (!event.target.closest(".priority-select") && !event.target.closest(".priority-dropdown")) {
            document.getElementById("priorityDropdown")?.classList.add("hidden");
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
});
