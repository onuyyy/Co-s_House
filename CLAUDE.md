# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.5 e-commerce application (Co's House) built with Java 21, using JPA/Hibernate for persistence, Querydsl for complex queries, Spring Security for authentication/authorization, and Thymeleaf for server-side rendering. The application supports both local registration and OAuth2 social login (Kakao, Naver), integrated payment processing via Toss Payments, and email verification.

## Build Commands

### Build & Run
```bash
./gradlew bootRun                    # Run application (starts on port 8080)
./gradlew build                      # Full build with tests
./gradlew bootJar                    # Create executable JAR
./gradlew clean build                # Clean build from scratch
```

### Testing
```bash
./gradlew test                       # Run all tests
./gradlew test --tests CartServiceTest  # Run specific test class
./gradlew test --tests CartServiceTest.testMethodName  # Run specific test method
./gradlew check                      # Run all verification tasks
```

### Development
```bash
./gradlew clean                      # Clean build directory
./gradlew classes                    # Compile main classes only
```

### Database
MySQL container is automatically managed via Spring Boot Docker Compose support:
- Port: 13306 (mapped from container's 3306)
- Database: cos
- Root credentials: root/root
- Application connects via jdbc:mysql://localhost:13306/cos

SQL initialization scripts in `document/sql/`:
```sql
source document/sql/1_table_insert.sql;           # Create tables
source document/sql/0_user_role_migration.sql;    # Migrate user roles (IMPORTANT)
source document/sql/2_basic_data_insert.sql;      # Insert seed data
source document/sql/3_test_queries.sql;           # Test queries (optional)
```

## Architecture

### Package Structure
- **domain/**: JPA entities organized by business domain (user, order, product, cart, payment, inventory, post, etc.)
- **dto/**: Request/Response DTOs organized by feature area, mirroring controller structure
- **repository/**: Spring Data JPA repositories with Querydsl custom implementations (*Custom.java + *CustomImpl.java pattern)
- **service/**: Business logic layer organized by domain
- **controller/**: MVC controllers handling web requests and returning Thymeleaf views or JSON
- **config/**: Spring configuration classes (SecurityConfig, QuerydslConfig, WebMvcConfig, TossPaymentsProperties)
- **security/**: Spring Security components (CustomUserDetails, OAuth2 handlers, custom filters)
- **exception/**: Centralized exception handling (GlobalExceptionHandler with ErrorResponse, ErrorCode enum)
- **aop/**: Aspect-oriented programming (LoggingAspect for user activity tracking)

### Key Architectural Patterns

**Security Architecture:**
- Session-based authentication (not JWT)
- Form login disabled, custom login flow via `/controller/register/login`
- OAuth2 login supported (Kakao, Naver) with `SocialOAuth2UserService` and `OAuth2LoginSuccessHandler`
- RegisterSecurityFilter protects registration endpoint with additional security (Origin/Referer checks, rate limiting)
- Role-based authorization: User (USER), Admin (ADMIN), Super Admin (SUPER_ADMIN)
- Admin endpoints (`/api/admin/**`) require `admin_role` authority
- Method-level security enabled (@PreAuthorize, @Secured, @RolesAllowed)
- Custom authentication/authorization exception handling via ProblemDetails* handlers (returns JSON)

**Repository Pattern:**
- Standard repositories extend `JpaRepository<Entity, Long>`
- Complex queries use Querydsl: define `*RepositoryCustom` interface, implement in `*RepositoryCustomImpl`
- Repository extends both JpaRepository AND Custom interface: `interface OrderRepository extends JpaRepository<Order,Long>, OrderRepositoryCustom`
- QuerydslConfig provides JPAQueryFactory bean
- Querydsl Q-classes are generated in `build/generated/querydsl/` via annotation processing

**Entity Design:**
- Entities use Lombok (@Getter, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- User entity has no base entity with timestamps; uses direct @Column definitions with `insertable=false, updatable=false`
- Entity relationships: Order → OrderItems → Product/ProductOption, User → Orders, etc.
- Role management: User.userRole is ManyToOne relationship to UserRole entity
- Entity business logic methods: User.update(), User.changeRole(), User.isAdmin(), etc.

**Exception Handling:**
- GlobalExceptionHandler (@ControllerAdvice) catches all exceptions
- Custom BusinessException with ErrorCode enum for business logic errors
- ValidationException handling for @Valid DTO failures
- UnauthorizedException for authentication failures
- All errors return ErrorResponse record with timestamp, status, code, message, path

**Payment Integration:**
- Toss Payments API integration configured via TossPaymentsProperties
- Test mode keys in application.yml
- Success/fail URLs configured: http://localhost:8080/payment/success|fail

**File Uploads:**
- Upload directory configured in application.yml: `file.upload-dir: /Users/a/IdeaProjects/Co-s_House/uploads/`
- Images served via `/images/uploaded/**` endpoint (permitted in SecurityConfig)

**Email Verification:**
- Email service using Naver SMTP (smtp.naver.com:465)
- Auth endpoints: `/auth/email/**` (permitted in SecurityConfig)
- Verification workflow: User.emailVerified flag, User.markEmailVerified() method

### Domain Model Highlights

**User Management:**
- User entity with role relationship to UserRole
- Support for local accounts (email/password) and social accounts (socialProvider/socialId)
- Email verification flag and terms agreement tracking
- User activity logging via LoggingAspect

**Order Management:**
- Order → OrderItems → Product/ProductOption hierarchy
- OrderStatusHistory for status tracking
- Delivery status tracking per OrderItem
- Coupon support with MyCouponResponse DTO
- Payment/Refund entities linked to orders

**Product Management:**
- Product entity with multiple ProductOptions
- Brand association
- Inventory tracking
- Admin product creation/update via ProductCreateRequest/ProductUpdateRequest DTOs

**Inventory Management:**
- Inventory entity with receipt tracking
- InventoryHistory for audit trail
- Admin search and management via InventorySearchRequest

**Community Features:**
- Post/PostCategory entities
- Like/Scrap/Comment support
- User-generated content management

## Development Notes

**Querydsl Q-Classes:**
- Generated during compilation via annotation processors
- Located in `build/generated/querydsl/`
- Automatically included in source sets (configured in build.gradle)
- If Q-classes are missing, run `./gradlew clean build`

**Security Configuration:**
- Dev profile enables HTTP Basic for testing: `spring.profiles.active=dev`
- Production disables HTTP Basic
- Session management: IF_REQUIRED policy with session fixation protection (newSession)
- Maximum 1 concurrent session per user
- SecurityContext stored in HttpSession via HttpSessionSecurityContextRepository

**Database Considerations:**
- JPA ddl-auto: update (auto-updates schema on entity changes)
- show-sql: true (logs SQL queries)
- MySQL 8 dialect
- User role migration MUST run after table creation but before seed data

**Testing:**
- Tests use H2 in-memory database (testImplementation 'com.h2database:h2')
- Spring Security test support available
- Example: CartServiceTest demonstrates service layer testing

**Configuration Files:**
- Primary config: `src/main/resources/application.yml`
- Docker Compose: `docker-compose.yml` (MySQL 8.4 container)
- Docker Compose lifecycle managed by Spring Boot (start-and-stop)

**Static Resources:**
- Thymeleaf templates in `src/main/resources/templates/`
- Template organization mirrors controller structure (order/, home/, admin/, etc.)
- Fragments in `templates/fragments/`
- Layout templates in `templates/layout/`
- Static assets served from `/css/**`, `/js/**`, `/images/**`

**API Keys & Secrets:**
⚠️ application.yml contains test/development credentials. Never commit production secrets.
- OAuth2 client IDs/secrets are in application.yml
- Toss Payments test keys are in application.yml
- Email SMTP credentials are in application.yml
- Consider using environment variables or Spring Cloud Config for production

## Common Tasks

**Add New Entity:**
1. Create entity class in appropriate domain package
2. Add repository interface (extend JpaRepository)
3. If complex queries needed, add *Custom interface and *CustomImpl with Querydsl
4. Run `./gradlew clean compileJava` to generate Q-classes
5. Create service class with business logic
6. Create DTOs in dto package
7. Add controller endpoints
8. Update SQL scripts if needed for seed data

**Add Admin Endpoint:**
1. Create controller method under `/api/admin/**` path
2. Endpoint automatically requires `admin_role` authority (SecurityConfig:119)
3. Or use method-level security: `@PreAuthorize("hasAuthority('admin_role')")`

**Add Public Endpoint:**
1. If GET request, add path to SecurityConfig permitAll list (line 91-107)
2. If POST request, add to POST permitAll list (line 111-116)
3. Restart application for security changes to take effect

**Implement Custom Repository Query:**
1. Create `YourRepositoryCustom` interface with method signature
2. Create `YourRepositoryCustomImpl` class implementing interface
3. Inject `JPAQueryFactory` via constructor
4. Use Q-classes for type-safe queries
5. Update main repository to extend both JpaRepository and Custom interface
6. See OrderRepositoryCustomImpl for reference implementation

**Run Single Test:**
```bash
./gradlew test --tests com.bird.cos.service.cart.CartServiceTest
./gradlew test --tests CartServiceTest.testMethodName --info  # With detailed output
```

**Debug Database Issues:**
- Check MySQL container: `docker ps` (should show cos-mysql running on 13306)
- Connect to MySQL: `mysql -h 127.0.0.1 -P 13306 -u root -proot cos`
- View SQL logs: Check console output (show-sql: true enabled)
- Verify JPA entity mappings match database schema

**OAuth2 Configuration:**
- Kakao/Naver client IDs are in application.yml
- OAuth2 flow: `/oauth2/authorization/{provider}` → callback to `/login/oauth2/code/{provider}`
- Success handler: OAuth2LoginSuccessHandler creates or updates User entity
- Custom user service: SocialOAuth2UserService maps OAuth2 attributes to CustomUserDetails

## Important Constraints

- File upload directory path is hardcoded: `/Users/a/IdeaProjects/Co-s_House/uploads/` (update for your environment)
- MySQL runs on port 13306 (not standard 3306) to avoid conflicts
- JPA ddl-auto set to `update` - be cautious with entity changes in production
- Session-based auth means no JWT tokens - sessions stored in memory by default
- CSRF protection disabled - re-enable if adding public form submissions
- Thymeleaf cache disabled for development - enable in production for performance
