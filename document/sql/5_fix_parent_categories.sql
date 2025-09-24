-- 추가 2차 카테고리 데이터 삽입
INSERT IGNORE INTO product_category (category_id, category_name, category_type, level, display_order, category_created_at, parent_category_id) VALUES
(111, '2인용 소파', 'PD_001', 2, 1, NOW(), 100), -- 소파 하위
(112, '3인용 소파', 'PD_001', 2, 2, NOW(), 100), -- 소파 하위
(113, '싱글 침대', 'PD_001', 2, 1, NOW(), 101), -- 침대 하위
(114, '더블 침대', 'PD_001', 2, 2, NOW(), 101), -- 침대 하위
(115, '컴퓨터 책상', 'PD_001', 2, 1, NOW(), 102), -- 책상 하위
(116, '학습 책상', 'PD_001', 2, 2, NOW(), 102), -- 책상 하위
(117, '게이밍 의자', 'PD_001', 2, 2, NOW(), 103), -- 의자 하위
(118, '사무용 의자', 'PD_001', 2, 3, NOW(), 103); -- 의자 하위