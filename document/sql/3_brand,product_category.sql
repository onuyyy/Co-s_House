
-- Disable foreign key checks for faster insertion
SET FOREIGN_KEY_CHECKS = 0;

INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (100, '삼성', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (101, 'LG', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (102, '이케아', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (103, '한샘', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (104, '일룸', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (105, '시디즈', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (106, '허먼밀러', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (107, '무인양품', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (108, '자라홈', 'test.image', 'test description');
INSERT IGNORE INTO brand (brand_id, brand_name, logo_url, brand_description) VALUES (109, 'H&M홈', 'test.image', 'test description');

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
