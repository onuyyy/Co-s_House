-- ========================================
-- 상품 옵션 데이터 삽입 SQL
-- product_option 테이블을 위한 기본 데이터
-- ========================================

-- ========================================
-- 1. common_code_group에 상품 옵션 관련 그룹 추가
-- ========================================
INSERT IGNORE INTO common_code_group (group_id, group_name, description) VALUES
('PRODUCT_OPTION_TYPE', '상품 옵션 타입', '상품의 다양한 옵션 유형을 구분하는 코드'),
('FURNITURE_SIZE', '가구 크기', '가구 상품의 크기 옵션'),
('FURNITURE_COLOR', '가구 색상', '가구 상품의 색상 옵션'),
('FURNITURE_MATERIAL', '가구 소재', '가구 상품의 소재/재질 옵션');

-- ========================================
-- 2. common_code에 상품 옵션 타입 코드 추가
-- ========================================
INSERT IGNORE INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
-- 상품 옵션 타입 코드
('OPT_TYPE_001', 'PRODUCT_OPTION_TYPE', 'COLOR', '색상 옵션', 1, TRUE),
('OPT_TYPE_002', 'PRODUCT_OPTION_TYPE', 'SIZE', '크기 옵션', 2, TRUE),
('OPT_TYPE_003', 'PRODUCT_OPTION_TYPE', 'MATERIAL', '소재 옵션', 3, TRUE),
('OPT_TYPE_004', 'PRODUCT_OPTION_TYPE', 'STYLE', '스타일 옵션', 4, TRUE),
('OPT_TYPE_005', 'PRODUCT_OPTION_TYPE', 'FUNCTION', '기능 옵션', 5, TRUE),

-- 가구 크기 옵션
('SIZE_001', 'FURNITURE_SIZE', '1인용', '1인용 크기', 1, TRUE),
('SIZE_002', 'FURNITURE_SIZE', '2인용', '2인용 크기', 2, TRUE),
('SIZE_003', 'FURNITURE_SIZE', '3인용', '3인용 크기', 3, TRUE),
('SIZE_004', 'FURNITURE_SIZE', '4인용', '4인용 크기', 4, TRUE),
('SIZE_005', 'FURNITURE_SIZE', '킹사이즈', '킹사이즈 크기', 5, TRUE),
('SIZE_006', 'FURNITURE_SIZE', '퀸사이즈', '퀸사이즈 크기', 6, TRUE),
('SIZE_007', 'FURNITURE_SIZE', '슈퍼싱글', '슈퍼싱글 크기', 7, TRUE),
('SIZE_008', 'FURNITURE_SIZE', '싱글', '싱글 크기', 8, TRUE),

-- 가구 색상 옵션
('COLOR_001', 'FURNITURE_COLOR', '화이트', '화이트 색상', 1, TRUE),
('COLOR_002', 'FURNITURE_COLOR', '블랙', '블랙 색상', 2, TRUE),
('COLOR_003', 'FURNITURE_COLOR', '브라운', '브라운 색상', 3, TRUE),
('COLOR_004', 'FURNITURE_COLOR', '베이지', '베이지 색상', 4, TRUE),
('COLOR_005', 'FURNITURE_COLOR', '그레이', '그레이 색상', 5, TRUE),
('COLOR_006', 'FURNITURE_COLOR', '네이비', '네이비 색상', 6, TRUE),
('COLOR_007', 'FURNITURE_COLOR', '아이보리', '아이보리 색상', 7, TRUE),
('COLOR_008', 'FURNITURE_COLOR', '월넛', '월넛 색상', 8, TRUE),
('COLOR_009', 'FURNITURE_COLOR', '오크', '오크 색상', 9, TRUE),
('COLOR_010', 'FURNITURE_COLOR', '체리', '체리 색상', 10, TRUE),

