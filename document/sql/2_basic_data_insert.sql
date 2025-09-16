
-- ========================================
-- 기본 데이터 삽입
-- ========================================

-- 공통 코드 그룹 삽입
INSERT INTO COMMON_CODE_GROUP (group_id, group_name, description) VALUES
('PRODUCT_CATEGORY', '상품 카테고리', '상품 카테고리'),
('ORDER_STATUS', '주문 상태', '주문의 진행 상태를 나타내는 코드'),
('PAYMENT_STATUS', '결제 상태', '결제 진행 상태를 나타내는 코드'),
('PAYMENT_METHOD', '결제 수단', '사용 가능한 결제 수단 코드'),
('DELIVERY_STATUS', '배송 상태', '배송 진행 상태를 나타내는 코드'),
('DELIVERY_TYPE', '배송 타입', '배송 방식을 구분하는 코드'),
('COUPON_STATUS', '쿠폰 상태', '쿠폰 사용 상태를 나타내는 코드'),
('QUESTION_TYPE', '문의 타입', '고객 문의 유형 코드'),
('QUESTION_STATUS', '문의 상태', '문의 답변 상태 코드'),
('PRODUCT_STATUS', '상품 상태', '상품 판매 상태 코드'),
('ACTIVITY_TYPE', '사용자 활동', '사용자 활동 유형 코드'),
('ADMIN_ACTION', '관리자 작업', '관리자 작업 유형 코드'),
('REFUND_TYPE', '환불 타입', '환불 유형 코드'),
('REFUND_STATUS', '환불 상태', '환불 처리 상태 코드'),
('CONTACT_TYPE', '상담 타입', '고객 상담 방식 코드'),
('CUSTOMER_STATUS', '고객상담 상태', '고객상담 처리 상태 코드');

-- 기본 공통 코드 데이터 삽입
INSERT INTO COMMON_CODE (code_id, group_id, code_name, description, sort_order, is_active) VALUES
-- Product_category 코드
('PD_001', 'PRODUCT_CATEGORY', 'PRODUCT', '상품', 1, TRUE),
('PD_002', 'PRODUCT_CATEGORY', 'PRODUCT', '상품', 1, TRUE),
('PD_003', 'PRODUCT_CATEGORY', 'PRODUCT', '상품', 1, TRUE),
('PD_004', 'PRODUCT_CATEGORY', 'PRODUCT', '상품', 1, TRUE),
('PD_005', 'PRODUCT_CATEGORY', 'PRODUCT', '상품', 1, TRUE),
('PD_006', 'PRODUCT_CATEGORY', 'PRODUCT', '상품', 1, TRUE),

-- 주문 상태 코드
('ORDER_001', 'ORDER_STATUS', 'PENDING', '결제 대기', 1, TRUE),
('ORDER_002', 'ORDER_STATUS', 'PAID', '결제 완료', 2, TRUE),
('ORDER_003', 'ORDER_STATUS', 'PREPARING', '배송 준비중', 3, TRUE),
('ORDER_004', 'ORDER_STATUS', 'SHIPPING', '배송중', 4, TRUE),
('ORDER_005', 'ORDER_STATUS', 'DELIVERED', '배송 완료', 5, TRUE),
('ORDER_006', 'ORDER_STATUS', 'CONFIRMED', '구매 확정', 6, TRUE),
('ORDER_007', 'ORDER_STATUS', 'CANCELLED', '주문 취소', 7, TRUE),
('ORDER_008', 'ORDER_STATUS', 'REFUNDED', '환불 완료', 8, TRUE),

-- 결제 상태 코드
('PAY_001', 'PAYMENT_STATUS', 'PENDING', '결제 대기', 1, TRUE),
('PAY_002', 'PAYMENT_STATUS', 'COMPLETED', '결제 완료', 2, TRUE),
('PAY_003', 'PAYMENT_STATUS', 'FAILED', '결제 실패', 3, TRUE),
('PAY_004', 'PAYMENT_STATUS', 'CANCELLED', '결제 취소', 4, TRUE),
('PAY_005', 'PAYMENT_STATUS', 'REFUNDED', '환불 완료', 5, TRUE),

-- 결제 수단 코드
('PAY_METHOD_001', 'PAYMENT_METHOD', 'CARD', '신용/체크카드', 1, TRUE),
('PAY_METHOD_002', 'PAYMENT_METHOD', 'BANK_TRANSFER', '무통장입금', 2, TRUE),
('PAY_METHOD_003', 'PAYMENT_METHOD', 'VIRTUAL_ACCOUNT', '가상계좌', 3, TRUE),
('PAY_METHOD_004', 'PAYMENT_METHOD', 'KAKAOPAY', '카카오페이', 4, TRUE),
('PAY_METHOD_005', 'PAYMENT_METHOD', 'NAVERPAY', '네이버페이', 5, TRUE),
('PAY_METHOD_006', 'PAYMENT_METHOD', 'PAYCO', 'PAYCO', 6, TRUE),
('PAY_METHOD_007', 'PAYMENT_METHOD', 'TOSS', '토스', 7, TRUE),

