const GUEST_CART_KEY = 'cohouse_guest_cart_v1';
let cartItems = []; // 전역 장바구니 데이터 저장
const selectedItemIds = new Set();
let isGuestCart = false;
let optionModalRefs = null;
let optionModalInitialized = false;
let editingOptionIndex = null;

document.addEventListener('DOMContentLoaded', function() {
    loadCartPreview();
    setupEventListeners();
});

function setupEventListeners() {
    // 전체 선택/해제 버튼
    const selectAllBtn = document.getElementById('select-all-btn');
    const deselectAllBtn = document.getElementById('deselect-all-btn');
    const removeSelectedBtn = document.getElementById('remove-selected-btn');
    const checkoutBtn = document.getElementById('checkout-button');

    if (selectAllBtn) {
        selectAllBtn.addEventListener('click', selectAllItems);
    }
    if (deselectAllBtn) {
        deselectAllBtn.addEventListener('click', deselectAllItems);
    }
    if (removeSelectedBtn) {
        removeSelectedBtn.addEventListener('click', removeSelectedItems);
    }
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', proceedToCheckout);
    }

    initializeOptionModal();
}

function initializeOptionModal() {
    if (optionModalInitialized) {
        return;
    }

    const modal = document.getElementById('cart-option-modal');
    if (!modal) {
        return;
    }

    optionModalRefs = {
        modal,
        overlay: document.getElementById('cart-option-overlay'),
        panel: modal.querySelector('.cart-option-modal__panel'),
        productTitle: document.getElementById('cart-option-product-title'),
        select: document.getElementById('cart-option-select'),
        emptyMessage: document.getElementById('cart-option-empty-message'),
        cancelBtn: document.getElementById('cart-option-cancel'),
        saveBtn: document.getElementById('cart-option-save'),
        closeBtn: document.getElementById('cart-option-close'),
        lastTrigger: null
    };

    optionModalRefs.overlay?.addEventListener('click', () => closeOptionModal(true));
    optionModalRefs.cancelBtn?.addEventListener('click', () => closeOptionModal(true));
    optionModalRefs.closeBtn?.addEventListener('click', () => closeOptionModal(true));
    optionModalRefs.saveBtn?.addEventListener('click', handleOptionSave);

    document.addEventListener('keydown', handleOptionKeydown);

    optionModalInitialized = true;
}

function openOptionModal(index, triggerEl) {
    if (!optionModalInitialized) {
        initializeOptionModal();
    }
    if (!optionModalRefs || !optionModalRefs.modal) {
        return;
    }

    const item = cartItems[index];
    if (!item) {
        return;
    }

    editingOptionIndex = index;
    optionModalRefs.lastTrigger = triggerEl || null;

    const options = item && Array.isArray(item.options) ? item.options : [];
    const selectEl = optionModalRefs.select;
    const emptyMessageEl = optionModalRefs.emptyMessage;
    const saveBtn = optionModalRefs.saveBtn;
    const hasPendingOption = Object.prototype.hasOwnProperty.call(item, 'pendingOptionId');
    const initialOptionId = hasPendingOption ? item.pendingOptionId : (item.selectedOptionId != null ? item.selectedOptionId : null);

    if (optionModalRefs.productTitle) {
        optionModalRefs.productTitle.textContent = item.title || '상품';
    }

    if (!options.length) {
        if (selectEl) {
            selectEl.hidden = true;
            selectEl.innerHTML = '';
        }
        if (emptyMessageEl) {
            emptyMessageEl.hidden = false;
            emptyMessageEl.textContent = '변경 가능한 옵션이 없습니다.';
        }
        if (saveBtn) {
            saveBtn.disabled = true;
        }
    } else {
        if (selectEl) {
            selectEl.hidden = false;
            selectEl.innerHTML = options.map(opt => {
                const label = buildOptionLabelText(opt);
                return `<option value="${opt.optionId}">${escapeHtml(label)}</option>`;
            }).join('');
            if (initialOptionId != null && options.some(opt => String(opt.optionId) === String(initialOptionId))) {
                selectEl.value = String(initialOptionId);
            } else if (selectEl.options.length > 0) {
                selectEl.selectedIndex = 0;
            }
        }
        if (emptyMessageEl) {
            emptyMessageEl.hidden = true;
        }
        if (saveBtn) {
            saveBtn.disabled = false;
        }
    }

    optionModalRefs.modal.hidden = false;
    document.body.classList.add('modal-open');

    if (selectEl && !selectEl.hidden) {
        setTimeout(() => selectEl.focus(), 10);
    } else {
        optionModalRefs.closeBtn?.focus();
    }
}

function closeOptionModal(returnFocus = false) {
    if (!optionModalRefs || !optionModalRefs.modal) {
        return;
    }

    optionModalRefs.modal.hidden = true;
    document.body.classList.remove('modal-open');
    editingOptionIndex = null;

    if (returnFocus && optionModalRefs.lastTrigger && typeof optionModalRefs.lastTrigger.focus === 'function') {
        optionModalRefs.lastTrigger.focus();
    }
}

