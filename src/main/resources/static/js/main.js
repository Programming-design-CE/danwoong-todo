function getAccessToken() {
    return localStorage.getItem("accessToken");
}

async function requestAuthApi(url, options = {}) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${accessToken}`,
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        return;
    }

    if (!response.ok) {
        throw new Error("API 요청 실패");
    }

    const text = await response.text();

    if (!text) {
        return null;
    }

    return JSON.parse(text);
}

async function loadMyInfo() {
    try {
        const data = await requestAuthApi("/users");

        if (!data) {
            return;
        }

        const nicknameText = document.getElementById("nicknameText");

        nicknameText.textContent = data.nickname ;

    } catch (error) {
        console.error(error);
    }
}

async function loadMyCharacter() {
    try {
        const data = await requestAuthApi("/users/character");

        if (!data) {
            return;
        }

        console.log("내 캐릭터 정보:", data);

        /*
          나중에 equipped_items로 옷 이미지 붙일 때 여기서 처리하면 됨.

          data.equipped_items.forEach(item => {
              item.item_name
              item.item_type
              item.item_image
              item.slot_type
          });
        */

    } catch (error) {
        console.error(error);
    }
}

function bindMenuEvents() {
    const todoBtn = document.getElementById("todoBtn");
    const closetBtn = document.getElementById("closetBtn");
    const shopBtn = document.getElementById("shopBtn");

    todoBtn.addEventListener("click", function () {
        window.location.href = "/todos/working";
    });

    closetBtn.addEventListener("click", function () {
        window.location.href = "/closet";
    });

    shopBtn.addEventListener("click", function () {
        window.location.href = "/shop";
    });
}

document.addEventListener("DOMContentLoaded", function () {
    loadMyInfo();
    loadMyCharacter();
    bindMenuEvents();
});
