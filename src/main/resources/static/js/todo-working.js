let allGroups = [];
let friends = [];
let selectedAssignees = [];
let selectedColor = "#4CAF7D";
let selectedIcon = "S";

function getProgressPercent(group) {
    const total = group.total_garlic_reward || 0;
    const remaining = group.remaining_garlic_reward || 0;

    if (total <= 0) {
        return 0;
    }

    return Math.round(((total - remaining) / total) * 100);
}

function formatDday(deadline) {
    if (!deadline) {
        return "";
    }

    const target = new Date(deadline);
    const today = new Date();
    const diff = Math.ceil((target - today) / 86400000);

    return diff >= 0 ? "D-" + diff : "D+" + Math.abs(diff);
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

function getGroupInitial(groupName) {
    if (!groupName) {
        return "?";
    }

    return groupName.trim().charAt(0);
}

function buildMemberAvatars(group) {
    const members = Array.isArray(group.members) ? group.members : [];
    const visibleMembers = members.slice(0, 3).map((member) => {
        if (member.profile_image) {
            return '<div class="member-avatar"><img src="' + member.profile_image + '" alt="member"></div>';
        }

        return '<div class="member-avatar member-avatar--empty"></div>';
    }).join("");

    const extraCount = Math.max((group.member_count || 0) - members.slice(0, 3).length, 0);
    const more = extraCount > 0 ? '<div class="member-more">+' + extraCount + "</div>" : "";

    return visibleMembers + more;
}

function renderStatusMessage(message) {
    const list = document.getElementById("projectList");
    if (!list) {
        return;
    }

    list.innerHTML = '<div class="todo-empty-state">' + message + "</div>";
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

    const cards = groups.map((group) => {
        const progress = getProgressPercent(group);
        const dday = formatDday(group.deadline);
        const priority = getPriorityLabel(group.priority);
        const initial = getGroupInitial(group.group_name);

        return [
            '<div class="project-card" data-group-id="' + group.group_id + '" style="cursor: pointer;">',
            '  <div class="project-icon">' + initial + "</div>",
            '  <div class="project-name-block">',
            '      <span class="project-name">' + group.group_name + "</span>",
            dday ? '      <span class="project-dday">' + dday + "</span>" : "",
            "  </div>",
            '  <div class="project-progress-block">',
            '      <div class="progress-bar"><div class="progress-fill" style="width:' + progress + '%"></div></div>',
            '      <span class="project-percent">' + progress + ' %</span>',
            "  </div>",
            '  <div class="member-avatars">' + buildMemberAvatars(group) + "</div>",
            '  <div class="project-priority"><span class="project-priority-flag">⚑</span><span>' + priority + "</span></div>",
            '  <button class="project-more" type="button">⋮</button>',
            "</div>"
        ].join("");
    });

    list.innerHTML = cards.join("");

    list.querySelectorAll(".project-card").forEach((card) => {
        card.addEventListener("click", (event) => {
            if (event.target.closest(".project-more")) {
                event.stopPropagation();
                return;
            }
            const groupId = card.dataset.groupId;
            if (groupId) {
                window.location.href = `/todos/detail?groupId=${groupId}`;
            }
        });
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
        allGroups = data?.groups || [];
        renderProjects(allGroups);
    } catch (error) {
        console.error(error);
        renderStatusMessage("프로젝트 목록을 불러오지 못했습니다.");
    }
}

function closeModal() {
    document.getElementById("modalOverlay")?.classList.remove("open");
    document.getElementById("groupName").value = "";
    document.getElementById("groupDesc").value = "";
    document.getElementById("groupDeadline").value = "";
    document.getElementById("groupGarlic").value = "";
    document.getElementById("nameCount").textContent = "0";
    document.getElementById("descCount").textContent = "0";
    selectedAssignees = [];
    selectedColor = "#4CAF7D";
    selectedIcon = "S";

    document.querySelectorAll(".color-dot").forEach((dot) => {
        dot.classList.remove("active");
        dot.textContent = "";
    });

    const defaultDot = document.querySelector('.color-dot[data-color="#4CAF7D"]');
    if (defaultDot) {
        defaultDot.classList.add("active");
        defaultDot.textContent = "v";
    }

    document.querySelectorAll(".category-btn").forEach((button, index) => {
        button.classList.toggle("active", index === 0);
    });

    renderAssignees();
}

function bindModalEvents() {
    ["modalClose", "btnCancel"].forEach((id) => {
        const element = document.getElementById(id);
        element?.addEventListener("click", closeModal);
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

    document.getElementById("groupDesc")?.addEventListener("input", (event) => {
        document.getElementById("descCount").textContent = String(event.target.value.length);
    });

    document.querySelectorAll(".category-btn").forEach((button) => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".category-btn").forEach((item) => item.classList.remove("active"));
            button.classList.add("active");
            selectedIcon = button.dataset.icon || "S";
        });
    });

    document.querySelectorAll(".color-dot").forEach((button) => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".color-dot").forEach((item) => {
                item.classList.remove("active");
                item.textContent = "";
            });

            button.classList.add("active");
            button.textContent = "v";
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
            ? '<img src="' + friend.character_thumbnail_url + '" alt="' + friend.nickname + '">'
            : initial;

        return [
            '<button class="friend-item" type="button" data-friend-id="' + friend.user_id + '">',
            '  <div class="friend-avatar">' + avatar + "</div>",
            "  " + friend.nickname,
            "</button>"
        ].join("");
    }).join("");

    picker.querySelectorAll(".friend-item[data-friend-id]").forEach((item) => {
        item.addEventListener("click", (event) => {
            event.stopPropagation();
            const friendId = Number(item.dataset.friendId);
            const friend = friends.find((value) => value.user_id === friendId);
            if (!friend) {
                return;
            }

            if (selectedAssignees.some((value) => value.id === friendId)) {
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
            ? '<img src="' + assignee.thumbnail + '" alt="' + assignee.nickname + '">'
            : '<span class="chip-initial">' + assignee.nickname.charAt(0) + "</span>";

        return [
            '<button class="assignee-chip" type="button" data-assignee-id="' + assignee.id + '">',
            avatar,
            '<span class="chip-remove">x</span>',
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

    const addButton = document.getElementById("btnAddAssignee");
    addButton?.addEventListener("click", (event) => {
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

    const body = {
        group_name: groupName,
        deadline: document.getElementById("groupDeadline").value || null,
        priority: document.getElementById("groupPriority").value,
        invitee_ids: selectedAssignees.map((assignee) => assignee.id),
        group_icon_url: JSON.stringify({
            icon: selectedIcon,
            color: selectedColor
        })
    };

    try {
        const response = await fetch("/todo-groups", {
            method: "POST",
            headers: getTodoAuthHeaders(),
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error("Create failed: " + response.status);
        }

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
    renderAssignees();
    loadProjects();

    document.getElementById("btnSubmit")?.addEventListener("click", createProject);
});
