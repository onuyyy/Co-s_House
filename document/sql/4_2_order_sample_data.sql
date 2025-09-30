-- ========================================
-- 주문 테스트용 샘플 데이터
-- 주문 로직 검증을 위한 기본 주문 데이터
-- ========================================

-- ========================================
-- 전제조건 확인
-- ========================================
-- 1. 2_basic_Data_insert.sql (사용자, 공통코드 등)
-- 2. 3_brand,product_category_old.sql (브랜드, 카테고리)
-- 3. 3_4_product_option_insert.sql (상품, 옵션)
-- 4. 4_1_order_logic_setup.sql (주문 관련 공통코드)
-- 위 파일들이 모두 실행된 후에 이 파일을 실행하세요.

-- ========================================
-- 1. 샘플 주문 데이터 생성
-- ========================================

-- 주문 1: user1 (김집찾)의 소파 주문 - 결제 완료 상태
INSERT INTO `order` (
    user_id, order_status, total_amount, paid_amount, order_date, confirmed_date
) VALUES (
    6, -- user1 (김집찾)
    'ORDER_002', -- PAID (결제 완료)
    399000.00, -- 총 주문 금액
    399000.00, -- 결제 완료 금액
    DATE_SUB(NOW(), INTERVAL 3 DAY), -- 3일 전 주문
    NULL
);

-- 주문 2: user2 (박인테리어)의 책상 주문 - 배송중 상태
INSERT INTO `order` (
    user_id, order_status, total_amount, paid_amount, order_date, confirmed_date
) VALUES (
    7, -- user2 (박인테리어)
    'ORDER_004', -- SHIPPING (배송중)
    159000.00, -- 총 주문 금액
    159000.00, -- 결제 완료 금액
    DATE_SUB(NOW(), INTERVAL 2 DAY), -- 2일 전 주문
    NULL
);

-- 주문 3: user3 (이가구)의 의자 주문 - 배송 완료 상태
INSERT INTO `order` (
    user_id, order_status, total_amount, paid_amount, order_date, confirmed_date
) VALUES (
    8, -- user3 (이가구)
    'ORDER_005', -- DELIVERED (배송 완료)
    89000.00, -- 총 주문 금액
    89000.00, -- 결제 완료 금액
    DATE_SUB(NOW(), INTERVAL 5 DAY), -- 5일 전 주문
    NULL
);

-- 주문 4: user4 (최따뜻)의 다중 상품 주문 - 구매 확정 상태
INSERT INTO `order` (
    user_id, order_status, total_amount, paid_amount, order_date, confirmed_date
) VALUES (
    9, -- user4 (최따뜻)
    'ORDER_006', -- CONFIRMED (구매 확정)
    245000.00, -- 총 주문 금액
    245000.00, -- 결제 완료 금액
    DATE_SUB(NOW(), INTERVAL 10 DAY), -- 10일 전 주문
    DATE_SUB(NOW(), INTERVAL 3 DAY) -- 3일 전 구매 확정
);

-- 주문 5: user5 (정모던)의 주문 - 결제 대기 상태
INSERT INTO `order` (
    user_id, order_status, total_amount, paid_amount, order_date, confirmed_date
) VALUES (
    10, -- user5 (정모던)
    'ORDER_001', -- PENDING (결제 대기)
    129000.00, -- 총 주문 금액
    0.00, -- 결제 미완료
    NOW(), -- 방금 주문
    NULL
);

-- ========================================
-- 2. 주문 상품 데이터 생성
-- 실제 product_id는 상품 데이터에 따라 조정 필요
-- ========================================

-- 주문 1의 상품들 (product_id는 실제 데이터에 맞게 조정)
INSERT INTO order_item (
    order_id, product_id, product_option_id, quantity, unit_price, total_price
) VALUES
-- 소파 1개 (옵션: 브라운 색상)
(1, 1, 2, 1, 399000.00, 399000.00);

-- 주문 2의 상품들
INSERT INTO order_item (
    order_id, product_id, product_option_id, quantity, unit_price, total_price
) VALUES
-- 책상 1개 (옵션: 월넛)
(2, 6, 12, 1, 159000.00, 159000.00);

-- 주문 3의 상품들
INSERT INTO order_item (
    order_id, product_id, product_option_id, quantity, unit_price, total_price
) VALUES
-- 의자 1개 (옵션: 화이트)
(3, 8, 16, 1, 89000.00, 89000.00);

-- 주문 4의 상품들 (다중 상품)
INSERT INTO order_item (
    order_id, product_id, product_option_id, quantity, unit_price, total_price
) VALUES
-- 소파 1개
(4, 5, 9, 1, 149000.00, 149000.00),
-- 조명 2개
(4, 7, 13, 2, 48000.00, 96000.00);

-- 주문 5의 상품들
INSERT INTO order_item (
    order_id, product_id, product_option_id, quantity, unit_price, total_price
) VALUES
-- 러그 1개
(5, 10, 20, 1, 129000.00, 129000.00);

-- ========================================
-- 3. 주문 상태 변경 히스토리 데이터
-- ========================================

-- 주문 1의 상태 변경 히스토리
INSERT INTO order_status_history (
    order_id, order_status, changed_at, changed_by, reason
) VALUES
(1, 'ORDER_001', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, '주문 생성'),
(1, 'ORDER_002', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, '결제 완료');

-- 주문 2의 상태 변경 히스토리
INSERT INTO order_status_history (
    order_id, order_status, changed_at, changed_by, reason
) VALUES
(2, 'ORDER_001', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, '주문 생성'),
(2, 'ORDER_002', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, '결제 완료'),
(2, 'ORDER_003', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, '배송 준비'),
(2, 'ORDER_004', NOW(), NULL, '배송 시작');

