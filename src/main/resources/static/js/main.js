let currentFriends = [];
let currentSentRequests = [];

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
        throw new Error(`API 요청 실패 (${response.status})`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

async function loadMyInfo() {
    try {
        const data = await requestAuthApi("/users");
        if (!data) {
            return;
        }

        const nicknameText = document.getElementById("nicknameText");
        if (nicknameText) {
            nicknameText.textContent = data.nickname;
        }
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

    todoBtn?.addEventListener("click", () => {
        window.location.href = "/todos/working";
    });

    closetBtn?.addEventListener("click", () => {
        window.location.href = "/closet";
    });

    shopBtn?.addEventListener("click", () => {
        window.location.href = "/shop";
    });
}

function bindFriendEvents() {
    const friendIconBtn = document.getElementById("friendIconBtn");
    const friendPanel = document.getElementById("friendPanel");
    const friendCloseBtn = document.getElementById("friendCloseBtn");
    const openAddFriendBtn = document.getElementById("openAddFriendBtn");
    const friendSearchBox = document.getElementById("friendSearchBox");
    const friendSearchBtn = document.getElementById("friendSearchBtn");
    const friendSearchInput = document.getElementById("friendSearchInput");

    if (!friendIconBtn || !friendPanel || !friendSearchBox) {
        console.warn("친구 패널 요소를 찾지 못했습니다.");
        return;
    }

    friendIconBtn.addEventListener("click", async () => {
        const accessToken = getAccessToken();
        if (!accessToken) {
            alert("로그인이 필요한 기능입니다.");
            window.location.href = "/login";
            return;
        }

        friendPanel.classList.add("active");
        friendSearchBox.classList.remove("active");

        await refreshFriendPanel();

        // 친구창 열었을 때 말풍선 상태도 다시 확인
        await checkIncomingFriendRequests();
    });

    friendCloseBtn?.addEventListener("click", () => {
        friendPanel.classList.remove("active");
        closeFriendSearchBox();
    });

    openAddFriendBtn?.addEventListener("click", () => {
        friendSearchBox.classList.toggle("active");
    });

    friendSearchBtn?.addEventListener("click", () => {
        searchFriends();
    });

    friendSearchInput?.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            searchFriends();
        }

        if (event.key === "Escape") {
            closeFriendSearchBox();
        }
    });

    document.addEventListener("click", (event) => {
        if (!friendPanel.classList.contains("active")) {
            return;
        }

        const target = event.target;
        if (!(target instanceof Node)) {
            return;
        }

        const clickedInsideSearchBox = friendSearchBox.contains(target);
        const clickedOpenButton = openAddFriendBtn?.contains(target);

        if (!clickedInsideSearchBox && !clickedOpenButton) {
            closeFriendSearchBox();
        }
    });
}

async function refreshFriendPanel() {
    await Promise.all([
        loadFriends(),
        loadFriendRequests(),
        loadSentFriendRequests()
    ]);
}

async function loadFriends() {
    const friendList = document.getElementById("friendList");

    try {
        const data = await requestAuthApi("/friends");
        if (!data) {
            return;
        }

        currentFriends = data.friends || [];
        renderFriends(currentFriends);
    } catch (error) {
        console.error(error);
        currentFriends = [];
        if (friendList) {
            friendList.innerHTML = `<p class="empty-friend-message">친구 목록을 불러오지 못했습니다.</p>`;
        }
    }
}

