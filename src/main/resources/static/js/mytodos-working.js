function formatDeadline(deadline) {
    if (!deadline) return "";
    const d = new Date(deadline);
    const month = d.getMonth() + 1;
    const day = d.getDate();
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    const dow = days[d.getDay()];
    return "마감 " + month + "/" + day + "(" + dow + ")";
}

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

function renderTodos(todos) {
    const list = document.getElementById("mytodoList");
    if (!list) return;

    if (!todos.length) {
        list.innerHTML = '<div class="mytodo-empty-state">진행 중인 할 일이 없습니다.</div>';
        return;
    }

    list.innerHTML = todos.map((todo) => {
        const initial = getInitial(todo.todo_name);
        const deadline = formatDeadline(todo.deadline);
        const groupBadge = todo.group_name
            ? '<span class="mytodo-group">' + todo.group_name + '</span>'
            : '';
        const garlic = todo.garlic_reward != null
            ? '<span class="mytodo-garlic">🧄 ' + todo.garlic_reward + '개</span>'
            : '';
        const priorityColor = getPriorityColor(todo.priority);
        const priorityLabel = getPriorityLabel(todo.priority);
        const priority = '<span class="mytodo-priority" style="color:' + priorityColor + '">⚑ ' + priorityLabel + '</span>';

        return [
            '<div class="mytodo-card" data-id="' + todo.todo_id + '">',
            '  <div class="mytodo-icon">' + initial + '</div>',
            '  <div class="mytodo-info">',
            '    <div class="mytodo-name-row">',
            '      <span class="mytodo-name">' + todo.todo_name + '</span>',
            groupBadge,
            '    </div>',
            deadline ? '    <div class="mytodo-meta"><span class="mytodo-meta-icon">📅</span>' + deadline + '</div>' : '',
            '  </div>',
            garlic,
            priority,
            '  <span class="mytodo-arrow">›</span>',
            '</div>'
        ].join('');
    }).join('');

    document.querySelectorAll(".mytodo-card").forEach(card => {
        card.addEventListener("click", () => {
            const todoId = card.dataset.id;
            openTodoModal(todoId);
        });
    });
}

async function openTodoModal(todoId) {
    try {
        const data = await fetchTodoJson("/todos/" + todoId);
        showModal(data);
    } catch (e) {
        console.error(e);
    }
}

function showModal(todo) {
    const overlay = document.getElementById("mytodoModalOverlay");
    if (!overlay) return;

    const priorityColor = getPriorityColor(todo.priority);
    const priorityLabel = getPriorityLabel(todo.priority);
    const deadlineFull = formatDeadlineFull(todo.deadline);

    overlay.querySelector(".mtm-title").textContent = todo.todo_name;
    overlay.querySelector(".mtm-category").textContent = todo.category || "";
    overlay.querySelector(".mtm-deadline-val").textContent = deadlineFull;
    overlay.querySelector(".mtm-garlic-val").textContent = todo.garlic_reward != null ? todo.garlic_reward + "개" : "-";
    overlay.querySelector(".mtm-priority-val").innerHTML = '<span style="color:' + priorityColor + '">⚑ ' + priorityLabel + '</span>';
    overlay.querySelector(".mtm-desc-val").textContent = todo.description || "-";

    // 진행률
    const pct = 0;
    overlay.querySelector(".mtm-progress-fill").style.width = pct + "%";
    overlay.querySelector(".mtm-progress-pct").textContent = pct + "%";
    overlay.querySelector(".mtm-progress-pct-right").textContent = pct + "%";

    // 담당자별 완료 여부
    const assigneeList = overlay.querySelector(".mtm-assignee-list");
    if (assigneeList && todo.assignees && todo.assignees.length) {
        assigneeList.innerHTML = todo.assignees.map(a => {
            return [
                '<div class="mtm-assignee-row">',
                '  <div class="mtm-assignee-avatar">' + getInitial(a.nickname) + '</div>',
                '  <span class="mtm-assignee-name">' + a.nickname + '</span>',
                '  <span class="mtm-assignee-status mtm-status--progress">진행 중</span>',
                '</div>'
            ].join('');
        }).join('');
    }

    const completeBtn = overlay.querySelector(".mtm-complete-btn");
    completeBtn.onclick = async () => {
        try {
            await fetch("/todos/" + todo.todo_id + "/complete", {
                method: "PATCH",
                headers: getTodoAuthHeaders()
            });
            overlay.classList.remove("active");
            window.location.href = "/mytodos/completed";
        } catch (e) {
            console.error(e);
        }
    };

    overlay.classList.add("active");
}

async function loadMyTodos() {
    try {
        const data = await fetchTodoJson("/todos/my");
        renderTodos(data?.todos || []);
    } catch (e) {
        console.error(e);
        const list = document.getElementById("mytodoList");
        if (list) list.innerHTML = '<div class="mytodo-empty-state">할 일 목록을 불러오지 못했습니다.</div>';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadMyTodos();

    const overlay = document.getElementById("mytodoModalOverlay");
    if (overlay) {
        overlay.querySelector(".mtm-close").addEventListener("click", () => {
            overlay.classList.remove("active");
        });
        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) overlay.classList.remove("active");
        });
    }
});