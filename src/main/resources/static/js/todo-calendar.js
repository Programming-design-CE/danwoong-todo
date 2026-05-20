const CALENDAR_CATEGORY_COLORS = {
    "학교": "#D7C45C",
    "대외활동": "#7FA2FF",
    "스터디": "#9BCB63",
    "개인": "#B69BDF",
    "기타": "#E2A3B8"
};

const calendarState = {
    viewYear: 0,
    viewMonth: 0,
    selectedDate: "",
    selectedGroup: "all",
    monthDays: []
};

function getTodayParts() {
    const now = new Date();
    return {
        year: now.getFullYear(),
        month: now.getMonth() + 1,
        day: now.getDate()
    };
}

function toIsoDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return year + "-" + month + "-" + day;
}

function parseIsoDate(value) {
    const [year, month, day] = value.split("-").map(Number);
    return new Date(year, month - 1, day);
}

function addDays(date, days) {
    const copy = new Date(date);
    copy.setDate(copy.getDate() + days);
    return copy;
}

function formatMonthLabel(year, month) {
    return year + "년 " + month + "월";
}

function getCategoryColor(todo) {
    if (todo?.category && CALENDAR_CATEGORY_COLORS[todo.category]) {
        return CALENDAR_CATEGORY_COLORS[todo.category];
    }

    if (todo?.priority === "HIGH") {
        return "#E3A05E";
    }
    if (todo?.priority === "LOW") {
        return "#9EC7E3";
    }
    return "#B9A8D9";
}

function getCurrentMonthMap() {
    return new Map(calendarState.monthDays.map((day) => [day.date, day]));
}

function buildCalendarCells() {
    const firstOfMonth = new Date(calendarState.viewYear, calendarState.viewMonth - 1, 1);
    const firstGridDate = addDays(firstOfMonth, -firstOfMonth.getDay());
    const lastOfMonth = new Date(calendarState.viewYear, calendarState.viewMonth, 0);
    const totalVisibleWeeks = Math.ceil((firstOfMonth.getDay() + lastOfMonth.getDate()) / 7);
    const totalCells = totalVisibleWeeks * 7;
    const monthMap = getCurrentMonthMap();
    const todayParts = getTodayParts();
    const todayIso = [
        todayParts.year,
        String(todayParts.month).padStart(2, "0"),
        String(todayParts.day).padStart(2, "0")
    ].join("-");

    const cells = [];

    for (let index = 0; index < totalCells; index += 1) {
        const cellDate = addDays(firstGridDate, index);
        const isoDate = toIsoDate(cellDate);
        const dayData = monthMap.get(isoDate) || { date: isoDate, count: 0, todos: [] };
        const isCurrentMonth = cellDate.getMonth() + 1 === calendarState.viewMonth;
        const isToday = isoDate === todayIso;
        const isSelected = isoDate === calendarState.selectedDate;
        const filteredTodos = (dayData.todos || []).filter((todo) => {
            return calendarState.selectedGroup === "all" || todo.group_name === calendarState.selectedGroup;
        });
        const visibleTodos = filteredTodos.slice(0, 3);
        const hiddenCount = Math.max(filteredTodos.length - visibleTodos.length, 0);

        cells.push({
            isoDate,
            dayNumber: cellDate.getDate(),
            isCurrentMonth,
            isToday,
            isSelected,
            todos: visibleTodos,
            hiddenCount
        });
    }

    return cells;
}

