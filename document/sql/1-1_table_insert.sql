-- 통합 DDL 스크립트: 테이블 생성 + 인덱스 + 외래키 제약조건

-- 1. 공통 코드 그룹 테이블
CREATE TABLE `COMMON_CODE_GROUP` (
                                     `group_id` VARCHAR(50) PRIMARY KEY NOT NULL COMMENT '코드 그룹 ID',
                                     `group_name` VARCHAR(100) NOT NULL COMMENT '코드 그룹명',
                                     `description` VARCHAR(255) COMMENT '그룹에 대한 상세 설명',
                                     `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',
                                     `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시'
) COMMENT = '공통 코드 그룹 테이블';

-- 2. 공통 코드 테이블
CREATE TABLE `COMMON_CODE` (
                               `code_id` VARCHAR(50) PRIMARY KEY NOT NULL COMMENT '코드 ID',
                               `group_id` VARCHAR(50) NOT NULL COMMENT '상위 코드 그룹 ID',
                               `code_name` VARCHAR(100) NOT NULL COMMENT '코드명',
                               `description` VARCHAR(255) COMMENT '코드에 대한 상세 설명',
                               `sort_order` INT DEFAULT 0 COMMENT '정렬 순서',
                               `is_active` BOOLEAN DEFAULT true COMMENT '코드 활성화 여부',
                               `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',
                               `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                               UNIQUE INDEX `uk_group_code` (`group_id`, `code_id`),
                               INDEX `idx_group_id` (`group_id`),
                               INDEX `idx_active` (`is_active`),
                               INDEX `idx_sort_order` (`group_id`, `sort_order`),

    -- 외래키
                               FOREIGN KEY (`group_id`) REFERENCES `COMMON_CODE_GROUP` (`group_id`)
) COMMENT = '공통 코드 테이블';

-- 3. 관리자 역할 테이블
CREATE TABLE `ADMIN_ROLE` (
                              `admin_role_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                              `admin_role_name` VARCHAR(50) NOT NULL COMMENT '역할 명',
                              `role_description` VARCHAR(200) COMMENT '역할설명',
                              `role_created_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',
                              `role_updated_date` TIMESTAMP COMMENT '수정일시'
) COMMENT = '관리자 역할 테이블';

-- 4. 관리자 테이블
CREATE TABLE `ADMIN` (
                         `admin_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                         `admin_role_id` BIGINT NOT NULL COMMENT '관리자 역할 ID',
                         `admin_password` VARCHAR(250) NOT NULL COMMENT '관리자 비밀번호',
                         `admin_name` VARCHAR(30) NOT NULL COMMENT '관리자 명',
                         `admin_email` VARCHAR(100) UNIQUE NOT NULL COMMENT '관리자 이메일',
                         `admin_phone` VARCHAR(20) COMMENT '관리자 전화번호',
                         `admin_status` VARCHAR(10) NOT NULL COMMENT '관리자 계정상태',
                         `admin_created_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일시',
                         `admin_updated_date` TIMESTAMP COMMENT '수정일시',

    -- 인덱스
                         INDEX `idx_admin_role` (`admin_role_id`),
                         INDEX `idx_admin_email` (`admin_email`),

    -- 외래키
                         FOREIGN KEY (`admin_role_id`) REFERENCES `ADMIN_ROLE` (`admin_role_id`)
) COMMENT = '관리자 테이블';

-- 5. 사용자 테이블
CREATE TABLE `USER` (
                        `user_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                        `user_email` VARCHAR(255) UNIQUE NOT NULL COMMENT '이메일',
                        `user_password` VARCHAR(255) COMMENT '비밀번호 (소셜로그인시 NULL)',
                        `user_nickname` VARCHAR(20) UNIQUE NOT NULL COMMENT '닉네임',
                        `user_name` VARCHAR(50) NOT NULL COMMENT '이름',
                        `user_address` TEXT COMMENT '주소',
                        `user_phone` VARCHAR(20) COMMENT '전화번호',
                        `social_provider` VARCHAR(20) COMMENT '소셜로그인 제공자',
                        `social_id` VARCHAR(255) COMMENT '소셜로그인 ID',
                        `terms_agreed` BOOLEAN DEFAULT false COMMENT '약관동의 여부',
                        `user_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '가입일시',
                        `user_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                        INDEX `idx_user_email` (`user_email`),
                        INDEX `idx_user_nickname` (`user_nickname`),
                        INDEX `idx_social_login` (`social_provider`, `social_id`)
) COMMENT = '사용자 테이블';

-- 6. 사용자 등급 테이블
CREATE TABLE `USER_GRADE` (
                              `grade_id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                              `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                              `grade_level` INT NOT NULL COMMENT '등급명',
                              `purchase_count` INT DEFAULT 0 COMMENT '구매횟수',
                              `total_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '실결제금액',
                              `grade_period_start` DATE NOT NULL COMMENT '등급산정 시작일',
                              `grade_period_end` DATE NOT NULL COMMENT '등급산정 종료일',
                              `grade_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',

    -- 인덱스
                              INDEX `idx_user_grade` (`user_id`),

    -- 외래키
                              FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`)
) COMMENT = '사용자 등급 테이블';

