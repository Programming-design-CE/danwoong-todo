document.addEventListener("DOMContentLoaded", () => {
    loadGarlicCount();
    loadShopItems();
});

const garlicCountEl = document.getElementById("garlicCount");
const itemListEl = document.getElementById("itemList");

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
            "Authorization": `Bearer ${accessToken}`,
            ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
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
        throw new Error(`API 요청에 실패했습니다. (${response.status})`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

async function loadGarlicCount() {
    try {
        const payload = await requestAuthApi("/shop/garlic");

        if (!payload) {
            return;
        }

        const body = getResponseBody(payload);

        if (garlicCountEl) {
            garlicCountEl.textContent = Number(body?.garlicCount ?? body?.garlic_count ?? 0).toLocaleString();
        }
    } catch (error) {
        console.error(error);
        if (garlicCountEl) {
            garlicCountEl.textContent = "0";
        }
    }
}

async function loadShopItems() {
    try {
        const payload = await requestAuthApi("/shop/items");

        if (!payload) {
            return;
        }

        const body = getResponseBody(payload);
        const items = Array.isArray(body) ? body : (body?.items || []);

        renderShopItems(items);
    } catch (error) {
        console.error(error);
        if (itemListEl) {
            itemListEl.innerHTML = `
                <p class="error-message">아이템을 불러오지 못했습니다.</p>
            `;
        }
    }
}

function renderShopItems(items) {
    if (!itemListEl) {
        return;
    }

    itemListEl.innerHTML = "";

    if (items.length === 0) {
        itemListEl.innerHTML = `
            <p class="empty-message">판매 중인 아이템이 없습니다.</p>
        `;
        return;
    }

    items.forEach((item) => {
        const itemElement = document.createElement("article");
        itemElement.className = "shop-item";

        itemElement.innerHTML = `
            <img
                class="item-image"
                src="${getItemImage(item)}"
                alt="${escapeHtml(getItemName(item))}"
            >

            <div class="price-box">
                <img src="/assets/garlic.png" alt="마늘">
                <span>${Number(item.price || 0).toLocaleString()}</span>
            </div>
        `;

        itemElement.addEventListener("click", () => {
            purchaseItem(item);
        });

        itemListEl.appendChild(itemElement);
    });
}

function getItemImage(item) {
    const itemImage = item.itemImage || item.item_image || "";
    return itemImage.trim() !== "" ? itemImage : "/assets/item_red.png";
}

function getItemName(item) {
    return item.itemName || item.item_name || "이름 없는 아이템";
}

function getItemId(item) {
    return item.itemId || item.item_id;
}

async function purchaseItem(item) {
    const confirmed = confirm(
        `${getItemName(item)}을(를) 구매하시겠습니까?\n가격: ${Number(item.price || 0).toLocaleString()} 마늘`
    );

    if (!confirmed) {
        return;
    }

    try {
        const payload = await requestAuthApi(`/shop/items/${getItemId(item)}/purchase`, {
            method: "POST",
            body: JSON.stringify({
                count: 1
            })
        });

        const body = getResponseBody(payload);

        alert("구매가 완료되었습니다.");

        if (body?.remainingGarlic !== undefined || body?.remaining_garlic !== undefined) {
            if (garlicCountEl) {
                garlicCountEl.textContent = Number(body.remainingGarlic ?? body.remaining_garlic ?? 0).toLocaleString();
            }
        } else {
            loadGarlicCount();
        }
    } catch (error) {
        console.error(error);
        alert("구매에 실패했습니다. 보유 마늘과 서버 상태를 확인해주세요.");
    }
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
