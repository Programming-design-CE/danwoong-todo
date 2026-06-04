// 현재 착용 중인 아이템 ID Set (중복 착용 방지 및 뱃지 표시용)
let equippedItemIds = new Set();

document.addEventListener("DOMContentLoaded", async () => {
    // 착용 아이템을 먼저 로드한 뒤 인벤토리를 렌더링해야 "착용 중" 뱃지가 정확히 표시됨
    await loadEquippedItems();
    await loadClosetItems();
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
        throw new Error(`API 요청 실패 (${response.status})`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

async function loadClosetItems() {
    const itemList = document.getElementById("closetItemList");

    try {
        const payload = await requestAuthApi("/closet/items");

        if (!payload) {
            return;
        }

        const body = getResponseBody(payload);
        const items = Array.isArray(body) ? body : (body?.items || []);

        renderClosetItems(items);
    } catch (error) {
        console.error(error);
        if (itemList) {
            itemList.innerHTML = `
                <p class="error-message">아이템을 불러오지 못했습니다.</p>
            `;
        }
    }
}

async function loadEquippedItems() {
    try {
        const payload = await requestAuthApi("/closet/equipped-items");

        if (!payload) {
            return;
        }

        const body = getResponseBody(payload);
        const items = Array.isArray(body) ? body : (body?.items || []);

        renderEquippedItems(items);
    } catch (error) {
        console.error(error);
    }
}

function renderClosetItems(items) {
    const itemList = document.getElementById("closetItemList");
    if (!itemList) {
        return;
    }

    itemList.innerHTML = "";

    if (items.length === 0) {
        itemList.innerHTML = `
            <p class="empty-message">보유한 아이템이 없습니다.</p>
        `;
        return;
    }

    items.forEach((item) => {
        const isEquipped = equippedItemIds.has(getItemId(item));
        const itemElement = document.createElement("div");
        itemElement.className = "closet-item" + (isEquipped ? " closet-item--equipped" : "");

        itemElement.innerHTML = `
            <div class="item-card-inner">
                <img
                    class="item-image"
                    src="${getItemImage(item)}"
                    alt="${escapeHtml(getItemName(item))}"
                >
                ${isEquipped ? `<span class="equipped-badge">착용 중</span>` : ""}
            </div>
            <div class="item-quantity">x ${item.quantity || 0}</div>
        `;

        itemElement.addEventListener("click", () => {
            handleItemClick(item);
        });

        itemList.appendChild(itemElement);
    });
}

function renderEquippedItems(items) {
    // 착용 중인 itemId Set 갱신
    equippedItemIds = new Set(items.map((item) => getItemId(item)));

    const equippedBySlot = {
        BACKGROUND: null,
        HAT: null,
        ACCESSORY: null,
        CLOTHES: null,
        FACE: null
    };

    items.forEach((item) => {
        const slotType = (item.slotType || item.slot_type || "").toUpperCase();
        if (slotType in equippedBySlot) {
            equippedBySlot[slotType] = item;
        }
    });

    applyEquippedLayer("equippedBackground", equippedBySlot.BACKGROUND);
    applyEquippedLayer("equippedHat", equippedBySlot.HAT);
    applyEquippedLayer("equippedAccessory", equippedBySlot.ACCESSORY);
    applyEquippedLayer("equippedClothes", equippedBySlot.CLOTHES);
    applyEquippedLayer("equippedFace", equippedBySlot.FACE);
}

function applyEquippedLayer(elementId, item) {
    const layer = document.getElementById(elementId);
    if (!layer) {
        return;
    }

    const imageUrl = item ? getItemImage(item) : "";

    if (!item || !imageUrl) {
        layer.src = "";
        layer.alt = "";
        layer.hidden = true;
        layer.classList.remove("is-visible");
        return;
    }

    layer.src = imageUrl;
    layer.alt = getItemName(item);
    layer.hidden = false;
    layer.classList.add("is-visible");
}

function handleItemClick(item) {
    const itemType = getItemType(item);

    if (isEquipItem(itemType)) {
        equipItem(item);
        return;
    }

    useItem(item);
}

function isEquipItem(itemType) {
    return itemType === "HAT"
        || itemType === "CLOTHES"
        || itemType === "ACCESSORY"
        || itemType === "FACE"
        || itemType === "BACKGROUND";
}

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

async function equipItem(item) {
    // 이미 착용 중이면 중복 착용 방지
    if (equippedItemIds.has(getItemId(item))) {
        alert(`${getItemName(item)}은(는) 이미 착용 중입니다.`);
        return;
    }

    const confirmed = confirm(`${getItemName(item)}을(를) 착용하시겠습니까?`);

    if (!confirmed) {
        return;
    }

    try {
        await requestAuthApi("/closet/equipped-items", {
            method: "PATCH",
            body: JSON.stringify({
                itemId: getItemId(item),
                slotType: getSlotType(getItemType(item))
            })
        });

        alert("아이템을 착용했습니다.");
        // 착용 상태 갱신 → 뱃지 반영을 위해 인벤토리도 다시 렌더링
        await loadEquippedItems();
        await loadClosetItems();
    } catch (error) {
        console.error(error);
        alert(error.message || "아이템 착용에 실패했습니다.");
    }
}

async function useItem(item) {
    const confirmed = confirm(`${getItemName(item)}을(를) 사용하시겠습니까?`);

    if (!confirmed) {
        return;
    }

    try {
        await requestAuthApi(`/closet/items/${getItemId(item)}/use`, {
            method: "POST"
        });

        alert("아이템을 사용했습니다.");
        await loadClosetItems();
    } catch (error) {
        console.error(error);
        alert("아이템 사용에 실패했습니다.");
    }
}

function getItemImage(item) {
    const itemImage = item.itemImage || item.item_image || "";
    return itemImage.trim() !== "" ? itemImage : "";
}

function getItemName(item) {
    return item.itemName || item.item_name || "이름 없는 아이템";
}

function getItemId(item) {
    return item.itemId || item.item_id;
}

function getItemType(item) {
    return (item.itemType || item.item_type || "").toUpperCase();
}

function getResponseBody(response) {
    if (!response) {
        return null;
    }

    return response.data ?? response;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}