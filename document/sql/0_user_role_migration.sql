-- ========================================
-- User 엔티티 통합을 위한 마이그레이션
-- ========================================
use cos;
-- 1. USER_ROLE 테이블 생성
CREATE TABLE `USER_ROLE` (
    `user_role_id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `user_role_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '역할명 (USER, ADMIN, SUPER_ADMIN)',
    `role_description` VARCHAR(200) COMMENT '역할 설명',
    `role_created_date` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '생성일시',
    `role_updated_date` TIMESTAMP DEFAULT NULL COMMENT '수정일시'
);

-- 2. USER 테이블에 user_role_id 컬럼 추가
ALTER TABLE `user` ADD COLUMN `user_role_id` BIGINT NOT NULL DEFAULT 1 COMMENT '사용자 역할 ID' AFTER `user_id`;

-- 3. USER_ROLE 테이블에 외래키 제약조건 추가
ALTER TABLE `user` ADD FOREIGN KEY (`user_role_id`) REFERENCES `USER_ROLE` (`user_role_id`);

-- 4. USER_ROLE 테이블에 인덱스 추가
CREATE INDEX `idx_user_role_name` ON `USER_ROLE` (`user_role_name`);
CREATE INDEX `idx_user_user_role` ON `USER` (`user_role_id`);

-- ========================================
-- 테이블 코멘트 추가
-- ========================================
ALTER TABLE `USER_ROLE` COMMENT = '사용자 역할 테이블 (USER, ADMIN, SUPER_ADMIN)';