function applyCalendarRowHeights(cells) {
    const grid = document.getElementById("calendarGrid");
    const board = document.querySelector(".calendar-board");
    const main = document.querySelector(".calendar-main");
    const tabs = document.querySelector(".calendar-main .todo-tabs");
    const toolbar = document.querySelector(".calendar-toolbar");
    const weekdays = document.querySelector(".calendar-weekdays");

    if (!grid) {
        return;
    }

    const weeks = [];
    for (let index = 0; index < cells.length; index += 7) {
        weeks.push(cells.slice(index, index + 7));
    }

    const weekCount = weeks.length;
    if (!board || !main || !tabs || !toolbar || !weekdays || weekCount === 0) {
        grid.style.gridTemplateRows = `repeat(${weekCount}, 1fr)`;
        return;
    }

    const mainStyles = window.getComputedStyle(main);
    const mainPaddingTop = parseFloat(mainStyles.paddingTop) || 0;
    const mainPaddingBottom = parseFloat(mainStyles.paddingBottom) || 0;
    const mainGap = parseFloat(mainStyles.gap) || 0;
    const availableBoardHeight = main.clientHeight
        - mainPaddingTop
        - mainPaddingBottom
        - tabs.offsetHeight
        - toolbar.offsetHeight
        - (mainGap * 2);

    board.style.height = availableBoardHeight + "px";

    const weekdayHeight = weekdays.offsetHeight;
    const gridHeight = Math.max(availableBoardHeight - weekdayHeight, 0);
    const rowHeight = gridHeight / weekCount;

    grid.style.height = gridHeight + "px";
    grid.style.flexBasis = gridHeight + "px";
    grid.style.gridTemplateRows = `repeat(${weekCount}, ${rowHeight}px)`;
}

function renderCalendarGrid() {
    const grid = document.getElementById("calendarGrid");
    if (!grid) {
        return;
    }

    const cells = buildCalendarCells();
    applyCalendarRowHeights(cells);

    grid.innerHTML = cells.map((cell) => {
        const todoItems = cell.todos.map((todo) => {
            const classes = ["calendar-todo-item"];
            if (todo.isCompleted) {
                classes.push("is-completed");
            }

            return [
                '<div class="' + classes.join(" ") + '">',
                '  <span class="calendar-dot" style="background:' + getCategoryColor(todo) + '"></span>',
                '  <span class="calendar-todo-title">' + escapeCalendarHtml(todo.title || "") + "</span>",
                "</div>"
            ].join("");
        }).join("");

        const moreText = cell.hiddenCount > 0
            ? '<div class="calendar-more-count">+' + cell.hiddenCount + "</div>"
            : "";

        const emptyText = cell.isCurrentMonth && cell.todos.length === 0 && cell.hiddenCount === 0
            ? '<div class="calendar-empty-text"></div>'
            : "";

        const classes = ["calendar-day"];
        if (!cell.isCurrentMonth) {
            classes.push("is-outside-month");
        }
        if (cell.isToday) {
            classes.push("is-today");
        }
        if (cell.isSelected) {
            classes.push("is-selected");
        }

        const todayLabel = cell.isToday ? '<span class="calendar-date-label">(오늘)</span>' : "";

        return [
            '<button class="' + classes.join(" ") + '" type="button" data-date="' + cell.isoDate + '">',
            '  <div class="calendar-date-row">',
            '      <span class="calendar-date-number">' + cell.dayNumber + "</span>",
            todayLabel,
            "  </div>",
            '  <div class="calendar-day-list">',
            todoItems,
            moreText,
            emptyText,
            "  </div>",
            "</button>"
        ].join("");
    }).join("");

    grid.querySelectorAll(".calendar-day").forEach((cell) => {
        cell.addEventListener("click", () => {
            const isoDate = cell.dataset.date;
            if (!isoDate) {
                return;
            }

            const clickedDate = parseIsoDate(isoDate);
            if (clickedDate.getMonth() + 1 !== calendarState.viewMonth || clickedDate.getFullYear() !== calendarState.viewYear) {
                calendarState.viewYear = clickedDate.getFullYear();
                calendarState.viewMonth = clickedDate.getMonth() + 1;
                calendarState.selectedDate = isoDate;
                loadCalendarMonth();
                return;
            }

            calendarState.selectedDate = isoDate;
            renderCalendarGrid();
        });
    });
}