async function handleOptionSave() {
    if (editingOptionIndex == null || !optionModalRefs) {
        closeOptionModal(true);
        return;
    }

    const saveBtn = optionModalRefs.saveBtn;
    if (saveBtn) {
        saveBtn.disabled = true;
    }

    const item = cartItems[editingOptionIndex];
    if (!item) {
        closeOptionModal(true);
        if (saveBtn) saveBtn.disabled = false;
        return;
    }

    const selectEl = optionModalRefs.select;
    const selectedValue = selectEl && !selectEl.hidden && selectEl.value ? selectEl.value : null;

    const normalizedValue = selectedValue && String(selectedValue).length ? String(selectedValue) : null;
    const options = item && Array.isArray(item.options) ? item.options : [];
    const matched = options.find(opt => String(opt.optionId) === String(normalizedValue));
    const newOptionId = normalizedValue != null ? Number(normalizedValue) : null;
    const originalOptionId = item && item.selectedOptionId != null ? Number(item.selectedOptionId) : null;
    const label = matched ? buildOptionLabelText(matched) : '';

    if (newOptionId === originalOptionId) {
        if (Object.prototype.hasOwnProperty.call(item, 'pendingOptionId')) {
            delete item.pendingOptionId;
            delete item.pendingSelectedOptions;
            delete item.pendingOptionLabel;
        }
    } else {
        item.pendingOptionId = newOptionId;
        item.pendingSelectedOptions = normalizedValue;
        item.pendingOptionLabel = label;
    }

    const targetIndex = editingOptionIndex;

    closeOptionModal(true);
    refreshCart();
    if (targetIndex != null) {
        applyItemChanges(targetIndex);
    }

    if (saveBtn) {
        saveBtn.disabled = false;
    }
}

function handleOptionKeydown(event) {
    if (event.key !== 'Escape') {
        return;
    }
    if (!optionModalRefs || !optionModalRefs.modal || optionModalRefs.modal.hidden) {
        return;
    }
    closeOptionModal(true);
}

function buildOptionLabelText(option) {
    if (!option) {
        return '';
    }
    const name = option.optionName ? String(option.optionName).trim() : '';
    const value = option.optionValue ? String(option.optionValue).trim() : '';
    if (name && value) {
        return `${name} - ${value}`;
    }
    return name || value;
}

async function loadCartPreview() {
    const refs = {
        loadingEl: document.getElementById('cart-loading'),
        emptyEl: document.getElementById('cart-empty'),
        errorEl: document.getElementById('cart-error'),
        unauthEl: document.getElementById('cart-unauth'),
        guestInfoEl: document.getElementById('guest-info'),
        listEl: document.getElementById('cart-item-list'),
        countEl: document.getElementById('cart-count'),
        actionsEl: document.getElementById('item-actions'),
        checkoutBtn: document.getElementById('checkout-button')
    };

    selectedItemIds.clear();
    resetCartState(refs);

    if (refs.loadingEl) {
        refs.loadingEl.hidden = false;
    }

    try {
        // 실제 API 호출 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 800));

        const response = await fetch('/api/cart', {
            headers: { 'Accept': 'application/json' },
            credentials: 'include'
        });

        if (refs.loadingEl) {
            refs.loadingEl.hidden = true;
        }

        if (response.status === 401 || response.status === 403) {
            const rendered = renderGuestCart(refs);
            if (!rendered) {
                if (refs.unauthEl) refs.unauthEl.hidden = false;
            }
            return;
        }

        if (!response.ok) {
            throw new Error('Cart API error');
        }

        const data = await response.json();
        const items = Array.isArray(data?.items) ? data.items : [];

        cartItems = normalizeApiItems(items);
        isGuestCart = false;
        syncSelectionWithCart();

        const baseSummary = calculateSummary(cartItems);
        renderItems(cartItems, refs, mergedSummary);
        updateSelectedSummary();

        if (cartItems.length > 0) {
            if (refs.actionsEl) refs.actionsEl.hidden = false;
            if (refs.checkoutBtn) refs.checkoutBtn.disabled = false;
        }
    } catch (error) {
        console.error('Cart loading error:', error);
        if (refs.loadingEl) refs.loadingEl.hidden = true;

        // 데모 데이터로 폴백
        const demoData = createDemoData();
        cartItems = demoData.items;
        isGuestCart = false;
        selectedItemIds.clear();
        syncSelectionWithCart();

        const mergedSummary = Object.assign({}, calculateSummary(cartItems), demoData.summary || {});
        renderSummary(mergedSummary, refs);
        renderItems(cartItems, refs, mergedSummary);

        if (cartItems.length > 0) {
            if (refs.actionsEl) refs.actionsEl.hidden = false;
            if (refs.checkoutBtn) refs.checkoutBtn.disabled = false;
        }
    }
}

function createDemoData() {
    return {
        items: [
            {
                id: 1,
                cartItemId: null,
                title: '모던 원목 책상',
                quantity: 2,
                unitPrice: 150000,
                finalPrice: 150000,
                lineTotal: 300000,
                imageUrl: 'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=300&h=300&fit=crop',
                optionLabel: '오크 브라운, 120cm',
                outOfStock: false
            },
            {
                id: 2,
                cartItemId: null,
                title: '에르고 체어',
                quantity: 1,
                unitPrice: 89000,
                finalPrice: 89000,
                lineTotal: 89000,
                imageUrl: 'https://images.unsplash.com/photo-1581539250439-c96689b516dd?w=300&h=300&fit=crop',
                optionLabel: '블랙',
                outOfStock: false
            },
            {
                id: 3,
                cartItemId: null,
                title: '북유럽 스탠드 조명',
                quantity: 1,
                unitPrice: 45000,
                finalPrice: 45000,
                lineTotal: 45000,
                imageUrl: 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=300&h=300&fit=crop',
                optionLabel: '화이트',
                outOfStock: false
            },
            {
                id: 4,
                cartItemId: null,
                title: '미니멀 수납함',
                quantity: 3,
                unitPrice: 25000,
                finalPrice: 25000,
                lineTotal: 75000,
                imageUrl: 'https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=300&h=300&fit=crop',
                optionLabel: '네이처 우드',
                outOfStock: false
            },
            {
                id: 5,
                cartItemId: null,
                title: '소프트 쿠션',
                quantity: 2,
                unitPrice: 18000,
                finalPrice: 18000,
                lineTotal: 36000,
                imageUrl: 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=300&h=300&fit=crop',
                optionLabel: '베이지, 45x45cm',
                outOfStock: true
            }
        ],
        summary: {
            totalQuantity: 9,
            totalAmount: 545000,
            expectedDiscount: 27250,
            expectedAmount: 517750
        }
    };
}

