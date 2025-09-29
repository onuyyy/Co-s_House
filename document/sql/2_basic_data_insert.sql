-- ========================================
-- USER_ROLE 테이블 기본 데이터 (먼저 실행)
-- ========================================

INSERT INTO user_role (user_role_name, role_description, role_created_date) VALUES
('USER', '일반 사용자 - 상품 구매 및 기본 서비스 이용', NOW()),
('ADMIN', '관리자 - 사용자/상품/주문 관리 권한', NOW()),
('SUPER_ADMIN', '최고 관리자 - 모든 시스템 관리 권한', NOW());

-- ========================================
-- 기본 데이터 삽입 (User 엔티티 기반)
-- ========================================

-- 공통 코드 그룹 삽입
INSERT INTO common_code_group (group_id, group_name, description) VALUES
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
('CUSTOMER_STATUS', '고객상담 상태', '고객상담 처리 상태 코드'),
('POINT_TYPE', '포인트 타입', '포인트 변동 유형 코드'),
('COUPON_SCOPE', '쿠폰 적용 범위', '쿠폰 적용 범위 코드'),
('PRODUCT_TYPE', '상품 타입', '상품 분류 타입 코드');

-- 기본 공통 코드 데이터 삽입
INSERT INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
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
('CS_STATUS_003', 'CUSTOMER_STATUS', 'closed', '상담 종료', 3, TRUE),

-- 포인트 타입 코드
('POINT_001', 'POINT_TYPE', 'EARN', '포인트 적립', 1, TRUE),
('POINT_002', 'POINT_TYPE', 'USE', '포인트 사용', 2, TRUE),
('POINT_003', 'POINT_TYPE', 'EXPIRE', '포인트 만료', 3, TRUE),

-- 쿠폰 적용 범위 코드
('COUPON_SCOPE_001', 'COUPON_SCOPE', 'GLOBAL', '전역 쿠폰', 1, TRUE),
('COUPON_SCOPE_002', 'COUPON_SCOPE', 'BRAND', '브랜드 쿠폰', 2, TRUE),
('COUPON_SCOPE_003', 'COUPON_SCOPE', 'PRODUCT', '상품 쿠폰', 3, TRUE),

-- 상품 타입 코드
('PRODUCT_TYPE_001', 'PRODUCT_TYPE', 'PHYSICAL', '실물 상품', 1, TRUE),
('PRODUCT_TYPE_002', 'PRODUCT_TYPE', 'DIGITAL', '디지털 상품', 2, TRUE),
('PRODUCT_TYPE_003', 'PRODUCT_TYPE', 'SERVICE', '서비스 상품', 3, TRUE),
('PRODUCT_TYPE_004', 'PRODUCT_TYPE', 'SUBSCRIPTION', '구독 상품', 4, TRUE);

-- ========================================
-- USER 테이블 테스트 데이터 (관리자 + 일반 사용자)
-- ========================================

