document.addEventListener('DOMContentLoaded', () => {
    loadGarlicCount();
    loadShopItems();
});

const garlicCountEl = document.getElementById('garlicCount');
const itemListEl = document.getElementById('itemList');

/**
 * 보유 마늘 조회
 * GET /shop/garlic
 */
async function loadGarlicCount() {
    try {
        const response = await fetch('/shop/garlic');

        if (!response.ok) {
            throw new Error('보유 마늘 조회 실패');
        }

        const data = await response.json();

        garlicCountEl.textContent = Number(data.garlic_count).toLocaleString();
    } catch (error) {
        console.error(error);
        garlicCountEl.textContent = '0';
    }
}

/**
 * 판매 아이템 목록 조회
 * GET /shop/items
 */
async function loadShopItems() {
    try {
        const response = await fetch('/shop/items');

        if (!response.ok) {
            throw new Error('상점 아이템 목록 조회 실패');
        }

        const data = await response.json();

        renderShopItems(data.items || []);
    } catch (error) {
        console.error(error);
        itemListEl.innerHTML = `
            <p class="error-message">아이템을 불러오지 못했습니다.</p>
        `;
    }
}

/**
 * 아이템 화면 렌더링
 */
function renderShopItems(items) {
    itemListEl.innerHTML = '';

    if (items.length === 0) {
        itemListEl.innerHTML = `
            <p class="empty-message">판매 중인 아이템이 없습니다.</p>
        `;
        return;
    }

    items.forEach((item) => {
        const itemElement = document.createElement('article');
        itemElement.className = 'shop-item';

        itemElement.innerHTML = `
            <img
                class="item-image"
                src="${getItemImage(item)}"
                alt="${item.item_name}"
            >

            <div class="price-box">
                <img src="/assets/garlic.png" alt="마늘">
                <span>${Number(item.price).toLocaleString()}</span>
            </div>
        `;

        itemElement.addEventListener('click', () => {
            purchaseItem(item);
        });

        itemListEl.appendChild(itemElement);
    });
}

/**
 * 아이템 이미지 결정
 */
function getItemImage(item) {
    if (item.item_image && item.item_image.trim() !== '') {
        return item.item_image;
    }

    return '/assets/item_red.png';
}

/**
 * 아이템 구매
 * POST /shop/items/{itemId}/purchase
 */
async function purchaseItem(item) {
    const confirmed = confirm(
        `${item.item_name}을(를) 구매하시겠습니까?\n가격: ${item.price.toLocaleString()} 마늘`
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(`/shop/items/${item.item_id}/purchase`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                count: 1
            })
        });

        if (!response.ok) {
            throw new Error('아이템 구매 실패');
        }

        const data = await response.json();

        alert('구매가 완료되었습니다.');

        if (data.remaining_garlic !== undefined) {
            garlicCountEl.textContent = Number(data.remaining_garlic).toLocaleString();
        } else {
            loadGarlicCount();
        }

    } catch (error) {
        console.error(error);
        alert('구매에 실패했습니다. 보유 마늘 또는 서버 상태를 확인해주세요.');
    }
}