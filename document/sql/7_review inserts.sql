
-- `review` 테이블 더미 데이터 생성을 위한 전체 스크립트

-- 1. `common_code_group` 테이블에 데이터 삽입
INSERT INTO common_code_group (group_id, group_name) VALUES
                                                         ('PRODUCT_CATEGORY_TYPE', '상품 카테고리 타입'),
                                                         ('PRODUCT_STATUS', '상품 상태 코드'),
                                                         ('ORDER_STATUS', '주문 상태 코드'),
                                                         ('PRODUCT_OPTION_TYPE', '상품 옵션 타입');

-- 2. `common_code` 테이블에 데이터 삽입
INSERT INTO common_code (code_id, group_id, code_name) VALUES
                                                           ('MAIN_CATEGORY', 'PRODUCT_CATEGORY_TYPE', '메인 카테고리'),
                                                           ('SUB_CATEGORY', 'PRODUCT_CATEGORY_TYPE', '서브 카테고리'),
                                                           ('ON_SALE', 'PRODUCT_STATUS', '판매중'),
                                                           ('SOLD_OUT', 'PRODUCT_STATUS', '품절'),
                                                           ('DISCONTINUED', 'PRODUCT_STATUS', '단종'),
                                                           ('DELIVERED', 'ORDER_STATUS', '배송완료'),
                                                           ('SHIPPED', 'ORDER_STATUS', '배송중'),
                                                           ('CANCELED', 'ORDER_STATUS', '주문취소'),
                                                           ('COLOR', 'PRODUCT_OPTION_TYPE', '색상'),
                                                           ('SIZE', 'PRODUCT_OPTION_TYPE', '사이즈');

-- 3. `brand` 테이블에 데이터 삽입
INSERT INTO brand (brand_id, brand_name) VALUES
                                             (1, 'TechWave'),
                                             (2, 'EcoHome'),
                                             (3, 'UrbanThreads');

-- 4. `product_category` 테이블에 데이터 삽입
INSERT INTO product_category (category_id, category_name, category_type, level, display_order) VALUES
                                                                                                   (100, '가구', 'MAIN_CATEGORY', 1, 1),
                                                                                                   (200, '가전', 'MAIN_CATEGORY', 1, 2),
                                                                                                   (300, '의류', 'MAIN_CATEGORY', 1, 3);

INSERT INTO product_category (category_id, category_name, category_type, level, display_order, parent_id) VALUES
                                                                                                              (101, '침대', 'SUB_CATEGORY', 2, 1, 100),
                                                                                                              (201, '냉장고', 'SUB_CATEGORY', 2, 1, 200),
                                                                                                              (301, '티셔츠', 'SUB_CATEGORY', 2, 1, 300);

-- 5. `user_role` 테이블에 데이터 삽입 (가정)
INSERT INTO user_role (user_role_id, user_role_name, role_created_date, role_description) VALUES
    (1, 'CUSTOMER', NOW(), '일반 고객');

-- 6. `user` 테이블에 데이터 삽입
INSERT INTO user (user_id, user_name, user_nickname, user_email, user_role_id, user_password, user_phone, terms_agreed, user_created_at) VALUES
                                                                                                                                             (101, '김민지', 'minji_kim', 'minji.kim@example.com', 1, 'password123', '010-1234-5678', 1, NOW()),
                                                                                                                                             (102, '박서준', 'seojun_park', 'seojun.park@example.com', 1, 'password123', '010-2345-6789', 1, NOW()),
                                                                                                                                             (103, '이하나', 'hana_lee', 'hana.lee@example.com', 1, 'password123', '010-3456-7890', 1, NOW()),
                                                                                                                                             (104, '정우성', 'woosung_jeong', 'woosung.jeong@example.com', 1, 'password123', '010-4567-8901', 1, NOW()),
                                                                                                                                             (105, '최유리', 'yuri_choi', 'yuri.choi@example.com', 1, 'password123', '010-5678-9012', 1, NOW()),
                                                                                                                                             (106, '강하늘', 'haneul_kang', 'haneul.kang@example.com', 1, 'password123', '010-6789-0123', 1, NOW()),
                                                                                                                                             (107, '윤아영', 'ayoung_yun', 'ayoung.yun@example.com', 1, 'password123', '010-7890-1234', 1, NOW()),
                                                                                                                                             (108, '송지효', 'jihyo_song', 'jihyo.song@example.com', 1, 'password123', '010-8901-2345', 1, NOW()),
                                                                                                                                             (109, '고지영', 'jiyoung_go', 'jiyoung.go@example.com', 1, 'password123', '010-9012-3456', 1, NOW()),
                                                                                                                                             (110, '홍길동', 'gildong_hong', 'gildong.hong@example.com', 1, 'password123', '010-0123-4567', 1, NOW());

-- 7. `product` 테이블에 데이터 삽입
INSERT INTO product (product_id, product_title, product_status, main_image_url, original_price, stock_quantity, brand_id, product_category_id, product_created_at) VALUES
                                                                                                                                                                       (201, '블루투스 스피커', 'ON_SALE', 'http://example.com/images/speaker.jpg', 55000.00, 100, 1, 201, NOW()),
                                                                                                                                                                       (202, '냉장고', 'ON_SALE', 'http://example.com/images/fridge.jpg', 1200000.00, 20, 2, 201, NOW()),
                                                                                                                                                                       (203, '기본 티셔츠', 'ON_SALE', 'http://example.com/images/tshirt.jpg', 25000.00, 500, 3, 301, NOW());