-- 최고 관리자 (SUPER_ADMIN) 
INSERT INTO user (user_role, email_verified, user_email, user_password, user_nickname, user_name, user_phone, user_address, terms_agreed) VALUES
(3, true, 'superadmin@cos.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'SuperAdmin', '최고관리자', '010-0000-0001', '서울특별시 강남구 테헤란로 123', TRUE);

-- 일반 관리자 (ADMIN) - 4명
INSERT INTO user (user_role, email_verified, user_email, user_password, user_nickname, user_name, user_phone, user_address, terms_agreed) VALUES
(2, true, 'admin1@cos.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ProductAdmin', '상품관리자', '010-1111-0001', '서울특별시 서초구 서초대로 456', TRUE),
(2, true, 'admin2@cos.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'OrderAdmin', '주문관리자', '010-1111-0002', '서울특별시 송파구 올림픽로 789', TRUE),
(2, true, 'admin3@cos.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'CSAdmin', '고객서비스관리자', '010-1111-0003', '서울특별시 마포구 월드컵북로 111', TRUE),
(2, true, 'admin4@cos.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'MarketingAdmin', '마케팅관리자', '010-1111-0004', '서울특별시 용산구 한강대로 222', TRUE);

-- 일반 사용자 (USER) - 5명  
INSERT INTO user (user_role, email_verified, user_email, user_password, user_nickname, user_name, user_phone, user_address, terms_agreed) VALUES
(1, true, 'user1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'HomeSeeker', '김집찾', '010-2222-0001', '경기도 성남시 분당구 판교로 333', TRUE),
(1, true, 'user2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'DesignLover', '박인테리어', '010-2222-0002', '경기도 수원시 영통구 광교로 444', TRUE),
(1, true, 'user3@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'FurnitureFan', '이가구', '010-2222-0003', '인천광역시 연수구 컨벤시아대로 555', TRUE),
(1, true, 'user4@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'CozyHome', '최따뜻', '010-2222-0004', '부산광역시 해운대구 센텀중앙로 666', TRUE),
(1, true, 'user5@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ModernLife', '정모던', '010-2222-0005', '대구광역시 수성구 동대구로 777', TRUE);

-- ========================================
-- 추가 테스트용 사용자 (소셜 로그인 포함)
-- ========================================

-- 카카오 소셜 사용자
INSERT INTO user (user_role, email_verified, user_email, user_password, user_nickname, user_name, user_phone, user_address, social_provider, social_id, terms_agreed) VALUES
(1, true, 'kakao_user@kakao.com', NULL, 'KakaoUser', '카카오유저', '010-3333-0001', '광주광역시 서구 상무대로 888', 'KAKAO', 'kakao_12345', TRUE);

-- 네이버 소셜 사용자  
INSERT INTO user (user_role, email_verified,  user_email, user_password, user_nickname, user_name, user_phone, user_address, social_provider, social_id, terms_agreed) VALUES
(1, true, 'naver_user@naver.com', NULL, 'NaverUser', '네이버유저', '010-3333-0002', '대전광역시 유성구 대학로 999', 'NAVER', 'naver_67890', TRUE);

-- 구글 소셜 사용자
INSERT INTO user (user_role, email_verified, user_email, user_password, user_nickname, user_name, user_phone, user_address, social_provider, social_id, terms_agreed) VALUES
(1, true, 'google_user@gmail.com', NULL, 'GoogleUser', '구글유저', '010-3333-0003', '울산광역시 남구 삼산로 101', 'GOOGLE', 'google_abcde', TRUE);

-- ========================================
-- 테스트용 포인트 데이터
-- ========================================

-- 일반 사용자들에게 초기 포인트 지급
INSERT INTO user_point (user_id, available_point, total_point, updated_at) VALUES
(6, 5000, 5000, NOW()),  -- user1@example.com (김집찾)
(7, 3000, 3000, NOW()),  -- user2@example.com (박인테리어)
(8, 10000, 10000, NOW()), -- user3@example.com (이가구)
(9, 7500, 7500, NOW()),  -- user4@example.com (최따뜻)
(10, 2000, 2000, NOW()); -- user5@example.com (정모던)

-- 포인트 적립 히스토리
INSERT INTO point_history (user_id, type, amount, balance_before, balance_after, description, reference_id, reference_type, created_at) VALUES
(6, 'EARN', 5000, 0, 5000, '회원가입 적립', 'SIGNUP_6', 'SIGNUP', NOW()),
(7, 'EARN', 3000, 0, 3000, '회원가입 적립', 'SIGNUP_7', 'SIGNUP', NOW()),
(8, 'EARN', 10000, 0, 10000, '회원가입 적립', 'SIGNUP_8', 'SIGNUP', NOW()),
(9, 'EARN', 7500, 0, 7500, '회원가입 적립', 'SIGNUP_9', 'SIGNUP', NOW()),
(10, 'EARN', 2000, 0, 2000, '회원가입 적립', 'SIGNUP_10', 'SIGNUP', NOW());

-- ========================================
-- 테스트용 쿠폰 데이터
-- ========================================

-- 전역 쿠폰 (모든 상품에 사용 가능)
INSERT INTO coupon (scope, coupon_title, coupon_description, discount_rate, discount_amount, max_discount_amount, min_purchase_amount, start_date, expired_at, is_active) VALUES
('GLOBAL', '신규가입 10% 할인', '신규 회원 대상 10% 할인 쿠폰', 10.00, NULL, 50000.00, 50000.00, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE),
('GLOBAL', '무료배송 쿠폰', '배송비 무료 쿠폰', NULL, 3000.00, 3000.00, 30000.00, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE),
('GLOBAL', '5만원 이상 5천원 할인', '5만원 이상 구매시 5천원 할인', NULL, 5000.00, 5000.00, 50000.00, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), TRUE);

-- 사용자별 쿠폰 발급 (user1, user2, user3에게 발급)
INSERT INTO user_coupon (user_id, coupon_id, coupon_status, used_at) VALUES
(6, 1, 'COUPON_001', NULL),  -- user1에게 신규가입 할인 (ISSUED)
(6, 2, 'COUPON_001', NULL),  -- user1에게 무료배송 (ISSUED)
(7, 1, 'COUPON_001', NULL),  -- user2에게 신규가입 할인 (ISSUED)
(7, 3, 'COUPON_001', NULL),  -- user2에게 5천원 할인 (ISSUED)
(8, 2, 'COUPON_001', NULL),  -- user3에게 무료배송 (ISSUED)
(8, 3, 'COUPON_001', NULL);  -- user3에게 5천원 할인 (ISSUED)

-- ========================================
-- 비밀번호 정보
-- ========================================
-- 모든 일반 계정의 비밀번호: "password123!"
-- BCrypt 해시값: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
-- 소셜 로그인 계정은 비밀번호가 NULL



