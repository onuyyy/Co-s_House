# Spring Boot 프로젝트 리팩토링 종합 분석

> **프로젝트**: Co's House E-commerce Platform
> **기술 스택**: Spring Boot 3.5.5, Java 21, JPA/Querydsl, Thymeleaf
> **분석일**: 2025-10-14

---

## 📋 목차

1. [현재 구조 분석](#1-현재-구조-분석)
2. [리팩토링이 필요한 부분](#2-리팩토링이-필요한-부분)
3. [코드 품질 향상 제안](#3-코드-품질-향상-제안)
4. [고도화 아이디어](#4-고도화-아이디어)
5. [RESTful API 재설계](#5-restful-api-재설계)
6. [프론트-백 분리 전략](#6-프론트-백-분리-전략)

---

## 1. 현재 구조 분석

### 1.1 프로젝트 아키텍처

```
cos/
├── controller/        # MVC 컨트롤러 (View + API 혼재)
│   ├── admin/        # 관리자 페이지 (/api/admin/**)
│   ├── cart/         # 장바구니 (CartController + CartPageController)
│   ├── order/        # 주문 (OrderController)
│   └── ...
├── service/          # 비즈니스 로직
├── repository/       # 데이터 접근 (JPA + Querydsl Custom)
├── domain/           # JPA 엔티티
├── dto/              # Request/Response DTO
├── config/           # Spring 설정
├── security/         # Spring Security
└── exception/        # 예외 처리
```

### 1.2 주요 문제점 식별

#### 🔴 Critical Issues

1. **Controller 계층 혼란**
   - View Controller와 API Controller가 같은 클래스에 혼재
   - RESTful 원칙 위반 (비표준 URL, 혼란스러운 HTTP 메서드)
   - 응답 형식 불일치 (JSON vs Thymeleaf Model)

2. **예외 처리 부실**
   - Service에서 try-catch로 Exception 처리 후 일반 응답 반환
   - RuntimeException 남발
   - 비즈니스 예외와 시스템 예외 구분 없음

3. **트랜잭션 경계 불분명**
   - Service 전체에 `@Transactional` 붙이고 내부에서 다시 readOnly 지정
   - 엔티티를 직접 빌더로 재생성하여 저장 (영속성 컨텍스트 무시)

#### 🟡 Important Issues

4. **DTO 구조 문제**
   - Request/Response 분리 부족
   - 중복 DTO (OrderRequest vs OrderPreviewRequest 등)
   - Validation 로직 부재 또는 불충분

5. **Repository 패턴 복잡성**
   - Querydsl Custom 구현이 복잡하고 불필요한 경우 많음
   - N+1 문제 잠재적 존재

6. **Entity 설계 문제**
   - User 엔티티에 너무 많은 update 메서드 (책임 과다)
   - Order 엔티티가 불변이 아님 (Builder로 재생성하여 수정)
   - 연관관계 관리 미흡

---

## 2. 리팩토링이 필요한 부분

### 2.1 Controller 계층

#### 🔴 문제 1: View와 API가 섞여 있음

**현재 코드 (`OrderController.java`)**

```java
@Controller  // @RestController가 아님
@RequestMapping("/order")
public class OrderController {

    @PostMapping("/preview")  // View 반환
    public String getOrderView(..., Model model) {
        return "order/create";  // Thymeleaf 템플릿
    }

    @PostMapping("/create")  // JSON 반환
    @ResponseBody
    public OrderCreateResponse createOrder(...) {
        return OrderCreateResponse.success(...);
    }

    @GetMapping("/my-points")  // Map<String, Object> 반환
    @ResponseBody
    public Map<String, Object> getMyPoints(...) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}
```

**문제점:**
- 하나의 Controller에 View 반환과 JSON 반환이 혼재
- `@ResponseBody`를 일부 메서드에만 사용
- 응답 형식이 통일되지 않음 (OrderCreateResponse vs Map<String, Object>)
- `/order/preview`는 View, `/order/create`는 API → 일관성 없음

**리팩토링 제안:**

```java
// 1. View Controller (SSR)
@Controller
@RequestMapping("/order")
public class OrderViewController {

    @PostMapping("/preview")
    public String showPreview(..., Model model) {
        OrderPreviewResponse preview = orderService.getOrderPreview(...);
        model.addAttribute("order", preview);
        return "order/create";
    }
}

// 2. API Controller (REST)
@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(...) {
        OrderResponse order = orderService.createOrder(...);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> getPreview(...) {
        OrderPreviewResponse preview = orderService.getOrderPreview(...);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }
}
```

---

#### 🔴 문제 2: 비RESTful한 URL 구조

**현재 문제:**

```java
// OrderController
POST /order/preview        // 미리보기 (저장 안함)
POST /order/create         // 실제 생성
GET  /order/my-coupons     // 쿠폰 조회
GET  /order/my-points      // 포인트 조회

// ProductManageController
GET  /api/admin/products/new           // 상품 등록 폼
POST /api/admin/products               // 상품 등록
GET  /api/admin/products               // 상품 목록
GET  /api/admin/products/{id}          // 상품 상세
POST /api/admin/products/{id}/update   // 상품 수정 (PUT이 아님!)
POST /api/admin/products/{id}/delete   // 상품 삭제 (DELETE가 아님!)

// CartController
POST   /api/cart                    // 장바구니 추가
GET    /api/cart                    // 장바구니 조회
PATCH  /api/cart/{id}               // 수량 수정
DELETE /api/cart/{id}               // 단일 삭제
DELETE /api/cart?ids=1,2,3          // 다중 삭제
```

**문제점:**
- POST 메서드로 update/delete 처리 (RESTful 위반)
- `/order/my-coupons`, `/order/my-points`는 주문 리소스가 아님
- `/api/admin/products/new`는 API가 아니라 View 엔드포인트
- 리소스 계층 구조가 불명확

**리팩토링 제안:**

```java
// 주문 API (RESTful)
POST   /api/orders                      // 주문 생성
GET    /api/orders                      // 내 주문 목록
GET    /api/orders/{orderId}            // 주문 상세
PATCH  /api/orders/{orderId}            // 주문 수정
DELETE /api/orders/{orderId}            // 주문 취소
POST   /api/orders/{orderId}/confirm    // 구매 확정 (상태 변경)

// 쿠폰 API (별도 리소스)
GET    /api/coupons                     // 내 쿠폰 목록
GET    /api/coupons/{couponId}          // 쿠폰 상세
POST   /api/coupons/{couponId}/validate // 쿠폰 검증

// 포인트 API (별도 리소스)
GET    /api/points                      // 내 포인트 조회
GET    /api/points/history              // 포인트 이력

// 관리자 상품 API
GET    /api/admin/products              // 상품 목록
POST   /api/admin/products              // 상품 생성
GET    /api/admin/products/{id}         // 상품 상세
PUT    /api/admin/products/{id}         // 상품 수정 (전체)
PATCH  /api/admin/products/{id}         // 상품 수정 (부분)
DELETE /api/admin/products/{id}         // 상품 삭제

// 관리자 View (별도 Controller)
GET    /admin/products/new              // 상품 등록 폼 (View)
GET    /admin/products                  // 상품 관리 페이지 (View)
```

---

#### 🔴 문제 3: 예외 처리를 Controller에서 함

**현재 코드:**

```java
@PostMapping("/create")
@ResponseBody
public OrderCreateResponse createOrder(...) {
    try {
        OrderResponse order = orderService.createOrder(...);
        return OrderCreateResponse.success(order.getOrderId());
    } catch (Exception e) {
        return OrderCreateResponse.failure("주문 처리 중 오류가 발생했습니다: " + e.getMessage());
    }
}
```

**문제점:**
- Controller가 예외를 직접 처리 → GlobalExceptionHandler 무용지물
- 모든 예외를 동일하게 처리 (비즈니스 예외 vs 시스템 예외 구분 없음)
- HTTP 상태 코드가 항상 200 OK (실패해도 성공 응답)

**리팩토링 제안:**

```java
// Controller: 예외를 던지기만 함
@PostMapping
public ResponseEntity<ApiResponse<OrderResponse>> createOrder(...) {
    OrderResponse order = orderService.createOrder(...);
    return ResponseEntity.ok(ApiResponse.success(order));
}

// GlobalExceptionHandler에서 처리
@ExceptionHandler(OrderCreationException.class)
public ResponseEntity<ApiResponse<Void>> handleOrderCreation(OrderCreationException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
}
```

---

### 2.2 Service 계층

#### 🔴 문제 1: 트랜잭션 경계 불명확

**현재 코드 (`OrderService.java:98-190`)**

```java
@Transactional  // 클래스 전체에 적용
@Service
public class OrderService {

    @Transactional(readOnly = true)  // 읽기 전용 재지정
    public OrderPreviewResponse getOrderPreview(...) {
        // ...
    }

    public OrderResponse createOrder(...) {  // 쓰기 트랜잭션
        User user = getUserByEmail(email);

        Order order = Order.builder()  // 새 객체 생성
            .user(user)
            .orderStatusCode(orderStatusCode)
            .build();

        order = orderRepository.save(order);  // 1차 저장

        addOrderItemsToOrder(order, orderItems);  // OrderItem 추가

        order = orderRepository.save(order);  // 2차 저장 (왜?)

        // 재고 출고
        inventoryOutboundService.processOutboundForOrder(order.getOrderId());

        // 쿠폰 사용
        if (userCouponId != null) {
            useMyCoupon(userCouponId);
        }

        // 포인트 사용
        pointService.useOrderPoints(...);

        // 장바구니 삭제
        cartService.delete(...);

        return response;
    }
}
```

**문제점:**
1. Order를 두 번 저장 (불필요)
2. 트랜잭션 내에서 외부 서비스 호출 (재고, 쿠폰, 포인트, 장바구니)
3. 트랜잭션이 너무 길어짐 → 동시성 문제, 데드락 가능성
4. 영속성 컨텍스트를 활용하지 않음 (Builder로 재생성)

**리팩토링 제안:**

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    // 읽기 전용 메서드는 명시적으로 표시
    @Transactional(readOnly = true)
    public OrderPreviewResponse getOrderPreview(...) {
        // ...
    }

    // 쓰기 트랜잭션은 최소한으로
    @Transactional
    public OrderResponse createOrder(...) {
        // 1. 주문 생성 (트랜잭션 필요)
        Order order = createOrderEntity(email, orderItems, ...);
        order = orderRepository.save(order);

        // 2. 트랜잭션 분리: 재고 차감 (별도 트랜잭션)
        Long orderId = order.getOrderId();

        // 트랜잭션 커밋 후 외부 처리
        return response;
    }

    // 주문 생성 후 처리는 별도 메서드 (이벤트 기반 추천)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAfterOrderCreated(Long orderId, ...) {
        // 재고 차감
        inventoryOutboundService.processOutbound(orderId);

        // 쿠폰 사용
        if (userCouponId != null) {
            couponService.useCoupon(userCouponId);
        }

        // 포인트 차감
        pointService.deductPoints(...);

        // 장바구니 정리
        cartService.deleteItems(...);
    }

    // Order 엔티티 생성 로직 분리
    private Order createOrderEntity(...) {
        User user = getUserByEmail(email);
        CommonCode status = getOrderStatus();

        Order order = Order.create(user, status, totalAmount);

        // OrderItem 추가 (영속성 컨텍스트 활용)
        for (OrderRequest item : orderItems) {
            Product product = getProduct(item.getProductId());
            ProductOption option = getProductOption(item.getProductOptionId());
            order.addOrderItem(product, option, item.getQuantity(), item.getPrice());
        }

        return order;
    }
}
```

---

#### 🟡 문제 2: 비즈니스 로직이 Service에 집중됨

**현재 코드:**

```java
// OrderService에서 구매 확정 처리
public boolean confirmOrder(Long orderId, String userEmail) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> BusinessException.orderNotFound(orderId));

    // 권한 검증 (Service에서?)
    if (!order.getUser().getUserEmail().equals(userEmail)) {
        throw BusinessException.orderAccessDenied();
    }

    // 중복 확정 검증
    if (order.getConfirmedDate() != null) {
        return false;
    }

    // Order를 Builder로 재생성 (엔티티 패턴 무시)
    order = Order.builder()
        .orderId(order.getOrderId())
        .user(order.getUser())
        .orderStatusCode(order.getOrderStatusCode())
        .totalAmount(order.getTotalAmount())
        .paidAmount(order.getPaidAmount())
        .orderDate(order.getOrderDate())
        .confirmedDate(LocalDateTime.now())  // 이것만 변경
        .build();

    orderRepository.save(order);
    return true;
}
```

**문제점:**
1. 권한 검증이 Service에 있음 → AOP나 Security로 이동
2. Order 엔티티를 Builder로 재생성 → 영속성 컨텍스트 무시
3. 비즈니스 규칙이 Service에 흩어짐 → Entity로 이동

**리팩토링 제안:**

```java
// Order 엔티티에 비즈니스 로직 이동
@Entity
public class Order {

    // ...

    public void confirm() {
        if (this.confirmedDate != null) {
            throw new OrderAlreadyConfirmedException(this.orderId);
        }

        if (!this.orderStatusCode.getCodeId().equals("ORDER_003")) {
            throw new OrderCannotBeConfirmedException(this.orderId);
        }

        this.confirmedDate = LocalDateTime.now();
    }

    public boolean isOwnedBy(User user) {
        return this.user.equals(user);
    }
}

// Service는 단순해짐
@Transactional
public void confirmOrder(Long orderId, User currentUser) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    // 권한 검증
    if (!order.isOwnedBy(currentUser)) {
        throw new OrderAccessDeniedException();
    }

    // 비즈니스 로직은 엔티티가 처리
    order.confirm();

    // 영속성 컨텍스트가 자동 저장 (Dirty Checking)
}
```

---

### 2.3 Repository 계층

#### 🟡 문제 1: Querydsl Custom 구현 과도

**현재 코드 (`OrderRepositoryCustomImpl.java`)**

```java
@Override
public Page<Order> searchOrders(Long userId, MyOrderRequest request, Pageable pageable) {
    QOrder order = QOrder.order;
    QOrderItem orderItem = QOrderItem.orderItem;
    QProduct product = QProduct.product;
    QProductOption productOption = QProductOption.productOption;

    JPAQuery<Order> query = queryFactory
        .selectFrom(order)
        .distinct()
        .leftJoin(order.orderItems, orderItem).fetchJoin()  // N+1 방지
        .leftJoin(orderItem.product, product).fetchJoin()
        .leftJoin(orderItem.productOption, productOption).fetchJoin()
        .leftJoin(orderItem.deliveryStatusCode).fetchJoin()
        .leftJoin(order.orderStatusCode).fetchJoin()
        .where(
            userIdEq(userId),
            searchDateCondition(request.getSearchDate()),
            orderStatusEq(request.getOrderStatus()),
            searchValueContains(request.getSearchValue())
        )
        .orderBy(order.orderDate.desc());

    // 전체 개수 조회 (비효율적!)
    long total = query.fetch().size();

    // 페이징 적용
    List<Order> orders = query
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    return new PageImpl<>(orders, pageable, total);
}
```

**문제점:**
1. Total count를 위해 전체 데이터를 fetch() → 매우 비효율적
2. fetchJoin을 너무 많이 사용 → 카테시안 곱 발생 가능
3. distinct를 사용했지만 메모리에서 중복 제거 (DB가 아님)

**리팩토링 제안:**

```java
@Override
public Page<Order> searchOrders(Long userId, MyOrderRequest request, Pageable pageable) {
    // 1. Count 쿼리 분리 (효율적)
    Long total = queryFactory
        .select(order.count())
        .from(order)
        .where(
            userIdEq(userId),
            searchDateCondition(request.getSearchDate()),
            orderStatusEq(request.getOrderStatus())
        )
        .fetchOne();

    // 2. 데이터 조회 (페이징 먼저)
    List<Long> orderIds = queryFactory
        .select(order.orderId)
        .from(order)
        .where(/* 동일 조건 */)
        .orderBy(order.orderDate.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 3. IN 쿼리로 fetchJoin (카테시안 곱 방지)
    if (orderIds.isEmpty()) {
        return new PageImpl<>(Collections.emptyList(), pageable, total);
    }

    List<Order> orders = queryFactory
        .selectFrom(order)
        .distinct()
        .leftJoin(order.orderItems, orderItem).fetchJoin()
        .leftJoin(orderItem.product, product).fetchJoin()
        .where(order.orderId.in(orderIds))
        .orderBy(order.orderDate.desc())
        .fetch();

    return new PageImpl<>(orders, pageable, total);
}
```

---

### 2.4 DTO 구조

#### 🟡 문제 1: Request/Response 분리 부족

**현재 구조:**

```
dto/
├── order/
│   ├── OrderRequest.java           // 주문 생성 요청
│   ├── OrderResponse.java          // 주문 응답
│   ├── OrderPreviewResponse.java   // 미리보기 응답
│   ├── OrderCreateResponse.java    // 생성 결과 응답 (success/failure)
│   ├── OrderForm.java              // Form 데이터
│   └── MyOrderResponse.java        // 내 주문 응답
```

**문제점:**
- OrderCreateResponse가 성공/실패를 담음 → 표준화된 ApiResponse 필요
- OrderForm과 OrderRequest 중복
- Response가 너무 세분화됨

**리팩토링 제안:**

```
dto/
├── common/
│   ├── ApiResponse.java          // 표준 응답 래퍼
│   ├── PageResponse.java         // 페이징 응답
│   └── ErrorResponse.java        // 에러 응답 (이미 존재)
├── order/
│   ├── request/
│   │   ├── OrderCreateRequest.java
│   │   ├── OrderUpdateRequest.java
│   │   └── OrderSearchRequest.java
│   └── response/
│       ├── OrderResponse.java
│       ├── OrderDetailResponse.java
│       └── OrderSummaryResponse.java
```

```java
// 표준 API 응답
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    String errorCode,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode, LocalDateTime.now());
    }
}
```

---

### 2.5 Entity 설계

#### 🟡 문제 1: User 엔티티 책임 과다

**현재 코드 (`User.java`)**

```java
@Entity
public class User {
    // 14개의 update 관련 메서드
    public void update(UserUpdateRequest request) { ... }
    public void changeRole(UserRole newRole) { ... }
    public void updatePassword(String encodedPassword) { ... }
    public void updateEmail(String email) { ... }
    public void updateNameIfBlank(String name) { ... }
    public void updateNickname(String nickname) { ... }
    public void updatePhone(String phone) { ... }
    public void updateAddress(String address) { ... }
    public void linkSocialAccount(String provider, String id) { ... }
    public void agreeTerms() { ... }
    public void markEmailVerified() { ... }
    public void updateUserRole(UserRole newRole) { ... }