-- 7. 브랜드 테이블
CREATE TABLE `BRAND` (
                         `brand_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                         `brand_name` VARCHAR(100) UNIQUE NOT NULL COMMENT '브랜드명',
                         `logo_url` VARCHAR(500) COMMENT '브랜드 로고 URL',
                         `brand_description` TEXT COMMENT '브랜드 설명',

    -- 인덱스
                         INDEX `idx_brand_name` (`brand_name`)
) COMMENT = '브랜드 테이블';

-- 8. 상품 카테고리 테이블
CREATE TABLE `PRODUCT_CATEGORY` (
                                    `category_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                    `category_name` VARCHAR(100) NOT NULL COMMENT '카테고리명',
                                    `parent_id` BIGINT COMMENT '상위 카테고리 ID',
                                    `category_type` VARCHAR(50) NOT NULL COMMENT '카테고리 타입 (COMMON_CODE 참조)',
                                    `level` INT DEFAULT 1 COMMENT '카테고리 레벨',
                                    `display_order` INT DEFAULT 0 COMMENT '정렬 순서',
                                    `category_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',

    -- 인덱스
                                    INDEX `idx_category_parent` (`parent_id`),
                                    INDEX `idx_category_type` (`category_type`),
                                    INDEX `idx_category_display` (`display_order`),

    -- 외래키
                                    FOREIGN KEY (`parent_id`) REFERENCES `PRODUCT_CATEGORY` (`category_id`),
                                    FOREIGN KEY (`category_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '상품 카테고리 테이블';

-- 9. 상품 테이블
CREATE TABLE `PRODUCT` (
                           `product_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                           `product_title` VARCHAR(255) NOT NULL COMMENT '상품명',
                           `brand_id` BIGINT NOT NULL COMMENT '브랜드 ID',
                           `product_category_id` BIGINT NOT NULL COMMENT '카테고리 ID',
                           `main_image_url` VARCHAR(500) NOT NULL COMMENT '메인 이미지 URL',
                           `description` TEXT COMMENT '상품 설명',
                           `original_price` DECIMAL(10,2) NOT NULL COMMENT '정가',
                           `sale_price` DECIMAL(10,2) COMMENT '할인가',
                           `coupon_price` DECIMAL(10,2) COMMENT '쿠폰 적용가',
                           `discount_rate` DECIMAL(5,2) COMMENT '할인율 (%)',
                           `is_free_shipping` BOOLEAN DEFAULT false COMMENT '무료배송 여부',
                           `is_today_deal` BOOLEAN DEFAULT false COMMENT '오늘의 딜 여부',
                           `is_ohouse_only` BOOLEAN DEFAULT false COMMENT '오늘의집 단독상품 여부',
                           `product_color` VARCHAR(50) COMMENT '주요 색상',
                           `material` VARCHAR(100) COMMENT '소재',
                           `capacity` VARCHAR(50) COMMENT '사용 인원/용량',
                           `stock_quantity` INT DEFAULT 0 COMMENT '재고 수량',
                           `view_count` BIGINT DEFAULT 0 COMMENT '조회수',
                           `sales_count` BIGINT DEFAULT 0 COMMENT '판매량',
                           `review_count` INT DEFAULT 0 COMMENT '리뷰 개수',
                           `average_rating` DECIMAL(3,2) DEFAULT 0 COMMENT '평균 평점',
                           `bookmark_count` INT DEFAULT 0 COMMENT '북마크(스크랩) 개수',
                           `product_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '상품 상태 (COMMON_CODE 참조)',
                           `product_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',
                           `product_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일',

    -- 인덱스
                           INDEX `idx_product_title` (`product_title`),
                           INDEX `idx_product_brand` (`brand_id`),
                           INDEX `idx_product_category` (`product_category_id`),
                           INDEX `idx_product_price` (`original_price`),
                           INDEX `idx_product_status` (`product_status`),
                           INDEX `idx_product_features` (`is_free_shipping`, `is_today_deal`, `is_ohouse_only`),

    -- 외래키
                           FOREIGN KEY (`brand_id`) REFERENCES `BRAND` (`brand_id`),
                           FOREIGN KEY (`product_category_id`) REFERENCES `PRODUCT_CATEGORY` (`category_id`),
                           FOREIGN KEY (`product_status`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '상품 테이블';

-- 10. 상품 이미지 테이블
CREATE TABLE `PRODUCT_IMAGE` (
                                 `image_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                 `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                                 `image_url` VARCHAR(500) NOT NULL COMMENT '이미지 URL',
                                 `image_type` VARCHAR(50) NOT NULL COMMENT '이미지 타입 (COMMON_CODE 참조)',
                                 `alt_text` VARCHAR(255) COMMENT '이미지 설명',
                                 `sort_order` INT DEFAULT 0 COMMENT '정렬 순서',
                                 `product_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',

    -- 인덱스
                                 INDEX `idx_product_image` (`product_id`, `image_type`),
                                 INDEX `idx_image_order` (`product_id`, `sort_order`),

    -- 외래키
                                 FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`) ON DELETE CASCADE,
                                 FOREIGN KEY (`image_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '상품 이미지 테이블';

-- 11. 상품 옵션 테이블
CREATE TABLE `PRODUCT_OPTION` (
                                  `option_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                  `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                                  `option_type` VARCHAR(50) NOT NULL COMMENT '옵션 타입 (COMMON_CODE 참조)',
                                  `option_name` VARCHAR(100) NOT NULL COMMENT '옵션명',
                                  `option_value` VARCHAR(100) NOT NULL COMMENT '옵션값',
                                  `additional_price` DECIMAL(8,2) DEFAULT 0 COMMENT '추가 금액',
                                  `sort_order` INT DEFAULT 0 COMMENT '정렬 순서',

    -- 인덱스
                                  INDEX `idx_product_option` (`product_id`, `option_type`),

    -- 외래키
                                  FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`) ON DELETE CASCADE,
                                  FOREIGN KEY (`option_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '상품 옵션 테이블';

-- 12. 장바구니 테이블
CREATE TABLE `CART` (
                        `cart_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                        `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                        `cart_quantity` INT NOT NULL DEFAULT 1 COMMENT '담은 수량',
                        `selected_options` JSON COMMENT '선택한 상품 옵션 (JSON)',
                        `cart_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '장바구니 담은 일시',
                        `cart_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                        UNIQUE INDEX `uk_cart_user_product` (`user_id`, `product_id`),
                        INDEX `idx_cart_user` (`user_id`),
                        INDEX `idx_cart_product` (`product_id`),
                        INDEX `idx_cart_created` (`cart_created_at`),

    -- 외래키
                        FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`) ON DELETE CASCADE,
                        FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`) ON DELETE CASCADE
) COMMENT = '장바구니 테이블';

-- 13. 배송 정보 테이블
CREATE TABLE `DELIVERY_INFO` (
                                 `delivery_info_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                 `product_id` BIGINT UNIQUE NOT NULL COMMENT '상품 ID',
                                 `delivery_type` VARCHAR(50) NOT NULL DEFAULT 'NORMAL' COMMENT '배송 타입 (COMMON_CODE 참조)',
                                 `delivery_fee` DECIMAL(8,2) DEFAULT 0 COMMENT '배송비',
                                 `free_shipping_threshold` DECIMAL(10,2) COMMENT '무료배송 기준 금액',
                                 `expected_delivery_days` INT DEFAULT 3 COMMENT '예상 배송일',
                                 `is_today_departure` BOOLEAN DEFAULT false COMMENT '오늘 출발 여부',
                                 `delivery_company` VARCHAR(50) COMMENT '택배사',
                                 `special_notes` TEXT COMMENT '배송 특이사항',
                                 `delivery_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',
                                 `delivery_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일',

    -- 외래키
                                 FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`) ON DELETE CASCADE,
                                 FOREIGN KEY (`delivery_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '배송 정보 테이블';

-- 14. 주문 테이블
CREATE TABLE `ORDER` (
                         `order_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                         `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                         `order_status` VARCHAR(50) NOT NULL COMMENT '주문상태 (COMMON_CODE 참조)',
                         `total_amount` DECIMAL(10,2) NOT NULL COMMENT '총 주문금액',
                         `paid_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '실결제금액',
                         `order_date` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '주문일시',
                         `confirmed_date` TIMESTAMP COMMENT '구매확정일시',
                         `order_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',
                         `order_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                         INDEX `idx_order_user` (`user_id`),
                         INDEX `idx_order_status` (`order_status`),
                         INDEX `idx_order_date` (`order_date`),

    -- 외래키
                         FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                         FOREIGN KEY (`order_status`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '주문 테이블';

-- 15. 주문 상품 테이블
CREATE TABLE `ORDER_ITEM` (
                              `order_item_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                              `order_id` BIGINT NOT NULL COMMENT '주문 ID',
                              `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                              `quantity` INT NOT NULL COMMENT '수량',
                              `price` DECIMAL(10,2) NOT NULL COMMENT '단가',
                              `delivery_status` VARCHAR(50) NOT NULL COMMENT '배송상태 (COMMON_CODE 참조)',

    -- 인덱스
                              INDEX `idx_order_item` (`order_id`),
                              INDEX `idx_order_product` (`product_id`),

    -- 외래키
                              FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`) ON DELETE CASCADE,
                              FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`),
                              FOREIGN KEY (`delivery_status`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '주문 상품 테이블';

-- 16. 배송 테이블
CREATE TABLE `DELIVERY` (
                            `delivery_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                            `delivery_info_id` BIGINT COMMENT '배송 정보 ID (정책 참조)',
                            `order_id` BIGINT NOT NULL COMMENT '주문 ID',
                            `tracking_number` VARCHAR(50) NOT NULL COMMENT '송장번호',
                            `delivery_status` VARCHAR(20) NOT NULL COMMENT '배송상태',
                            `recipient_name` VARCHAR(50) NOT NULL COMMENT '수령인명',
                            `recipient_phone` VARCHAR(20) NOT NULL COMMENT '수령인연락처',
                            `delivery_address` VARCHAR(500) NOT NULL COMMENT '배송주소',
                            `shipped_at` TIMESTAMP COMMENT '발송일시',
                            `delivered_at` TIMESTAMP COMMENT '배송완료일시',
                            `updated_at` TIMESTAMP COMMENT '수정일시',

    -- 인덱스
                            INDEX `idx_delivery_order` (`order_id`),
                            INDEX `idx_delivery_tracking` (`tracking_number`),
                            INDEX `idx_delivery_status` (`delivery_status`),

    -- 외래키
                            FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`),
                            FOREIGN KEY (`delivery_info_id`) REFERENCES `DELIVERY_INFO` (`delivery_info_id`)
) COMMENT = '배송 테이블';

-- 17. 결제내역 테이블
CREATE TABLE `PAYMENT` (
                           `payment_id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                           `order_id` BIGINT NOT NULL COMMENT '결제와 연결된 주문번호',
                           `payment_method_id` INT NOT NULL COMMENT '사용한 결제수단 ID',
                           `payment_amount` DECIMAL(10,2) NOT NULL COMMENT '결제 금액',
                           `paid_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '결제 시각',

    -- 인덱스
                           INDEX `idx_payment_order` (`order_id`),
                           INDEX `idx_payment_method` (`payment_method_id`),

    -- 외래키
                           FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`)
) COMMENT = '결제내역 테이블';

-- 18. 환불 테이블
CREATE TABLE `REFUND` (
                          `refund_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                          `order_id` BIGINT NOT NULL COMMENT '주문 ID',
                          `payment_id` INT NOT NULL COMMENT '결제 ID',
                          `refund_type` VARCHAR(10) NOT NULL COMMENT 'CANCEL / REFUND',
                          `refund_reason` VARCHAR(255) COMMENT '사유',
                          `refund_amount` DECIMAL(12,2) NOT NULL COMMENT '환불 금액',
                          `refund_status` VARCHAR(20) DEFAULT 'REQUESTED' COMMENT 'REQUESTED/APPROVED/COMPLETED/REJECTED',
                          `requested_at` DATE NOT NULL COMMENT '환불 신청일',
                          `completed_at` DATE COMMENT '환불 완료일',

    -- 인덱스
                          INDEX `idx_refund_order` (`order_id`),
                          INDEX `idx_refund_payment` (`payment_id`),
                          INDEX `idx_refund_status` (`refund_status`),

    -- 외래키
                          FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`),
                          FOREIGN KEY (`payment_id`) REFERENCES `PAYMENT` (`payment_id`)
) COMMENT = '환불 테이블';

