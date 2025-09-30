-- ========================================
-- 주문 로직에 필요한 추가 공통 코드 및 기본 데이터
-- OrderController에서 확인된 필수 데이터
-- ========================================

-- ========================================
-- 1. 주문 처리에 필요한 공통 코드 그룹 추가
-- ========================================
INSERT IGNORE INTO common_code_group (group_id, group_name, description) VALUES
('DELIVERY_FEE', '배송비 정책', '배송비 정책 관련 코드'),
('ORDER_CANCEL_REASON', '주문 취소 사유', '주문 취소 사유 코드'),
('REFUND_REASON', '환불 사유', '환불 사유 코드'),
('PAYMENT_GATEWAY', '결제 게이트웨이', '결제 처리 업체 코드');

-- ========================================
-- 2. 주문 처리에 필요한 추가 공통 코드
-- ========================================
INSERT IGNORE INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES

-- 배송비 정책 코드
('DELIVERY_FEE_001', 'DELIVERY_FEE', 'FREE', '무료 배송', 1, TRUE),
('DELIVERY_FEE_002', 'DELIVERY_FEE', 'STANDARD', '일반 배송 (3,000원)', 2, TRUE),
('DELIVERY_FEE_003', 'DELIVERY_FEE', 'JEJU_ISLAND', '제주/도서산간 추가 (5,000원)', 3, TRUE),
('DELIVERY_FEE_004', 'DELIVERY_FEE', 'EXPRESS', '당일/익일 배송 (5,000원)', 4, TRUE),

-- 주문 취소 사유 코드
('CANCEL_001', 'ORDER_CANCEL_REASON', 'CUSTOMER_REQUEST', '고객 요청', 1, TRUE),
('CANCEL_002', 'ORDER_CANCEL_REASON', 'PAYMENT_FAILED', '결제 실패', 2, TRUE),
('CANCEL_003', 'ORDER_CANCEL_REASON', 'OUT_OF_STOCK', '재고 부족', 3, TRUE),
('CANCEL_004', 'ORDER_CANCEL_REASON', 'PRODUCT_DEFECT', '상품 불량', 4, TRUE),
('CANCEL_005', 'ORDER_CANCEL_REASON', 'ADMIN_CANCEL', '관리자 취소', 5, TRUE),

-- 환불 사유 코드
('REFUND_REASON_001', 'REFUND_REASON', 'DEFECTIVE_PRODUCT', '불량 상품', 1, TRUE),
('REFUND_REASON_002', 'REFUND_REASON', 'WRONG_PRODUCT', '오배송', 2, TRUE),
('REFUND_REASON_003', 'REFUND_REASON', 'CUSTOMER_CHANGE_MIND', '단순 변심', 3, TRUE),
('REFUND_REASON_004', 'REFUND_REASON', 'SIZE_NOT_FIT', '사이즈 불일치', 4, TRUE),
('REFUND_REASON_005', 'REFUND_REASON', 'LATE_DELIVERY', '배송 지연', 5, TRUE),

-- 결제 게이트웨이 코드
('GATEWAY_001', 'PAYMENT_GATEWAY', 'TOSS', '토스페이먼츠', 1, TRUE),
('GATEWAY_002', 'PAYMENT_GATEWAY', 'IAMPORT', '아임포트', 2, TRUE),
('GATEWAY_003', 'PAYMENT_GATEWAY', 'KAKAO', '카카오페이', 3, TRUE),
('GATEWAY_004', 'PAYMENT_GATEWAY', 'NAVER', '네이버페이', 4, TRUE),
('GATEWAY_005', 'PAYMENT_GATEWAY', 'PAYCO', 'PAYCO', 5, TRUE);

-- ========================================
-- 3. 기본 배송비 정책 설정
-- ========================================

-- 배송비 정책 테이블이 있다면 기본 정책 삽입 (스키마에 맞게 조정 필요)
/*
INSERT IGNORE INTO delivery_policy (policy_name, min_free_amount, standard_fee, express_fee, island_additional_fee, is_active) VALUES
('기본 배송 정책', 50000.00, 3000.00, 5000.00, 2000.00, TRUE);
*/

-- ========================================
-- 4. 주문 상태 변경 히스토리를 위한 데이터 검증
-- ========================================