    // 검증 메서드
    public boolean isAdmin() { ... }
    public boolean isUser() { ... }
}
```

**문제점:**
- 너무 많은 update 메서드 → 단일 책임 원칙 위반
- updateUserRole과 changeRole 중복
- update 메서드가 DTO를 직접 받음 → 계층 간 결합

**리팩토링 제안:**

```java
// User 엔티티는 핵심 비즈니스 로직만
@Entity
public class User {

    // 핵심 정보 변경
    public void updateProfile(String name, String nickname, String phone, String address) {
        this.userName = validateAndTrim(name, this.userName);
        this.userNickname = validateAndTrim(nickname, this.userNickname);
        this.userPhone = phone;
        this.userAddress = address;
    }

    // 권한 변경
    public void changeRole(UserRole newRole) {
        validateRoleChange(newRole);
        this.userRole = newRole;
    }

    // 비밀번호 변경
    public void changePassword(String newEncodedPassword) {
        this.userPassword = newEncodedPassword;
    }

    // 소셜 계정 연동
    public void linkSocial(String provider, String socialId) {
        this.socialProvider = provider;
        this.socialId = socialId;
    }

    // 인증/약관
    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void agreeToTerms() {
        this.termsAgreed = true;
    }

    // 권한 확인
    public boolean hasRole(String roleName) {
        return this.userRole != null &&
               this.userRole.getUserRoleName().equals(roleName);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }
}
```

---

#### 🔴 문제 2: Order 엔티티 불변성 부족

**현재 코드:**

```java
@Entity
public class Order {