-- 19. 재고 테이블
CREATE TABLE `INVENTORY` (
                             `inventory_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                             `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                             `current_quantity` INT COMMENT '현재재고수량',
                             `safety_quantity` INT NOT NULL COMMENT '안전재고수량',
                             `inventory_created_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',
                             `inventory_updated_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                             INDEX `idx_inventory_product` (`product_id`),

    -- 외래키
                             FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`)
) COMMENT = '재고 테이블';

-- 20. 재고 입고 테이블
CREATE TABLE `INVENTORY_RECEIPT` (
                                     `receipt_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                     `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                                     `receipt_quantity` INT COMMENT '입고 수량',
                                     `receipt_status` VARCHAR(20) NOT NULL COMMENT '입고상태',
                                     `receipt_date` DATE NOT NULL COMMENT '입고일',

    -- 인덱스
                                     INDEX `idx_receipt_product` (`product_id`),
                                     INDEX `idx_receipt_date` (`receipt_date`),

    -- 외래키
                                     FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`)
) COMMENT = '재고 입고 테이블';

-- 21. 재고 출고 테이블
CREATE TABLE `INVENTORY_OUTBOUND` (
                                      `outbound_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                      `order_id` BIGINT NOT NULL COMMENT '주문 ID',
                                      `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                                      `outbound_quantity` INT NOT NULL COMMENT '출고 수량',
                                      `outbound_status` VARCHAR(20) NOT NULL COMMENT '출고 상태',
                                      `outbound_date` DATE NOT NULL COMMENT '출고일',
                                      `outbound_created_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',

    -- 인덱스
                                      INDEX `idx_outbound_order` (`order_id`),
                                      INDEX `idx_outbound_product` (`product_id`),
                                      INDEX `idx_outbound_date` (`outbound_date`),

    -- 외래키
                                      FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`),
                                      FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`)
) COMMENT = '재고 출고 테이블';

-- 22. 재고 이력 테이블
CREATE TABLE `INVENTORY_HISTORY` (
                                     `history_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                     `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                                     `inventory_id` BIGINT NOT NULL COMMENT '재고 ID',
                                     `receipt_id` BIGINT NOT NULL COMMENT '입고 ID',
                                     `outbound_id` BIGINT NOT NULL COMMENT '출고 ID',
                                     `change_quantity` INT NOT NULL COMMENT '변동 수량',
                                     `after_quantity` INT NOT NULL COMMENT '변동 후 재고수량',
                                     `change_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '변동 일시',

    -- 인덱스
                                     INDEX `idx_history_product` (`product_id`),
                                     INDEX `idx_history_date` (`change_date`),

    -- 외래키
                                     FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`),
                                     FOREIGN KEY (`inventory_id`) REFERENCES `INVENTORY` (`inventory_id`),
                                     FOREIGN KEY (`receipt_id`) REFERENCES `INVENTORY_RECEIPT` (`receipt_id`),
                                     FOREIGN KEY (`outbound_id`) REFERENCES `INVENTORY_OUTBOUND` (`outbound_id`)
) COMMENT = '재고 이력 테이블';

-- 23. 포스트 카테고리 테이블
CREATE TABLE `POST_CATEGORY` (
                                 `post_category_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                 `post_category_name` VARCHAR(100) NOT NULL COMMENT '카테고리명',
                                 `post_category_type` VARCHAR(50) NOT NULL COMMENT '카테고리 타입 (COMMON_CODE 참조)',
                                 `icon_url` VARCHAR(500) COMMENT '카테고리 아이콘 이미지',
                                 `display_order` INT DEFAULT 0 COMMENT '카테고리 표시 순서',

    -- 인덱스
                                 INDEX `idx_post_category_type` (`post_category_type`),
                                 INDEX `idx_post_category_order` (`display_order`),

    -- 외래키
                                 FOREIGN KEY (`post_category_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '포스트 카테고리 테이블';

-- 24. 게시물 테이블
CREATE TABLE `POST` (
                        `post_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                        `post_category_id` BIGINT COMMENT '포스트 카테고리 ID',
                        `title` VARCHAR(255) NOT NULL COMMENT '게시물 제목',
                        `content` TEXT NOT NULL COMMENT '게시물 내용',
                        `like_count` INT DEFAULT 0 COMMENT '좋아요 수',
                        `view_count` INT DEFAULT 0 COMMENT '조회수',
                        `comment_count` INT DEFAULT 0 COMMENT '댓글 수',
                        `is_public` BOOLEAN DEFAULT true COMMENT '공개 여부',
                        `report_count` INT DEFAULT 0 COMMENT '신고 수',
                        `post_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일시',
                        `post_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',
                        `post_updated_by` BIGINT COMMENT '최종 수정 유저 ID',

    -- 인덱스
                        INDEX `idx_post_user` (`user_id`),
                        INDEX `idx_post_category` (`post_category_id`),
                        INDEX `idx_post_public` (`is_public`, `post_created_at`),

    -- 외래키
                        FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                        FOREIGN KEY (`post_category_id`) REFERENCES `POST_CATEGORY` (`post_category_id`),
                        FOREIGN KEY (`post_updated_by`) REFERENCES `USER` (`user_id`)
) COMMENT = '게시물 테이블';