function renderGuestCart(refs) {
    const guestItems = loadGuestCartItems();
    isGuestCart = true;

    if (guestItems.length === 0) {
        selectedItemIds.clear();
        saveGuestCartItems([]);
        updateSummary(null, refs);
        if (refs.emptyEl) refs.emptyEl.hidden = false;
        if (refs.guestInfoEl) refs.guestInfoEl.hidden = true;
        return false;
    }

    const normalized = guestItems.map(toGuestDisplayItem);
    cartItems = normalized;
    selectedItemIds.clear();
    syncSelectionWithCart();
    saveGuestCartItems(cartItems);

    const guestSummary = calculateGuestSummary(normalized);

    renderSummary(guestSummary, refs);
    renderItems(normalized, refs, guestSummary);
    updateSelectedSummary();

    if (refs.guestInfoEl) refs.guestInfoEl.hidden = false;
    if (refs.actionsEl) refs.actionsEl.hidden = false;
    if (refs.checkoutBtn) refs.checkoutBtn.disabled = false;

    return true;
}

function resetCartState(refs) {
    if (refs.emptyEl) refs.emptyEl.hidden = true;
    if (refs.errorEl) refs.errorEl.hidden = true;
    if (refs.unauthEl) refs.unauthEl.hidden = true;
    if (refs.guestInfoEl) refs.guestInfoEl.hidden = true;
    if (refs.actionsEl) refs.actionsEl.hidden = true;
    if (refs.listEl) refs.listEl.innerHTML = '';
    if (refs.countEl) refs.countEl.textContent = '0';
    if (refs.checkoutBtn) refs.checkoutBtn.disabled = true;

    updateSummary(null, refs);
    selectedItemIds.clear();
    updateSelectionUI();
}

function renderSummary(summary, refs) {
    const fallback = {
        totalQuantity: 0,
        totalAmount: 0,
        expectedDiscount: 0,
        expectedAmount: 0
    };
    updateSummary(Object.assign(fallback, summary || {}), refs);
}

