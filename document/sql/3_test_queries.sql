-- ========================================
-- 테스트용 확인 쿼리들
-- ========================================

-- 1. USER_ROLE 테이블 확인
SELECT * FROM USER_ROLE ORDER BY user_role_id;

-- 2. 전체 사용자 확인 (역할 포함)
SELECT 
    u.user_id,
    u.user_name,
    u.user_email,
    u.user_nickname,
    ur.user_role_name,
    ur.role_description,
    u.social_provider,
    u.user_created_at
FROM USER u
LEFT JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id
ORDER BY ur.user_role_id DESC, u.user_created_at;

-- 3. 관리자만 조회 (ADMIN + SUPER_ADMIN)
SELECT 
    u.user_id,
    u.user_name,
    u.user_email,
    ur.user_role_name,
    u.user_phone
FROM USER u
JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id
WHERE ur.user_role_name IN ('ADMIN', 'SUPER_ADMIN')
ORDER BY ur.user_role_name DESC, u.user_name;

-- 4. 일반 사용자만 조회
SELECT 
    u.user_id,
    u.user_name,
    u.user_email,
    u.user_nickname,
    u.social_provider,
    u.user_phone
FROM USER u
JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id
WHERE ur.user_role_name = 'USER'
ORDER BY u.user_name;

-- 5. 소셜 로그인 사용자 확인
SELECT 
    u.user_name,
    u.user_email,
    u.social_provider,
    u.social_id,
    ur.user_role_name
FROM USER u
JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id
WHERE u.social_provider IS NOT NULL
ORDER BY u.social_provider;

-- 6. 역할별 사용자 수 통계
SELECT 
    ur.user_role_name,
    ur.role_description,
    COUNT(u.user_id) as user_count
FROM USER_ROLE ur
LEFT JOIN USER u ON ur.user_role_id = u.user_role_id
GROUP BY ur.user_role_id, ur.user_role_name, ur.role_description
ORDER BY ur.user_role_id DESC;

-- 7. 관리자 시스템 로그인 테스트용 계정 정보
SELECT 
    '=== 관리자 시스템 로그인 테스트 계정 ===' as info
UNION ALL
SELECT CONCAT('최고관리자: ', user_email, ' / password123!') 
FROM USER u JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id 
WHERE ur.user_role_name = 'SUPER_ADMIN'
UNION ALL  
SELECT CONCAT('일반관리자: ', user_email, ' / password123!')
FROM USER u JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id 
WHERE ur.user_role_name = 'ADMIN'
UNION ALL
SELECT '=== 일반 사용자 테스트 계정 ==='
UNION ALL
SELECT CONCAT('일반사용자: ', user_email, ' / password123!')
FROM USER u JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id 
WHERE ur.user_role_name = 'USER' AND u.social_provider IS NULL
LIMIT 20;
