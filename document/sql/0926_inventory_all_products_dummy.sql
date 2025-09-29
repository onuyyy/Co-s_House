-- ========================================
-- 모든 상품(1-1000)에 대한 INVENTORY 더미데이터
-- ========================================

-- 재고량을 다양하게 설정하여 현실적인 데이터 생성
-- 일부는 재주문 필요, 일부는 주의, 일부는 정상 상태

-- 1-100: 다양한 재고 상태
INSERT INTO inventory (product_id, current_quantity, safety_quantity, inventory_created_date, inventory_updated_date)
SELECT
    product_id,
    CASE
        WHEN product_id % 10 = 1 THEN 5      -- 재주문 필요
        WHEN product_id % 10 = 2 THEN 15     -- 재주문 필요
        WHEN product_id % 10 = 3 THEN 25     -- 주의 상태
        WHEN product_id % 10 = 4 THEN 35     -- 주의 상태
        WHEN product_id % 10 = 5 THEN 45     -- 주의 상태
        WHEN product_id % 10 = 6 THEN 80     -- 정상
        WHEN product_id % 10 = 7 THEN 120    -- 정상
        WHEN product_id % 10 = 8 THEN 200    -- 정상
        WHEN product_id % 10 = 9 THEN 300    -- 정상
        ELSE 150                              -- 정상
    END as current_quantity,
    CASE
        WHEN product_id % 5 = 1 THEN 10      -- 안전재고 10개
        WHEN product_id % 5 = 2 THEN 15      -- 안전재고 15개
        WHEN product_id % 5 = 3 THEN 20      -- 안전재고 20개
        WHEN product_id % 5 = 4 THEN 25      -- 안전재고 25개
        ELSE 30                               -- 안전재고 30개
    END as safety_quantity,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) as inventory_created_date,
    NOW() as inventory_updated_date
FROM (
    SELECT product_id FROM product WHERE product_id BETWEEN 1 AND 1000
) as products;

-- 일부 상품은 품절 상태로 설정 (약 5%)
UPDATE inventory
SET current_quantity = 0,
    inventory_updated_date = NOW()
WHERE product_id % 20 = 1;

-- 조회용 쿼리 (실행 후 확인)
/*
SELECT
    COUNT(*) as total_inventory,
    SUM(CASE WHEN current_quantity = 0 THEN 1 ELSE 0 END) as out_of_stock,
    SUM(CASE WHEN current_quantity > 0 AND current_quantity <= safety_quantity THEN 1 ELSE 0 END) as need_reorder,
    SUM(CASE WHEN current_quantity > safety_quantity AND current_quantity <= safety_quantity * 2 THEN 1 ELSE 0 END) as warning,
    SUM(CASE WHEN current_quantity > safety_quantity * 2 THEN 1 ELSE 0 END) as normal
FROM INVENTORY;

-- 샘플 데이터 확인
SELECT
    i.inventory_id,
    i.product_id,
    p.product_title,
    i.current_quantity,
    i.safety_quantity,
    CASE
        WHEN i.current_quantity = 0 THEN '품절'
        WHEN i.current_quantity <= i.safety_quantity THEN '재주문 필요'
        WHEN i.current_quantity <= i.safety_quantity * 2 THEN '주의'
        ELSE '정상'
    END as stock_status
FROM INVENTORY i
JOIN product p ON i.product_id = p.product_id
ORDER BY i.product_id
LIMIT 20;
*/