    @Id
    private Long orderId;

    @ManyToOne
    private User user;

    private LocalDateTime orderDate;
    private LocalDateTime confirmedDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);  // 양방향 연관관계
    }
}

// OrderItem
@Entity
public class OrderItem {
    @ManyToOne
    private Order order;

    public void setOrder(Order order) {  // Setter 존재
        this.order = order;
    }
}
```

**문제점:**
- OrderItem.setOrder() Setter가 public → 불변성 위반
- Order가 생성 후 수정 가능 → 주문은 불변이어야 함
- confirmedDate를 Builder로 재생성하여 변경 (OrderService:424-432)

**리팩토링 제안:**

```java
@Entity
@Table(name = "`ORDER`")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;  // Enum 사용

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;

    private LocalDateTime orderedAt;
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 생성 메서드 (정적 팩토리)
    public static Order create(User user, List<OrderItem> items, BigDecimal totalAmount) {
        Order order = new Order();
        order.user = user;
        order.status = OrderStatus.PENDING;
        order.totalAmount = totalAmount;
        order.paidAmount = BigDecimal.ZERO;
        order.orderedAt = LocalDateTime.now();

        for (OrderItem item : items) {
            order.addOrderItem(item);
        }

        return order;
    }

    // 상태 변경 메서드 (불변성 유지)
    public void confirm() {
        validateConfirmable();
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        validateCancelable();
        this.status = OrderStatus.CANCELLED;
    }

    // 내부 메서드 (package-private)
    void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.assignOrder(this);  // Setter 대신 assign 메서드
    }

    // 검증 메서드
    private void validateConfirmable() {
        if (this.confirmedAt != null) {
            throw new OrderAlreadyConfirmedException(this.orderId);
        }
        if (this.status != OrderStatus.DELIVERED) {
            throw new OrderNotDeliveredException(this.orderId);
        }
    }
}

// OrderItem
@Entity
public class OrderItem {
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    // Setter 제거, package-private assign 메서드 추가
    void assignOrder(Order order) {
        this.order = order;
    }
}
```

---

### 2.6 예외 처리

#### 🔴 문제: GlobalExceptionHandler가 제대로 활용되지 않음

**현재 코드:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOthers(Exception ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_OPERATION, req.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_OPERATION.getStatus()).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ex.getErrorCode(), req.getRequestURI());
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(body);
    }
}
```

**문제점:**
1. Service에서 try-catch로 예외를 먹어버림 → Handler에 도달하지 않음
2. 모든 Exception을 INVALID_OPERATION으로 처리 → 구체적인 에러 정보 손실
3. 로깅 부재
4. ErrorCode가 충분하지 않음

**리팩토링 제안:**

```java
// 예외 계층 구조
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

// 구체적인 예외 클래스들
public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long orderId) {
        super(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId);
    }
}

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(Long productId, int required, int available) {
        super(ErrorCode.INSUFFICIENT_STOCK,
              String.format("재고 부족: 상품 ID=%d, 필요=%d, 가용=%d", productId, required, available));
    }
}

// GlobalExceptionHandler 개선
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest req) {

        log.warn("Business exception: code={}, message={}, path={}",
                ex.getErrorCode(), ex.getMessage(), req.getRequestURI());

        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "입력값 검증 실패", errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "접근 권한이 없습니다"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception ex, HttpServletRequest req) {

        log.error("Unexpected error: path={}, error={}",
                req.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"));
    }
}

// ErrorCode enum 확장
public enum ErrorCode {
    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다"),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다"),
    ORDER_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "ORDER_002", "이미 구매확정된 주문입니다"),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "ORDER_003", "취소할 수 없는 주문입니다"),

    // 재고
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "STOCK_001", "재고가 부족합니다"),

    // 쿠폰
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "COUPON_001", "만료된 쿠폰입니다"),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "COUPON_002", "이미 사용된 쿠폰입니다"),

    // 결제
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_001", "결제에 실패했습니다"),

    // 일반
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

---

## 3. 코드 품질 향상 제안

### 3.1 SOLID 원칙 적용

#### 단일 책임 원칙 (SRP)

**현재 문제:**
- OrderService가 주문 생성, 재고 관리, 쿠폰 사용, 포인트 차감, 장바구니 정리까지 담당

**개선:**

```java
// 주문 서비스: 주문 도메인만 담당
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, User user) {
        // 1. 주문 생성
        Order order = Order.create(user, request.getItems(), request.getTotalAmount());
        order = orderRepository.save(order);

        // 2. 이벤트 발행 (다른 처리는 이벤트 핸들러가)
        eventPublisher.publishOrderCreated(order);

        return OrderResponse.from(order);
    }
}

