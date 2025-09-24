
-- Disable foreign key checks for faster insertion
SET FOREIGN_KEY_CHECKS = 0;


-- Brand data (schema matched)
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (109, 'H&M홈', 'https://example.com/logo/109.png', 'H&M홈 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (110, 'LG', 'https://example.com/logo/110.png', 'LG 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (111, '까사미아', 'https://example.com/logo/111.png', '까사미아 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (112, '동서가구', 'https://example.com/logo/112.png', '동서가구 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (113, '레이', 'https://example.com/logo/113.png', '레이 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (114, '리바트', 'https://example.com/logo/114.png', '리바트 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (115, '무인양품', 'https://example.com/logo/115.png', '무인양품 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (116, '보니애가구', 'https://example.com/logo/116.png', '보니애가구 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (117, '삼성', 'https://example.com/logo/117.png', '삼성 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (118, '스스디', 'https://example.com/logo/118.png', '스스디 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (119, '시디즈', 'https://example.com/logo/119.png', '시디즈 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (120, '썸앤데코', 'https://example.com/logo/120.png', '썸앤데코 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (121, '에보니아', 'https://example.com/logo/121.png', '에보니아 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (122, '웰퍼니쳐', 'https://example.com/logo/122.png', '웰퍼니쳐 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (123, '이케아', 'https://example.com/logo/123.png', '이케아 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (124, '일룸', 'https://example.com/logo/124.png', '일룸 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (125, '자라홈', 'https://example.com/logo/125.png', '자라홈 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (126, '지아가구', 'https://example.com/logo/126.png', '지아가구 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (127, '클렙튼', 'https://example.com/logo/127.png', '클렙튼 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (128, '필립스', 'https://example.com/logo/128.png', '필립스 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (129, '한샘', 'https://example.com/logo/129.png', '한샘 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (130, '허먼밀러', 'https://example.com/logo/130.png', '허먼밀러 브랜드 설명');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (131, '헤이미쉬홈', 'https://example.com/logo/131.png', '헤이미쉬홈 브랜드 설명');

-- Product category data (schema matched)

INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (100, '소파', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (101, '침대', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (102, '책상', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (103, '의자', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (104, '조명', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (105, '수납장', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (106, '식탁', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (107, '매트리스', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (108, '커튼', 'PD_001', 1, 1, NOW());
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at) VALUES (109, '러그', 'PD_001', 1, 1, NOW());