-- 배송 상태 코드
('DELIVERY_001', 'DELIVERY_STATUS', 'PREPARING', '배송 준비', 1, TRUE),
('DELIVERY_002', 'DELIVERY_STATUS', 'SHIPPED', '배송 출발', 2, TRUE),
('DELIVERY_003', 'DELIVERY_STATUS', 'IN_TRANSIT', '배송중', 3, TRUE),
('DELIVERY_004', 'DELIVERY_STATUS', 'DELIVERED', '배송 완료', 4, TRUE),
('DELIVERY_005', 'DELIVERY_STATUS', 'FAILED', '배송 실패', 5, TRUE),

-- 배송 타입 코드
('DELIVERY_TYPE_001', 'DELIVERY_TYPE', 'NORMAL', '일반 배송', 1, TRUE),
('DELIVERY_TYPE_002', 'DELIVERY_TYPE', 'JEJU_ISLAND', '제주/도서산간', 2, TRUE),
('DELIVERY_TYPE_003', 'DELIVERY_TYPE', 'MOUNTAIN', '산간지역', 3, TRUE),
('DELIVERY_TYPE_004', 'DELIVERY_TYPE', 'EXPRESS', '당일/익일 배송', 4, TRUE),

-- 쿠폰 상태 코드
('COUPON_001', 'COUPON_STATUS', 'ISSUED', '발급됨', 1, TRUE),
('COUPON_002', 'COUPON_STATUS', 'USED', '사용됨', 2, TRUE),
('COUPON_003', 'COUPON_STATUS', 'EXPIRED', '만료됨', 3, TRUE),

-- 문의 타입 코드
('INQUIRY_001', 'QUESTION_TYPE', 'PRODUCT', '상품 문의', 1, TRUE),
('INQUIRY_002', 'QUESTION_TYPE', 'DELIVERY', '배송 문의', 2, TRUE),
('INQUIRY_003', 'QUESTION_TYPE', 'PAYMENT', '결제 문의', 3, TRUE),
('INQUIRY_004', 'QUESTION_TYPE', 'REFUND', '교환/환불', 4, TRUE),
('INQUIRY_005', 'QUESTION_TYPE', 'ETC', '기타 문의', 5, TRUE),

-- 문의 상태 코드
('INQUIRY_STATUS_001', 'QUESTION_STATUS', 'PENDING', '답변 대기', 1, TRUE),
('INQUIRY_STATUS_002', 'QUESTION_STATUS', 'ANSWERED', '답변 완료', 2, TRUE),
('INQUIRY_STATUS_003', 'QUESTION_STATUS', 'CLOSED', '문의 종료', 3, TRUE),

-- 상품 상태 코드
('PRODUCT_001', 'PRODUCT_STATUS', 'ACTIVE', '판매중', 1, TRUE),
('PRODUCT_002', 'PRODUCT_STATUS', 'INACTIVE', '판매 중지', 2, TRUE),
('PRODUCT_003', 'PRODUCT_STATUS', 'SOLD_OUT', '품절', 3, TRUE),
('PRODUCT_004', 'PRODUCT_STATUS', 'DISCONTINUED', '단종', 4, TRUE),

-- 사용자 활동 타입 코드
('ACTIVITY_001', 'ACTIVITY_TYPE', 'LOGIN', '로그인', 1, TRUE),
('ACTIVITY_002', 'ACTIVITY_TYPE', 'LOGOUT', '로그아웃', 2, TRUE),
('ACTIVITY_003', 'ACTIVITY_TYPE', 'VIEW_PRODUCT', '상품 조회', 3, TRUE),
('ACTIVITY_004', 'ACTIVITY_TYPE', 'SEARCH', '검색', 4, TRUE),
('ACTIVITY_005', 'ACTIVITY_TYPE', 'ADD_CART', '장바구니 담기', 5, TRUE),
('ACTIVITY_006', 'ACTIVITY_TYPE', 'REMOVE_CART', '장바구니 빼기', 6, TRUE),
('ACTIVITY_007', 'ACTIVITY_TYPE', 'LIKE', '좋아요', 7, TRUE),
('ACTIVITY_008', 'ACTIVITY_TYPE', 'SCRAP', '스크랩', 8, TRUE),
('ACTIVITY_009', 'ACTIVITY_TYPE', 'WRITE_POST', '포스트 작성', 9, TRUE),
('ACTIVITY_010', 'ACTIVITY_TYPE', 'WRITE_COMMENT', '댓글 작성', 10, TRUE),

-- 관리자 작업 타입 코드
('ADMIN_001', 'ADMIN_ACTION', 'CREATE', '생성', 1, TRUE),
('ADMIN_002', 'ADMIN_ACTION', 'UPDATE', '수정', 2, TRUE),
('ADMIN_003', 'ADMIN_ACTION', 'DELETE', '삭제', 3, TRUE),
('ADMIN_004', 'ADMIN_ACTION', 'APPROVE', '승인', 4, TRUE),
('ADMIN_005', 'ADMIN_ACTION', 'REJECT', '거부', 5, TRUE),
('ADMIN_006', 'ADMIN_ACTION', 'EXPORT', '내보내기', 6, TRUE),
('ADMIN_007', 'ADMIN_ACTION', 'IMPORT', '가져오기', 7, TRUE),

