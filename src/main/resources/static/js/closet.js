document.addEventListener("DOMContentLoaded", function () {
    loadClosetItems();
});

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

/**
 * 보유 아이템 조회
 * GET /closet/items
 */
async function loadClosetItems() {
    const itemList = document.getElementById("closetItemList");

    try {
        const data = await requestAuthApi("/closet/items");

        if (!data) {
            return;
        }

        renderClosetItems(data.items || []);

    } catch (error) {
        console.error(error);
        itemList.innerHTML = `
            <p class="error-message">아이템을 불러오지 못했습니다.</p>
        `;
    }
}

function renderClosetItems(items) {
    const itemList = document.getElementById("closetItemList");
    itemList.innerHTML = "";

    if (items.length === 0) {
        itemList.innerHTML = `
            <p class="empty-message">보유한 아이템이 없습니다.</p>
        `;
        return;
    }

    items.forEach(function (item) {
        const itemElement = document.createElement("div");
        itemElement.className = "closet-item";

        itemElement.innerHTML = `
            <img
                class="item-image"
                src="${getItemImage(item)}"
                alt="${item.item_name}"
            >
            <div class="item-quantity">× ${item.quantity}</div>
        `;

        itemElement.addEventListener("click", function () {
            handleItemClick(item);
        });

        itemList.appendChild(itemElement);
    });
}

/**
 * 아이템 클릭 시 처리
 * - 착용 아이템이면 PATCH /closet/equipped-items
 * - 소비 아이템이면 POST /closet/items/{itemId}/use
 */
function handleItemClick(item) {
    const itemType = item.item_type;

    if (isEquipItem(itemType)) {
        equipItem(item);
        return;
    }

    useItem(item);
}

/**
 * 장착 아이템 판별
 */
function isEquipItem(itemType) {
    return itemType === "HAT"
        || itemType === "CLOTHES"
        || itemType === "ACCESSORY"
        || itemType === "FACE"
        || itemType === "BACKGROUND";
}

/**
 * item_type을 slot_type으로 변환
 */
function getSlotType(itemType) {
    if (itemType === "HAT") {
        return "HAT";
    }

    if (itemType === "CLOTHES") {
        return "CLOTHES";
    }

    if (itemType === "ACCESSORY") {
        return "ACCESSORY";
    }

    if (itemType === "FACE") {
        return "FACE";
    }

    if (itemType === "BACKGROUND") {
        return "BACKGROUND";
    }

    return itemType;
}

/**
 * 아이템 장착
 * PATCH /closet/equipped-items
 */
async function equipItem(item) {
    const confirmed = confirm(`${item.item_name}을(를) 장착하시겠습니까?`);

    if (!confirmed) {
        return;
    }

    try {
        const data = await requestAuthApi("/closet/equipped-items", {
            method: "PATCH",
            body: JSON.stringify({
                item_id: item.item_id,
                slot_type: getSlotType(item.item_type)
            })
        });

        alert("아이템을 장착했습니다.");

        if (data && data.character_thumbnail_url) {
            updateCharacterImage(data.character_thumbnail_url);
        }

    } catch (error) {
        console.error(error);
        alert("아이템 장착에 실패했습니다.");
    }
}

/**
 * 아이템 사용
 * POST /closet/items/{itemId}/use
 */
async function useItem(item) {
    const confirmed = confirm(`${item.item_name}을(를) 사용하시겠습니까?`);

    if (!confirmed) {
        return;
    }

    try {
        await requestAuthApi(`/closet/items/${item.item_id}/use`, {
            method: "POST"
        });

        alert("아이템을 사용했습니다.");
        loadClosetItems();

    } catch (error) {
        console.error(error);
        alert("아이템 사용에 실패했습니다.");
    }
}

/**
 * 장착 후 캐릭터 이미지 갱신
 */
function updateCharacterImage(imageUrl) {
    const danwoongImage = document.getElementById("danwoongImage");

    if (!danwoongImage) {
        return;
    }

    danwoongImage.src = imageUrl;
}

/**
 * 아이템 이미지 결정
 * 현재 API 명세에는 item_image가 없으므로 기본 이미지를 사용.
 * 나중에 백엔드에서 item_image 내려주면 자동 반영됨.
 */
function getItemImage(item) {
    if (item.item_image && item.item_image.trim() !== "") {
        return item.item_image;
    }

    return "/assets/item_red.png";
}