// 이벤트 핸들러: 주문 후속 처리
@Component
public class OrderCreatedEventHandler {

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCreated(OrderCreatedEvent event) {
        Long orderId = event.getOrderId();

        // 재고 차감
        inventoryService.deductStock(orderId);

        // 쿠폰 사용
        if (event.getCouponId() != null) {
            couponService.useCoupon(event.getCouponId());
        }

        // 포인트 차감
        if (event.getUsedPoints() > 0) {
            pointService.deductPoints(event.getUserId(), event.getUsedPoints());
        }

        // 장바구니 정리
        cartService.deleteItems(event.getCartItemIds());
    }
}
```

---

#### 개방-폐쇄 원칙 (OCP)

**현재 문제:**
- 쿠폰 할인 로직이 하드코딩됨 → 새로운 할인 정책 추가 시 코드 수정 필요

**개선:**

```java
// 할인 정책 인터페이스
public interface DiscountPolicy {
    BigDecimal calculate(Order order);
    boolean isApplicable(Order order);
}

// 쿠폰 할인
@Component
public class CouponDiscountPolicy implements DiscountPolicy {
    @Override
    public BigDecimal calculate(Order order) {
        // 쿠폰 할인 계산
    }

    @Override
    public boolean isApplicable(Order order) {
        return order.hasCoupon();
    }
}

// 포인트 할인
@Component
public class PointDiscountPolicy implements DiscountPolicy {
    @Override
    public BigDecimal calculate(Order order) {
        // 포인트 할인 계산
    }

    @Override
    public boolean isApplicable(Order order) {
        return order.getUsedPoints() > 0;
    }
}

// 할인 서비스
@Service
public class DiscountService {
    private final List<DiscountPolicy> policies;

    public BigDecimal calculateTotalDiscount(Order order) {
        return policies.stream()
                .filter(policy -> policy.isApplicable(order))
                .map(policy -> policy.calculate(order))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

---

### 3.2 DRY (Don't Repeat Yourself)

**현재 문제:**
- User 조회 로직이 모든 Service에 중복

```java
// OrderService
private User getUserByEmail(String email) {
    return userRepository.findByUserEmail(email)
        .orElseThrow(BusinessException::userNotFound);
}

// CartService (동일)
private User getUserByEmail(String email) {
    return userRepository.findByUserEmail(email)
        .orElseThrow(BusinessException::userNotFound);
}
```

**개선:**

```java
// UserService에 통합
@Service
public class UserService {

    public User getUserByEmail(String email) {
        return userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User getCurrentUser(Authentication auth) {
        return getUserByEmail(auth.getName());
    }
}

// 또는 Spring Security에서 직접 주입
@RestController
public class OrderApiController {

    @PostMapping("/api/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {  // User 엔티티 직접 주입

        User user = (User) userDetails;  // CustomUserDetails 구현 필요
        OrderResponse order = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
```

---

### 3.3 계층 간 책임 분리

**현재 문제:**

```
Controller → Service (모든 로직) → Repository
```

**개선:**

```
Controller     → 요청/응답 변환, 인증/인가 확인
  ↓
Service        → 트랜잭션 관리, 비즈니스 로직 조율
  ↓
Domain (Entity) → 핵심 비즈니스 규칙
  ↓
Repository     → 데이터 접근
```

**예시:**

```java
// Controller: 요청/응답 변환
@RestController
public class OrderApiController {

    @PostMapping("/api/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal User user) {

        OrderResponse response = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

// Service: 트랜잭션 관리와 조율
@Service
public class OrderService {

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, User user) {
        // 1. DTO → Domain 변환
        List<OrderItem> items = request.getItems().stream()
                .map(dto -> OrderItem.create(
                        productRepository.findById(dto.getProductId()).orElseThrow(),
                        dto.getQuantity(),
                        dto.getPrice()
                ))
                .toList();

        // 2. Domain 로직 실행
        Order order = Order.create(user, items, request.getTotalAmount());
        order.applyCoupon(request.getCouponId());
        order.usePoints(request.getUsedPoints());

        // 3. 저장
        Order savedOrder = orderRepository.save(order);

        // 4. Domain → DTO 변환
        return OrderResponse.from(savedOrder);
    }
}

// Entity: 핵심 비즈니스 규칙
@Entity
public class Order {

    public static Order create(User user, List<OrderItem> items, BigDecimal totalAmount) {
        validateItems(items);
        validateTotalAmount(totalAmount, items);

        Order order = new Order();
        order.user = user;
        order.status = OrderStatus.PENDING;
        order.totalAmount = totalAmount;
        items.forEach(order::addOrderItem);

        return order;
    }

    public void applyCoupon(Long couponId) {
        if (couponId == null) return;

        // 쿠폰 적용 가능 검증
        validateCouponApplicable();
        this.couponId = couponId;
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new EmptyOrderItemsException();
        }
    }
}
```

---

## 4. 고도화 아이디어

### 4.1 테스트 코드 전략

#### 현재 상황
- 일부 서비스에만 테스트 존재 (`CartServiceTest`)
- 통합 테스트 부족
- E2E 테스트 없음

#### 개선 방안

**1. 단위 테스트 (Unit Tests)**

```java
// Service 계층 테스트
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        User user = createTestUser();
        OrderCreateRequest request = createTestRequest();

        when(userRepository.findByUserEmail(anyString())).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderResponse response = orderService.createOrder(request, user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualTo(request.getTotalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("재고 부족 시 주문 실패")
    void createOrder_InsufficientStock() {
        // given
        User user = createTestUser();
        OrderCreateRequest request = createTestRequestWithLargeQuantity();

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request, user))
                .isInstanceOf(InsufficientStockException.class);
    }
}
```

**2. 통합 테스트 (Integration Tests)**

```java
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    @DisplayName("주문 생성 API 통합 테스트")
    void createOrder_Integration() throws Exception {
        // given
        User user = createAndSaveTestUser();
        Product product = createAndSaveTestProduct();
        OrderCreateRequest request = OrderCreateRequest.builder()
                .items(List.of(new OrderItemRequest(product.getProductId(), 1, BigDecimal.valueOf(10000))))
                .totalAmount(BigDecimal.valueOf(10000))
                .build();

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").exists())
                .andExpect(jsonPath("$.data.totalAmount").value(10000));
    }
}
```

**3. E2E 테스트 (End-to-End Tests)**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("주문 전체 플로우 E2E 테스트")
    void orderCompleteFlow() {
        String baseUrl = "http://localhost:" + port;

        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest(...);
        ResponseEntity<ApiResponse> registerResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerRequest,
                ApiResponse.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest(...);
        ResponseEntity<ApiResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                ApiResponse.class);
        String token = extractToken(loginResponse);

        // 3. 장바구니에 상품 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<AddToCartRequest> cartRequest = new HttpEntity<>(new AddToCartRequest(...), headers);
        ResponseEntity<ApiResponse> cartResponse = restTemplate.postForEntity(
                baseUrl + "/api/cart",
                cartRequest,
                ApiResponse.class);
        assertThat(cartResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 4. 주문 생성
        HttpEntity<OrderCreateRequest> orderRequest = new HttpEntity<>(new OrderCreateRequest(...), headers);
        ResponseEntity<ApiResponse> orderResponse = restTemplate.postForEntity(
                baseUrl + "/api/orders",
                orderRequest,
                ApiResponse.class);
        assertThat(orderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderResponse.getBody().isSuccess()).isTrue();

        // 5. 주문 조회
        ResponseEntity<ApiResponse> getOrderResponse = restTemplate.exchange(
                baseUrl + "/api/orders",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ApiResponse.class);
        assertThat(getOrderResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

**4. 테스트 커버리지 목표**

```yaml
테스트 커버리지:
  전체: 80% 이상
  핵심 비즈니스 로직: 90% 이상

계층별 우선순위:
  1. Service 계층: 필수 (모든 메서드)
  2. Domain (Entity): 필수 (비즈니스 로직 메서드)
  3. Controller: 중요 API만
  4. Repository: Custom 구현만

테스트 도구:
  - JUnit 5
  - Mockito
  - AssertJ
  - Spring Test
  - Testcontainers (DB 테스트)
```

---

### 4.2 보안 강화

#### 🔴 CSRF 보호 재활성화

**현재 문제:**

```java
// SecurityConfig.java:74
http.csrf(AbstractHttpConfigurer::disable)
```

**개선:**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            // API는 토큰 기반이므로 CSRF 제외
            .ignoringRequestMatchers("/api/**")
            // View 엔드포인트는 CSRF 보호
            .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
        )
        // ...
}
```

---

#### 🔴 XSS 방지

**현재 문제:**
- Thymeleaf 템플릿에서 `th:utext` 사용 시 XSS 취약
- 사용자 입력을 그대로 출력

**개선:**

```java
// XSS 필터 추가
@Component
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        XssRequestWrapper wrappedRequest = new XssRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XssRequestWrapper extends HttpServletRequestWrapper {

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;

        return Arrays.stream(values)
                .map(this::sanitize)
                .toArray(String[]::new);
    }

    private String sanitize(String value) {
        if (value == null) return null;

        // HTML 특수문자 이스케이프
        return value.replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;")
                    .replaceAll("'", "&#x27;")
                    .replaceAll("/", "&#x2F;");
    }
}
```

---

#### 🔴 SQL Injection 방지

**현재 상태:**
- Querydsl 사용 → 파라미터 바인딩 자동 → 안전
- 하지만 Native Query 사용 시 주의 필요

**권장사항:**

```java
// ❌ 위험한 코드
@Query(value = "SELECT * FROM USER WHERE user_email = '" + email + "'", nativeQuery = true)
User findByEmailUnsafe(String email);

// ✅ 안전한 코드
@Query(value = "SELECT * FROM USER WHERE user_email = :email", nativeQuery = true)
User findByEmailSafe(@Param("email") String email);
```

---

#### 🟡 비밀번호 정책 강화

```java
@Component
public class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    );

    public boolean isValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public List<String> validateAndGetErrors(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < 8) {
            errors.add("비밀번호는 최소 8자 이상이어야 합니다");
        }

        if (!password.matches(".*[A-Za-z].*")) {
            errors.add("비밀번호는 최소 1개의 영문자를 포함해야 합니다");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("비밀번호는 최소 1개의 숫자를 포함해야 합니다");
        }

        if (!password.matches(".*[@$!%*#?&].*")) {
            errors.add("비밀번호는 최소 1개의 특수문자를 포함해야 합니다");
        }

        return errors;
    }
}
```

---

### 4.3 로깅 및 모니터링

#### 구조화된 로깅

**현재 문제:**

```java
log.info("Debug - Order confirmed successfully: orderId={}, confirmedDate={}",
    orderId, order.getConfirmedDate());
