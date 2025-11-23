# Co's House (COS) 🏪

> Spring Boot 기반의 현대적인 E-Commerce 플랫폼

Co's House는 '오늘의 집'을 벤치마킹 한 E-Commerce 플랫폼입니다. 

<img width="595" height="569" alt="image" src="https://github.com/user-attachments/assets/578fd96b-5474-466b-aa7b-7a1b7b52e900" />


## 📋 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [보안](#보안)
- [모니터링](#모니터링)
- [데이터베이스](#데이터베이스)

---

## 🎯 주요 기능

### 사용자 관리
- **다중 인증 방식**
  - 로컬 회원가입 (이메일/비밀번호)
  - OAuth2 소셜 로그인 (카카오, 네이버)
  - 이메일 인증 시스템
- **권한 기반 접근 제어** (USER, ADMIN, SUPER_ADMIN)
- **세션 기반 인증** (Spring Security)

### 상품 및 주문 관리
- 다중 옵션 상품 지원
- 장바구니 시스템
- 주문 처리 및 배송 추적
- 쿠폰 및 할인 시스템
- 재고 관리 및 입고 이력

### 결제
- **Toss Payments API 통합**
- 결제 성공/실패 처리
- 환불 처리 시스템

### 커뮤니티
- 게시글 작성 및 카테고리 관리
- 좋아요, 스크랩, 댓글 기능
- 사용자 생성 콘텐츠 관리

### 관리자 기능
- 상품 등록 및 관리
- 재고 조회 및 관리
- 주문 처리 및 상태 관리
- 사용자 활동 로깅 (AOP)

---

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **ORM**: Spring Data JPA, Hibernate
- **Query DSL**: Querydsl 5.0.0
- **Security**: Spring Security 6 (Session-based)
- **Template Engine**: Thymeleaf
- **Validation**: Spring Validation

### Database
- **Production**: MySQL 8.4
- **Test**: H2 (in-memory)
- **Connection Pool**: HikariCP

### Infrastructure
- **Containerization**: Docker Compose
- **Monitoring**: Prometheus, Grafana
- **Metrics**: Micrometer, Spring Boot Actuator

### External APIs
- **Payment**: Toss Payments
- **Email**: Naver SMTP
- **OAuth2**: Kakao, Naver

### Build & Tools
- **Build Tool**: Gradle 8.x
- **Code Generation**: Lombok, Querydsl APT
- **Version Control**: Git

---

## 🚀 시작하기

### 요구사항

- **Java**: 21 이상
- **Docker**: 최신 버전 (Docker Compose 지원)
- **MySQL**: 8.4 (Docker로 자동 실행)
- **Gradle**: 8.x (Wrapper 포함)

### 설치 및 실행

#### 1. 저장소 클론
```bash
git clone <repository-url>
cd cos
```

#### 2. Docker Compose로 인프라 실행
```bash
# MySQL, Prometheus, Grafana 컨테이너 시작
docker-compose up -d

# 컨테이너 상태 확인
docker ps
```

예상 출력:
```
CONTAINER ID   IMAGE               PORTS                      NAMES
...            mysql:8.4           0.0.0.0:13306->3306/tcp   cos-mysql
...            prom/prometheus     0.0.0.0:9090->9090/tcp    cos-prometheus
...            grafana/grafana     0.0.0.0:3000->3000/tcp    cos-grafana
```

#### 3. 데이터베이스 초기화
```bash
# MySQL 접속
mysql -h 127.0.0.1 -P 13306 -u root -proot cos

# SQL 스크립트 실행 (순서 중요!)
source document/sql/0_user_role_migration.sql;    # 역할 마이그레이션 (필수)
source document/sql/2_basic_data_insert.sql;      # 기본 데이터 삽입
source document/sql/3_test_queries.sql;           # 테스트 쿼리 (선택)
```

#### 4. 애플리케이션 실행
```bash
# 빌드 및 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
./gradlew bootJar
java -jar build/libs/cos-0.0.1-SNAPSHOT.jar
```

#### 5. 접속 확인
- **애플리케이션**: http://localhost:8080
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

---

## 📁 프로젝트 구조

```
cos/
├── src/main/java/com/bird/cos/
│   ├── domain/              # JPA 엔티티 (도메인 별 구조화)
│   │   ├── user/           # 사용자 관리
│   │   ├── order/          # 주문 관리
│   │   ├── product/        # 상품 관리
│   │   ├── cart/           # 장바구니
│   │   ├── payment/        # 결제
│   │   ├── inventory/      # 재고
│   │   └── post/           # 게시판
│   ├── dto/                # 요청/응답 DTO
│   │   ├── order/
│   │   ├── product/
│   │   ├── auth/
│   │   └── admin/
│   ├── repository/         # Spring Data JPA 리포지토리
│   │   ├── *Repository.java
│   │   ├── *RepositoryCustom.java      # Querydsl 커스텀 인터페이스
│   │   └── *RepositoryCustomImpl.java  # Querydsl 구현체
│   ├── service/            # 비즈니스 로직
│   ├── controller/         # MVC 컨트롤러
│   ├── config/             # Spring 설정
│   │   ├── SecurityConfig.java
│   │   ├── QuerydslConfig.java
│   │   └── TossPaymentsProperties.java
│   ├── security/           # 보안 컴포넌트
│   │   ├── CustomUserDetails.java
│   │   ├── OAuth2LoginSuccessHandler.java
│   │   └── RegisterSecurityFilter.java
│   ├── exception/          # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   └── ErrorCode.java
│   └── aop/                # AOP (로깅 등)
│       └── LoggingAspect.java
├── src/main/resources/
│   ├── application.yml     # 애플리케이션 설정
│   ├── templates/          # Thymeleaf 템플릿
│   │   ├── layout/
│   │   ├── fragments/
│   │   ├── order/
│   │   ├── admin/
│   │   └── home/
│   └── static/             # 정적 리소스 (CSS, JS, 이미지)
├── document/
│   └── sql/                # SQL 초기화 스크립트
├── docker-compose.yml      # Docker 컨테이너 설정
├── prometheus.yml          # Prometheus 설정
├── build.gradle            # Gradle 빌드 설정
├── CLAUDE.md               # 개발자 가이드 (Claude Code용)
└── MONITORING.md           # 모니터링 가이드
```

### Querydsl 패턴

복잡한 쿼리는 Querydsl로 작성합니다:

```java
// 1. Custom 인터페이스 정의
public interface OrderRepositoryCustom {
    List<Order> findOrdersWithDetails(Long userId);
}

// 2. CustomImpl 구현체 작성
@Repository
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    // Querydsl Q-class 사용
    @Override
    public List<Order> findOrdersWithDetails(Long userId) {
        return queryFactory
            .selectFrom(QOrder.order)
            .join(QOrder.order.user).fetchJoin()
            .where(QOrder.order.user.id.eq(userId))
            .fetch();
    }
}

// 3. Repository가 양쪽 모두 확장
public interface OrderRepository extends
    JpaRepository<Order, Long>, OrderRepositoryCustom {
}
```

Q-classes는 `build/generated/querydsl/`에 자동 생성됩니다.

---

## 🔒 보안

### 인증 아키텍처

- **세션 기반 인증** (JWT 아님)
- **Form Login 비활성화**, 커스텀 로그인: `/controller/register/login`
- **OAuth2 지원**: 카카오, 네이버
- **RegisterSecurityFilter**: Origin/Referer 검증, Rate Limiting

### 권한 관리

```java
// 역할 기반 접근 제어
USER          // 일반 사용자
ADMIN         // 관리자
SUPER_ADMIN   // 슈퍼 관리자

// 관리자 엔드포인트 자동 보호
/api/admin/** → admin_role 필요
```

### 메서드 레벨 보안

```java
@PreAuthorize("hasAuthority('admin_role')")
public void adminMethod() { ... }
```

### 예외 처리

모든 인증/인가 실패는 `GlobalExceptionHandler`에서 처리:

```json
{
  "timestamp": "2025-10-17T12:00:00",
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다",
  "path": "/api/admin/products"
}
```

### 보안 설정

```yaml
# application.yml
spring:
  security:
    # 개발 환경: HTTP Basic 활성화
    # 프로덕션: HTTP Basic 비활성화

  session:
    policy: IF_REQUIRED
    fixation: newSession
    max-sessions: 1  # 사용자당 최대 1개 세션
```

⚠️ **주의**: `application.yml`에 테스트/개발용 크레덴셜이 포함되어 있습니다. 프로덕션 환경에서는 환경 변수나 Spring Cloud Config를 사용하세요.

---

## 📊 모니터링

### 구성 요소

- **Spring Boot Actuator**: 애플리케이션 메트릭 수집
- **Prometheus**: 메트릭 저장 및 쿼리
- **Grafana**: 시각화 대시보드

### 주요 메트릭

| 카테고리 | 메트릭 | 설명 |
|---------|--------|------|
| **HTTP** | `http_server_requests_seconds` | 요청 응답 시간 및 처리량 |
| **JVM** | `jvm_memory_used_bytes` | JVM 메모리 사용량 |
| **JVM** | `jvm_gc_pause_seconds` | GC 정지 시간 |
| **시스템** | `system_cpu_usage` | CPU 사용률 |
| **DB** | `hikaricp_connections_active` | 활성 DB 커넥션 수 |

### 접속 URL

- **Actuator**: http://localhost:8080/actuator
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Prometheus UI**: http://localhost:9090
- **Grafana**: http://localhost:3000

### Grafana 대시보드 설정

```bash
# 1. Grafana 접속 (admin/admin)
# 2. Data Source 추가: Prometheus (http://prometheus:9090)
# 3. 대시보드 Import: ID 4701 (JVM Micrometer)
# 4. Application 이름: cos-house
```

상세한 모니터링 가이드는 [MONITORING.md](MONITORING.md)를 참고하세요.

---

## 🗄 데이터베이스

### 연결 정보

```yaml
Host: localhost
Port: 13306  # 표준 3306이 아닌 13306 사용
Database: cos
Username: root
Password: root
JDBC URL: jdbc:mysql://localhost:13306/cos
```

### Docker Compose 관리

Spring Boot Docker Compose 지원으로 자동 관리:
- 애플리케이션 시작 시 자동으로 컨테이너 시작
- `lifecycle-management: start-and-stop`

```bash
# 수동 관리
docker-compose up -d     # 시작
docker-compose stop      # 중지
docker-compose down      # 중지 및 제거
docker-compose down -v   # 볼륨까지 제거
```

### JPA 설정

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 엔티티 변경 시 자동 스키마 업데이트
    show-sql: true      # SQL 쿼리 로그 출력
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

⚠️ **주의**: 프로덕션에서는 `ddl-auto: validate` 사용 권장

---

## 📝 주요 설정

### 파일 업로드

```yaml
file:
  upload-dir: /Users/a/IdeaProjects/Co-s_House/uploads/
```

⚠️ **환경에 맞게 경로 수정 필요**

이미지는 `/images/uploaded/**` 경로로 접근 가능합니다.

### 이메일 설정

Naver SMTP 사용:
```yaml
spring:
  mail:
    host: smtp.naver.com
    port: 465
    username: your-email@naver.com
    password: your-password
```

### OAuth2 클라이언트

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: your-kakao-client-id
          naver:
            client-id: your-naver-client-id
```

### Toss Payments

```yaml
toss:
  payments:
    client-key: test_ck_...
    secret-key: test_sk_...
    success-url: http://localhost:8080/payment/success
    fail-url: http://localhost:8080/payment/fail
```

---

## 📚 추가 문서

- [CLAUDE.md](CLAUDE.md) - 개발자를 위한 상세 가이드
- [MONITORING.md](MONITORING.md) - 모니터링 시스템 설정 및 사용법
- [HELP.md](HELP.md) - Spring Boot 도움말

---