function renderItems(items, refs, summaryOverride) {
    const products = Array.isArray(items) ? items : [];

    if (!refs.listEl) return;

    if (products.length === 0) {
        if (refs.emptyEl) refs.emptyEl.hidden = false;
        updateSelectionUI();
        return;
    }

    if (refs.emptyEl) refs.emptyEl.hidden = true;

    const summary = summaryOverride || calculateSummary(products);
    updateCartCount(summary.totalQuantity);

    const fragment = document.createDocumentFragment();

    products.forEach((item, index) => {
        const title = item && item.title != null ? item.title : '상품';
        const quantity = Number(item && item.quantity != null ? item.quantity : 0);
        const unitPrice = getEffectiveUnitPrice(item);
        const baseLineTotal = Number(item && item.lineTotal != null ? item.lineTotal : unitPrice * quantity);
        const imageUrl = item && item.imageUrl ? item.imageUrl : '';
        const isOut = item && item.outOfStock === true;
        const optionLabel = item && item.optionLabel ? item.optionLabel : '';
        const selectedOptionLabel = item && item.selectedOptionLabel ? item.selectedOptionLabel : '';
        const optionsList = item && Array.isArray(item.options) ? item.options : [];
        const optionsAvailable = optionsList.length > 0;
        const itemId = String(item && item.id != null ? item.id : index);
        const cartItemId = item && item.cartItemId != null ? item.cartItemId : null;
        const isChecked = selectedItemIds.has(itemId);
        const pendingQuantityPresent = Object.prototype.hasOwnProperty.call(item || {}, 'pendingQuantity');
        const pendingOptionPresent = Object.prototype.hasOwnProperty.call(item || {}, 'pendingOptionId');
        const pendingQuantity = pendingQuantityPresent ? Number(item.pendingQuantity) : null;
        const displayQuantity = pendingQuantityPresent && Number.isFinite(pendingQuantity) ? pendingQuantity : quantity;
        const pendingLineTotal = pendingQuantityPresent && item.pendingLineTotal != null ? Number(item.pendingLineTotal) : null;
        const displayLineTotal = pendingLineTotal != null ? pendingLineTotal : (pendingQuantityPresent ? unitPrice * displayQuantity : baseLineTotal);
        const pendingOptionLabel = pendingOptionPresent ? (item.pendingOptionLabel || '') : '';
        const displayOptionLabel = pendingOptionPresent ? pendingOptionLabel : (selectedOptionLabel || optionLabel);
        const isApplying = Boolean(item && item.isApplying);
        const highlightPending = isApplying;

        const li = document.createElement('li');
        li.className = 'item-card';
        if (highlightPending) {
            li.classList.add('item-card--pending');
        }
        li.setAttribute('data-item-id', itemId);
        li.setAttribute('data-index', index);
        if (cartItemId !== null) {
            li.setAttribute('data-cart-item-id', cartItemId);
        }

        const statusMessage = isApplying
            ? '<p class="item-pending-message">변경 사항을 적용 중입니다…</p>'
            : '';

        li.innerHTML = `
            <div class="item-select">
                <input type="checkbox"
                       class="item-checkbox"
                       data-item-id="${escapeHtml(itemId)}"
                       aria-label="${escapeHtml(title)} 선택"
                       ${isChecked ? 'checked' : ''}>
            </div>
            <div class="item-thumb">
                ${imageUrl ? `<img src="${imageUrl}" alt="${escapeHtml(title)}" loading="lazy" />` :
            '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--gray-400);font-size:0.75rem;">No Image</div>'}
            </div>
            <div class="item-body">
                <h3 class="item-title">${escapeHtml(title)}</h3>
                ${optionsAvailable
            ? `<div class="item-option-group">
                            <div class="item-option">${displayOptionLabel ? escapeHtml(displayOptionLabel) : '옵션 미선택'}</div>
                            <button type="button" class="option-edit-btn" ${isApplying ? 'disabled' : ''}>옵션 변경</button>
                       </div>`
            : (displayOptionLabel ? `<div class="item-option">${escapeHtml(displayOptionLabel)}</div>` : '')}
                <div class="item-meta">금액 ${formatCurrency(unitPrice)}원</div>
                <div class="item-price">합계 ${formatCurrency(displayLineTotal)}원</div>
                ${statusMessage}
                ${isOut ? '<div class="item-status">재고가 부족한 상품입니다.</div>' : ''}
                
                <div class="item-controls">
                    <div class="quantity-control">
                        <button type="button" class="quantity-btn quantity-btn--minus" aria-label="수량 줄이기" ${displayQuantity <= 1 || isApplying ? 'disabled' : ''}>-</button>
                        <input type="number" class="quantity-input" min="1" max="99" value="${displayQuantity}" aria-label="수량 입력" ${isApplying ? 'disabled' : ''}>
                        <button type="button" class="quantity-btn quantity-btn--plus" aria-label="수량 늘리기" ${displayQuantity >= 99 || isApplying ? 'disabled' : ''}>+</button>
                    </div>
                </div>
            </div>
        `;
        const checkbox = li.querySelector('.item-checkbox');
        if (checkbox) {
            checkbox.addEventListener('change', event => {
                toggleItemSelection(itemId, event.target.checked);
            });
        }
        const minusBtn = li.querySelector('.quantity-btn--minus');
        const plusBtn = li.querySelector('.quantity-btn--plus');
        const quantityInput = li.querySelector('.quantity-input');

        if (minusBtn) {
            minusBtn.addEventListener('click', () => {
                changeQuantity(index, -1);
            });
        }

        if (plusBtn) {
            plusBtn.addEventListener('click', () => {
                changeQuantity(index, 1);
            });
        }

        if (quantityInput) {
            quantityInput.addEventListener('change', event => {
                updateQuantity(index, event.target.value);
            });
        }
        const optionBtn = li.querySelector('.option-edit-btn');
        if (optionBtn) {
            optionBtn.addEventListener('click', () => openOptionModal(index, optionBtn));
        }
        fragment.appendChild(li);
    });

    refs.listEl.appendChild(fragment);

    updateSelectionUI();
}

function toggleItemSelection(itemId, isChecked) {
    const id = String(itemId);
    if (isChecked) {
        selectedItemIds.add(id);
    } else {
        selectedItemIds.delete(id);
    }
    updateSelectionUI();
    updateSelectedSummary();
}

function syncSelectionWithCart() {
    const validIds = new Set(cartItems.map(item => String(item.id)));
    for (const id of Array.from(selectedItemIds)) {
        if (!validIds.has(id)) {
            selectedItemIds.delete(id);
        }
    }
}

function updateSelectionUI() {
    const checkboxes = document.querySelectorAll('.item-checkbox');
    const totalCheckboxes = checkboxes.length;
    const selectedCount = selectedItemIds.size;

    const removeSelectedBtn = document.getElementById('remove-selected-btn');
    const selectAllBtn = document.getElementById('select-all-btn');
    const deselectAllBtn = document.getElementById('deselect-all-btn');

    if (removeSelectedBtn) {
        removeSelectedBtn.disabled = selectedCount === 0;
    }

    if (selectAllBtn) {
        selectAllBtn.disabled = totalCheckboxes === 0 || selectedCount === totalCheckboxes;
    }

    if (deselectAllBtn) {
        deselectAllBtn.disabled = selectedCount === 0;
    }
}

function changeQuantity(index, delta) {
    if (index < 0 || index >= cartItems.length) {
        return;
    }

    const item = cartItems[index];
    if (!item || item.isApplying) {
        return;
    }

    const baseQuantity = Object.prototype.hasOwnProperty.call(item, 'pendingQuantity')
        ? Number(item.pendingQuantity)
        : Number(item.quantity ?? 0);
    const current = Number.isFinite(baseQuantity) ? baseQuantity : Number(item.quantity ?? 0);
    const next = current + Number(delta);

    updateQuantity(index, next);
}