-- 주문 상태 코드가 모두 존재하는지 확인하는 검증 쿼리 (주석 처리)
/*
SELECT
    cc.code_id,
    cc.code_name,
    cc.description,
    CASE WHEN cc.code_id IS NOT NULL THEN 'EXISTS' ELSE 'MISSING' END as status
FROM (
    SELECT 'ORDER_001' as required_code UNION ALL
    SELECT 'ORDER_002' UNION ALL
    SELECT 'ORDER_003' UNION ALL
    SELECT 'ORDER_004' UNION ALL
    SELECT 'ORDER_005' UNION ALL
    SELECT 'ORDER_006' UNION ALL
    SELECT 'ORDER_007' UNION ALL
    SELECT 'ORDER_008'
) required
LEFT JOIN common_code cc ON required.required_code = cc.code_id
WHERE cc.group_id = 'ORDER_STATUS' OR cc.group_id IS NULL;
*/

-- ========================================
-- 5. 주문 로직 지원을 위한 기본 설정 데이터
-- ========================================

-- 기본 포인트 적립률 (주문 금액의 1%)
INSERT IGNORE INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
('POINT_RATE_001', 'POINT_TYPE', 'ORDER_EARN_RATE', '주문 시 포인트 적립률 (1%)', 10, TRUE);

-- 포인트 사용 제한 (최대 주문금액의 30%)
INSERT IGNORE INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
('POINT_LIMIT_001', 'POINT_TYPE', 'MAX_USE_RATE', '포인트 최대 사용률 (30%)', 11, TRUE);

-- 쿠폰 중복 사용 정책
INSERT IGNORE INTO common_code_group (group_id, group_name, description) VALUES
('COUPON_POLICY', '쿠폰 정책', '쿠폰 사용 정책 관련 코드');

INSERT IGNORE INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
('COUPON_POLICY_001', 'COUPON_POLICY', 'SINGLE_USE', '단일 쿠폰만 사용 가능', 1, TRUE),
('COUPON_POLICY_002', 'COUPON_POLICY', 'MULTIPLE_USE', '다중 쿠폰 사용 가능', 2, TRUE);

-- ========================================
-- 6. 주문 관련 시스템 설정
-- ========================================

-- 주문 자동 확정 기간 (배송완료 후 7일)
INSERT IGNORE INTO common_code_group (group_id, group_name, description) VALUES
('SYSTEM_CONFIG', '시스템 설정', '시스템 운영 관련 설정값');

INSERT IGNORE INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
('CONFIG_001', 'SYSTEM_CONFIG', 'AUTO_CONFIRM_DAYS', '주문 자동 확정 기간 (7일)', 1, TRUE),
('CONFIG_002', 'SYSTEM_CONFIG', 'CANCEL_LIMIT_HOURS', '주문 취소 가능 시간 (24시간)', 2, TRUE),
('CONFIG_003', 'SYSTEM_CONFIG', 'REFUND_LIMIT_DAYS', '환불 신청 가능 기간 (7일)', 3, TRUE);

-- ========================================
-- 데이터 검증 쿼리 (참고용 - 실행하지 않음)
-- ========================================
/*
-- 주문 로직에 필요한 모든 공통 코드가 존재하는지 확인
SELECT
    group_id,
    COUNT(*) as code_count,
    GROUP_CONCAT(code_id) as codes
FROM common_code
WHERE group_id IN (
    'ORDER_STATUS', 'PAYMENT_STATUS', 'PAYMENT_METHOD',
    'DELIVERY_STATUS', 'COUPON_STATUS', 'POINT_TYPE',
    'DELIVERY_FEE', 'ORDER_CANCEL_REASON', 'REFUND_REASON',
    'PAYMENT_GATEWAY', 'COUPON_POLICY', 'SYSTEM_CONFIG'
)
AND is_active = TRUE
GROUP BY group_id
ORDER BY group_id;

-- 주문 상태별 코드 확인
SELECT
    cc.code_id,
    cc.code_name,
    cc.description,
    cc.sort_order
FROM common_code cc
WHERE cc.group_id = 'ORDER_STATUS'
AND cc.is_active = TRUE
ORDER BY cc.sort_order;
*/

-- ========================================
-- 주의사항
-- ========================================
-- 1. 이 파일은 2_basic_Data_insert.sql 실행 후에 실행해야 합니다.
-- 2. 상품, 브랜드, 카테고리 데이터는 별도 파일에서 관리됩니다.
-- 3. 실제 운영 시에는 각 정책값들을 환경에 맞게 조정하세요.
-- 4. 결제 게이트웨이는 실제 사용하는 업체에 맞게 수정하세요.