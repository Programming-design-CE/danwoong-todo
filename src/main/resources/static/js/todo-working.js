let allGroups = [];
let friends = [];
let selectedAssignees = [];
let selectedColor = "#4CAF7D";
let activeProjectMenuGroupId = null;
let sortProjectOrder = "recent";

function setCurrentGroupId(groupId) {
    if (!groupId) {
        return;
    }

    localStorage.setItem("currentGroupId", String(groupId));
    localStorage.setItem("groupId", String(groupId));
}

function clearCurrentGroupId() {
    localStorage.removeItem("currentGroupId");
    localStorage.removeItem("groupId");
}

function getProgressPercent(group) {
    const total = Number(group.total_todo_count || 0);
    const completed = Number(group.completed_todo_count || 0);

    if (total <= 0) {
        return 0;
    }

    return Math.max(0, Math.min(100, Math.round((completed / total) * 100)));
}

function formatDday(deadline) {
    if (!deadline) {
        return "";
    }

    const target = new Date(deadline);
    if (Number.isNaN(target.getTime())) {
        return "";
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    target.setHours(0, 0, 0, 0);

    const diff = Math.ceil((target.getTime() - today.getTime()) / 86400000);
    return diff >= 0 ? `D-${diff}` : `D+${Math.abs(diff)}`;
}

function getPriorityLabel(priority) {
    if (priority === "HIGH") {
        return "높음";
    }
    if (priority === "LOW") {
        return "낮음";
    }
    return "보통";
}

function getGroupInitial(group) {
    if (!group.group_name) {
        return "?";
    }

    return group.group_name.trim().charAt(0);
}

function getGroupIconStyle(group) {
    if (group.group_color) {
        return `background:${group.group_color};`;
    }

    if (!group.group_icon_url) {
        return "";
    }

    try {
        const parsed = JSON.parse(group.group_icon_url);
        if (parsed.color) {
            return `background:${parsed.color};`;
        }
    } catch (error) {
        console.warn("Failed to parse group icon color", error);
    }

    return "";
}

function buildMemberAvatars(group) {
    const members = Array.isArray(group.members) ? group.members : [];
    const visibleMembers = members.slice(0, 3).map((member) => {
        if (member.profile_image) {
            return `<div class="member-avatar" title="${member.nickname || "member"}"><img src="${member.profile_image}" alt="${member.nickname || "member"}"></div>`;
        }

        const initial = (member.nickname || "?").trim().charAt(0) || "?";
        return `<div class="member-avatar member-avatar--empty" title="${member.nickname || "member"}">${initial}</div>`;
    }).join("");

    const extraCount = Math.max((group.member_count || 0) - Math.min(members.length, 3), 0);
    const extra = extraCount > 0 ? `<div class="member-more" title="나머지 멤버 보기">+${extraCount}</div>` : "";
    return visibleMembers + extra;
}

function renderStatusMessage(message) {
    const list = document.getElementById("projectList");
    if (!list) {
        return;
    }

    list.innerHTML = `<div class="todo-empty-state">${message}</div>`;
}

function createProjectCard(group) {
    const progress = getProgressPercent(group);
    const dday = formatDday(group.deadline);
    const priority = getPriorityLabel(group.priority);

    return [
        `<div class="project-card" data-group-id="${group.group_id}">`,
        `  <div class="project-icon" style="${getGroupIconStyle(group)}">${getGroupInitial(group)}</div>`,
        '  <div class="project-name-block">',
        `      <span class="project-name">${group.group_name || "이름 없는 프로젝트"}</span>`,
        dday ? `      <span class="project-dday">${dday}</span>` : "",
        "  </div>",
        '  <div class="project-progress-block">',
        `      <div class="progress-bar"><div class="progress-fill" style="width:${progress}%; ${getGroupIconStyle(group)}"></div></div>`,
        `      <span class="project-percent">${progress}% (${group.completed_todo_count || 0}/${group.total_todo_count || 0})</span>`,
        "  </div>",
        `  <div class="member-avatars" onclick="event.stopPropagation(); alert('담당자: ' + '${(group.members || []).map(m=>m.nickname).join(', ')}')">${buildMemberAvatars(group)}</div>`,
        `  <div class="project-priority"><span class="project-priority-flag">&#9873;</span><span>${priority}</span></div>`,
        `  <button class="project-more" type="button" data-group-id="${group.group_id}" aria-label="프로젝트 메뉴">&#8942;</button>`,
        "</div>"
    ].join("");
}

function renderProjects(groups) {
    const list = document.getElementById("projectList");
    if (!list) {
        return;
    }

    if (!groups.length) {
        renderStatusMessage("진행 중인 프로젝트가 없습니다.");
        return;
    }

    list.innerHTML = groups.map(createProjectCard).join("");
}

function showProjectContextMenu(button) {
    const menu = document.getElementById("projectContextMenu");
    if (!menu || !button) {
        return;
    }

    const rect = button.getBoundingClientRect();
    const groupId = Number(button.dataset.groupId);
    activeProjectMenuGroupId = groupId;
    
    // 100% 진행된 프로젝트만 '프로젝트 완료' 버튼 표시
    const group = allGroups.find(g => g.group_id === groupId);
    const progress = group ? getProgressPercent(group) : 0;
    const completeBtn = document.getElementById("completeProjectAction");
    if (completeBtn) {
        if (progress >= 100 && group.total_todo_count > 0) {
            completeBtn.style.display = "block";
        } else {
            completeBtn.style.display = "none";
        }
    }

    menu.style.left = `${Math.max(16, rect.right - 112)}px`;
    menu.style.top = `${rect.bottom + 6}px`;
    menu.classList.remove("hidden");
}

function hideProjectContextMenu() {
    const menu = document.getElementById("projectContextMenu");
    if (!menu) {
        return;
    }

    menu.classList.add("hidden");
    activeProjectMenuGroupId = null;
}

async function deleteProject(groupId) {
    if (!groupId) {
        return;
    }

    const targetGroup = allGroups.find((group) => group.group_id === groupId);
    const groupName = targetGroup?.group_name || "이 프로젝트";

    if (!window.confirm(`${groupName} 프로젝트를 삭제할까요?`)) {
        return;
    }

    try {
        await fetchTodoJson(`/todo-groups/${groupId}`, {
            method: "DELETE"
        });

        const currentGroupId = Number(localStorage.getItem("currentGroupId") || 0);
        if (currentGroupId === Number(groupId)) {
            clearCurrentGroupId();
        }

        await loadProjects();
    } catch (error) {
        console.error(error);
        alert("프로젝트를 삭제하지 못했습니다.");
    }
}

async function completeProject(groupId) {
    if (!groupId) return;

    const targetGroup = allGroups.find((group) => group.group_id === groupId);
    const groupName = targetGroup?.group_name || "이 프로젝트";

    if (!window.confirm(`${groupName} 프로젝트를 완료 처리하시겠습니까?`)) {
        return;
    }

    try {
        // Find existing data to preserve other fields
        const body = {
            group_name: targetGroup.group_name,
            group_icon_url: targetGroup.group_color ? JSON.stringify({color: targetGroup.group_color}) : null,
            group_category: targetGroup.group_category,
            deadline: targetGroup.deadline,
            priority: targetGroup.priority,
            status: "COMPLETED"
        };

        await fetchTodoJson(`/todo-groups/${groupId}`, {
            method: "PATCH",
            headers: getTodoAuthHeaders(),
            body: JSON.stringify(body)
        });

        const currentGroupId = Number(localStorage.getItem("currentGroupId") || 0);
        if (currentGroupId === Number(groupId)) {
            clearCurrentGroupId();
        }

        await loadProjects();
    } catch (error) {
        console.error(error);
        alert("프로젝트를 완료 처리하지 못했습니다. 팀장 권한이 필요할 수 있습니다.");
    }
}

function bindProjectListEvents() {
    const list = document.getElementById("projectList");
    if (!list) {
        return;
    }

    list.addEventListener("click", (event) => {
        const moreButton = event.target.closest(".project-more");
        if (moreButton) {
            event.preventDefault();
            event.stopPropagation();
            showProjectContextMenu(moreButton);
            return;
        }

        const card = event.target.closest(".project-card");
        if (!card) {
            return;
        }

        const groupId = card.dataset.groupId;
        if (groupId) {
            setCurrentGroupId(groupId);
            window.location.href = `/todos/detail?groupId=${groupId}`;
        }
    });
}

function bindProjectAddButton() {
    const addButton = document.getElementById("btnAddProject");
    if (!addButton) {
        return;
    }

    addButton.addEventListener("click", async () => {
        await loadFriends();
        document.getElementById("modalOverlay")?.classList.add("open");
    });
}

async function loadProjects() {
    try {
        const data = await fetchTodoJson("/todo-groups");
        // 상태가 COMPLETED가 아닌 것만 진행 중에 표시
        allGroups = (data?.groups || []).filter(g => g.status !== 'COMPLETED');

        const savedGroupId = Number(localStorage.getItem("currentGroupId") || 0);
        const hasSavedGroup = allGroups.some((group) => group.group_id === savedGroupId);

        if (!hasSavedGroup && allGroups.length > 0) {
            setCurrentGroupId(allGroups[0].group_id);
        } else if (!allGroups.length) {
            clearCurrentGroupId();
        }

        sortAndRenderProjects();
    } catch (error) {
        console.error(error);
        renderStatusMessage("프로젝트 목록을 불러오지 못했습니다.");
    }
}

function sortAndRenderProjects() {
    const sorted = [...allGroups].sort((a, b) => {
        if (sortProjectOrder === "recent") {
            return b.group_id - a.group_id;
        } else if (sortProjectOrder === "oldest") {
            return a.group_id - b.group_id;
        } else if (sortProjectOrder === "deadline") {
            const timeA = a.deadline ? new Date(a.deadline).getTime() : Infinity;
            const timeB = b.deadline ? new Date(b.deadline).getTime() : Infinity;
            if (timeA === timeB) return b.group_id - a.group_id;
            return timeA - timeB;
        }
        return 0;
    });
    renderProjects(sorted);
}

function clearColorSelection() {
    document.querySelectorAll(".color-dot").forEach((dot) => {
        dot.classList.remove("active");
        dot.textContent = "";
    });
}

function closeModal() {
    document.getElementById("modalOverlay")?.classList.remove("open");
    document.getElementById("groupName").value = "";
    document.getElementById("groupDeadline").value = "";
    document.getElementById("groupGarlic").value = "";
    document.getElementById("nameCount").textContent = "0";

    selectedAssignees = [];
    selectedColor = "#4CAF7D";
    clearColorSelection();
    
    const defaultDot = document.querySelector('.color-dot[data-color="#4CAF7D"]');
    if (defaultDot) {
        defaultDot.classList.add("active");
        defaultDot.textContent = "✓";
    }
    
    renderAssignees();
}

function bindModalEvents() {
    ["modalClose", "btnCancel"].forEach((id) => {
        document.getElementById(id)?.addEventListener("click", closeModal);
    });

    const overlay = document.getElementById("modalOverlay");
    overlay?.addEventListener("click", (event) => {
        if (event.target === overlay) {
            closeModal();
        }
    });

    document.getElementById("groupName")?.addEventListener("input", (event) => {
        document.getElementById("nameCount").textContent = String(event.target.value.length);
    });

    document.querySelectorAll(".color-dot").forEach((button) => {
        button.addEventListener("click", () => {
            clearColorSelection();
            button.classList.add("active");
            button.textContent = "✓";
            selectedColor = button.dataset.color || "#4CAF7D";
        });
    });

    document.addEventListener("click", () => {
        document.getElementById("friendPicker")?.classList.remove("open");
    });
}

async function loadFriends() {
    try {
        const data = await fetchTodoJson("/friends");
        friends = Array.isArray(data) ? data : (data?.friends || []);
    } catch (error) {
        console.error(error);
        friends = [];
    }

    renderFriendPicker();
    renderAssignees();
}

function renderFriendPicker() {
    const picker = document.getElementById("friendPicker");
    if (!picker) {
        return;
    }

    if (!friends.length) {
        picker.innerHTML = '<div class="friend-item" style="color:#555">추가할 친구가 없습니다.</div>';
        return;
    }

    picker.innerHTML = friends.map((friend) => {
        const initial = friend.nickname ? friend.nickname.charAt(0) : "?";
        const avatar = friend.character_thumbnail_url
            ? `<img src="${friend.character_thumbnail_url}" alt="${friend.nickname}">`
            : initial;

        return [
            `<button class="friend-item" type="button" data-friend-id="${friend.user_id}">`,
            `  <div class="friend-avatar">${avatar}</div>`,
            `  ${friend.nickname}`,
            "</button>"
        ].join("");
    }).join("");

    picker.querySelectorAll(".friend-item[data-friend-id]").forEach((item) => {
        item.addEventListener("click", (event) => {
            event.stopPropagation();
            const friendId = Number(item.dataset.friendId);
            const friend = friends.find((value) => value.user_id === friendId);
            if (!friend || selectedAssignees.some((value) => value.id === friendId)) {
                return;
            }

            selectedAssignees.push({
                id: friendId,
                nickname: friend.nickname,
                thumbnail: friend.character_thumbnail_url || ""
            });

            renderAssignees();
            picker.classList.remove("open");
        });
    });
}

function renderAssignees() {
    const wrap = document.getElementById("assigneeWrap");
    if (!wrap) {
        return;
    }

    const chips = selectedAssignees.map((assignee) => {
        const avatar = assignee.thumbnail
            ? `<img src="${assignee.thumbnail}" alt="${assignee.nickname}">`
            : `<span class="chip-initial">${assignee.nickname.charAt(0)}</span>`;

        return [
            `<button class="assignee-chip" type="button" data-assignee-id="${assignee.id}">`,
            avatar,
            '<span class="chip-remove">×</span>',
            "</button>"
        ].join("");
    }).join("");

    wrap.innerHTML = chips + '<button class="btn-add-assignee" id="btnAddAssignee" type="button">+</button>';

    wrap.querySelectorAll(".assignee-chip").forEach((item) => {
        item.addEventListener("click", () => {
            const assigneeId = Number(item.dataset.assigneeId);
            selectedAssignees = selectedAssignees.filter((value) => value.id !== assigneeId);
            renderAssignees();
        });
    });

    document.getElementById("btnAddAssignee")?.addEventListener("click", (event) => {
        event.stopPropagation();
        document.getElementById("friendPicker")?.classList.toggle("open");
    });
}

async function createProject() {
    const groupName = document.getElementById("groupName").value.trim();
    if (!groupName) {
        alert("프로젝트 제목을 입력해주세요.");
        return;
    }

    const garlicValue = document.getElementById("groupGarlic").value.trim();
    if (!garlicValue) {
        alert("마늘 개수를 입력해주세요.");
        return;
    }

    const body = {
        group_name: groupName,
        deadline: document.getElementById("groupDeadline").value || null,
        priority: "MEDIUM",
        invitee_ids: selectedAssignees.map((assignee) => assignee.id),
        group_category: "etc",
        group_icon_url: JSON.stringify({
            icon: "S",
            color: selectedColor
        }),
        total_garlic_reward: Number(garlicValue)
    };

    try {
        const response = await fetch("/todo-groups", {
            method: "POST",
            headers: getTodoAuthHeaders(),
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error(`Create failed: ${response.status}`);
        }

        const createdGroup = await response.json();
        const createdGroupId = createdGroup?.group_id || createdGroup?.groupId;
        setCurrentGroupId(createdGroupId);

        closeModal();
        await loadProjects();
    } catch (error) {
        console.error(error);
        alert("프로젝트 생성에 실패했습니다.");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    bindModalEvents();
    bindProjectAddButton();
    bindProjectListEvents();
    renderAssignees();
    loadProjects();

    document.getElementById("btnSubmit")?.addEventListener("click", createProject);

    document.getElementById("completeProjectAction")?.addEventListener("click", async () => {
        const groupId = activeProjectMenuGroupId;
        hideProjectContextMenu();
        await completeProject(groupId);
    });

    document.getElementById("deleteProjectAction")?.addEventListener("click", async () => {
        const groupId = activeProjectMenuGroupId;
        hideProjectContextMenu();
        await deleteProject(groupId);
    });

    document.addEventListener("click", (event) => {
        if (!event.target.closest(".project-context-menu") && !event.target.closest(".project-more")) {
            hideProjectContextMenu();
        }
    });

    const sortProjectSelect = document.getElementById("sortProjectSelect");
    if (sortProjectSelect) {
        sortProjectSelect.addEventListener("change", (e) => {
            sortProjectOrder = e.target.value;
            sortAndRenderProjects();
        });
    }
});
