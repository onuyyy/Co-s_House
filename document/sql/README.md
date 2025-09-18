# SQL 실행 순서

User 엔티티 기반 관리 시스템을 위한 데이터베이스 설정 순서입니다.

## 1. 필수 전제조건
- MySQL 8.0 이상
- cos 데이터베이스 생성 완료

## 2. 실행 순서

### Step 1: 기본 테이블 생성
```sql
source document/sql/1_table_insert.sql;
```

### Step 2: User Role 마이그레이션 (중요!)
```sql
source document/sql/0_user_role_migration.sql;
```

### Step 3: 기본 데이터 삽입
```sql
source document/sql/2_basic_data_insert.sql;
```

### Step 4: 데이터 확인 (선택사항)
```sql
source document/sql/3_test_queries.sql;
```

## 3. 실행 결과 확인

### USER_ROLE 테이블
```sql
SELECT * FROM USER_ROLE;
```
예상 결과:
| user_role_id | user_role_name | role_description |
|--------------|----------------|------------------|
| 1 | USER | 일반 사용자 - 상품 구매 및 기본 서비스 이용 |
| 2 | ADMIN | 관리자 - 사용자/상품/주문 관리 권한 |
| 3 | SUPER_ADMIN | 최고 관리자 - 모든 시스템 관리 권한 |

### 사용자 데이터 확인
```sql
SELECT u.user_name, u.user_email, ur.user_role_name 
FROM USER u 
JOIN USER_ROLE ur ON u.user_role_id = ur.user_role_id 
ORDER BY ur.user_role_id DESC;
```

## 4. 테스트 계정

### 관리자 계정 (관