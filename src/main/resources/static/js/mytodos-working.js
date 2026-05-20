function formatDeadline(deadline) {
    if (!deadline) return "";
    const d = new Date(deadline);
    const month = d.getMonth() + 1;
    const day = d.getDate();
    const days = ["일", "월", "화", "수", "목", "금", "토"];
    const dow = days[d.getDay()];
    return "마감 " + month + "/" + day + "(" + dow + ")";
}

function getGroupInitial(name) {
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
        const initial = getGroupInitial(todo.todo_name);
        const deadline = formatDeadline(todo.deadline);
        const groupBadge = todo.group_name
            ? '<span class="mytodo-group">' + todo.group_name + '</span>'
            : '';

        return [
            '<div class="mytodo-card">',
            '  <div class="mytodo-icon">' + initial + '</div>',
            '  <div class="mytodo-info">',
            '    <div class="mytodo-name-row">',
            '      <span class="mytodo-name">' + todo.todo_name + '</span>',
            groupBadge,
            '    </div>',
            deadline ? '    <div class="mytodo-meta"><span class="mytodo-meta-icon">📅</span>' + deadline + '</div>' : '',
            '  </div>',
            '  <span class="mytodo-arrow">›</span>',
            '</div>'
        ].join('');
    }).join('');
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
});