```

**개선:**

```java
// JSON 구조화 로깅 (Logback + Logstash)
@Slf4j
@Service
public class OrderService {

    public void confirmOrder(Long orderId, User user) {
        // ...

        log.info("Order confirmed",
                kv("orderId", orderId),
                kv("userId", user.getUserId()),
                kv("confirmedAt", LocalDateTime.now()),
                kv("eventType", "ORDER_CONFIRMED"));
    }
}

// logback-spring.xml
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>userId</includeMdcKeyName>
            <includeMdcKeyName>orderId</includeMdcKeyName>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="LOGSTASH" />
    </root>
</configuration>
```

---

#### 메트릭 수집 (Actuator + Prometheus)

```java
// build.gradle 추가
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}

// application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true

// 커스텀 메트릭
@Component
public class OrderMetrics {
    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;

    public OrderMetrics(MeterRegistry registry) {
        this.orderCreatedCounter = Counter.builder("orders.created")
                .description("Total number of orders created")
                .tag("type", "ecommerce")
                .register(registry);

        this.orderProcessingTimer = Timer.builder("orders.processing.time")
                .description("Order processing time")
                .register(registry);
    }

    public void recordOrderCreated() {
        orderCreatedCounter.increment();
    }

    public void recordOrderProcessingTime(Duration duration) {
        orderProcessingTimer.record(duration);
    }
}

// Service에서 사용
@Service
public class OrderService {
    private final OrderMetrics metrics;

    public OrderResponse createOrder(...) {
        Timer.Sample sample = Timer.start();

        try {
            // 주문 생성 로직
            Order order = ...;

            metrics.recordOrderCreated();
            return OrderResponse.from(order);
        } finally {
            sample.stop(metrics.orderProcessingTimer);
        }
    }
}
```

---

### 4.4 성능 개선

#### 🔴 N+1 문제 해결

**현재 문제:**

```java
// OrderRepositoryCustomImpl.java:36-43
JPAQuery<Order> query = queryFactory
    .selectFrom(order)
    .leftJoin(order.orderItems, orderItem).fetchJoin()  // N+1 발생 가능
    .leftJoin(orderItem.product, product).fetchJoin()
    .leftJoin(orderItem.productOption, productOption).fetchJoin()
    // ...
```

**개선 (이미 제시했지만 재강조):**

```java
// 1. ID만 먼저 조회
List<Long> orderIds = queryFactory
    .select(order.orderId)
    .from(order)
    .where(/* 조건 */)
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize())
    .fetch();

// 2. IN 쿼리로 Batch Fetch
List<Order> orders = queryFactory
    .selectFrom(order)
    .leftJoin(order.orderItems, orderItem).fetchJoin()
    .where(order.orderId.in(orderIds))
    .fetch();

// 또는 @BatchSize 사용
@Entity
public class Order {
    @OneToMany(mappedBy = "order")
    @BatchSize(size = 100)  // IN 쿼리로 100개씩 한번에 조회
    private List<OrderItem> orderItems;
}
```

---

#### 🟡 캐싱 적용

```java
// 1. Redis 의존성 추가
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
}

// 2. 캐시 설정
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}

// 3. 캐시 적용
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductResponse.from(product);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        // 상품 수정 로직
    }

    @Cacheable(value = "productList", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductSummary> getProductList(Pageable pageable) {
        // 상품 목록 조회
    }
}
```

---

#### 🟡 데이터베이스 인덱싱

```sql
-- 주문 조회 최적화
CREATE INDEX idx_order_user_date ON `ORDER` (user_id, order_date DESC);
CREATE INDEX idx_order_status ON `ORDER` (order_status);

-- 상품 검색 최적화
CREATE INDEX idx_product_title ON PRODUCT (product_title);
CREATE INDEX idx_product_brand ON PRODUCT (brand_id);
CREATE INDEX idx_product_category ON PRODUCT (category_id);

-- 장바구니 조회 최적화
CREATE INDEX idx_cart_user ON CART (user_id, cart_created_at DESC);

-- 복합 인덱스 (자주 함께 조회되는 컬럼)
CREATE INDEX idx_order_user_status_date ON `ORDER` (user_id, order_status, order_date DESC);
```

---

### 4.5 API 문서화 (SpringDoc OpenAPI)

```java
// 1. 의존성 추가
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
}