function updateQuantity(index, newQuantity) {
    if (index < 0 || index >= cartItems.length) {
        return;
    }

    const item = cartItems[index];
    if (!item || item.isApplying) {
        return;
    }

    const qty = Math.max(1, Math.min(99, Number(newQuantity) || 1));

    if (qty === item.quantity) {
        if (Object.prototype.hasOwnProperty.call(item, 'pendingQuantity')) {
            delete item.pendingQuantity;
            delete item.pendingLineTotal;
            refreshCart();
        }
        return;
    }

    item.pendingQuantity = qty;
    item.pendingLineTotal = getEffectiveUnitPrice(item) * qty;

    refreshCart();
    applyItemChanges(index);
}

async function applyItemChanges(index) {
    if (index < 0 || index >= cartItems.length) {
        return;
    }

    const item = cartItems[index];
    if (!item || item.isApplying) {
        return;
    }

    const pendingQuantityPresent = Object.prototype.hasOwnProperty.call(item, 'pendingQuantity');
    const pendingOptionPresent = Object.prototype.hasOwnProperty.call(item, 'pendingOptionId');
    const quantityChanged = pendingQuantityPresent && Number(item.pendingQuantity) !== Number(item.quantity);
    const optionChanged = pendingOptionPresent && (item.pendingOptionId ?? null) !== (item.selectedOptionId ?? null);

    if (!quantityChanged && !optionChanged) {
        return;
    }

    item.isApplying = true;
    refreshCart();

    try {
        if (!isGuestCart && item.cartItemId != null) {
            if (quantityChanged) {
                await updateCartItemQuantity(item.cartItemId, item.pendingQuantity);
            }
            if (optionChanged) {
                const nextOptionValue = item.pendingSelectedOptions != null
                    ? item.pendingSelectedOptions
                    : (item.pendingOptionId != null ? String(item.pendingOptionId) : null);
                await updateCartItemOptions(item.cartItemId, nextOptionValue);
            }
        }

        if (quantityChanged) {
            item.quantity = Number(item.pendingQuantity);
            item.lineTotal = item.pendingLineTotal != null ? Number(item.pendingLineTotal) : getEffectiveUnitPrice(item) * item.quantity;
            delete item.pendingQuantity;
            delete item.pendingLineTotal;
        }

        if (optionChanged) {
            const nextOptionId = item.pendingOptionId ?? null;
            const nextOptionLabel = item.pendingOptionLabel || '';
            item.selectedOptions = item.pendingSelectedOptions ?? null;
            item.selectedOptionId = nextOptionId;
            item.selectedOptionLabel = nextOptionLabel;
            if (!item.optionLabel && nextOptionLabel) {
                item.optionLabel = nextOptionLabel;
            }
            delete item.pendingSelectedOptions;
            delete item.pendingOptionId;
            delete item.pendingOptionLabel;
        }

    } catch (error) {
        console.error('Failed to apply item changes:', error);
        alert('변경 사항을 적용하지 못했습니다. 잠시 후 다시 시도해주세요.');
        if (quantityChanged && Object.prototype.hasOwnProperty.call(item, 'pendingQuantity')) {
            delete item.pendingQuantity;
            delete item.pendingLineTotal;
        }
        if (optionChanged && Object.prototype.hasOwnProperty.call(item, 'pendingOptionId')) {
            delete item.pendingSelectedOptions;
            delete item.pendingOptionId;
            delete item.pendingOptionLabel;
        }
        item.isApplying = false;
        refreshCart();
        return;
    }

    item.isApplying = false;
    item.lineTotal = getEffectiveLineTotal(item);
    refreshCart();

    if (isGuestCart) {
        saveGuestCartItems(cartItems);
    }
}