function renderFriends(friends) {
    const friendList = document.getElementById("friendList");
    if (!friendList) {
        return;
    }

    friendList.innerHTML = "";

    if (friends.length === 0) {
        friendList.innerHTML = `<p class="empty-friend-message">아직 친구가 없습니다.</p>`;
        return;
    }

    friends.forEach((friend) => {
        const item = document.createElement("div");
        item.className = "friend-item";
        item.innerHTML = `
            <img class="friend-thumb" src="${getFriendThumbnail(friend)}" alt="친구">
            <span class="friend-name">${escapeHtml(friend.nickname || "이름 없음")}</span>
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
        if (requestList) {
            requestList.innerHTML = `<p class="empty-friend-message">받은 요청 목록을 불러오지 못했습니다.</p>`;
        }
    }
}

function renderFriendRequests(requests) {
    const requestList = document.getElementById("requestList");
    if (!requestList) {
        return;
    }

    requestList.innerHTML = "";

    if (requests.length === 0) {
        requestList.innerHTML = `<p class="empty-friend-message">받은 요청이 없습니다.</p>`;
        return;
    }

    requests.forEach((request) => {
        const item = document.createElement("div");
        item.className = "request-item";
        item.innerHTML = `
            <span class="friend-name">${escapeHtml(request.nickname || "이름 없음")}</span>
            <div class="request-actions">
                <button class="accept-btn" type="button">수락</button>
                <button class="reject-btn" type="button">거절</button>
            </div>
        `;

        item.querySelector(".accept-btn")?.addEventListener("click", () => {
            respondFriendRequest(request.request_id, "accept");
        });

        item.querySelector(".reject-btn")?.addEventListener("click", () => {
            respondFriendRequest(request.request_id, "reject");
        });

        requestList.appendChild(item);
    });
}

async function loadSentFriendRequests() {
    const sentRequestList = document.getElementById("sentRequestList");

    try {
        const data = await requestAuthApi("/friends/requests/sent");
        if (!data) {
            return;
        }

        currentSentRequests = data.requests || [];
        renderSentFriendRequests(currentSentRequests);
    } catch (error) {
        console.error(error);
        currentSentRequests = [];
        if (sentRequestList) {
            sentRequestList.innerHTML = `<p class="empty-friend-message">보낸 요청 목록을 불러오지 못했습니다.</p>`;
        }
    }
}

function renderSentFriendRequests(requests) {
    const sentRequestList = document.getElementById("sentRequestList");
    if (!sentRequestList) {
        return;
    }

    sentRequestList.innerHTML = "";

    if (requests.length === 0) {
        sentRequestList.innerHTML = `<p class="empty-friend-message">보낸 요청이 없습니다.</p>`;
        return;
    }

    requests.forEach((request) => {
        const item = document.createElement("div");
        item.className = "request-item";
        item.innerHTML = `
            <span class="friend-name">${escapeHtml(request.nickname || "이름 없음")}</span>
            <span class="pending-badge">대기중</span>
        `;
        sentRequestList.appendChild(item);
    });
}

async function respondFriendRequest(requestId, action) {
    try {
        await requestAuthApi(`/friends/requests/${requestId}/${action}`, {
            method: "PATCH"
        });

        await refreshFriendPanel();

        // 수락/거절 후 남은 요청 개수에 따라 말풍선 갱신
        await checkIncomingFriendRequests();
    } catch (error) {
        console.error(error);
        alert("친구 요청 처리에 실패했습니다.");
    }
}

async function searchFriends() {
    const keywordInput = document.getElementById("friendSearchInput");
    const resultBox = document.getElementById("friendSearchResult");
    const keyword = keywordInput?.value.trim() || "";

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
        if (resultBox) {
            resultBox.innerHTML = `<p class="empty-friend-message">검색에 실패했습니다.</p>`;
        }
    }
}

function renderFriendSearchResult(users) {
    const resultBox = document.getElementById("friendSearchResult");
    if (!resultBox) {
        return;
    }

    resultBox.innerHTML = "";

    if (users.length === 0) {
        resultBox.innerHTML = `<p class="empty-friend-message">검색 결과가 없습니다.</p>`;
        return;
    }

    users.forEach((user) => {
        const isFriend = currentFriends.some((friend) => Number(friend.user_id) === Number(user.user_id));
        const isPending = currentSentRequests.some((request) => Number(request.receiver_id) === Number(user.user_id));
        const buttonLabel = isFriend ? "친구" : (isPending ? "대기중" : "추가");
        const buttonDisabled = isFriend || isPending;

        const item = document.createElement("div");
        item.className = "search-user-item";
        item.innerHTML = `
            <img class="friend-thumb" src="${getFriendThumbnail(user)}" alt="유저">
            <span class="friend-name">${escapeHtml(user.nickname || "이름 없음")}</span>
            <button class="add-request-btn" type="button" ${buttonDisabled ? "disabled" : ""}>${buttonLabel}</button>
        `;

        if (!buttonDisabled) {
            item.querySelector(".add-request-btn")?.addEventListener("click", () => {
                sendFriendRequest(user.user_id);
            });
        }

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

        await loadSentFriendRequests();

        const currentKeyword = document.getElementById("friendSearchInput")?.value.trim();
        if (currentKeyword) {
            await searchFriends();
        }

        closeFriendSearchBox();
        alert("친구 요청을 보냈습니다.");
    } catch (error) {
        console.error(error);
        alert("친구 요청에 실패했습니다.");
    }
}

async function checkIncomingFriendRequests() {
    const bubble = document.getElementById("friendRequestBubble");

    if (!bubble) {
        return;
    }

    const accessToken = getAccessToken();

    if (!accessToken) {
        bubble.classList.remove("active");
        return;
    }

    try {
        const data = await requestAuthApi("/friends/requests");
        if (!data) {
            bubble.classList.remove("active");
            return;
        }

        const requests = data.requests || [];

        if (requests.length > 0) {
            bubble.textContent = requests.length === 1
                ? "친구 요청이 왔어요!"
                : `친구 요청 ${requests.length}개가 왔어요!`;

            bubble.classList.add("active");
        } else {
            bubble.classList.remove("active");
        }
    } catch (error) {
        console.error(error);
        bubble.classList.remove("active");
    }
}

function getFriendThumbnail(user) {
    if (user.character_thumbnail_url && user.character_thumbnail_url.trim() !== "") {
        return user.character_thumbnail_url;
    }

    return "/assets/garlic_one.png";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function closeFriendSearchBox() {
    const friendSearchBox = document.getElementById("friendSearchBox");
    const friendSearchInput = document.getElementById("friendSearchInput");
    const friendSearchResult = document.getElementById("friendSearchResult");

    friendSearchBox?.classList.remove("active");

    if (friendSearchInput) {
        friendSearchInput.value = "";
    }

    if (friendSearchResult) {
        friendSearchResult.innerHTML = "";
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadMyInfo();
    loadMyCharacter();
    bindMenuEvents();
    bindFriendEvents();
    checkIncomingFriendRequests();
});