// 2. 설정
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Co's House E-commerce API")
                        .version("1.0.0")
                        .description("전자상거래 플랫폼 REST API 문서")
                        .contact(new Contact()
                                .name("Co's House Team")
                                .email("contact@coshouse.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

// 3. Controller에 문서화 어노테이션
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "주문 관리 API")
public class OrderApiController {

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 생성 성공",
                content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "주문 생성 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderCreateRequest.class)))
            @Valid @RequestBody OrderCreateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {

        OrderResponse response = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

// 4. DTO에 문서화 어노테이션
@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(

        @Schema(description = "주문 상품 목록", required = true)
        @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
        List<OrderItemRequest> items,

        @Schema(description = "총 주문 금액", example = "50000", required = true)
        @NotNull
        @Min(value = 0, message = "주문 금액은 0 이상이어야 합니다")
        BigDecimal totalAmount,

        @Schema(description = "사용할 쿠폰 ID", example = "123")
        Long couponId,

        @Schema(description = "사용할 포인트", example = "1000")
        @Min(value = 0)
        Integer usedPoints
) {}

// Swagger UI 접근: http://localhost:8080/swagger-ui.html
```

---

## 5. RESTful API 재설계

### 5.1 RESTful 원칙

#### 리소스 중심 설계

**❌ 비RESTful:**

```
POST /order/create
POST /order/update
POST /order/delete
GET  /order/my-coupons
GET  /order/my-points
```

**✅ RESTful:**

```
POST   /api/orders              # 주문 생성
GET    /api/orders              # 주문 목록
GET    /api/orders/{id}         # 주문 상세
PUT    /api/orders/{id}         # 주문 수정
DELETE /api/orders/{id}         # 주문 취소

GET    /api/coupons             # 쿠폰 목록 (별도 리소스)
GET    /api/points              # 포인트 조회 (별도 리소스)
```

---

#### HTTP 메서드 사용 원칙

```
GET    - 조회 (Safe, Idempotent)
POST   - 생성 (Non-idempotent)
PUT    - 전체 수정 (Idempotent)
PATCH  - 부분 수정 (Idempotent)
DELETE - 삭제 (Idempotent)
```

**예시:**

```java
// ✅ 올바른 사용
@GetMapping("/api/orders/{id}")           // 조회
@PostMapping("/api/orders")               // 생성
@PutMapping("/api/orders/{id}")           // 전체 수정
@PatchMapping("/api/orders/{id}/status")  // 부분 수정 (상태만)
@DeleteMapping("/api/orders/{id}")        // 삭제

// ❌ 잘못된 사용
@PostMapping("/api/orders/{id}/update")   // POST로 수정 (PUT 사용)
@GetMapping("/api/orders/delete/{id}")    // GET으로 삭제 (DELETE 사용)
```

---

### 5.2 표준 응답 형식

#### 성공 응답

```java
// 표준 성공 응답
{
  "success": true,
  "data": {
    "orderId": 123,
    "totalAmount": 50000,
    "status": "PENDING"
  },
  "message": null,
  "timestamp": "2025-10-14T10:30:00"
}

// 페이징 응답
{
  "success": true,
  "data": {
    "content": [...],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5
    }
  },
  "timestamp": "2025-10-14T10:30:00"
}
```

#### 실패 응답

```java
// 비즈니스 오류
{
  "success": false,
  "data": null,
  "message": "재고가 부족합니다",
  "errorCode": "STOCK_001",
  "timestamp": "2025-10-14T10:30:00"
}

// Validation 오류
{
  "success": false,
  "data": null,
  "message": "입력값 검증 실패",
  "errorCode": "VALIDATION_ERROR",
  "errors": {
    "email": "이메일 형식이 올바르지 않습니다",
    "password": "비밀번호는 8자 이상이어야 합니다"
  },
  "timestamp": "2025-10-14T10:30:00"
}
```

---

### 5.3 RESTful API 전체 명세

#### 인증/인가

```
POST   /api/auth/register          # 회원가입
POST   /api/auth/login             # 로그인
POST   /api/auth/logout            # 로그아웃
POST   /api/auth/refresh           # 토큰 갱신
POST   /api/auth/password/reset    # 비밀번호 재설정 요청
PUT    /api/auth/password          # 비밀번호 변경

POST   /api/auth/email/send        # 이메일 인증 발송
POST   /api/auth/email/verify      # 이메일 인증 확인
```

---

#### 사용자

```
GET    /api/users/me               # 내 정보 조회
PUT    /api/users/me               # 내 정보 수정
PATCH  /api/users/me/password      # 비밀번호 변경
PATCH  /api/users/me/email         # 이메일 변경
DELETE /api/users/me               # 회원 탈퇴

GET    /api/users/me/addresses     # 배송지 목록
POST   /api/users/me/addresses     # 배송지 추가
PUT    /api/users/me/addresses/{id}    # 배송지 수정
DELETE /api/users/me/addresses/{id}    # 배송지 삭제
```

---

#### 상품

```
GET    /api/products                    # 상품 목록 (페이징, 필터링)
GET    /api/products/{id}               # 상품 상세
GET    /api/products/{id}/options       # 상품 옵션 목록
GET    /api/products/{id}/reviews       # 상품 리뷰 목록
POST   /api/products/{id}/reviews       # 리뷰 작성
GET    /api/products/{id}/questions     # 상품 문의 목록
POST   /api/products/{id}/questions     # 상품 문의 작성
POST   /api/products/{id}/like          # 좋아요 추가
DELETE /api/products/{id}/like          # 좋아요 취소

# 검색
GET    /api/products/search?q=키워드&category=1&brand=2&minPrice=1000&maxPrice=50000
```

---

#### 장바구니

```
GET    /api/cart                    # 장바구니 조회
POST   /api/cart/items              # 장바구니 추가
PATCH  /api/cart/items/{id}         # 수량 수정
DELETE /api/cart/items/{id}         # 단일 삭제
DELETE /api/cart/items              # 선택 삭제 (query: ?ids=1,2,3)
DELETE /api/cart                    # 전체 삭제

POST   /api/cart/merge              # 게스트 장바구니 병합
```

---

#### 주문

```
POST   /api/orders                      # 주문 생성
GET    /api/orders                      # 주문 목록 (페이징, 필터)
GET    /api/orders/{id}                 # 주문 상세
PATCH  /api/orders/{id}/cancel          # 주문 취소
PATCH  /api/orders/{id}/confirm         # 구매 확정

GET    /api/orders/{id}/items           # 주문 상품 목록
PATCH  /api/orders/{id}/items/{itemId}/cancel    # 주문 상품 취소
POST   /api/orders/{id}/items/{itemId}/exchange  # 교환 신청
POST   /api/orders/{id}/items/{itemId}/return    # 반품 신청

GET    /api/orders/{id}/delivery        # 배송 조회
```

---

#### 쿠폰

```
GET    /api/coupons                     # 사용 가능한 쿠폰 목록
POST   /api/coupons/{id}/issue          # 쿠폰 발급
POST   /api/coupons/code                # 쿠폰 코드 등록
DELETE /api/coupons/{id}                # 쿠폰 삭제

GET    /api/coupons/applicable?orderAmount=50000&productIds=1,2,3
                                         # 주문에 적용 가능한 쿠폰 조회
```

---

#### 포인트

```
GET    /api/points                      # 포인트 잔액 조회
GET    /api/points/history              # 포인트 이력 (페이징)
POST   /api/points/charge               # 포인트 충전 (관리자)
```

---

#### 결제

```
POST   /api/payments                    # 결제 요청
GET    /api/payments/{id}               # 결제 상세
POST   /api/payments/{id}/confirm       # 결제 승인
POST   /api/payments/{id}/cancel        # 결제 취소

GET    /api/payments/callback           # 결제 콜백 (Toss Payments)
```

---

#### 브랜드

```
GET    /api/brands                      # 브랜드 목록
GET    /api/brands/{id}                 # 브랜드 상세
GET    /api/brands/{id}/products        # 브랜드별 상품 목록
```

---

#### 커뮤니티

```
GET    /api/posts                       # 게시글 목록 (페이징)
POST   /api/posts                       # 게시글 작성
GET    /api/posts/{id}                  # 게시글 상세
PUT    /api/posts/{id}                  # 게시글 수정
DELETE /api/posts/{id}                  # 게시글 삭제

POST   /api/posts/{id}/like             # 좋아요
DELETE /api/posts/{id}/like             # 좋아요 취소
POST   /api/posts/{id}/scrap            # 스크랩
DELETE /api/posts/{id}/scrap            # 스크랩 취소

GET    /api/posts/{id}/comments         # 댓글 목록
POST   /api/posts/{id}/comments         # 댓글 작성
PUT    /api/comments/{id}               # 댓글 수정
DELETE /api/comments/{id}               # 댓글 삭제
```

---

#### 관리자 - 상품 관리

```
POST   /api/admin/products              # 상품 등록
PUT    /api/admin/products/{id}         # 상품 수정
DELETE /api/admin/products/{id}         # 상품 삭제
GET    /api/admin/products              # 상품 목록 (관리자용)
GET    /api/admin/products/{id}         # 상품 상세 (관리자용)

POST   /api/admin/products/{id}/options         # 옵션 추가
PUT    /api/admin/products/{id}/options/{optionId}    # 옵션 수정
DELETE /api/admin/products/{id}/options/{optionId}    # 옵션 삭제
```

---

#### 관리자 - 주문 관리

```
GET    /api/admin/orders                # 전체 주문 목록
GET    /api/admin/orders/{id}           # 주문 상세
PATCH  /api/admin/orders/{id}/status    # 주문 상태 변경
PATCH  /api/admin/orders/{id}/delivery  # 배송 정보 수정
```

---

#### 관리자 - 사용자 관리

```
GET    /api/admin/users                 # 사용자 목록
GET    /api/admin/users/{id}            # 사용자 상세
PATCH  /api/admin/users/{id}/role       # 권한 변경
PATCH  /api/admin/users/{id}/status     # 계정 상태 변경
```

---

#### 관리자 - 재고 관리

```
GET    /api/admin/inventory             # 재고 목록
GET    /api/admin/inventory/{id}        # 재고 상세
POST   /api/admin/inventory/receipt     # 입고 처리
POST   /api/admin/inventory/outbound    # 출고 처리
GET    /api/admin/inventory/{id}/history    # 재고 이력
```

---

#### 관리자 - 브랜드 관리

```
GET    /api/admin/brands                # 브랜드 목록
POST   /api/admin/brands                # 브랜드 등록
PUT    /api/admin/brands/{id}           # 브랜드 수정
DELETE /api/admin/brands/{id}           # 브랜드 삭제
```

---

#### 관리자 - 쿠폰 관리

```
GET    /api/admin/coupons               # 쿠폰 목록
POST   /api/admin/coupons               # 쿠폰 생성
PUT    /api/admin/coupons/{id}          # 쿠폰 수정
DELETE /api/admin/coupons/{id}          # 쿠폰 삭제
POST   /api/admin/coupons/{id}/issue    # 쿠폰 일괄 발급
```

---

#### 관리자 - 통계/로그

```
GET    /api/admin/statistics/sales      # 매출 통계
GET    /api/admin/statistics/products   # 상품 통계
GET    /api/admin/statistics/users      # 사용자 통계

GET    /api/admin/logs                  # 활동 로그 목록
GET    /api/admin/logs/{id}             # 로그 상세
```

---

### 5.4 HTTP 상태 코드 가이드

```
200 OK              - 성공 (조회, 수정)
201 Created         - 생성 성공
204 No Content      - 성공 (응답 본문 없음, 삭제)

400 Bad Request     - 잘못된 요청 (Validation 실패, 비즈니스 규칙 위반)
401 Unauthorized    - 인증 실패 (로그인 필요)
403 Forbidden       - 권한 없음 (인증은 되었으나 접근 불가)
404 Not Found       - 리소스 없음
409 Conflict        - 충돌 (중복 데이터 등)
422 Unprocessable Entity  - 처리 불가능한 엔티티 (Validation은 통과했으나 비즈니스 로직 실패)

500 Internal Server Error  - 서버 내부 오류
503 Service Unavailable    - 서비스 이용 불가 (점검 등)
```

---

### 5.5 버전 관리 전략

#### URL 경로 버저닝 (권장)

```
/api/v1/orders
/api/v2/orders
```

**장점:**
- 명시적이고 이해하기 쉬움
- 브라우저에서 직접 접근 가능
- API 문서화에 유리

#### 헤더 버저닝

```
GET /api/orders
Accept: application/vnd.coshouse.v1+json
```

**장점:**
- URL이 깔끔함
- RESTful 원칙에 더 부합

#### 권장 전략

```java
// V1 Controller
@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiControllerV1 {
    // V1 구현
}

// V2 Controller (새로운 필드 추가, 응답 구조 변경 등)
@RestController
@RequestMapping("/api/v2/orders")
public class OrderApiControllerV2 {
    // V2 구현
}

// 버전별 설정
@Configuration
public class ApiVersionConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**").allowedOrigins("*");
                registry.addMapping("/api/v2/**").allowedOrigins("*");
            }
        };
    }
}
```

---

## 6. 프론트-백 분리 전략

### 6.1 현재 아키텍처 분석

**현재 구조: Monolithic SSR**

```
Browser → Spring Boot (Thymeleaf SSR)
                ↓
           Database
