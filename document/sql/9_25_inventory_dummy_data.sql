-- ========================================
-- INVENTORY 테이블 더미데이터 (product_id = 1)
-- ========================================

-- Inventory 기본 재고 정보 (product_id = 1)
INSERT INTO inventory (
    product_id,
    current_quantity,
    safety_quantity,
    inventory_created_date,
    inventory_updated_date
) VALUES (
    1,                    -- product_id: 클렙튼 1인용 리클라이너 소파
    150,                  -- current_quantity: 현재 재고량
    20,                   -- safety_quantity: 안전 재고량 (최소 유지해야 할 재고)
    NOW(),                -- inventory_created_date
    NOW()                 -- inventory_updated_date
);

-- 추가 상품들의 재고 정보 (참고용)
INSERT INTO inventory (
    product_id,
    current_quantity,
    safety_quantity,
    inventory_created_date,
    inventory_updated_date
) VALUES
(2, 75, 15, NOW(), NOW()),    -- 보니애가구 소파
(3, 200, 25, NOW(), NOW()),   -- 에보니아 리클라이너
(4, 45, 10, NOW(), NOW());    -- 헤이미쉬홈 소파

-- 재고 상태 확인 쿼리 (실행 후 확인용)
-- SELECT
--     i.inventory_id,
--     p.product_title,
--     i.current_quantity,
--     i.safety_quantity,
--     CASE
--         WHEN i.current_quantity <= i.safety_quantity THEN '재주문 필요'
--         WHEN i.current_quantity <= i.safety_quantity * 2 THEN '주의'
--         ELSE '정상'
--     END AS stock_status,
--     i.inventory_created_date,
--     i.inventory_updated_date
-- FROM INVENTORY i
-- JOIN product p ON i.product_id = p.product_id
-- WHERE i.product_id = 1;