function escapeCalendarHtml(value) {
    return value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function renderGroupFilter() {
    const select = document.getElementById("calendarGroupFilter");
    if (!select) {
        return;
    }

    const groupNames = Array.from(new Set(
        calendarState.monthDays
            .flatMap((day) => (day.todos || []).map((todo) => todo.group_name))
            .filter(Boolean)
    ));

    const previousValue = calendarState.selectedGroup;
    const options = ['<option value="all">전체</option>']
        .concat(groupNames.map((groupName) => {
            return '<option value="' + escapeCalendarHtml(groupName) + '">' + escapeCalendarHtml(groupName) + "</option>";
        }));

    select.innerHTML = options.join("");
    if (previousValue !== "all" && groupNames.includes(previousValue)) {
        select.value = previousValue;
        calendarState.selectedGroup = previousValue;
    } else {
        select.value = "all";
        calendarState.selectedGroup = "all";
    }
}

function renderMonthLabel() {
    const label = document.getElementById("calendarMonthLabel");
    if (label) {
        label.textContent = formatMonthLabel(calendarState.viewYear, calendarState.viewMonth);
    }
}

function renderCalendarLoading() {
    const grid = document.getElementById("calendarGrid");
    if (grid) {
        grid.style.gridTemplateRows = "";
        grid.innerHTML = '<div class="calendar-loading">캘린더를 불러오는 중입니다...</div>';
    }
}

function renderCalendarError() {
    const grid = document.getElementById("calendarGrid");
    if (grid) {
        grid.style.gridTemplateRows = "";
        grid.innerHTML = '<div class="calendar-error">캘린더 데이터를 불러오지 못했습니다.</div>';
    }
}

async function loadCalendarMonth() {
    renderMonthLabel();
    renderCalendarLoading();

    try {
        const data = await fetchTodoJson(
            "/calendar/month?year=" + calendarState.viewYear + "&month=" + calendarState.viewMonth
        );
        calendarState.monthDays = Array.isArray(data?.days) ? data.days : [];
        renderGroupFilter();
        renderCalendarGrid();
    } catch (error) {
        console.error(error);
        renderCalendarError();
    }
}

function moveCalendarMonth(offset) {
    const nextDate = new Date(calendarState.viewYear, calendarState.viewMonth - 1 + offset, 1);
    calendarState.viewYear = nextDate.getFullYear();
    calendarState.viewMonth = nextDate.getMonth() + 1;

    const today = getTodayParts();
    if (calendarState.viewYear === today.year && calendarState.viewMonth === today.month) {
        calendarState.selectedDate = [
            today.year,
            String(today.month).padStart(2, "0"),
            String(today.day).padStart(2, "0")
        ].join("-");
    } else {
        calendarState.selectedDate = "";
    }

    loadCalendarMonth();
}

function bindCalendarControls() {
    document.getElementById("calendarPrevBtn")?.addEventListener("click", () => moveCalendarMonth(-1));
    document.getElementById("calendarNextBtn")?.addEventListener("click", () => moveCalendarMonth(1));
    document.getElementById("calendarTodayBtn")?.addEventListener("click", () => {
        const today = getTodayParts();
        calendarState.viewYear = today.year;
        calendarState.viewMonth = today.month;
        calendarState.selectedDate = [
            today.year,
            String(today.month).padStart(2, "0"),
            String(today.day).padStart(2, "0")
        ].join("-");
        loadCalendarMonth();
    });

    document.getElementById("calendarGroupFilter")?.addEventListener("change", (event) => {
        calendarState.selectedGroup = event.target.value;
        renderCalendarGrid();
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const today = getTodayParts();
    calendarState.viewYear = today.year;
    calendarState.viewMonth = today.month;
    calendarState.selectedDate = [
        today.year,
        String(today.month).padStart(2, "0"),
        String(today.day).padStart(2, "0")
    ].join("-");

    bindCalendarControls();
    loadCalendarMonth();
});

window.addEventListener("resize", () => {
    renderCalendarGrid();
});