-- 가구 소재 옵션
('MATERIAL_001', 'FURNITURE_MATERIAL', '천연가죽', '천연 가죽 소재', 1, TRUE),
('MATERIAL_002', 'FURNITURE_MATERIAL', '인조가죽', '인조 가죽 소재', 2, TRUE),
('MATERIAL_003', 'FURNITURE_MATERIAL', '패브릭', '패브릭 소재', 3, TRUE),
('MATERIAL_004', 'FURNITURE_MATERIAL', '린넨', '린넨 소재', 4, TRUE),
('MATERIAL_005', 'FURNITURE_MATERIAL', '벨벳', '벨벳 소재', 5, TRUE),
('MATERIAL_006', 'FURNITURE_MATERIAL', '마이크로파이버', '마이크로파이버 소재', 6, TRUE),
('MATERIAL_007', 'FURNITURE_MATERIAL', '솔리드우드', '원목 소재', 7, TRUE),
('MATERIAL_008', 'FURNITURE_MATERIAL', 'MDF', 'MDF 소재', 8, TRUE),
('MATERIAL_009', 'FURNITURE_MATERIAL', '파티클보드', '파티클보드 소재', 9, TRUE),
('MATERIAL_010', 'FURNITURE_MATERIAL', '스틸', '스틸 소재', 10, TRUE);

-- ========================================
-- 3. product_option 테이블에 상품 옵션 데이터 삽입
-- 주의: 먼저 0922_products_inserts.sql이 실행되어야 합니다.
-- ========================================

-- 상품 1: 클렙튼 1인용 리클라이너 소파 전동 K05M (product_id = 1)
INSERT INTO product_option (product_id, option_type, option_name, option_value, additional_price, sort_order) VALUES
-- 색상 옵션
(1, 'OPT_TYPE_001', '색상', '블랙', 0.00, 1),
(1, 'OPT_TYPE_001', '색상', '브라운', 10000.00, 2),
(1, 'OPT_TYPE_001', '색상', '그레이', 5000.00, 3),

-- 기능 옵션
(1, 'OPT_TYPE_005', '마사지 기능', '기본형', 0.00, 1),
(1, 'OPT_TYPE_005', '마사지 기능', '프리미엄형', 150000.00, 2),

-- 소재 옵션
(1, 'OPT_TYPE_003', '소재', '인조가죽', 0.00, 1),
(1, 'OPT_TYPE_003', '소재', '천연가죽', 80000.00, 2);

-- 상품 2: 보니애가구 시에나 데이비드 모션 슬라이딩 리클라이너 패브릭 소파 (product_id = 2)
INSERT INTO product_option (product_id, option_type, option_name, option_value, additional_price, sort_order) VALUES
-- 색상 옵션
(2, 'OPT_TYPE_001', '색상', '아이보리', 0.00, 1),
(2, 'OPT_TYPE_001', '색상', '베이지', 20000.00, 2),
(2, 'OPT_TYPE_001', '색상', '그레이', 30000.00, 3),

-- 크기 옵션 (4인용 기본, 다른 크기 옵션)
(2, 'OPT_TYPE_002', '크기', '3인용', -100000.00, 1),
(2, 'OPT_TYPE_002', '크기', '4인용', 0.00, 2),

-- 스툴 옵션
(2, 'OPT_TYPE_005', '스툴', '스툴 미포함', 0.00, 1),
(2, 'OPT_TYPE_005', '스툴', '스툴 포함', 150000.00, 2);

-- 상품 3: 에보니아 이오 1인용 전동 리클라이너 컵홀더형 (product_id = 3)
INSERT INTO product_option (product_id, option_type, option_name, option_value, additional_price, sort_order) VALUES
-- 색상 옵션
(3, 'OPT_TYPE_001', '색상', '블랙', 0.00, 1),
(3, 'OPT_TYPE_001', '색상', '브라운', 15000.00, 2),
(3, 'OPT_TYPE_001', '색상', '화이트', 10000.00, 3),

-- 컵홀더 옵션
(3, 'OPT_TYPE_005', '컵홀더', '좌측', 0.00, 1),
(3, 'OPT_TYPE_005', '컵홀더', '우측', 0.00, 2),
(3, 'OPT_TYPE_005', '컵홀더', '양측', 25000.00, 3);

-- 상품 4: 헤이미쉬홈 몽드 4인용 아쿠아텍스 이지클린 조야 패브릭 스윙 소파 (product_id = 4)
INSERT INTO product_option (product_id, option_type, option_name, option_value, additional_price, sort_order) VALUES
-- 색상 옵션
(4, 'OPT_TYPE_001', '색상', '베이지', 0.00, 1),
(4, 'OPT_TYPE_001', '색상', '그레이', 20000.00, 2),
(4, 'OPT_TYPE_001', '색상', '네이비', 30000.00, 3),