// 상품 삭제 함수
async function removeItem(index) {
    if (index < 0 || index >= cartItems.length) return;

    const target = cartItems[index];
    const itemName = target?.title ?? '상품';
    const itemKey = String(target?.id ?? index);
    const cartItemId = target?.cartItemId ?? null;

    if (!confirm(`"${itemName}"을(를) 장바구니에서 삭제하시겠습니까?`)) {
        return;
    }

    if (!isGuestCart && cartItemId != null) {
        try {
            const response = await fetch(`/api/cart/${cartItemId}`, {
                method: 'DELETE',
                credentials: 'include'
            });
            if (!response.ok && response.status !== 204) {
                throw new Error(`Unexpected status ${response.status}`);
            }
        } catch (error) {
            console.error('Failed to delete cart item:', error);
            alert('상품을 삭제하지 못했습니다. 잠시 후 다시 시도해주세요.');
            return;
        }
    } else if (!isGuestCart && cartItemId == null) {
        console.warn('cartItemId가 없어 서버와 동기화할 수 없습니다. 로컬 상태만 갱신합니다.');
    }

    const commitRemoval = () => {
        const removalIndex = cartItems.findIndex(item => String(item.id) === itemKey);
        if (removalIndex === -1) {
            return;
        }
        selectedItemIds.delete(itemKey);
        cartItems.splice(removalIndex, 1);
        refreshCart();
        if (isGuestCart) {
            saveGuestCartItems(cartItems);
        }
    };

    const escapedKey = (typeof CSS !== 'undefined' && typeof CSS.escape === 'function')
        ? CSS.escape(itemKey)
        : String(itemKey).replace(/"/g, '\\"');
    const itemCard = document.querySelector(`[data-item-id="${escapedKey}"]`);
    if (itemCard) {
        itemCard.classList.add('removing');
        setTimeout(commitRemoval, 300);
    } else {
        commitRemoval();
    }
}

// 전체 장바구니 새로고침
function refreshCart() {
    const refs = {
        emptyEl: document.getElementById('cart-empty'),
        listEl: document.getElementById('cart-item-list'),
        countEl: document.getElementById('cart-count'),
        actionsEl: document.getElementById('item-actions'),
        checkoutBtn: document.getElementById('checkout-button')
    };

    syncSelectionWithCart();

    const totalSummary = calculateSummary(cartItems);
    updateCartCount(totalSummary.totalQuantity);
    updateSelectedSummary();

    if (refs.listEl) refs.listEl.innerHTML = '';

    if (cartItems.length === 0) {
        if (refs.emptyEl) refs.emptyEl.hidden = false;
        if (refs.actionsEl) refs.actionsEl.hidden = true;
        if (refs.checkoutBtn) refs.checkoutBtn.disabled = true;
        selectedItemIds.clear();
        updateSelectionUI();
    } else {
        renderItems(cartItems, refs, totalSummary);
        if (refs.actionsEl) refs.actionsEl.hidden = false;
        if (refs.checkoutBtn) refs.checkoutBtn.disabled = false;
    }
}

// 요약 정보 계산
function calculateSummary(items) {
    const totals = items.reduce((acc, item) => {
        if (!item) {
            return acc;
        }
        const qty = getEffectiveQuantity(item);
        const lineTotal = getEffectiveLineTotal(item);
        acc.quantity += Number.isFinite(qty) ? qty : 0;
        acc.amount += Number.isFinite(lineTotal) ? lineTotal : 0;
        return acc;
    }, { quantity: 0, amount: 0 });

    const totalQuantity = totals.quantity;
    const totalAmount = totals.amount;
    const expectedDiscount = totalAmount >= 100000 ? Math.floor(totalAmount * 0.05) : 0;
    const expectedAmount = totalAmount - expectedDiscount;

    return {
        totalQuantity,
        totalAmount,
        shippingCost: 0,
        expectedDiscount,
        expectedAmount
    };
}

function resolveItemsForCheckout() {
    if (!Array.isArray(cartItems)) {
        return [];
    }
    // If no items are selected, return an empty array for checkout.
    if (selectedItemIds.size === 0) {
        return [];
    }
    return cartItems.filter(item => selectedItemIds.has(String(item?.id)));
}

function hasPendingChanges(item) {
    if (!item) {
        return false;
    }
    return Object.prototype.hasOwnProperty.call(item, 'pendingQuantity')
        || Object.prototype.hasOwnProperty.call(item, 'pendingOptionId');
}

function getEffectiveQuantity(item) {
    if (!item) {
        return 0;
    }
    const hasPending = Object.prototype.hasOwnProperty.call(item, 'pendingQuantity');
    const value = hasPending ? Number(item.pendingQuantity) : Number(item.quantity ?? 0);
    return Number.isFinite(value) ? value : 0;
}

function getEffectiveUnitPrice(item) {
    if (!item) {
        return 0;
    }
    const candidates = [
        Object.prototype.hasOwnProperty.call(item, 'pendingUnitPrice') ? item.pendingUnitPrice : undefined,
        item.finalUnitPrice,
        item.finalPrice,
        item.unitPrice,
        item.price
    ];
    for (const candidate of candidates) {
        const value = Number(candidate);
        if (Number.isFinite(value) && value >= 0) {
            return value;
        }
    }
    return 0;
}

function getEffectiveLineTotal(item) {
    if (!item) {
        return 0;
    }
    if (Object.prototype.hasOwnProperty.call(item, 'pendingLineTotal')) {
        const pending = Number(item.pendingLineTotal);
        if (Number.isFinite(pending)) {
            return pending;
        }
    }
    const stored = Number(item.lineTotal ?? 0);
    if (Number.isFinite(stored)) {
        return stored;
    }
    return getEffectiveUnitPrice(item) * getEffectiveQuantity(item);
}

function createHiddenInput(name, value) {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = name;
    input.value = value != null ? value : '';
    return input;
}

function appendCsrfToken(form) {
    if (!form) {
        return;
    }
    const csrfInput = document.querySelector('input[name="_csrf"]');
    if (csrfInput && csrfInput.value) {
        form.appendChild(createHiddenInput(csrfInput.name, csrfInput.value));
    }
}

function selectAllItems() {
    selectedItemIds.clear();
    cartItems.forEach(item => selectedItemIds.add(String(item.id)));
    document.querySelectorAll('.item-checkbox').forEach(checkbox => {
        checkbox.checked = true;
    });
    updateSelectionUI();
    updateSelectedSummary();
}

function deselectAllItems() {
    selectedItemIds.clear();
    document.querySelectorAll('.item-checkbox').forEach(checkbox => {
        checkbox.checked = false;
    });
    updateSelectionUI();
    updateSelectedSummary();
}

async function removeSelectedItems() {
    if (selectedItemIds.size === 0) {
        return;
    }

    if (!confirm('선택한 상품들을 모두 삭제하시겠습니까?')) {
        return;
    }

    if (!isGuestCart) {
        const idsForRemoval = cartItems
            .filter(item => selectedItemIds.has(String(item.id)))
            .map(item => item.cartItemId)
            .filter(id => id != null);

        if (idsForRemoval.length > 0) {
            const query = idsForRemoval.join(',');
            try {
                const response = await fetch(`/api/cart?ids=${encodeURIComponent(query)}`, {
                    method: 'DELETE',
                    credentials: 'include'
                });
                if (!response.ok && response.status !== 204) {
                    throw new Error(`Unexpected status ${response.status}`);
                }
            } catch (error) {
                console.error('Failed to delete selected cart items:', error);
                alert('선택한 상품을 삭제하지 못했습니다. 잠시 후 다시 시도해주세요.');
                return;
            }
        } else {
            // 서버에서 삭제할 항목이 없는 경우 (데모 데이터 등)
            console.log('No valid cartItemIds found for server deletion - proceeding with local deletion only');
        }
    }

    cartItems = cartItems.filter(item => !selectedItemIds.has(String(item.id)));
    selectedItemIds.clear();
    refreshCart();
    if (isGuestCart) {
        saveGuestCartItems(cartItems);
    }
}

// 주문하기
function proceedToCheckout() {
    if (!Array.isArray(cartItems) || cartItems.length === 0) {
        alert('장바구니가 비어있습니다.');
        return;
    }

    const itemsToOrder = resolveItemsForCheckout();
    if (itemsToOrder.length === 0) {
        alert('주문할 상품을 선택해주세요.');
        return;
    }

    const applyingItem = itemsToOrder.find(item => item?.isApplying);
    const pendingItem = itemsToOrder.find(hasPendingChanges);
    if (applyingItem || pendingItem) {
        alert('변경 사항을 적용 중입니다. 잠시 후 다시 시도해주세요.');
        return;
    }

    const outOfStockItems = itemsToOrder.filter(item => item?.outOfStock);
    if (outOfStockItems.length > 0) {
        alert('재고가 부족한 상품이 있습니다. 해당 상품을 제거하고 다시 시도해주세요.');
        return;
    }

    const summary = calculateSummary(itemsToOrder);
    const message = `총 ${formatNumber(summary.totalQuantity)}개 상품\n예상 결제 금액: ${formatCurrency(summary.expectedAmount)}원\n\n주문 페이지로 이동하시겠습니까?`;

    if (!confirm(message)) {
        return;
    }

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/order/preview';
    form.style.display = 'none';

    appendCsrfToken(form);

    let appended = 0;
    itemsToOrder.forEach(item => {
        if (!item || item.productId == null) {
            return;
        }
        const quantity = Math.max(1, Math.floor(getEffectiveQuantity(item)));
        const unitPrice = Math.max(0, Math.floor(getEffectiveUnitPrice(item)));

        form.appendChild(createHiddenInput(`orderItems[${appended}].productId`, item.productId));
        const optionValue = item.selectedOptionId != null ? item.selectedOptionId : 'default';
        form.appendChild(createHiddenInput(`orderItems[${appended}].productOptionId`, optionValue));
        form.appendChild(createHiddenInput(`orderItems[${appended}].quantity`, quantity));
        form.appendChild(createHiddenInput(`orderItems[${appended}].price`, unitPrice));
        appended += 1;
    });

    if (appended === 0) {
        alert('주문할 상품 정보를 찾을 수 없습니다.');
        return;
    }

    document.body.appendChild(form);
    form.submit();
}

// 유틸리티 함수들
async function updateCartItemQuantity(cartItemId, quantity) {
    const payload = { quantity: Number(quantity) };
    const response = await fetch(`/api/cart/${cartItemId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error('Failed to update cart quantity');
    }
}

async function updateCartItemOptions(cartItemId, selectedOptionValue) {
    const payload = { selectedOptions: selectedOptionValue != null ? String(selectedOptionValue) : null };
    const response = await fetch(`/api/cart/${cartItemId}/options`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error('Failed to update cart option');
    }
}

function normalizeApiItems(items) {
    return items.map(item => {
        const cartItemId = item?.cartItemId ?? item?.id ?? null;
        const resolvedId = cartItemId ?? item?.productId ?? Math.random().toString(36).slice(2);
        const quantity = Math.max(1, Number(item?.quantity ?? 1));
        const finalUnit = Number(item?.finalPrice ?? item?.finalUnitPrice ?? item?.unitPrice ?? item?.price ?? 0);
        const unit = Number(item?.unitPrice ?? item?.price ?? item?.finalPrice ?? finalUnit);
        const resolvedUnit = Number.isFinite(unit) ? unit : 0;
        const resolvedFinalUnit = Number.isFinite(finalUnit) ? finalUnit : resolvedUnit;
        const rawLineTotal = Number(item?.lineTotal);
        const lineTotal = Number.isFinite(rawLineTotal) ? rawLineTotal : resolvedFinalUnit * quantity;
        const selectedOptionId = item?.selectedOptionId != null ? Number(item.selectedOptionId) : null;
        const selectedOptionLabel = item?.selectedOptionLabel ?? '';
        const options = Array.isArray(item?.options) ? item.options : [];

        return {
            id: resolvedId,
            cartItemId,
            productId: item?.productId ?? null,
            title: item?.title ?? '상품',
            quantity,
            unitPrice: resolvedUnit,
            finalUnitPrice: resolvedFinalUnit,
            lineTotal,
            imageUrl: item?.imageUrl ?? item?.thumbnailUrl ?? '',
            outOfStock: Boolean(item?.outOfStock),
            optionLabel: item?.optionLabel ?? '',
            selectedOptions: item?.selectedOptions ?? null,
            selectedOptionId,
            selectedOptionLabel,
            options
        };
    });
}

function toGuestDisplayItem(item) {
    const quantity = Math.max(1, Number(item?.quantity ?? 1));
    const unit = Number(item?.unitPrice ?? item?.finalUnitPrice ?? item?.price ?? 0);
    const resolvedId = item?.id ?? Math.random().toString(36).slice(2);
    const selectedOptionId = item?.selectedOptionId != null ? Number(item.selectedOptionId) : (item?.selectedOptions ? Number(item.selectedOptions) : null);
    const options = Array.isArray(item?.options) ? item.options : [];
    const selectedOptionLabel = item?.selectedOptionLabel ?? '';
    return {
        id: resolvedId,
        cartItemId: resolvedId,
        title: item?.title ?? '상품',
        quantity,
        unitPrice: unit,
        finalUnitPrice: unit,
        lineTotal: unit * quantity,
        imageUrl: item?.imageUrl ?? item?.thumbnailUrl ?? '',
        outOfStock: false,
        optionLabel: item?.optionLabel ?? '',
        selectedOptions: item?.selectedOptions ?? null,
        selectedOptionId,
        selectedOptionLabel,
        options
    };
}

function calculateGuestSummary(items) {
    const totalQuantity = items.reduce((sum, item) => sum + Number(item.quantity ?? 0), 0);
    const totalAmount = items.reduce((sum, item) => sum + Number(item.lineTotal ?? 0), 0);
    return {
        totalQuantity,
        totalAmount,
        shippingCost: 0,
        expectedDiscount: 0,
        expectedAmount: totalAmount
    };
}

function updateSummary(summary, refs) {
    const safeSummary = summary && typeof summary === 'object' ? summary : {};
    const totalQuantity = Number(safeSummary.totalQuantity ?? 0);
    const totalAmount = Number(safeSummary.totalAmount ?? 0);
    const shippingAmount = Number(safeSummary.shippingCost ?? safeSummary.deliveryFee ?? 0);
    const expectedDiscount = Number(safeSummary.expectedDiscount ?? 0);
    const expectedAmount = Number(safeSummary.expectedAmount ?? (totalAmount - expectedDiscount + shippingAmount));

    const elements = {
        quantity: document.getElementById('summary-quantity'),
        total: document.getElementById('summary-total'),
        shipping: document.getElementById('summary-shipping'),
        discount: document.getElementById('summary-discount'),
        expected: document.getElementById('summary-expected')
    };

    if (elements.quantity) elements.quantity.textContent = formatNumber(totalQuantity);
    if (elements.total) elements.total.textContent = formatCurrency(totalAmount);
    if (elements.shipping) elements.shipping.textContent = formatCurrency(shippingAmount);
    if (elements.discount) elements.discount.textContent = formatCurrency(expectedDiscount);
    if (elements.expected) elements.expected.textContent = formatCurrency(expectedAmount);
}

function updateSelectedSummary() {
    const selectedItems = cartItems.filter(item => selectedItemIds.has(String(item.id)));
    const summary = calculateSummary(selectedItems);

    const elements = {
        quantity: document.getElementById('summary-quantity'),
        total: document.getElementById('summary-total'),
        shipping: document.getElementById('summary-shipping'),
        discount: document.getElementById('summary-discount'),
        expected: document.getElementById('summary-expected')
    };

    if (elements.quantity) elements.quantity.textContent = formatNumber(summary.totalQuantity);
    if (elements.total) elements.total.textContent = formatCurrency(summary.totalAmount);
    if (elements.shipping) elements.shipping.textContent = formatCurrency(summary.shippingCost);
    if (elements.discount) elements.discount.textContent = formatCurrency(summary.expectedDiscount);
    if (elements.expected) elements.expected.textContent = formatCurrency(summary.expectedAmount);
}

function loadGuestCartItems() {
    try {
        const raw = localStorage.getItem(GUEST_CART_KEY);
        if (!raw) return [];
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
        console.warn('Failed to parse guest cart data', error);
        return [];
    }
}

function saveGuestCartItems(items) {
    try {
        const payload = items.map(item => ({
            id: item.id,
            cartItemId: item.cartItemId ?? item.id,
            title: item.title,
            quantity: getEffectiveQuantity(item),
            unitPrice: getEffectiveUnitPrice(item),
            finalUnitPrice: getEffectiveUnitPrice(item),
            lineTotal: getEffectiveLineTotal(item),
            imageUrl: item.imageUrl,
            outOfStock: item.outOfStock,
            optionLabel: item.optionLabel,
            selectedOptions: item.selectedOptions ?? null,
            selectedOptionId: item.selectedOptionId ?? null,
            selectedOptionLabel: item.selectedOptionLabel ?? '',
            options: Array.isArray(item.options) ? item.options : []
        }));

        if (payload.length === 0) {
            localStorage.removeItem(GUEST_CART_KEY);
        } else {
            localStorage.setItem(GUEST_CART_KEY, JSON.stringify(payload));
        }
    } catch (error) {
        console.warn('Failed to save guest cart data', error);
    }
}

function formatNumber(value) {
    const num = Number(value ?? 0);
    return Number.isFinite(num) ? num.toLocaleString('ko-KR') : '0';
}

function formatCurrency(value) {
    const num = Number(value ?? 0);
    if (!Number.isFinite(num)) return '0';
    return num.toLocaleString('ko-KR');
}

function updateCartCount(totalQuantity) {
    const badge = document.getElementById('cart-count');
    if (!badge) return;
    badge.textContent = formatNumber(totalQuantity);
}

function escapeHtml(value) {
    const safe = value == null ? '' : value;
    return safe.toString()
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}