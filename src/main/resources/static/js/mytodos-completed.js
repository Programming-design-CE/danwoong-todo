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

    list.innerHTML = todos.map((todo) => {
        const initial = getInitial(todo.todo_name);
        const completedAt = formatCompletedAt(todo.completed_at);
        const garlic = todo.garlic_reward != null
            ? '<span class="mytodo-garlic">🧄 ' + todo.garlic_reward + '개</span>'
            : '';

        return [
            '<div class="mytodo-card">',
            '  <div class="mytodo-icon">' + initial + '</div>',
            '  <div class="mytodo-info">',
            '    <div class="mytodo-name-row">',
            '      <span class="mytodo-name">' + todo.todo_name + '</span>',
            '      <span class="mytodo-badge mytodo-badge--done">완료</span>',
            '    </div>',
            completedAt ? '    <div class="mytodo-meta"><span class="mytodo-meta-icon">📅</span>' + completedAt + '</div>' : '',
            '  </div>',
            garlic,
            '  <span class="mytodo-arrow">›</span>',
            '</div>'
        ].join('');
    }).join('');
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
});