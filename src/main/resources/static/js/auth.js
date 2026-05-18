const API_BASE_URL = "";

function setMessage(elementId, text, type) {
    const messageElement = document.getElementById(elementId);

    if (!messageElement) {
        return;
    }

    messageElement.textContent = text;
    messageElement.className = `message ${type}`;
}

async function requestApi(url, options) {
    const response = await fetch(`${API_BASE_URL}${url}`, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {
        throw new Error("요청 처리에 실패했습니다.");
    }

    const text = await response.text();

    if (!text) {
        return null;
    }

    return JSON.parse(text);
}

const signupForm = document.getElementById("signupForm");

if (signupForm) {
    signupForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const loginId = document.getElementById("signupLoginId").value.trim();
        const password = document.getElementById("signupPassword").value.trim();
        const nickname = document.getElementById("signupNickname").value.trim();
        const agreeCheck = document.getElementById("agreeCheck").checked;

        if (!agreeCheck) {
            setMessage("signupMessage", "약관에 동의해주세요.", "error");
            return;
        }

        try {
            await requestApi("/users/signup", {
                method: "POST",
                body: JSON.stringify({
                    login_id: loginId,
                    password: password,
                    nickname: nickname
                })
            });

            setMessage("signupMessage", "회원가입이 완료되었습니다.", "success");

            setTimeout(function () {
                window.location.href = "/login";
            }, 800);

        } catch (error) {
            setMessage("signupMessage", "회원가입에 실패했습니다.", "error");
        }
    });
}

const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const loginId = document.getElementById("loginId").value.trim();
        const password = document.getElementById("loginPassword").value.trim();

        try {
            const data = await requestApi("/users/login", {
                method: "POST",
                body: JSON.stringify({
                    login_id: loginId,
                    password: password
                })
            });

            localStorage.setItem("accessToken", data.access_token);
            localStorage.setItem("refreshToken", data.refresh_token);

            setMessage("loginMessage", "로그인 성공!", "success");

            setTimeout(function () {
                window.location.href = "/";
            }, 800);

        } catch (error) {
            setMessage("loginMessage", "아이디 또는 비밀번호를 확인해주세요.", "error");
        }
    });
}