-- 25. 게시물 이미지 테이블
CREATE TABLE `POST_IMAGE` (
                              `image_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                              `post_id` BIGINT NOT NULL COMMENT '게시물 ID',
                              `image_url` VARCHAR(500) NOT NULL COMMENT '이미지 URL',
                              `is_thumbnail` BOOLEAN DEFAULT false COMMENT '대표 이미지 여부',
                              `display_order` INT DEFAULT 0 COMMENT '노출 순서',

    -- 인덱스
                              INDEX `idx_post_image` (`post_id`, `display_order`),

    -- 외래키
                              FOREIGN KEY (`post_id`) REFERENCES `POST` (`post_id`) ON DELETE CASCADE
) COMMENT = '게시물 이미지 테이블';

-- 26. 댓글 테이블
CREATE TABLE `COMMENT` (
                           `comment_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                           `post_id` BIGINT NOT NULL COMMENT '게시물 ID',
                           `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                           `parent_comment_id` BIGINT COMMENT '부모 댓글 ID',
                           `content` TEXT NOT NULL COMMENT '댓글 내용',
                           `like_count` INT DEFAULT 0 COMMENT '좋아요 수',
                           `report_count` INT DEFAULT 0 COMMENT '신고 수',
                           `comment_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '작성일시',
                           `comment_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                           INDEX `idx_comment_post` (`post_id`),
                           INDEX `idx_comment_user` (`user_id`),
                           INDEX `idx_comment_parent` (`parent_comment_id`),

    -- 외래키
                           FOREIGN KEY (`post_id`) REFERENCES `POST` (`post_id`) ON DELETE CASCADE,
                           FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                           FOREIGN KEY (`parent_comment_id`) REFERENCES `COMMENT` (`comment_id`)
) COMMENT = '댓글 테이블';

-- 27. 리뷰 테이블
CREATE TABLE `REVIEW` (
                          `review_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                          `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                          `product_id` BIGINT NOT NULL COMMENT '상품 ID',
                          `order_item_id` BIGINT NOT NULL COMMENT '주문상품 ID',
                          `rating` DECIMAL(2,1) NOT NULL COMMENT '평점 (0.0~5.0)',
                          `review_content` TEXT NOT NULL COMMENT '리뷰 내용',
                          `is_verified_purchase` BOOLEAN DEFAULT false COMMENT '구매 확인 여부',
                          `is_photo_review` BOOLEAN DEFAULT false COMMENT '포토 리뷰 여부',
                          `review_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '작성일시',
                          `review_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 인덱스
                          INDEX `idx_review_product` (`product_id`),
                          INDEX `idx_review_user` (`user_id`),
                          INDEX `idx_review_rating` (`rating`),

    -- 외래키
                          FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                          FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`),
                          FOREIGN KEY (`order_item_id`) REFERENCES `ORDER_ITEM` (`order_item_id`)
) COMMENT = '리뷰 테이블';