-- 크기 옵션
(4, 'OPT_TYPE_002', '크기', '3인용', -80000.00, 1),
(4, 'OPT_TYPE_002', '크기', '4인용', 0.00, 2),

-- 패브릭 옵션
(4, 'OPT_TYPE_003', '패브릭', '일반 패브릭', 0.00, 1),
(4, 'OPT_TYPE_003', '패브릭', '아쿠아텍스 이지클린', 50000.00, 2);

-- 상품 5-10: 추가 샘플 옵션들
INSERT INTO product_option (product_id, option_type, option_name, option_value, additional_price, sort_order) VALUES
-- 상품 5 옵션
(5, 'OPT_TYPE_001', '색상', '화이트', 0.00, 1),
(5, 'OPT_TYPE_001', '색상', '블랙', 10000.00, 2),
(5, 'OPT_TYPE_002', '크기', '2인용', -50000.00, 1),
(5, 'OPT_TYPE_002', '크기', '3인용', 0.00, 2),

-- 상품 6 옵션
(6, 'OPT_TYPE_001', '색상', '오크', 0.00, 1),
(6, 'OPT_TYPE_001', '색상', '월넛', 20000.00, 2),
(6, 'OPT_TYPE_003', '소재', 'MDF', 0.00, 1),
(6, 'OPT_TYPE_003', '소재', '솔리드우드', 100000.00, 2),

-- 상품 7 옵션
(7, 'OPT_TYPE_001', '색상', '베이지', 0.00, 1),
(7, 'OPT_TYPE_001', '색상', '그레이', 5000.00, 2),
(7, 'OPT_TYPE_002', '크기', '싱글', 0.00, 1),
(7, 'OPT_TYPE_002', '크기', '슈퍼싱글', 50000.00, 2),
(7, 'OPT_TYPE_002', '크기', '퀸사이즈', 100000.00, 3),

-- 상품 8 옵션
(8, 'OPT_TYPE_001', '색상', '화이트', 0.00, 1),
(8, 'OPT_TYPE_001', '색상', '블랙', 15000.00, 2),
(8, 'OPT_TYPE_003', '소재', '스틸', 0.00, 1),
(8, 'OPT_TYPE_003', '소재', '솔리드우드', 80000.00, 2),

-- 상품 9 옵션
(9, 'OPT_TYPE_001', '색상', '브라운', 0.00, 1),
(9, 'OPT_TYPE_001', '색상', '체리', 25000.00, 2),
(9, 'OPT_TYPE_004', '스타일', '클래식', 0.00, 1),
(9, 'OPT_TYPE_004', '스타일', '모던', 30000.00, 2),

-- 상품 10 옵션
(10, 'OPT_TYPE_001', '색상', '그레이', 0.00, 1),
(10, 'OPT_TYPE_001', '색상', '네이비', 20000.00, 2),
(10, 'OPT_TYPE_003', '소재', '패브릭', 0.00, 1),
(10, 'OPT_TYPE_003', '소재', '벨벳', 60000.00, 2),
(10, 'OPT_TYPE_005', '기능', '기본형', 0.00, 1),
(10, 'OPT_TYPE_005', '기능', '수납형', 40000.00, 2);

-- ========================================
-- 주의사항
-- ========================================
-- product_id 11 이상은 0922_products_inserts.sql에 실제 데이터가 있는지 확인 후 사용하세요.
-- 현재는 product_id 1-10까지만 안전하게 옵션을 추가합니다.
-- 필요시 실제 존재하는 product_id에 맞춰 수정하세요.

-- ========================================
-- 데이터 확인용 쿼리 (실행하지 않음, 참고용)
-- ========================================
/*
-- 상품별 옵션 확인 쿼리
SELECT
    p.product_id,
    p.product_title,
    po.option_name,
    po.option_value,
    po.additional_price,
    cc.code_name as option_type_name
FROM product p
JOIN product_option po ON p.product_id = po.product_id
JOIN common_code cc ON po.option_type = cc.code_id
ORDER BY p.product_id, po.option_name, po.sort_order;

-- 옵션 타입별 통계
SELECT
    cc.code_name as option_type,
    COUNT(*) as option_count
FROM product_option po
JOIN common_code cc ON po.option_type = cc.code_id
GROUP BY cc.code_name
ORDER BY option_count DESC;
*/