-- 8. `product_option` 테이블에 데이터 삽입
INSERT INTO product_option (option_id, product_id, option_type, option_name, option_value, additional_price, sort_order) VALUES
                                                                                                                             (1, 201, 'COLOR', '색상', '블랙', 0, 1),
                                                                                                                             (2, 201, 'COLOR', '색상', '화이트', 5000.00, 2),
                                                                                                                             (3, 202, 'SIZE', '용량', '300L', 0, 1),
                                                                                                                             (4, 202, 'SIZE', '용량', '500L', 200000.00, 2),
                                                                                                                             (5, 203, 'COLOR', '색상', '블랙', 0, 1),
                                                                                                                             (6, 203, 'COLOR', '색상', '화이트', 0, 2),
                                                                                                                             (7, 203, 'SIZE', '사이즈', 'M', 0, 1),
                                                                                                                             (8, 203, 'SIZE', '사이즈', 'L', 0, 2);

-- 9. `order` 테이블에 데이터 삽입
INSERT INTO `order` (order_id, user_id, order_status, total_amount, paid_amount, order_created_at, confirmed_date) VALUES
                                                                                                                       (301, 101, 'DELIVERED', 55000.00, 55000.00, NOW(), NOW()),
                                                                                                                       (302, 102, 'DELIVERED', 1200000.00, 1200000.00, NOW(), NOW()),
                                                                                                                       (303, 103, 'DELIVERED', 25000.00, 25000.00, NOW(), NOW()),
                                                                                                                       (304, 104, 'DELIVERED', 25000.00, 25000.00, NOW(), NOW()),
                                                                                                                       (305, 105, 'DELIVERED', 55000.00, 55000.00, NOW(), NOW()),
                                                                                                                       (306, 106, 'DELIVERED', 1200000.00, 1200000.00, NOW(), NOW()),
                                                                                                                       (307, 107, 'DELIVERED', 25000.00, 25000.00, NOW(), NOW()),
                                                                                                                       (308, 108, 'DELIVERED', 25000.00, 25000.00, NOW(), NOW()),
                                                                                                                       (309, 109, 'DELIVERED', 55000.00, 55000.00, NOW(), NOW()),
                                                                                                                       (310, 110, 'DELIVERED', 1200000.00, 1200000.00, NOW(), NOW());

-- 10. `order_item` 테이블에 데이터 삽입
INSERT INTO order_item (order_item_id, order_id, product_id, quantity, price, delivery_status) VALUES
                                                                                                   (401, 301, 201, 1, 55000.00, 'DELIVERED'),
                                                                                                   (402, 302, 202, 1, 1200000.00, 'DELIVERED'),
                                                                                                   (403, 303, 203, 1, 25000.00, 'DELIVERED'),
                                                                                                   (404, 304, 203, 1, 25000.00, 'DELIVERED'),
                                                                                                   (405, 305, 201, 1, 55000.00, 'DELIVERED'),
                                                                                                   (406, 306, 202, 1, 1200000.00, 'DELIVERED'),
                                                                                                   (407, 307, 203, 1, 25000.00, 'DELIVERED'),
                                                                                                   (408, 308, 203, 1, 25000.00, 'DELIVERED'),
                                                                                                   (409, 309, 201, 1, 55000.00, 'DELIVERED'),
                                                                                                   (410, 310, 202, 1, 1200000.00, 'DELIVERED');

-- 11. `review` 테이블에 데이터 삽입 (10개)
INSERT INTO review (review_id, is_photo_review, is_verified_purchase, rating, order_item_id, product_id, review_created_at, user_id, review_content) VALUES
                                                                                                                                                         (501, 1, 1, 5.0, 401, 201, NOW(), 101, '블루투스 스피커 음질이 정말 좋아요!'),
                                                                                                                                                         (502, 1, 1, 4.0, 402, 202, NOW(), 102, '냉장고 배송이 빨랐어요. 만족합니다.'),
                                                                                                                                                         (503, 0, 1, 5.0, 403, 203, NOW(), 103, '티셔츠 재질이 부드럽고 편해요.'),
                                                                                                                                                         (504, 1, 1, 4.5, 404, 203, NOW(), 104, '색상도 예쁘고 잘 맞아요!'),
                                                                                                                                                         (505, 0, 1, 3.5, 405, 201, NOW(), 105, '음질은 좋지만 배터리가 빨리 닳네요.'),
                                                                                                                                                         (506, 1, 1, 5.0, 406, 202, NOW(), 106, '용량이 커서 너무 좋아요. 배송도 빠름!'),
                                                                                                                                                         (507, 0, 1, 2.0, 407, 203, NOW(), 107, '기대했던 것보다 옷감이 얇아요.'),
                                                                                                                                                         (508, 1, 1, 4.0, 408, 203, NOW(), 108, '가격 대비 괜찮은 품질입니다.'),
                                                                                                                                                         (509, 0, 1, 5.0, 409, 201, NOW(), 109, '선물했는데 받는 사람이 아주 좋아했어요.'),
                                                                                                                                                         (510, 1, 1, 4.5, 410, 202, NOW(), 110, '디자인이 깔끔하고 공간 효율적입니다.');

-- 12. `review_image` 테이블에 데이터 삽입 (사진 리뷰에 맞춰 5개)
INSERT INTO review_image (image_id, review_id, image_url, sort_order) VALUES
                                                                          (1, 501, 'http://example.com/review_images/speaker_photo1.jpg', 1),
                                                                          (2, 502, 'http://example.com/review_images/fridge_photo1.jpg', 1),
                                                                          (3, 504, 'http://example.com/review_images/tshirt_photo1.jpg', 1),
                                                                          (4, 506, 'http://example.com/review_images/fridge_photo2.jpg', 1),
                                                                          (5, 508, 'http://example.com/review_images/tshirt_photo2.jpg', 1),
                                                                          (6, 510, 'http://example.com/review_images/fridge_photo3.jpg', 1);