-- 28. 리뷰 이미지 테이블
CREATE TABLE `REVIEW_IMAGE` (
                                `image_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                `review_id` BIGINT NOT NULL COMMENT '리뷰 ID',
                                `image_url` VARCHAR(500) NOT NULL COMMENT '이미지 URL',
                                `sort_order` INT DEFAULT 0 COMMENT '정렬 순서',

    -- 인덱스
                                INDEX `idx_review_image` (`review_id`, `sort_order`),

    -- 외래키
                                FOREIGN KEY (`review_id`) REFERENCES `REVIEW` (`review_id`) ON DELETE CASCADE
) COMMENT = '리뷰 이미지 테이블';

-- 29. 상품 문의 테이블
CREATE TABLE `QUESTION` (
                            `question_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                            `product_id` BIGINT COMMENT '상품 ID',
                            `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                            `question_type` VARCHAR(50) NOT NULL COMMENT '문의 타입 (COMMON_CODE 참조)',
                            `question_title` VARCHAR(255) NOT NULL COMMENT '문의 제목',
                            `question_content` TEXT NOT NULL COMMENT '문의 내용',
                            `is_secret` BOOLEAN DEFAULT false COMMENT '비밀글 여부',
                            `question_status` VARCHAR(50) DEFAULT 'PENDING' COMMENT '답변 상태 (COMMON_CODE 참조)',
                            `question_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '작성일시',

    -- 인덱스
                            INDEX `idx_question_product` (`product_id`),
                            INDEX `idx_question_user` (`user_id`),
                            INDEX `idx_question_status` (`question_status`),

    -- 외래키
                            FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`),
                            FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                            FOREIGN KEY (`question_type`) REFERENCES `COMMON_CODE` (`code_id`),
                            FOREIGN KEY (`question_status`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '상품 문의 테이블';

-- 30. 답변 테이블
CREATE TABLE `ANSWER` (
                          `answer_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                          `question_id` BIGINT UNIQUE NOT NULL COMMENT '문의 ID',
                          `answerer_id` BIGINT NOT NULL COMMENT '답변자 ID',
                          `answer_content` TEXT NOT NULL COMMENT '답변 내용',
                          `answer_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '답변일시',
                          `answer_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일시',

    -- 외래키
                          FOREIGN KEY (`question_id`) REFERENCES `QUESTION` (`question_id`) ON DELETE CASCADE,
                          FOREIGN KEY (`answerer_id`) REFERENCES `USER` (`user_id`)
) COMMENT = '답변 테이블';

