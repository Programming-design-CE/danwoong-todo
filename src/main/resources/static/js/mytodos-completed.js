function formatCompletedAt(completedAt) {
    if (!completedAt) return "";
    const d = new Date(completedAt);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    const dow = days[d.getDay()];
    return "완료일 " + year + ". " + month + ". " + day + " (" + dow + ")";
}

function formatDeadlineFull(deadline) {
    if (!deadline) return "-";
    const d = new Date(deadline);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    return year + ". " + month + ". " + day + " (" + days[d.getDay()] + ")";
}

function getPriorityLabel(priority) {
    if (priority === "HIGH") return "높음";
    if (priority === "LOW") return "낮음";
    return "보통";
}

function getPriorityColor(priority) {
    if (priority === "HIGH") return "#e07b54";
    if (priority === "LOW") return "#7b9ec8";
    return "#c9c3bb";
}

function getInitial(name) {
    if (!name) return "?";
    return name.trim().charAt(0);
}

function renderCompletedTodos(todos) {
    const list = document.getElementById("mytodoList");
    if (!list) return;

    if (!todos.length) {
        list.innerHTML = '<div class="mytodo-empty-state">완료된 할 일이 없습니다.</div>';
        return;
    }

    // 피그마 구조: 아이콘 | 이름+완료+날짜(flex:1) | 마늘 | 담당자라벨+아바타 | ›
    list.innerHTML = todos.map((todo) => {
        const initial = getInitial(todo.todo_name);
        const completedAt = formatCompletedAt(todo.completed_at);

        const garlic = todo.garlic_reward != null
            ? '<span class="mytodo-garlic">🧄 ' + todo.garlic_reward + '개</span>'
            : '';

        const nicknames = todo.assignee_nicknames || [];
        const avatars = nicknames.map(name =>
            '<div class="mytodo-avatar" title="' + name + '">' + getInitial(name) + '</div>'
        ).join('');
        const assigneeBlock = nicknames.length
            ? '<div class="mytodo-assignees"><span class="mytodo-assignees-label">담당자</span><div class="mytodo-avatars">' + avatars + '</div></div>'
            : '';

        return [
            '<div class="mytodo-card" data-id="' + todo.todo_id + '">',
            '  <div class="mytodo-icon">' + initial + '</div>',
            '  <div class="mytodo-info">',
            '    <div class="mytodo-name-row">',
            '      <span class="mytodo-name">' + todo.todo_name + '</span>',
            '      <span class="mytodo-badge mytodo-badge--done">완료</span>',
            '    </div>',
            completedAt ? '<div class="mytodo-meta"><span class="mytodo-meta-icon">📅</span>' + completedAt + '</div>' : '',
            '  </div>',
            garlic,
            assigneeBlock,
            '  <span class="mytodo-arrow">›</span>',
            '</div>'
        ].join('');
    }).join('');

    document.querySelectorAll(".mytodo-card").forEach(card => {
        card.addEventListener("click", () => openTodoModal(card.dataset.id));
    });
}

async function openTodoModal(todoId) {
    try {
        const todo = await fetchTodoJson("/todos/" + todoId);
        showModal(todo);
    } catch (e) {
        console.error(e);
    }
}

function showModal(todo) {
    const overlay = document.getElementById("mytodoModalOverlay");
    if (!overlay) return;

    overlay.querySelector(".mtm-title").textContent = todo.todo_name;
    overlay.querySelector(".mtm-category").textContent = todo.category || "";
    overlay.querySelector(".mtm-deadline-val").textContent = formatDeadlineFull(todo.deadline);
    overlay.querySelector(".mtm-completed-val").textContent = formatCompletedAt(todo.completed_at);
    overlay.querySelector(".mtm-garlic-val").textContent = todo.garlic_reward != null ? todo.garlic_reward + "개" : "-";
    overlay.querySelector(".mtm-priority-val").innerHTML =
        '<span style="color:' + getPriorityColor(todo.priority) + '">⚑ ' + getPriorityLabel(todo.priority) + '</span>';
    overlay.querySelector(".mtm-desc-val").textContent = todo.description || "-";

    const avatarWrap = overlay.querySelector(".mtm-assignee-avatars");
    if (avatarWrap) {
        avatarWrap.innerHTML = (todo.assignees || []).map(a =>
            '<div class="mtm-assignee-avatar" title="' + a.nickname + '">' + getInitial(a.nickname) + '</div>'
        ).join('');
    }

    const assigneeList = overlay.querySelector(".mtm-assignee-list");
    if (assigneeList) {
        assigneeList.innerHTML = (todo.assignees || []).map(a =>
            '<div class="mtm-assignee-row">' +
            '<div class="mtm-assignee-avatar">' + getInitial(a.nickname) + '</div>' +
            '<span class="mtm-assignee-name">' + a.nickname + '</span>' +
            '<span class="mtm-assignee-status mtm-status--done">완료</span>' +
            '</div>'
        ).join('');
    }

    overlay.classList.add("active");
}

async function loadCompletedTodos() {
    try {
        const data = await fetchTodoJson("/todos/my/completed");
        renderCompletedTodos(data?.todos || []);
    } catch (e) {
        console.error(e);
        const list = document.getElementById("mytodoList");
        if (list) list.innerHTML = '<div class="mytodo-empty-state">완료된 할 일을 불러오지 못했습니다.</div>';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadCompletedTodos();

    const overlay = document.getElementById("mytodoModalOverlay");
    if (overlay) {
        overlay.querySelector(".mtm-close").addEventListener("click", () => overlay.classList.remove("active"));
        overlay.addEventListener("click", (e) => { if (e.target === overlay) overlay.classList.remove("active"); });
    }
});