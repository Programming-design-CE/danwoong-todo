function getTodoAccessToken() {
    return localStorage.getItem("accessToken") || "";
}

function getTodoAuthHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getTodoAccessToken()
    };
}

async function fetchTodoJson(url, options) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...getTodoAuthHeaders(),
            ...(options?.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        return null;
    }

    if (!response.ok) {
        throw new Error("Request failed: " + response.status);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function initTodoModeToggle() {
    const toggle = document.getElementById("todoModeToggle");
    if (!toggle) {
        return;
    }

    toggle.addEventListener("change", () => {
        const nextUrl = toggle.dataset.toggleUrl;
        if (nextUrl) {
            window.location.href = nextUrl;
        }
    });
}

function initMemoPanel() {
    const panel = document.querySelector(".memo-panel");
    const textarea = document.getElementById("memoTextarea");
    const footer = document.getElementById("memoFooter");

    if (!panel || !textarea) {
        return;
    }

    const noteUrl = panel.dataset.noteUrl;
    let memoTimer;

    textarea.addEventListener("input", () => {
        clearTimeout(memoTimer);
        memoTimer = window.setTimeout(async () => {
            try {
                await fetch(noteUrl, {
                    method: "PUT",
                    headers: getTodoAuthHeaders(),
                    body: JSON.stringify({ content: textarea.textContent })
                });

                const now = new Date();
                const hour = String(now.getHours()).padStart(2, "0");
                const minute = String(now.getMinutes()).padStart(2, "0");
                if (footer) {
                    footer.textContent = "자동 저장됨 · 오늘 " + hour + ":" + minute;
                }
            } catch (error) {
                console.error(error);
            }
        }, 800);
    });

    if (!noteUrl) {
        return;
    }

    fetchTodoJson(noteUrl)
        .then((data) => {
            if (data && data.content) {
                textarea.textContent = data.content;
            }
        })
        .catch((error) => {
            console.error(error);
        });
}

document.addEventListener("DOMContentLoaded", () => {
    initTodoModeToggle();
    initMemoPanel();
});