```

**문제점:**
1. View와 API가 혼재 → 유지보수 어려움
2. 프론트엔드 기술 스택 제한 (Thymeleaf, jQuery)
3. 모바일 앱 개발 시 별도 API 필요
4. SEO는 유리하지만 UX 개선 어려움

---

### 6.2 분리 전략 옵션

#### 옵션 1: 점진적 분리 (하이브리드)

**구조:**

```
Browser → Spring Boot
          ├─ Thymeleaf Views (기존 유지)
          └─ REST API (/api/**)
                ↓
            Database
```

**장점:**
- 기존 View 유지하면서 점진적 전환
- 리스크 최소화
- 모바일 앱 개발 가능 (API 활용)

**단점:**
- 장기적으로 복잡성 증가
- 일부 중복 코드 발생

**구현 방안:**

```java
// View Controller (기존 유지)
@Controller
@RequestMapping("/orders")
public class OrderViewController {

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal User user) {
        // API 호출 또는 직접 Service 호출
        List<OrderSummary> orders = orderService.getMyOrders(user);
        model.addAttribute("orders", orders);
        return "order/list";
    }

    @GetMapping("/preview")
    public String preview(Model model, @RequestParam List<Long> cartItemIds) {
        OrderPreviewResponse preview = orderService.getPreview(cartItemIds);
        model.addAttribute("preview", preview);
        return "order/preview";
    }
}

// API Controller (신규)
@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(@AuthenticationPrincipal User user) {
        List<OrderResponse> orders = orderService.getMyOrders(user);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse order = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
```

---

#### 옵션 2: 완전 분리 (SPA + API)

**구조:**

```
Browser → React/Vue/Angular (Frontend)
                ↓ (HTTP REST API)
          Spring Boot (Backend API only)
                ↓
            Database
```

**장점:**
- 완전한 관심사 분리
- 프론트엔드 최신 기술 활용 (React, Vue, etc.)
- 모바일 앱과 API 공유
- UX 개선 용이 (SPA의 빠른 반응성)

**단점:**
- 초기 전환 비용 큰
- SEO 불리 (SSR 또는 Pre-rendering 필요)
- 인프라 복잡도 증가 (Frontend 서버 별도 필요)

**구현 방안:**

```
# 프로젝트 구조 분리
cos-backend/          # Spring Boot (API only)
├── src/
│   └── main/
│       └── java/
│           └── com/bird/cos/
│               ├── controller/api/     # API Controller만
│               ├── service/
│               ├── repository/
│               └── domain/
└── build.gradle

cos-frontend/         # React (별도 프로젝트)
├── src/
│   ├── components/
│   ├── pages/
│   ├── api/          # API 호출 로직
│   └── App.tsx
└── package.json
```

**Spring Boot 설정 (API only):**

```java
// CORS 설정 (Frontend 허용)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://coshouse.com")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

// Security 설정 (JWT 기반으로 전환)
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)  // SPA는 CSRF 비활성화
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/**").authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

#### 옵션 3: 마이크로 프론트엔드 (MFE)

**구조:**

```
Browser → Gateway
          ├─ Order MFE (React)
          ├─ Product MFE (Vue)
          └─ Admin MFE (Angular)
                ↓
          Backend API (Spring Boot)
```

**장점:**
- 팀별 독립 개발 가능
- 기술 스택 다양화
- 배포 독립성

**단점:**
- 아키텍처 복잡도 매우 높음
- 초기 투자 비용 큼
- 중소 규모 프로젝트에는 과도함

---

### 6.3 권장 전략: 점진적 분리

**1단계: API 분리 (현재 → 3개월)**

```
목표: API Controller 분리 및 표준화

작업:
1. View Controller와 API Controller 완전 분리
2. RESTful API 명세 작성 및 구현
3. 표준 응답 형식 적용 (ApiResponse)
4. API 문서화 (Swagger/OpenAPI)
5. 인증/인가 개선 (JWT 도입 검토)

결과:
- /api/** : REST API (JSON 응답)
- /** : Thymeleaf View (기존 유지)
```

**2단계: 프론트엔드 기술 도입 (3~6개월)**

```
목표: 일부 페이지 SPA 전환

작업:
1. 복잡한 UI부터 React/Vue로 전환 (예: 주문 페이지)
2. Thymeleaf와 SPA 혼합 사용
3. API 호출로 데이터 렌더링
4. 점진적으로 전환 범위 확대

결과:
- 주요 페이지: SPA (React/Vue)
- 단순 페이지: Thymeleaf 유지
```

**3단계: 완전 분리 (6~12개월)**

```
목표: Frontend와 Backend 완전 분리

작업:
1. 모든 페이지 SPA로 전환
2. Backend는 API만 제공
3. Frontend 빌드 자동화
4. CI/CD 파이프라인 구축

결과:
- Backend: API Server (Spring Boot)
- Frontend: SPA (React/Vue) - 별도 서버
- Mobile: 동일 API 사용
```

---

### 6.4 구체적 전환 예시

#### Before (Thymeleaf + jQuery)

```html
<!-- order/list.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<table>
    <tr th:each="order : ${orders}">
        <td th:text="${order.orderId}"></td>
        <td th:text="${order.totalAmount}"></td>
        <td th:text="${order.status}"></td>
    </tr>
</table>

<script>
$('.cancel-btn').click(function() {
    const orderId = $(this).data('order-id');
    $.post('/order/cancel', {orderId}, function(response) {
        alert('취소되었습니다');
        location.reload();
    });
});
</script>
</body>
</html>
```

#### After (React + REST API)

```typescript
// OrderListPage.tsx
import React, { useEffect, useState } from 'react';
import { orderApi } from '@/api/orderApi';

export const OrderListPage: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      const response = await orderApi.getMyOrders();
      setOrders(response.data);
    } catch (error) {
      console.error('주문 목록 조회 실패', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (orderId: number) => {
    if (!confirm('주문을 취소하시겠습니까?')) return;

    try {
      await orderApi.cancelOrder(orderId);
      alert('취소되었습니다');
      loadOrders();  // 목록 새로고침
    } catch (error) {
      alert('취소 실패');
    }
  };

  if (loading) return <div>로딩중...</div>;

  return (
    <div>
      <h1>주문 목록</h1>
      <table>
        <thead>
          <tr>
            <th>주문번호</th>
            <th>금액</th>
            <th>상태</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.orderId}>
              <td>{order.orderId}</td>
              <td>{order.totalAmount.toLocaleString()}원</td>
              <td>{order.status}</td>
              <td>
                <button onClick={() => handleCancel(order.orderId)}>
                  취소
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// api/orderApi.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// 요청 인터셉터 (JWT 토큰 자동 추가)
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const orderApi = {
  getMyOrders: () => apiClient.get<ApiResponse<Order[]>>('/orders'),

  getOrderDetail: (orderId: number) =>
    apiClient.get<ApiResponse<OrderDetail>>(`/orders/${orderId}`),

  createOrder: (request: OrderCreateRequest) =>
    apiClient.post<ApiResponse<OrderResponse>>('/orders', request),

  cancelOrder: (orderId: number) =>
    apiClient.patch<ApiResponse<void>>(`/orders/${orderId}/cancel`),
};
```

---

### 6.5 SEO 대응 방안 (SPA 전환 시)

#### 옵션 1: SSR (Server-Side Rendering)

```
React: Next.js
Vue: Nuxt.js

장점:
- SEO 완벽 지원
- 초기 로딩 속도 빠름

단점:
- Node.js 서버 필요
- 서버 리소스 사용 증가
```

#### 옵션 2: Pre-rendering

```
도구: react-snap, prerender.io

장점:
- 정적 HTML 생성 → SEO 지원
- 서버 부담 없음

단점:
- 동적 콘텐츠 제한적
```

#### 옵션 3: Hybrid (일부만 SSR)

```
전략:
- 공개 페이지 (상품 목록, 상세): SSR
- 인증 필요 페이지 (마이페이지, 주문): CSR
```

---

## 7. 우선순위별 실행 계획

### 🔴 높음 (즉시 착수)

1. **Controller API 분리** (2주)
   - View Controller와 API Controller 완전 분리
   - `/api/**` 경로로 통일

2. **예외 처리 개선** (1주)
   - Service의 try-catch 제거
   - GlobalExceptionHandler 강화
   - ErrorCode enum 확장

3. **트랜잭션 경계 정리** (1주)
   - Service 트랜잭션 최소화
   - 이벤트 기반 후속 처리

4. **Entity 비즈니스 로직 이동** (2주)
   - Order, User 엔티티 리팩토링
   - 불변성 보장

### 🟡 중간 (1~2개월 내)

5. **RESTful API 재설계** (3주)
   - 전체 API 명세 작성
   - 표준 응답 형식 적용
   - HTTP 메서드 정규화

6. **DTO 구조 개선** (2주)
   - Request/Response 분리
   - Validation 강화
   - ApiResponse 통일

7. **테스트 코드 작성** (4주)
   - Service 단위 테스트
   - Controller 통합 테스트
   - 커버리지 80% 목표

8. **보안 강화** (2주)
   - CSRF 재활성화
   - XSS 필터
   - 비밀번호 정책

### 🟢 낮음 (3개월 이후)

9. **성능 최적화** (3주)
   - N+1 해결
   - 캐싱 적용
   - 인덱싱 최적화

10. **로깅/모니터링** (2주)
    - 구조화된 로깅
    - Prometheus 메트릭
    - Grafana 대시보드

11. **API 문서화** (1주)
    - SpringDoc OpenAPI
    - Swagger UI

12. **프론트엔드 전환 검토** (장기)
    - 점진적 SPA 전환
    - React/Vue 도입

---

## 8. 요약 및 결론

### 핵심 개선 사항

| 영역 | 현재 문제 | 개선 방향 |
|-----|---------|---------|
| **Controller** | View와 API 혼재, 비RESTful | 완전 분리, RESTful 재설계 |
| **Service** | 트랜잭션 비효율, 책임 과다 | 트랜잭션 최소화, 이벤트 기반 |
| **Entity** | 비즈니스 로직 부족, 불변성 위반 | Domain 중심 설계, 불변성 보장 |
| **Exception** | Service에서 예외 처리 | GlobalExceptionHandler 활용 |
| **DTO** | Request/Response 혼재 | 명확한 분리, 표준화 |
| **Repository** | Querydsl 과도, N+1 문제 | 쿼리 최적화, Batch Fetch |
| **Security** | CSRF 비활성화, 취약점 | CSRF 재활성화, XSS 방지 |
| **Test** | 테스트 부족 | 포괄적 테스트 작성 |
| **API** | 비표준 URL, 메서드 오용 | RESTful 원칙 준수 |
| **Architecture** | Monolithic SSR | 점진적 API 분리 |

### 기대 효과

1. **유지보수성 향상**
   - 명확한 계층 분리
   - 코드 가독성 개선
   - 버그 추적 용이

2. **확장성 확보**
   - 모바일 앱 개발 가능
   - 마이크로서비스 전환 용이
   - 팀 단위 독립 개발

3. **품질 향상**
   - 테스트 커버리지 증가
   - 버그 감소
   - 성능 개선

4. **보안 강화**
   - 취약점 제거
   - 데이터 보호
   - 컴플라이언스 준수

---

## 부록

### A. 참고 자료

- Spring Boot Best Practices: https://docs.spring.io/spring-boot/docs/current/reference/html/
- RESTful API Design: https://restfulapi.net/
- Clean Code by Robert C. Martin
- Domain-Driven Design by Eric Evans

### B. 코드 리뷰 체크리스트

```markdown
## Controller
- [ ] View와 API Controller 분리되었는가?
- [ ] RESTful 원칙을 준수하는가?
- [ ] HTTP 메서드를 올바르게 사용하는가?
- [ ] 예외를 Controller에서 처리하지 않는가?
- [ ] 응답 형식이 통일되어 있는가?

## Service
- [ ] 트랜잭션 경계가 명확한가?
- [ ] 비즈니스 로직이 적절히 분리되어 있는가?
- [ ] 외부 서비스 호출이 트랜잭션 밖에 있는가?
- [ ] try-catch를 남용하지 않는가?

## Entity
- [ ] 비즈니스 로직이 Entity에 있는가?
- [ ] 불변성이 보장되는가?
- [ ] 연관관계가 명확한가?
- [ ] Setter를 무분별하게 사용하지 않는가?

## Repository
- [ ] N+1 문제가 없는가?
- [ ] Querydsl이 필요한 경우에만 사용되는가?
- [ ] 쿼리 최적화가 적용되었는가?

## DTO
- [ ] Request/Response가 분리되어 있는가?
- [ ] Validation이 적용되어 있는가?
- [ ] 불필요한 필드가 없는가?

## Test
- [ ] 단위 테스트가 작성되어 있는가?
- [ ] 통합 테스트가 작성되어 있는가?
- [ ] 경계값 테스트가 있는가?
- [ ] 예외 상황 테스트가 있는가?

## Security
- [ ] 인증/인가가 적용되어 있는가?
- [ ] CSRF 보호가 필요한 곳에 적용되었는가?
- [ ] XSS 방지가 되어 있는가?
- [ ] SQL Injection에 안전한가?

## General
- [ ] 코드가 읽기 쉬운가?
- [ ] SOLID 원칙을 따르는가?
- [ ] DRY 원칙을 따르는가?
- [ ] 적절한 로깅이 있는가?
```

---

**문서 버전**: 1.0
**최종 수정일**: 2025-10-14
**작성자**: Claude Code Analysis
**검토자**: (프로젝트 팀에서 지정)