-- 환불 타입 코드
('REFUND_001', 'REFUND_TYPE', 'CANCEL', '주문 취소', 1, TRUE),
('REFUND_002', 'REFUND_TYPE', 'RETURN', '상품 반품', 2, TRUE),
('REFUND_003', 'REFUND_TYPE', 'EXCHANGE', '상품 교환', 3, TRUE),
('REFUND_004', 'REFUND_TYPE', 'PARTIAL', '부분 환불', 4, TRUE),

-- 환불 상태 코드
('REFUND_STATUS_001', 'REFUND_STATUS', 'REQUESTED', '환불 신청', 1, TRUE),
('REFUND_STATUS_002', 'REFUND_STATUS', 'APPROVED', '환불 승인', 2, TRUE),
('REFUND_STATUS_003', 'REFUND_STATUS', 'PROCESSING', '환불 처리중', 3, TRUE),
('REFUND_STATUS_004', 'REFUND_STATUS', 'COMPLETED', '환불 완료', 4, TRUE),
('REFUND_STATUS_005', 'REFUND_STATUS', 'REJECTED', '환불 거부', 5, TRUE),

-- 상담 타입 코드
('CONTACT_001', 'CONTACT_TYPE', 'email', '이메일', 1, TRUE),
('CONTACT_002', 'CONTACT_TYPE', 'phone', '전화', 2, TRUE),
('CONTACT_003', 'CONTACT_TYPE', 'kakao', '카카오톡', 3, TRUE),
('CONTACT_004', 'CONTACT_TYPE', 'chat', '채팅상담', 4, TRUE),

-- 고객상담 상태 코드
('CS_STATUS_001', 'CUSTOMER_STATUS', 'pending', '답변 대기', 1, TRUE),
('CS_STATUS_002', 'CUSTOMER_STATUS', 'answered', '답변 완료', 2, TRUE),
('CS_STATUS_003', 'CUSTOMER_STATUS', 'closed', '상담 종료', 3, TRUE);

-- 기본 관리자 역할
INSERT INTO admin_role (admin_role_name, role_description, role_created_date) VALUES
('슈퍼관리자', '모든 권한을 가진 최고 관리자', '2025-05-05'),
('상품관리자', '상품 등록 및 관리 담당', '2025-05-05'),
('주문관리자', '주문 및 배송 관리 담당', '2025-05-05'),
('고객서비스', '고객 문의 및 상담 담당', '2025-05-05'),
('마케팅관리자', '이벤트 및 프로모션 관리 담당', '2025-05-05');

INSERT INTO admin
(admin_role_id, admin_password, admin_name, admin_email, admin_phone, admin_status, admin_created_date)
VALUES
    (1, 'password123!', '슈퍼관리자1', 'superadmin1@cos.com', '010-1111-1111', 'ACTIVE', NOW()),
    (1, 'password123!', '슈퍼관리자2', 'superadmin2@cos.com', '010-1111-1112', 'ACTIVE', NOW()),
    (2, ß'password123!', '상품관리자1', 'product1@cos.com', '010-2222-1111', 'ACTIVE', NOW()),
    (2, 'password123!', '상품관리자2', 'product2@cos.com', '010-2222-1112', 'ACTIVE', NOW()),
    (2, 'password123!', '상품관리자3', 'product3@cos.com', '010-2222-1113', 'ACTIVE', NOW()),
    (3, 'password123!', '주문관리자1', 'order1@cos.com', '010-3333-1111', 'ACTIVE', NOW()),
    (3, 'password123!', '주문관리자2', 'order2@cos.com', '010-3333-1112', 'ACTIVE', NOW()),
    (3, 'password123!', '주문관리자3', 'order3@cos.com', '010-3333-1113', 'ACTIVE', NOW()),
    (3, 'password123!', '주문관리자4', 'order4@cos.com', '010-3333-1114', 'ACTIVE', NOW()),
    (4, 'password123!', '고객서비스1', 'cs1@cos.com', '010-4444-1111', 'ACTIVE', NOW()),
    (4, 'password123!', '고객서비스2', 'cs2@cos.com', '010-4444-1112', 'ACTIVE', NOW()),
    (4, 'password123!', '고객서비스3', 'cs3@cos.com', '010-4444-1113', 'ACTIVE', NOW()),
    (4, 'password123!', '고객서비스4', 'cs4@cos.com', '010-4444-1114', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자1', 'marketing1@cos.com', '010-5555-1111', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자2', 'marketing2@cos.com', '010-5555-1112', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자3', 'marketing3@cos.com', '010-5555-1113', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자4', 'marketing4@cos.com', '010-5555-1114', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자5', 'marketing5@cos.com', '010-5555-1115', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자6', 'marketing6@cos.com', '010-5555-1116', 'ACTIVE', NOW()),
    (5, 'password123!', '마케팅관리자7', 'marketing7@cos.com', '010-5555-1117', 'ACTIVE', NOW());