-- 31. 스크랩 테이블
CREATE TABLE `SCRAP` (
                         `scrap_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                         `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                         `post_id` BIGINT NOT NULL COMMENT '대상 ID',
                         `scrap_type` VARCHAR(50) NOT NULL COMMENT '스크랩 타입 (COMMON_CODE 참조)',
                         `scrap_folder_name` VARCHAR(255) COMMENT '스크랩 폴더명',
                         `scrap_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '스크랩일시',

    -- 인덱스
                         UNIQUE INDEX `uk_scrap` (`user_id`, `post_id`, `scrap_type`),
                         INDEX `idx_scrap_user` (`user_id`),
                         INDEX `idx_scrap_type` (`scrap_type`, `post_id`),

    -- 외래키
                         FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                         FOREIGN KEY (`post_id`) REFERENCES `POST` (`post_id`),
                         FOREIGN KEY (`scrap_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '스크랩 테이블';

-- 32. 좋아요 테이블
CREATE TABLE `LIKE` (
                        `like_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                        `post_id` BIGINT NOT NULL COMMENT '대상 ID',
                        `like_status` BOOLEAN DEFAULT false COMMENT '좋아요 상태',
                        `like_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '좋아요 일시',

    -- 인덱스
                        UNIQUE INDEX `uk_like` (`user_id`, `post_id`),
                        INDEX `idx_like_target` (`post_id`),

    -- 외래키
                        FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                        FOREIGN KEY (`post_id`) REFERENCES `POST` (`post_id`)
) COMMENT = '좋아요 테이블';

-- 33. 쿠폰 테이블
CREATE TABLE `COUPON` (
                          `coupon_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                          `product_id` BIGINT COMMENT '상품 ID (특정 상품 쿠폰)',
                          `coupon_title` VARCHAR(255) NOT NULL COMMENT '쿠폰 제목',
                          `coupon_description` TEXT COMMENT '쿠폰 설명',
                          `discount_rate` DECIMAL(5,2) COMMENT '할인율 (%)',
                          `discount_amount` DECIMAL(10,2) COMMENT '할인 금액',
                          `max_discount_amount` DECIMAL(10,2) COMMENT '최대 할인 금액',
                          `min_purchase_amount` DECIMAL(10,2) COMMENT '최소 구매 금액',
                          `coupon_image_url` VARCHAR(500) COMMENT '쿠폰 이미지 URL',
                          `start_date` TIMESTAMP NOT NULL COMMENT '쿠폰 시작일',
                          `expired_at` TIMESTAMP NOT NULL COMMENT '쿠폰 만료일',
                          `is_active` BOOLEAN DEFAULT true COMMENT '활성화 여부',
                          `coupon_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',
                          `coupon_updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '수정일',

    -- 인덱스
                          INDEX `idx_coupon_product` (`product_id`),
                          INDEX `idx_coupon_active` (`is_active`, `start_date`, `expired_at`),

    -- 외래키
                          FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`)
) COMMENT = '쿠폰 테이블';

