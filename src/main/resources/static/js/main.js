function getAccessToken() {
    return localStorage.getItem("accessToken");
}

async function requestAuthApi(url, options = {}) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return null;
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
        return null;
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
        nicknameText.textContent = data.nickname;

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

/* =========================
   친구창 기능
========================= */

function bindFriendEvents() {
    const friendIconBtn = document.getElementById("friendIconBtn");
    const friendPanel = document.getElementById("friendPanel");
    const friendCloseBtn = document.getElementById("friendCloseBtn");
    const openAddFriendBtn = document.getElementById("openAddFriendBtn");
    const friendSearchBox = document.getElementById("friendSearchBox");
    const friendSearchBtn = document.getElementById("friendSearchBtn");
    const friendSearchInput = document.getElementById("friendSearchInput");

    if (!friendIconBtn || !friendPanel) {
        console.warn("친구창 HTML 요소를 찾지 못했습니다.");
        return;
    }

    friendIconBtn.addEventListener("click", function () {
        const accessToken = getAccessToken();

        if (!accessToken) {
            alert("로그인이 필요한 기능입니다.");
            window.location.href = "/login";
            return;
        }

        friendPanel.classList.add("active");
        friendSearchBox.classList.remove("active");

        loadFriends();
        loadFriendRequests();
    });

    friendCloseBtn.addEventListener("click", function () {
        friendPanel.classList.remove("active");
        friendSearchBox.classList.remove("active");
    });

    openAddFriendBtn.addEventListener("click", function () {
        friendSearchBox.classList.toggle("active");
    });

    friendSearchBtn.addEventListener("click", function () {
        searchFriends();
    });

    friendSearchInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            searchFriends();
        }
    });
}

async function loadFriends() {
    const friendList = document.getElementById("friendList");

    try {
        const data = await requestAuthApi("/friends");

        if (!data) {
            return;
        }

        renderFriends(data.friends || []);

    } catch (error) {
        console.error(error);
        friendList.innerHTML = `<p class="empty-friend-message">친구 목록을 불러오지 못했습니다.</p>`;
    }
}

function renderFriends(friends) {
    const friendList = document.getElementById("friendList");
    friendList.innerHTML = "";

    if (friends.length === 0) {
        friendList.innerHTML = `<p class="empty-friend-message">아직 친구가 없습니다.</p>`;
        return;
    }

    friends.forEach(function (friend) {
        const item = document.createElement("div");
        item.className = "friend-item";

        item.innerHTML = `
            <img class="friend-thumb" src="${getFriendThumbnail(friend)}" alt="친구">
            <span class="friend-name">${friend.nickname}</span>
        `;

        friendList.appendChild(item);
    });
}

async function loadFriendRequests() {
    const requestList = document.getElementById("requestList");

    try {
        const data = await requestAuthApi("/friends/requests");

        if (!data) {
            return;
        }

        renderFriendRequests(data.requests || []);

    } catch (error) {
        console.error(error);
        requestList.innerHTML = `<p class="empty-friend-message">요청 목록을 불러오지 못했습니다.</p>`;
    }
}

function renderFriendRequests(requests) {
    const requestList = document.getElementById("requestList");
    requestList.innerHTML = "";

    if (requests.length === 0) {
        requestList.innerHTML = `<p class="empty-friend-message">받은 요청이 없습니다.</p>`;
        return;
    }

    requests.forEach(function (request) {
        const item = document.createElement("div");
        item.className = "request-item";

        item.innerHTML = `
            <span class="friend-name">${request.nickname}</span>
            <div class="request-actions">
                <button class="accept-btn" type="button">수락</button>
                <button class="reject-btn" type="button">거절</button>
            </div>
        `;

        item.querySelector(".accept-btn").addEventListener("click", function () {
            respondFriendRequest(request.request_id, "accept");
        });

        item.querySelector(".reject-btn").addEventListener("click", function () {
            respondFriendRequest(request.request_id, "reject");
        });

        requestList.appendChild(item);
    });
}

async function respondFriendRequest(requestId, action) {
    try {
        await requestAuthApi(`/friends/requests/${requestId}/${action}`, {
            method: "PATCH"
        });

        loadFriends();
        loadFriendRequests();

    } catch (error) {
        console.error(error);
        alert("친구 요청 처리에 실패했습니다.");
    }
}

async function searchFriends() {
    const keyword = document.getElementById("friendSearchInput").value.trim();
    const resultBox = document.getElementById("friendSearchResult");

    if (!keyword) {
        alert("검색어를 입력해주세요.");
        return;
    }

    try {
        const data = await requestAuthApi(`/friends/search?keyword=${encodeURIComponent(keyword)}`);

        if (!data) {
            return;
        }

        renderFriendSearchResult(data.users || []);

    } catch (error) {
        console.error(error);
        resultBox.innerHTML = `<p class="empty-friend-message">검색에 실패했습니다.</p>`;
    }
}

function renderFriendSearchResult(users) {
    const resultBox = document.getElementById("friendSearchResult");
    resultBox.innerHTML = "";

    if (users.length === 0) {
        resultBox.innerHTML = `<p class="empty-friend-message">검색 결과가 없습니다.</p>`;
        return;
    }

    users.forEach(function (user) {
        const item = document.createElement("div");
        item.className = "search-user-item";

        item.innerHTML = `
            <img class="friend-thumb" src="${getFriendThumbnail(user)}" alt="유저">
            <span class="friend-name">${user.nickname}</span>
            <button class="add-request-btn" type="button">추가</button>
        `;

        item.querySelector(".add-request-btn").addEventListener("click", function () {
            sendFriendRequest(user.user_id);
        });

        resultBox.appendChild(item);
    });
}

async function sendFriendRequest(receiverId) {
    try {
        await requestAuthApi("/friends/requests", {
            method: "POST",
            body: JSON.stringify({
                receiver_id: receiverId
            })
        });

        alert("친구 요청을 보냈습니다.");

    } catch (error) {
        console.error(error);
        alert("친구 요청에 실패했습니다.");
    }
}

function getFriendThumbnail(user) {
    if (user.character_thumbnail_url && user.character_thumbnail_url.trim() !== "") {
        return user.character_thumbnail_url;
    }

    return "/assets/garlic_one.png";
}

/* =========================
   최초 실행
========================= */

document.addEventListener("DOMContentLoaded", function () {
    loadMyInfo();
    loadMyCharacter();
    bindMenuEvents();
    bindFriendEvents();
});