-- 주문 3의 상태 변경 히스토리
INSERT INTO order_status_history (
    order_id, order_status, changed_at, changed_by, reason
) VALUES
(3, 'ORDER_001', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, '주문 생성'),
(3, 'ORDER_002', DATE_SUB(NOW(), INTERVAL 5 DAY), NULL, '결제 완료'),
(3, 'ORDER_003', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, '배송 준비'),
(3, 'ORDER_004', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, '배송 시작'),
(3, 'ORDER_005', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, '배송 완료');

-- 주문 4의 상태 변경 히스토리 (완전한 주문 생명주기)
INSERT INTO order_status_history (
    order_id, order_status, changed_at, changed_by, reason
) VALUES
(4, 'ORDER_001', DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, '주문 생성'),
(4, 'ORDER_002', DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, '결제 완료'),
(4, 'ORDER_003', DATE_SUB(NOW(), INTERVAL 8 DAY), NULL, '배송 준비'),
(4, 'ORDER_004', DATE_SUB(NOW(), INTERVAL 6 DAY), NULL, '배송 시작'),
(4, 'ORDER_005', DATE_SUB(NOW(), INTERVAL 4 DAY), NULL, '배송 완료'),
(4, 'ORDER_006', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, '구매 확정');

-- 주문 5의 상태 변경 히스토리
INSERT INTO order_status_history (
    order_id, order_status, changed_at, changed_by, reason
) VALUES
(5, 'ORDER_001', NOW(), NULL, '주문 생성');

-- ========================================
-- 4. 포인트 사용/적립 히스토리 (주문 관련)
-- ========================================

-- 주문 1: 포인트 적립 (구매 금액의 1%)
INSERT INTO point_history (
    user_id, type, amount, balance_before, balance_after, description, reference_id, reference_type, created_at
) VALUES
(6, 'EARN', 3990, 5000, 8990, '주문 적립 (주문번호: 1)', '1', 'ORDER', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 주문 2: 포인트 사용 + 적립
INSERT INTO point_history (
    user_id, type, amount, balance_before, balance_after, description, reference_id, reference_type, created_at
) VALUES
(7, 'USE', -1000, 3000, 2000, '주문 시 포인트 사용 (주문번호: 2)', '2', 'ORDER', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 'EARN', 1590, 2000, 3590, '주문 적립 (주문번호: 2)', '2', 'ORDER', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 주문 3: 포인트 적립
INSERT INTO point_history (
    user_id, type, amount, balance_before, balance_after, description, reference_id, reference_type, created_at
) VALUES
(8, 'EARN', 890, 10000, 10890, '주문 적립 (주문번호: 3)', '3', 'ORDER', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 주문 4: 포인트 적립
INSERT INTO point_history (
    user_id, type, amount, balance_before, balance_after, description, reference_id, reference_type, created_at
) VALUES
(9, 'EARN', 2450, 7500, 9950, '주문 적립 (주문번호: 4)', '4', 'ORDER', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- user_point 테이블 업데이트 (포인트 잔액 반영)
UPDATE user_point SET
    available_point = 8990,
    total_point = 8990,
    updated_at = NOW()
WHERE user_id = 6;

UPDATE user_point SET
    available_point = 3590,
    total_point = 3590,
    updated_at = NOW()
WHERE user_id = 7;

UPDATE user_point SET
    available_point = 10890,
    total_point = 10890,
    updated_at = NOW()
WHERE user_id = 8;

UPDATE user_point SET
    available_point = 9950,
    total_point = 9950,
    updated_at = NOW()
WHERE user_id = 9;

-- ========================================
-- 5. 쿠폰 사용 히스토리 (주문 관련)
-- ========================================

-- 주문 2에서 무료배송 쿠폰 사용
UPDATE user_coupon SET
    coupon_status = 'COUPON_002', -- USED
    used_at = DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE user_id = 7 AND coupon_id = 2; -- user2의 무료배송 쿠폰

-- ========================================
-- 데이터 검증 쿼리 (참고용 - 실행하지 않음)
-- ========================================
/*
-- 주문 현황 요약
SELECT
    o.order_id,
    u.user_nickname,
    cc.description as order_status,
    o.total_amount,
    o.paid_amount,
    o.order_date,
    COUNT(oi.order_item_id) as item_count
FROM `order` o
JOIN user u ON o.user_id = u.user_id
JOIN common_code cc ON o.order_status = cc.code_id
LEFT JOIN order_item oi ON o.order_id = oi.order_id
GROUP BY o.order_id
ORDER BY o.order_date DESC;

-- 사용자별 주문 통계
SELECT
    u.user_nickname,
    COUNT(o.order_id) as total_orders,
    SUM(o.total_amount) as total_amount,
    AVG(o.total_amount) as avg_order_amount
FROM user u
LEFT JOIN `order` o ON u.user_id = o.user_id
WHERE u.user_role = 1 -- 일반 사용자만
GROUP BY u.user_id, u.user_nickname
ORDER BY total_amount DESC;

-- 상품별 판매 현황
SELECT
    p.product_title,
    COUNT(oi.order_item_id) as order_count,
    SUM(oi.quantity) as total_quantity,
    SUM(oi.total_price) as total_sales
FROM order_item oi
JOIN product p ON oi.product_id = p.product_id
GROUP BY p.product_id, p.product_title
ORDER BY total_sales DESC;
*/

-- ========================================
-- 주의사항
-- ========================================
-- 1. product_id, product_option_id는 실제 데이터에 맞게 조정하세요
-- 2. order_status_history 테이블 스키마에 맞게 컬럼명을 조정하세요
-- 3. 날짜/시간은 테스트 환경에 맞게 조정 가능합니다
-- 4. 실제 운영에서는 이런 샘플 데이터를 사용하지 마세요