-- 34. 사용자 쿠폰 테이블
CREATE TABLE `USER_COUPON` (
                               `user_coupon_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                               `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                               `coupon_id` BIGINT NOT NULL COMMENT '쿠폰 ID',
                               `order_id` BIGINT COMMENT '주문 ID (사용된 경우)',
                               `coupon_status` VARCHAR(50) DEFAULT 'ISSUED' COMMENT '쿠폰 상태 (COMMON_CODE 참조)',
                               `issued_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '발급일',
                               `used_at` TIMESTAMP COMMENT '사용일',

    -- 인덱스
                               UNIQUE INDEX `uk_user_coupon` (`user_id`, `coupon_id`),
                               INDEX `idx_user_coupon_status` (`user_id`, `coupon_status`),

    -- 외래키
                               FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                               FOREIGN KEY (`coupon_id`) REFERENCES `COUPON` (`coupon_id`),
                               FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`),
                               FOREIGN KEY (`coupon_status`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '사용자 쿠폰 테이블';

-- 35. 포인트 테이블
CREATE TABLE `POINT` (
                         `point_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                         `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                         `point_amount` INT NOT NULL COMMENT '포인트 금액 (+ 적립, - 사용)',
                         `point_type` VARCHAR(50) NOT NULL COMMENT '포인트 타입 (COMMON_CODE 참조)',
                         `point_description` VARCHAR(255) NOT NULL COMMENT '포인트 설명',
                         `point_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '발생일시',

    -- 인덱스
                         INDEX `idx_point_user` (`user_id`),
                         INDEX `idx_point_type` (`point_type`),

    -- 외래키
                         FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                         FOREIGN KEY (`point_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '포인트 테이블';

-- 36. 이벤트 테이블
CREATE TABLE `EVENT` (
                         `event_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                         `event_name` VARCHAR(255) NOT NULL COMMENT '이벤트명',
                         `event_type` VARCHAR(50) NOT NULL COMMENT '이벤트 타입 (COMMON_CODE 참조)',
                         `brand_id` BIGINT COMMENT '브랜드 ID',
                         `event_description` TEXT NOT NULL COMMENT '이벤트 설명',
                         `event_start_date` TIMESTAMP NOT NULL COMMENT '시작일시',
                         `event_end_date` TIMESTAMP NOT NULL COMMENT '종료일시',
                         `is_active` BOOLEAN DEFAULT true COMMENT '활성화 여부',
                         `event_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',

    -- 인덱스
                         INDEX `idx_event_type` (`event_type`),
                         INDEX `idx_event_brand` (`brand_id`),
                         INDEX `idx_event_active` (`is_active`, `event_start_date`, `event_end_date`),

    -- 외래키
                         FOREIGN KEY (`brand_id`) REFERENCES `BRAND` (`brand_id`),
                         FOREIGN KEY (`event_type`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '이벤트 테이블';

-- 37. 오늘의 딜 테이블
CREATE TABLE `TODAY_DEAL` (
                              `deal_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                              `product_id` BIGINT UNIQUE NOT NULL COMMENT '상품 ID',
                              `deal_price` DECIMAL(10,2) NOT NULL COMMENT '딜 가격',
                              `original_price` DECIMAL(10,2) NOT NULL COMMENT '원가',
                              `discount_rate` DECIMAL(5,2) NOT NULL COMMENT '할인율',
                              `limited_quantity` INT COMMENT '한정 수량',
                              `sold_quantity` INT DEFAULT 0 COMMENT '판매된 수량',
                              `start_datetime` TIMESTAMP NOT NULL COMMENT '딜 시작 시간',
                              `end_datetime` TIMESTAMP NOT NULL COMMENT '딜 종료 시간',
                              `todaydeal_is_active` BOOLEAN DEFAULT true COMMENT '활성화 여부',
                              `todaydeal_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '등록일',

    -- 인덱스
                              INDEX `idx_today_deal_active` (`todaydeal_is_active`, `start_datetime`, `end_datetime`),

    -- 외래키
                              FOREIGN KEY (`product_id`) REFERENCES `PRODUCT` (`product_id`)
) COMMENT = '오늘의 딜 테이블';

-- 38. 고객상담 테이블
CREATE TABLE `CUSTOMER_SERVICE` (
                                    `cs_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                    `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                                    `contact_type` VARCHAR(50) NOT NULL COMMENT '상담 타입 (COMMON_CODE 참조)',
                                    `title` VARCHAR(255) NOT NULL COMMENT '제목',
                                    `question_content` TEXT NOT NULL COMMENT '문의 내용',
                                    `customer_status` VARCHAR(50) DEFAULT 'pending' COMMENT '처리상태 (COMMON_CODE 참조)',
                                    `customer_created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '상담일시',
                                    `responded_at` TIMESTAMP COMMENT '답변일시',

    -- 인덱스
                                    INDEX `idx_cs_user` (`user_id`),
                                    INDEX `idx_cs_status` (`customer_status`),

    -- 외래키
                                    FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`),
                                    FOREIGN KEY (`contact_type`) REFERENCES `COMMON_CODE` (`code_id`),
                                    FOREIGN KEY (`customer_status`) REFERENCES `COMMON_CODE` (`code_id`)
) COMMENT = '고객상담 테이블';

-- 39. 사용자 활동 로그 테이블
CREATE TABLE `USER_ACTIVITY_LOG` (
                                     `user_activity_log_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '행위 ID',
                                     `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                                     `page_url` VARCHAR(2083) NOT NULL COMMENT '행위 발생 url',
                                     `referrer_url` VARCHAR(2083) COMMENT '유입 url',
                                     `activity_time` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '행위 발생 시간',
                                     `user_agent` TEXT COMMENT '브라우저 정보',
                                     `activity_created_at` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '발생 시간',
                                     `activity_type` VARCHAR(50) NOT NULL COMMENT '행위 유형 (LOGIN, VIEW, PURCHASE 등)',
                                     `ip_address` VARCHAR(45) COMMENT 'IP 주소',
                                     `session_id` VARCHAR(100) COMMENT '세션 ID',
                                     `device_type` VARCHAR(20) COMMENT '디바이스 타입 (mobile, desktop, tablet)',
                                     `target_id` BIGINT COMMENT '대상 ID (상품 ID, 포스트 ID 등)',

    -- 인덱스
                                     INDEX `idx_user_activity_user` (`user_id`),
                                     INDEX `idx_user_activity_type` (`activity_type`),
                                     INDEX `idx_user_activity_time` (`activity_time`),
                                     INDEX `idx_user_activity_session` (`session_id`),
                                     INDEX `idx_user_activity_target` (`target_id`),

    -- 외래키
                                     FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`) ON DELETE CASCADE
) COMMENT = '사용자 활동 로그 테이블';

-- 40. 주문 상태 변경 이력 테이블
CREATE TABLE `ORDER_STATUS_HISTORY` (
                                        `order_status_history_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '주문 상태 변경 ID',
                                        `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                                        `order_id` BIGINT NOT NULL COMMENT '주문 ID',
                                        `previous_status` VARCHAR(20) COMMENT '이전 상태',
                                        `current_status` VARCHAR(20) NOT NULL COMMENT '현재 상태',
                                        `change_description` TEXT COMMENT '변경 상세 설명',
                                        `status_changed_at` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '상태 변경 일시',
                                        `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) COMMENT '레코드 생성 일시',

    -- 인덱스
                                        INDEX `idx_order_status_user` (`user_id`),
                                        INDEX `idx_order_status_order` (`order_id`),
                                        INDEX `idx_status_change_date` (`status_changed_at`),
                                        INDEX `idx_current_status` (`current_status`),

    -- 외래키
                                        FOREIGN KEY (`user_id`) REFERENCES `USER` (`user_id`) ON DELETE CASCADE,
                                        FOREIGN KEY (`order_id`) REFERENCES `ORDER` (`order_id`) ON DELETE CASCADE
) COMMENT = '주문 상태 변경 이력 테이블';

-- 41. 관리자 행위 로그
CREATE TABLE `ADMIN_ACTION_LOG` (
                                    `admin_log_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '관리자 로그 식별 ID',
                                    `admin_id` BIGINT NOT NULL COMMENT '행위를 수행한 관리자의 아이디',
                                    `admin_name` VARCHAR(100) NOT NULL COMMENT '행위를 수행한 관리자의 이름',
                                    `action_type` ENUM ('CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT') NOT NULL COMMENT '수행한 행위의 종류 (CRUD)',
                                    `target_resource` VARCHAR(100) COMMENT '행위의 대상이 된 리소스',
                                    `target_resource_id` VARCHAR(100) COMMENT '행위 대상 리소스의 고유 ID',
                                    `action_detail` TEXT COMMENT '수행한 행위에 대한 상세 설명',
                                    `request_ip` VARCHAR(50) NOT NULL COMMENT '관리자가 접속한 IP 주소',
                                    `status` ENUM ('SUCCESS', 'FAILURE') NOT NULL COMMENT '행위의 성공/실패 여부',
                                    `error_message` TEXT COMMENT '실패 시 발생한 오류 메시지',
                                    `created_at` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '로그가 기록된 시간',

    -- 인덱스
                                    INDEX `idx_admin_action_admin` (`admin_id`),
                                    INDEX `idx_admin_action_type` (`action_type`),
                                    INDEX `idx_admin_action_date` (`created_at`),
                                    INDEX `idx_admin_action_resource` (`target_resource`, `target_resource_id`),

    -- 외래키
                                    FOREIGN KEY (`admin_id`) REFERENCES `ADMIN` (`admin_id`)
) COMMENT = '관리자 행위 로그';