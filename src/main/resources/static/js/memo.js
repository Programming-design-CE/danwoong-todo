function getPlannerDateString() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    const days = ["일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"];
    return {
        date: year + ". " + month + ". " + day,
        day: days[now.getDay()]
    };
}

async function loadMemos() {
    const body = document.getElementById("plannerBody");
    if (!body) return;

    try {
        const data = await requestAuthApi("/main/memo");
        if (!data) return;
        renderMemos(data.memos || []);
    } catch (e) {
        console.error(e);
    }
}

function renderMemos(memos) {
    const body = document.getElementById("plannerBody");
    if (!body) return;

    if (!memos.length) {
        body.innerHTML = '<div class="planner-empty">오늘의 할 일을 추가해보세요 ✏️</div>';
        return;
    }

    body.innerHTML = memos.map(memo =>
        '<div class="planner-item" data-id="' + memo.memo_id + '">' +
        '  <div class="planner-item-bullet"></div>' +
        '  <span class="planner-item-text">' + memo.content + '</span>' +
        '  <button class="planner-item-del" type="button">×</button>' +
        '</div>'
    ).join('');

    body.querySelectorAll(".planner-item-del").forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.closest(".planner-item").dataset.id;
            try {
                await requestAuthApi("/main/memo/" + id, { method: "DELETE" });
                loadMemos();
            } catch (e) {
                console.error(e);
            }
        });
    });

    body.querySelectorAll(".planner-item-text").forEach(span => {
        span.addEventListener("click", () => {
            const item = span.closest(".planner-item");
            const id = item.dataset.id;
            const original = span.textContent;

            const input = document.createElement("input");
            input.className = "planner-edit-input";
            input.value = original;
            span.replaceWith(input);
            input.focus();

            async function saveEdit() {
                const newContent = input.value.trim();
                if (newContent && newContent !== original) {
                    try {
                        await requestAuthApi("/main/memo/" + id, {
                            method: "PUT",
                            body: JSON.stringify({ content: newContent })
                        });
                        loadMemos();
                        return;
                    } catch (e) {
                        console.error(e);
                    }
                }
                const restored = document.createElement("span");
                restored.className = "planner-item-text";
                restored.textContent = original;
                input.replaceWith(restored);
                restored.addEventListener("click", () => restored.click());
            }

            input.addEventListener("blur", saveEdit);
            input.addEventListener("keydown", (e) => {
                if (e.key === "Enter") { e.preventDefault(); input.blur(); }
                if (e.key === "Escape") { input.value = original; input.blur(); }
            });
        });
    });
}

async function addMemo() {
    const input = document.getElementById("plannerInput");
    const content = input.value.trim();
    if (!content) return;

    try {
        await requestAuthApi("/main/memo", {
            method: "POST",
            body: JSON.stringify({ content })
        });
        input.value = "";
        loadMemos();
    } catch (e) {
        console.error(e);
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const toggleBtn = document.getElementById("memoToggleBtn");
    const panel = document.getElementById("memoPlannerPanel");
    const closeBtn = document.getElementById("plannerCloseBtn");
    const addBtn = document.getElementById("plannerAddBtn");
    const input = document.getElementById("plannerInput");

    if (!toggleBtn || !panel) return;

    // 날짜 설정
    const { date, day } = getPlannerDateString();
    const dateEl = document.getElementById("plannerDate");
    const dayEl = document.getElementById("plannerDay");
    if (dateEl) dateEl.textContent = date;
    if (dayEl) dayEl.textContent = day;

    toggleBtn.addEventListener("click", () => {
        panel.classList.toggle("open");
        if (panel.classList.contains("open")) {
            loadMemos();
        }
    });

    closeBtn.addEventListener("click", () => {
        panel.classList.remove("open");
    });

    addBtn.addEventListener("click", addMemo);

    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") addMemo();
    });
});