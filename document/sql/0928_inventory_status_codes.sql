-- ========================================
-- 재고 관리 상태 공통 코드
-- ========================================

-- 공통 코드 그룹 삽입
INSERT INTO common_code_group (group_id, group_name, description) VALUES
('RECEIPT_STATUS', '입고 상태', '재고 입고 처리 상태를 나타내는 코드'),
('OUTBOUND_STATUS', '출고 상태', '재고 출고 처리 상태를 나타내는 코드');

-- 입고 상태 코드 삽입
INSERT INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
('PENDING', 'RECEIPT_STATUS', '대기', '입고 예정 상태', 1, true),
('COMPLETED', 'RECEIPT_STATUS', '완료', '입고 처리 완료', 2, true),
('CANCELLED', 'RECEIPT_STATUS', '취소', '입고 처리 취소', 3, true);

-- 출고 상태 코드 삽입
INSERT INTO common_code (code_id, group_id, code_name, description, sort_order, is_active) VALUES
('PENDING', 'OUTBOUND_STATUS', '대기', '출고 예정 상태', 1, true),
('COMPLETED', 'OUTBOUND_STATUS', '완료', '출고 처리 완료', 2, true),
('CANCELLED', 'OUTBOUND_STATUS', '취소', '출고 처리 취소', 3, true),
('SHIPPED', 'OUTBOUND_STATUS', '배송중', '출고 후 배송 진행중', 4, true);

-- 확인용 쿼리
/*
SELECT
    ccg.group_id,
    ccg.group_name,
    cc.code_id,
    cc.code_name,
    cc.description,
    cc.sort_order
FROM common_code_group ccg
JOIN common_code cc ON ccg.group_id = cc.group_id
WHERE ccg.group_id IN ('RECEIPT_STATUS', 'OUTBOUND_STATUS')
ORDER BY ccg.group_id, cc.sort_order;
*/