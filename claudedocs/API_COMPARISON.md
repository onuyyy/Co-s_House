# 현재 API vs RESTful API 비교 명세서

> **프로젝트**: Co's House E-commerce Platform
> **작성일**: 2025-10-14
> **목적**: 현재 API 구조와 RESTful 원칙에 맞춘 개선안 비교

---

## 📋 목차

1. [인증/회원가입 API](#1-인증회원가입-api)
2. [사용자 관리 API](#2-사용자-관리-api)
3. [주문 API](#3-주문-api)
4. [상품 API](#4-상품-api)
5. [장바구니 API](#5-장바구니-api)
6. [결제 API](#6-결제-api)
7. [리뷰 API](#7-리뷰-api)
8. [커뮤니티 API](#8-커뮤니티-api)
9. [마이페이지 API](#9-마이페이지-api)
10. [쿠폰/포인트 API](#10-쿠폰포인트-api)
11. [관리자 API](#11-관리자-api)
12. [공통 문제점 및 개선 방향](#12-공통-문제점-및-개선-방향)

---

## 1. 인증/회원가입 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| POST | `/controller/register/register` | 회원가입 | ❌ `/controller` 불필요, RESTful하지 않음 |
| POST | `/controller/register/login` | 로그인 | ❌ `/controller` 불필요 |
| GET | `/controller/register/login` | 로그인 페이지 | ⚠️ API와 View 분리 필요 |
| POST | `/controller/register/logout` | 로그아웃 | ⚠️ POST 대신 DELETE 권장 |
| GET | `/controller/register/me` | 현재 사용자 정보 | ❌ 경로 비표준 |
| POST | `/auth/email/verification/request` | 이메일 인증 요청 | ✅ 경로는 적절하나 `/api` prefix 필요 |
| POST | `/auth/email/verification/confirm` | 이메일 인증 확인 | ✅ 경로는 적절하나 `/api` prefix 필요 |
| POST | `/account/reset/request` | 비밀번호 재설정 요청 | ⚠️ `/api` prefix 필요 |
| POST | `/account/reset/complete` | 비밀번호 재설정 완료 | ⚠️ `/api` prefix 필요 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| POST | `/api/auth/register` | 회원가입 | `{ "success": true, "data": { "userId": 123 } }` |
| POST | `/api/auth/login` | 로그인 | `{ "success": true, "data": { "token": "..." } }` |
| DELETE | `/api/auth/logout` | 로그아웃 | `{ "success": true }` |
| POST | `/api/auth/refresh` | 토큰 갱신 | `{ "success": true, "data": { "token": "..." } }` |
| GET | `/api/users/me` | 현재 사용자 정보 | `{ "success": true, "data": { "userId": 123, ... } }` |
| POST | `/api/auth/email/send` | 이메일 인증 발송 | `{ "success": true }` |
| POST | `/api/auth/email/verify` | 이메일 인증 확인 | `{ "success": true }` |
| POST | `/api/auth/password/reset` | 비밀번호 재설정 요청 | `{ "success": true }` |
| PUT | `/api/auth/password` | 비밀번호 변경 | `{ "success": true }` |

**개선 사항:**
- `/controller` 경로 제거
- `/api` prefix 통일
- RESTful HTTP 메서드 사용 (DELETE for logout, PUT for password change)
- 표준 응답 형식 적용

---

## 2. 사용자 관리 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/mypage/mypageUser` | 내 정보 조회 (View) | ❌ RESTful하지 않음 |
| POST | `/mypage/mypageUserUpdate` | 내 정보 수정 | ❌ POST 대신 PUT/PATCH 사용 |
| POST | `/mypage/validatePassword` | 비밀번호 검증 | ⚠️ @ResponseBody, `/api` prefix 필요 |
| POST | `/mypage/mypageUserDelete` | 회원 탈퇴 | ❌ POST 대신 DELETE 사용 |
| GET | `/api/mypage/shipping-addresses` | 배송지 목록 | ✅ RESTful함 |
| POST | `/api/mypage/shipping-addresses` | 배송지 추가 | ✅ RESTful함 |
| PUT | `/api/mypage/shipping-addresses/{id}` | 배송지 수정 | ✅ RESTful함 |
| DELETE | `/api/mypage/shipping-addresses/{id}` | 배송지 삭제 | ✅ RESTful함|

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/users/me` | 내 정보 조회 | `{ "success": true, "data": { "userId": 123, "userName": "..." } }` |
| PUT | `/api/users/me` | 내 정보 수정 (전체) | `{ "success": true, "data": { ... } }` |
| PATCH | `/api/users/me` | 내 정보 수정 (부분) | `{ "success": true }` |
| PATCH | `/api/users/me/password` | 비밀번호 변경 | `{ "success": true }` |
| POST | `/api/users/me/password/verify` | 비밀번호 검증 | `{ "success": true, "data": { "valid": true } }` |
| PATCH | `/api/users/me/email` | 이메일 변경 | `{ "success": true }` |
| DELETE | `/api/users/me` | 회원 탈퇴 | `{ "success": true }` |
| GET | `/api/users/me/addresses` | 배송지 목록 | `{ "success": true, "data": [...] }` |
| POST | `/api/users/me/addresses` | 배송지 추가 | `{ "success": true, "data": { "addressId": 456 } }` |
| PUT | `/api/users/me/addresses/{id}` | 배송지 수정 | `{ "success": true }` |
| DELETE | `/api/users/me/addresses/{id}` | 배송지 삭제 | `{ "success": true }` |
| PATCH | `/api/users/me/addresses/{id}/default` | 기본 배송지 설정 | `{ "success": true }` |

**개선 사항:**
- `/mypage` → `/api/users/me`로 통일
- HTTP 메서드 정규화 (PUT/PATCH/DELETE 활용)
- 일관된 응답 형식

---

## 3. 주문 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| POST | `/order/preview` | 주문 미리보기 (View) | ❌ API와 View 혼재, `/api` prefix 없음 |
| POST | `/order/create` | 주문 생성 | ❌ POST `/api/orders`로 변경 |
| GET | `/order/my-coupons` | 쿠폰 목록 | ❌ `/order` 하위가 아님 |
| GET | `/order/my-coupons/{id}` | 쿠폰 검증 | ❌ 별도 리소스로 분리 |
| GET | `/order/my-points` | 포인트 조회 | ❌ 별도 리소스로 분리 |
| GET/POST | `/mypage/my-orders` | 주문 목록 (View) | ❌ GET/POST 혼용, `/api` prefix 없음 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| POST | `/api/orders` | 주문 생성 | `{ "success": true, "data": { "orderId": 789, "totalAmount": 50000 } }` |
| GET | `/api/orders` | 주문 목록 (페이징) | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/orders/{orderId}` | 주문 상세 | `{ "success": true, "data": { "orderId": 789, "items": [...] } }` |
| PATCH | `/api/orders/{orderId}/cancel` | 주문 취소 | `{ "success": true }` |
| PATCH | `/api/orders/{orderId}/confirm` | 구매 확정 | `{ "success": true }` |
| GET | `/api/orders/{orderId}/items` | 주문 상품 목록 | `{ "success": true, "data": [...] }` |
| POST | `/api/orders/{orderId}/items/{itemId}/exchange` | 교환 신청 | `{ "success": true }` |
| POST | `/api/orders/{orderId}/items/{itemId}/return` | 반품 신청 | `{ "success": true }` |
| GET | `/api/orders/{orderId}/delivery` | 배송 조회 | `{ "success": true, "data": { "status": "...", "trackingNumber": "..." } }` |
| POST | `/api/orders/preview` | 주문 미리보기 (계산만) | `{ "success": true, "data": { "totalAmount": 50000, "discountAmount": 5000 } }` |

**개선 사항:**
- `/order` → `/api/orders`
- 쿠폰/포인트를 별도 리소스로 분리
- 리소스 계층 구조 명확화 (`/orders/{orderId}/items/{itemId}`)
- 상태 변경은 PATCH 사용

---

## 4. 상품 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/product` | 상품 목록 (View) | ⚠️ 복수형 `/products` 권장 |
| GET | `/product/{productId}` | 상품 상세 (View) | ⚠️ `/api` prefix 필요 |
| GET | `/product/category/{categoryId}` | 카테고리별 상품 | ⚠️ 쿼리 파라미터 권장 |
| GET | `/product/category/{categoryId}/price-asc` | 가격 오름차순 정렬 | ❌ 쿼리 파라미터로 처리 |
| GET | `/product/category/{categoryId}/price-desc` | 가격 내림차순 정렬 | ❌ 쿼리 파라미터로 처리 |
| GET | `/product/{productId}/reviews` | 상품 리뷰 목록 (View) | ⚠️ `/api` prefix 필요 |
| POST | `/product/{productId}/reviews` | 리뷰 작성 (View Form) | ⚠️ API와 View 분리 |
| POST | `/api/products/{productId}/like` | 좋아요 추가 | ✅ RESTful함 |
| GET | `/api/products/{productId}/like` | 좋아요 상태 조회 | ⚠️ 단수 리소스로 처리 |
| GET | `/api/product/{productId}/reviews` | 리뷰 목록 API | ⚠️ `/api/products` 복수형 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/products` | 상품 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/products?category=1&sort=price,asc` | 카테고리별 + 정렬 | `{ "success": true, "data": {...} }` |
| GET | `/api/products/{productId}` | 상품 상세 | `{ "success": true, "data": { "productId": 1, "title": "..." } }` |
| GET | `/api/products/{productId}/options` | 상품 옵션 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/products/{productId}/reviews` | 리뷰 목록 | `{ "success": true, "data": { "content": [...], "averageRating": 4.5 } }` |
| POST | `/api/products/{productId}/reviews` | 리뷰 작성 | `{ "success": true, "data": { "reviewId": 123 } }` |
| GET | `/api/products/{productId}/questions` | 상품 문의 목록 | `{ "success": true, "data": [...] }` |
| POST | `/api/products/{productId}/questions` | 상품 문의 작성 | `{ "success": true, "data": { "questionId": 456 } }` |
| POST | `/api/products/{productId}/like` | 좋아요 추가 | `{ "success": true }` |
| DELETE | `/api/products/{productId}/like` | 좋아요 취소 | `{ "success": true }` |
| GET | `/api/products/{productId}/like` | 좋아요 상태 | `{ "success": true, "data": { "liked": true } }` |
| GET | `/api/products/search?q=키워드&minPrice=1000&maxPrice=50000` | 상품 검색 | `{ "success": true, "data": {...} }` |

**개선 사항:**
- `/product` → `/api/products` (복수형, `/api` prefix)
- 정렬/필터는 쿼리 파라미터로 통일
- 좋아요는 단일 리소스로 처리 (POST/DELETE/GET)

---

## 5. 장바구니 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| POST | `/api/cart` | 장바구니 추가 | ⚠️ `/api/cart/items`로 명확히 |
| GET | `/api/cart` | 장바구니 조회 | ✅ 적절함 |
| PATCH | `/api/cart/{cartItemId}` | 수량 수정 | ⚠️ `/api/cart/items/{id}` 권장 |
| PATCH | `/api/cart/{cartItemId}/options` | 옵션 수정 | ⚠️ `/api/cart/items/{id}` 권장 |
| DELETE | `/api/cart/{cartItemId}` | 단일 삭제 | ⚠️ `/api/cart/items/{id}` 권장 |
| DELETE | `/api/cart?ids=1,2,3` | 다중 삭제 | ⚠️ Request Body 사용 권장 |
| GET | `/api/cart/checkout-info` | 결제 정보 | ✅ 적절함 |
| POST | `/api/cart/merge` | 게스트 장바구니 병합 | ✅ 적절함 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/cart` | 장바구니 조회 | `{ "success": true, "data": { "items": [...], "totalPrice": 50000 } }` |
| POST | `/api/cart/items` | 장바구니 추가 | `{ "success": true, "data": { "cartItemId": 123 } }` |
| PATCH | `/api/cart/items/{itemId}` | 수량 수정 | `{ "success": true }` |
| PATCH | `/api/cart/items/{itemId}/options` | 옵션 수정 | `{ "success": true }` |
| DELETE | `/api/cart/items/{itemId}` | 단일 삭제 | `{ "success": true }` |
| DELETE | `/api/cart/items` (Body: `{"ids": [1,2,3]}`) | 선택 삭제 | `{ "success": true }` |
| DELETE | `/api/cart` | 전체 삭제 | `{ "success": true }` |
| POST | `/api/cart/merge` | 게스트 장바구니 병합 | `{ "success": true }` |
| GET | `/api/cart/summary` | 장바구니 요약 (개수, 총액) | `{ "success": true, "data": { "itemCount": 3, "totalPrice": 50000 } }` |

**개선 사항:**
- `/api/cart/{id}` → `/api/cart/items/{id}` (명확한 리소스 구분)
- 쿼리 파라미터 대신 Request Body 사용 (DELETE)
- 장바구니 요약 엔드포인트 추가

---

## 6. 결제 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| POST | `/api/payments/intent` | 결제 Intent 생성 | ⚠️ `/api/payments`로 통일 권장 |
| POST | `/api/payments/confirm` | 결제 승인 | ⚠️ `/{paymentId}/confirm` 권장 |
| POST | `/api/payments/cancel` | 결제 취소 | ⚠️ `/{paymentId}/cancel` 권장 |
| POST | `/api/payments/webhook` | Webhook 수신 | ✅ 적절함 |
| GET | `/payment/success` | 결제 성공 페이지 (View) | ⚠️ View 분리 |
| GET | `/payment/fail` | 결제 실패 페이지 (View) | ⚠️ View 분리 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| POST | `/api/payments` | 결제 요청 | `{ "success": true, "data": { "paymentId": "abc123", "approvalUrl": "..." } }` |
| GET | `/api/payments/{paymentId}` | 결제 상세 조회 | `{ "success": true, "data": { "paymentId": "...", "status": "PAID" } }` |
| POST | `/api/payments/{paymentId}/confirm` | 결제 승인 | `{ "success": true }` |
| POST | `/api/payments/{paymentId}/cancel` | 결제 취소 | `{ "success": true }` |
| POST | `/api/payments/webhook` | Toss Webhook | `{ "success": true }` |
| GET | `/api/payments/callback` | 결제 콜백 (리다이렉트) | Redirect to frontend |

**개선 사항:**
- 결제 ID를 경로에 포함
- Intent/Confirm을 하나의 플로우로 통합
- View 엔드포인트 분리

---

## 7. 리뷰 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/reviews` | 전체 리뷰 목록 (View) | ⚠️ `/api` prefix, 상품별 리뷰로 통합 |
| GET | `/product/{productId}/reviews` | 상품별 리뷰 (View) | ⚠️ `/api` prefix 필요 |
| GET | `/reviews/{reviewId}` | 리뷰 상세 (View) | ⚠️ `/api` prefix 필요 |
| GET | `/product/{productId}/reviews/new` | 리뷰 작성 폼 (View) | ⚠️ View 분리 |
| POST | `/product/{productId}/reviews` | 리뷰 작성 (Form) | ⚠️ API와 View 분리 |
| GET | `/reviews/{reviewId}/edit` | 리뷰 수정 폼 (View) | ⚠️ View 분리 |
| POST | `/reviews/{reviewId}/edit` | 리뷰 수정 (Form) | ❌ PUT/PATCH 사용 |
| POST | `/reviews/{reviewId}/delete` | 리뷰 삭제 | ❌ DELETE 메서드 사용 |
| POST | `/api/reviews/{reviewId}` | 리뷰 수정 (AJAX) | ❌ PUT/PATCH 사용 |
| DELETE | `/api/reviews/{reviewId}` | 리뷰 삭제 (AJAX) | ✅ RESTful함 |
| GET | `/api/product/{productId}/reviews` | 리뷰 목록 API | ⚠️ `/api/products` 복수형 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/reviews` | 전체 리뷰 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/products/{productId}/reviews` | 상품별 리뷰 목록 | `{ "success": true, "data": { "content": [...], "averageRating": 4.5 } }` |
| GET | `/api/reviews/{reviewId}` | 리뷰 상세 | `{ "success": true, "data": { "reviewId": 1, "title": "...", "content": "..." } }` |
| POST | `/api/products/{productId}/reviews` | 리뷰 작성 | `{ "success": true, "data": { "reviewId": 123 } }` |
| PUT | `/api/reviews/{reviewId}` | 리뷰 수정 (전체) | `{ "success": true }` |
| PATCH | `/api/reviews/{reviewId}` | 리뷰 수정 (부분) | `{ "success": true }` |
| DELETE | `/api/reviews/{reviewId}` | 리뷰 삭제 | `{ "success": true }` |
| POST | `/api/reviews/{reviewId}/images` | 리뷰 이미지 추가 | `{ "success": true, "data": { "imageId": 456 } }` |
| DELETE | `/api/reviews/{reviewId}/images/{imageId}` | 리뷰 이미지 삭제 | `{ "success": true }` |
| GET | `/api/users/me/reviews` | 내가 작성한 리뷰 | `{ "success": true, "data": [...] }` |

**개선 사항:**
- View와 API 완전 분리
- POST → PUT/PATCH/DELETE로 변경
- `/product` → `/products` (복수형)
- 이미지 관리 엔드포인트 추가

---

## 8. 커뮤니티 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/posts` | 게시글 목록 (View) | ⚠️ `/api` prefix 필요 |
| GET | `/posts/new` | 게시글 작성 폼 (View) | ⚠️ View 분리 |
| POST | `/posts/new` | 게시글 작성 (Form) | ❌ `/api/posts`로 변경 |
| GET | `/posts/{postId}` | 게시글 상세 (View) | ⚠️ `/api` prefix 필요 |
| POST | `/posts/{postId}/scrap` | 스크랩 추가 | ⚠️ `/api` prefix 필요 |
| POST | `/api/comments` | 댓글 작성 | ⚠️ `/api/posts/{postId}/comments` 권장 |
| GET | `/api/comments/{postId}` | 댓글 목록 | ❌ 경로 혼란 (postId인데 /comments 하위) |
| GET | `/mypage/scraps` | 스크랩 목록 (View) | ⚠️ `/api` prefix 필요 |
| POST | `/mypage/scraps/delete` | 스크랩 삭제 | ❌ DELETE 메서드 사용 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/posts` | 게시글 목록 (페이징) | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| POST | `/api/posts` | 게시글 작성 | `{ "success": true, "data": { "postId": 123 } }` |
| GET | `/api/posts/{postId}` | 게시글 상세 | `{ "success": true, "data": { "postId": 1, "title": "...", "views": 100 } }` |
| PUT | `/api/posts/{postId}` | 게시글 수정 | `{ "success": true }` |
| DELETE | `/api/posts/{postId}` | 게시글 삭제 | `{ "success": true }` |
| POST | `/api/posts/{postId}/like` | 좋아요 추가 | `{ "success": true }` |
| DELETE | `/api/posts/{postId}/like` | 좋아요 취소 | `{ "success": true }` |
| POST | `/api/posts/{postId}/scrap` | 스크랩 추가 | `{ "success": true }` |
| DELETE | `/api/posts/{postId}/scrap` | 스크랩 취소 | `{ "success": true }` |
| GET | `/api/posts/{postId}/comments` | 댓글 목록 | `{ "success": true, "data": [...] }` |
| POST | `/api/posts/{postId}/comments` | 댓글 작성 | `{ "success": true, "data": { "commentId": 456 } }` |
| PUT | `/api/comments/{commentId}` | 댓글 수정 | `{ "success": true }` |
| DELETE | `/api/comments/{commentId}` | 댓글 삭제 | `{ "success": true }` |
| GET | `/api/users/me/posts` | 내가 작성한 게시글 | `{ "success": true, "data": [...] }` |
| GET | `/api/users/me/scraps` | 내 스크랩 목록 | `{ "success": true, "data": [...] }` |

**개선 사항:**
- `/posts` → `/api/posts`
- 댓글은 게시글 하위 리소스로 구조화
- POST → PUT/DELETE 사용
- 내 콘텐츠는 `/api/users/me` 하위로 통일

---

## 9. 마이페이지 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/mypage` | 마이페이지 홈 (View) | ⚠️ View 분리 |
| GET | `/mypage/mypageUser` | 내 정보 (View) | ❌ RESTful하지 않음 |
| POST | `/mypage/mypageUserUpdate` | 내 정보 수정 | ❌ PUT/PATCH 사용 |
| POST | `/mypage/validatePassword` | 비밀번호 검증 | ⚠️ `/api` prefix 필요 |
| POST | `/mypage/mypageUserDelete` | 회원 탈퇴 | ❌ DELETE 사용 |
| GET/POST | `/mypage/my-orders` | 주문 목록 (View) | ❌ GET/POST 혼용 |
| GET/POST | `/mypage/points` | 포인트 내역 (View) | ❌ GET/POST 혼용 |
| GET | `/mypage/reviews` | 내 리뷰 목록 (View) | ⚠️ `/api` prefix 필요 |
| GET | `/mypage/coupons` | 쿠폰 목록 (View) | ⚠️ `/api` prefix 필요 |
| POST | `/mypage/coupons/{couponId}/claim` | 쿠폰 발급 | ⚠️ `/api` prefix 필요 |
| GET | `/mypage/likes` | 좋아요 목록 (View) | ⚠️ `/api` prefix 필요 |
| GET | `/mypage/scraps` | 스크랩 목록 (View) | ⚠️ `/api` prefix 필요 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/users/me` | 내 정보 조회 | `{ "success": true, "data": { "userId": 123, "userName": "..." } }` |
| PUT | `/api/users/me` | 내 정보 수정 | `{ "success": true }` |
| DELETE | `/api/users/me` | 회원 탈퇴 | `{ "success": true }` |
| POST | `/api/users/me/password/verify` | 비밀번호 검증 | `{ "success": true, "data": { "valid": true } }` |
| GET | `/api/users/me/orders` | 내 주문 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/users/me/points` | 포인트 잔액 | `{ "success": true, "data": { "totalPoints": 5000 } }` |
| GET | `/api/users/me/points/history` | 포인트 이력 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/users/me/reviews` | 내 리뷰 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/users/me/coupons` | 내 쿠폰 목록 | `{ "success": true, "data": [...] }` |
| POST | `/api/coupons/{couponId}/issue` | 쿠폰 발급 | `{ "success": true }` |
| GET | `/api/users/me/likes` | 좋아요한 상품 | `{ "success": true, "data": [...] }` |
| GET | `/api/users/me/scraps` | 스크랩한 게시글 | `{ "success": true, "data": [...] }` |
| GET | `/api/users/me/questions` | 내 문의 내역 | `{ "success": true, "data": [...] }` |
| GET | `/api/users/me/addresses` | 배송지 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/users/me/summary` | 마이페이지 요약 | `{ "success": true, "data": { "orderCount": 10, "reviewCount": 5, ... } }` |

**개선 사항:**
- `/mypage` → `/api/users/me`로 통일
- GET/POST 혼용 제거
- 쿠폰 발급은 `/api/coupons` 하위로 이동
- 마이페이지 요약 엔드포인트 추가

---

## 10. 쿠폰/포인트 API

### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/order/my-coupons` | 내 쿠폰 목록 | ❌ `/order` 하위가 아님 |
| GET | `/order/my-coupons/{userCouponId}` | 쿠폰 검증 | ❌ 별도 리소스로 분리 |
| GET | `/order/my-points` | 포인트 조회 | ❌ 별도 리소스로 분리 |
| GET | `/mypage/coupons` | 쿠폰 목록 (View) | ⚠️ `/api` prefix 필요 |
| POST | `/mypage/coupons/{couponId}/claim` | 쿠폰 발급 | ⚠️ `/api` prefix 필요 |
| GET | `/mypage/coupons/mine` | 내 쿠폰 목록 | ⚠️ 중복된 엔드포인트 |
| GET/POST | `/mypage/points` | 포인트 내역 | ❌ GET/POST 혼용 |

### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/coupons` | 발급 가능한 쿠폰 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/coupons/{couponId}` | 쿠폰 상세 | `{ "success": true, "data": { "couponId": 1, "discountRate": 10 } }` |
| POST | `/api/coupons/{couponId}/issue` | 쿠폰 발급 | `{ "success": true, "data": { "userCouponId": 123 } }` |
| DELETE | `/api/coupons/{userCouponId}` | 쿠폰 삭제 | `{ "success": true }` |
| POST | `/api/coupons/code` | 쿠폰 코드 등록 | `{ "success": true }` |
| GET | `/api/users/me/coupons` | 내 쿠폰 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/coupons/applicable?orderAmount=50000&productIds=1,2,3` | 주문에 적용 가능한 쿠폰 | `{ "success": true, "data": [...] }` |
| GET | `/api/points` | 포인트 잔액 | `{ "success": true, "data": { "totalPoints": 5000, "expiringPoints": 500 } }` |
| GET | `/api/points/history` | 포인트 이력 (페이징) | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| POST | `/api/points/charge` | 포인트 충전 (관리자) | `{ "success": true }` |

**개선 사항:**
- 쿠폰/포인트를 독립적인 리소스로 분리
- `/order`, `/mypage` 하위에서 제거
- 쿠폰 발급과 소유 구분 명확화

---

## 11. 관리자 API

### 11.1 관리자 - 상품 관리

#### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/api/admin/products/new` | 상품 등록 폼 (View) | ❌ API가 아닌 View |
| POST | `/api/admin/products` | 상품 등록 | ✅ RESTful함 |
| GET | `/api/admin/products` | 상품 목록 | ✅ RESTful함 |
| GET | `/api/admin/products/{productId}` | 상품 상세 | ✅ RESTful함 |
| POST | `/api/admin/products/{productId}/update` | 상품 수정 | ❌ PUT 사용 |
| POST | `/api/admin/products/{productId}/delete` | 상품 삭제 | ❌ DELETE 사용 |
| GET | `/api/admin/products/categories/{parentId}/children` | 자식 카테고리 조회 | ⚠️ `/api/categories` 별도 리소스 |

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/products` | 상품 목록 (관리자용) | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| POST | `/api/admin/products` | 상품 등록 | `{ "success": true, "data": { "productId": 123 } }` |
| GET | `/api/admin/products/{id}` | 상품 상세 | `{ "success": true, "data": { ... } }` |
| PUT | `/api/admin/products/{id}` | 상품 수정 (전체) | `{ "success": true }` |
| PATCH | `/api/admin/products/{id}` | 상품 수정 (부분) | `{ "success": true }` |
| DELETE | `/api/admin/products/{id}` | 상품 삭제 | `{ "success": true }` |
| POST | `/api/admin/products/{id}/options` | 옵션 추가 | `{ "success": true, "data": { "optionId": 456 } }` |
| PUT | `/api/admin/products/{id}/options/{optionId}` | 옵션 수정 | `{ "success": true }` |
| DELETE | `/api/admin/products/{id}/options/{optionId}` | 옵션 삭제 | `{ "success": true }` |
| GET | `/api/categories` | 카테고리 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/categories/{parentId}/children` | 자식 카테고리 | `{ "success": true, "data": [...] }` |

---

### 11.2 관리자 - 사용자 관리

#### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/api/admin/users` | 사용자 목록 | ✅ RESTful함 |
| GET | `/api/admin/users/{userId}` | 사용자 상세 | ✅ RESTful함 |
| POST | `/api/admin/users/{userId}/update` | 사용자 수정 | ❌ PUT 사용 |
| POST | `/api/admin/users/{userId}/delete` | 사용자 삭제 | ❌ DELETE 사용 |

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/users` | 사용자 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/admin/users/{id}` | 사용자 상세 | `{ "success": true, "data": { ... } }` |
| PUT | `/api/admin/users/{id}` | 사용자 수정 | `{ "success": true }` |
| PATCH | `/api/admin/users/{id}/role` | 권한 변경 | `{ "success": true }` |
| PATCH | `/api/admin/users/{id}/status` | 계정 상태 변경 | `{ "success": true }` |
| DELETE | `/api/admin/users/{id}` | 사용자 삭제 | `{ "success": true }` |

---

### 11.3 관리자 - 브랜드 관리

#### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/api/admin/brands/new` | 브랜드 등록 폼 (View) | ❌ API가 아닌 View |
| POST | `/api/admin/brands` | 브랜드 등록 | ✅ RESTful함 |
| GET | `/api/admin/brands` | 브랜드 목록 | ✅ RESTful함 |
| GET | `/api/admin/brands/{brandId}` | 브랜드 상세 | ✅ RESTful함 |
| POST | `/api/admin/brands/{brandId}/update` | 브랜드 수정 | ❌ PUT 사용 |
| POST | `/api/admin/brands/{brandId}/delete` | 브랜드 삭제 | ❌ DELETE 사용 |

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/brands` | 브랜드 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| POST | `/api/admin/brands` | 브랜드 등록 | `{ "success": true, "data": { "brandId": 123 } }` |
| GET | `/api/admin/brands/{id}` | 브랜드 상세 | `{ "success": true, "data": { ... } }` |
| PUT | `/api/admin/brands/{id}` | 브랜드 수정 | `{ "success": true }` |
| DELETE | `/api/admin/brands/{id}` | 브랜드 삭제 | `{ "success": true }` |

---

### 11.4 관리자 - 재고 관리

#### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/api/admin/inventory` | 재고 목록 | ✅ RESTful함 |
| GET | `/api/admin/inventory/{inventoryId}` | 재고 상세 | ✅ RESTful함 |
| POST | `/api/admin/inventory/receipt` | 입고 처리 | ⚠️ `/receipts` 별도 리소스 권장 |
| GET | `/api/admin/inventory/receipt` | 입고 내역 페이지 (View) | ❌ View 분리 |
| GET | `/api/admin/inventory/receipt-list` | 입고 내역 목록 | ⚠️ `/receipts` 사용 |
| GET | `/api/admin/inventory/history/{productId}` | 재고 이력 | ✅ 적절함 |
| PUT | `/api/admin/inventory/receipt/{receiptId}/status` | 입고 상태 변경 | ✅ RESTful함 |
| POST | `/api/inventory/outbound/{orderId}` | 출고 처리 | ⚠️ 경로 불일치 |
| POST | `/api/inventory/outbound/cancel/{orderId}` | 출고 취소 | ⚠️ PATCH 권장 |

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/inventory` | 재고 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/admin/inventory/{id}` | 재고 상세 | `{ "success": true, "data": { ... } }` |
| GET | `/api/admin/inventory/{id}/history` | 재고 이력 | `{ "success": true, "data": [...] }` |
| POST | `/api/admin/inventory/receipts` | 입고 처리 | `{ "success": true, "data": { "receiptId": 123 } }` |
| GET | `/api/admin/inventory/receipts` | 입고 내역 목록 | `{ "success": true, "data": [...] }` |
| GET | `/api/admin/inventory/receipts/{id}` | 입고 상세 | `{ "success": true, "data": { ... } }` |
| PATCH | `/api/admin/inventory/receipts/{id}/status` | 입고 상태 변경 | `{ "success": true }` |
| POST | `/api/admin/inventory/outbounds` | 출고 처리 | `{ "success": true, "data": { "outboundId": 456 } }` |
| GET | `/api/admin/inventory/outbounds` | 출고 내역 목록 | `{ "success": true, "data": [...] }` |
| PATCH | `/api/admin/inventory/outbounds/{id}/cancel` | 출고 취소 | `{ "success": true }` |

---

### 11.5 관리자 - 주문 관리

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/orders` | 전체 주문 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/admin/orders/{id}` | 주문 상세 | `{ "success": true, "data": { ... } }` |
| PATCH | `/api/admin/orders/{id}/status` | 주문 상태 변경 | `{ "success": true }` |
| PATCH | `/api/admin/orders/{id}/delivery` | 배송 정보 수정 | `{ "success": true }` |

---

### 11.6 관리자 - 공지사항 관리

#### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/api/admin/notices` | 공지사항 목록 | ✅ RESTful함 |
| GET | `/api/admin/notices/create` | 공지 작성 폼 (View) | ❌ View 분리 |
| POST | `/api/admin/notices/create` | 공지 작성 | ❌ `/api/admin/notices`로 통일 |
| GET | `/api/admin/notices/{noticeId}/detail` | 공지 상세 | ⚠️ `/detail` 불필요 |
| GET | `/api/admin/notices/{noticeId}/edit` | 공지 수정 폼 (View) | ❌ View 분리 |
| POST | `/api/admin/notices/{noticeId}/update` | 공지 수정 | ❌ PUT 사용 |
| POST | `/api/admin/notices/{noticeId}/delete` | 공지 삭제 | ❌ DELETE 사용 |

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/notices` | 공지사항 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| POST | `/api/admin/notices` | 공지사항 작성 | `{ "success": true, "data": { "noticeId": 123 } }` |
| GET | `/api/admin/notices/{id}` | 공지사항 상세 | `{ "success": true, "data": { ... } }` |
| PUT | `/api/admin/notices/{id}` | 공지사항 수정 | `{ "success": true }` |
| DELETE | `/api/admin/notices/{id}` | 공지사항 삭제 | `{ "success": true }` |

---

### 11.7 관리자 - 쿠폰 관리

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/coupons` | 쿠폰 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| POST | `/api/admin/coupons` | 쿠폰 생성 | `{ "success": true, "data": { "couponId": 123 } }` |
| GET | `/api/admin/coupons/{id}` | 쿠폰 상세 | `{ "success": true, "data": { ... } }` |
| PUT | `/api/admin/coupons/{id}` | 쿠폰 수정 | `{ "success": true }` |
| DELETE | `/api/admin/coupons/{id}` | 쿠폰 삭제 | `{ "success": true }` |
| POST | `/api/admin/coupons/{id}/issue` | 쿠폰 일괄 발급 | `{ "success": true, "data": { "issuedCount": 100 } }` |

---

### 11.8 관리자 - 통계/로그

#### 현재 구조
| HTTP 메서드 | 엔드포인트 | 설명 | 문제점 |
|------------|-----------|------|--------|
| GET | `/api/admin/log` | 로그 목록 | ⚠️ `/logs` 복수형 권장 |

#### 개선안 (RESTful)
| HTTP 메서드 | 엔드포인트 | 설명 | 응답 형식 |
|------------|-----------|------|----------|
| GET | `/api/admin/statistics/sales` | 매출 통계 | `{ "success": true, "data": { "totalSales": 1000000, "dailySales": [...] } }` |
| GET | `/api/admin/statistics/products` | 상품 통계 | `{ "success": true, "data": { "totalProducts": 500, "topProducts": [...] } }` |
| GET | `/api/admin/statistics/users` | 사용자 통계 | `{ "success": true, "data": { "totalUsers": 10000, "newUsers": 50 } }` |
| GET | `/api/admin/logs` | 활동 로그 목록 | `{ "success": true, "data": { "content": [...], "page": {...} } }` |
| GET | `/api/admin/logs/{id}` | 로그 상세 | `{ "success": true, "data": { ... } }` |

---

## 12. 공통 문제점 및 개선 방향

### 12.1 공통 문제점

| 문제 | 현재 상태 | 예시 |
|------|----------|------|
| **1. API와 View 혼재** | Controller에 View 반환과 JSON 반환이 섞여 있음 | `OrderController`가 Thymeleaf와 JSON 모두 반환 |
| **2. 비RESTful한 경로** | `/controller/register/login`, `/order/create` 등 | `/controller` prefix, 동사 사용 |
| **3. HTTP 메서드 오용** | POST로 update/delete 처리 | `POST /api/admin/products/{id}/update` |
| **4. 일관성 없는 경로** | `/api` prefix 누락, 단수/복수 혼용 | `/product` vs `/api/products` |
| **5. 응답 형식 불통일** | `Map<String, Object>`, `OrderCreateResponse`, 직접 DTO 등 | 여러 응답 형식 혼재 |
| **6. 리소스 계층 부적절** | 관련 없는 리소스가 하위에 위치 | `/order/my-coupons` (쿠폰은 주문 하위 아님) |
| **7. GET/POST 혼용** | 동일 URL에 GET과 POST 모두 사용 | `@RequestMapping(method = {GET, POST})` |
| **8. View URL이 `/api` 하위에** | API 경로에 View 반환 메서드 존재 | `/api/admin/products/new` (View 폼) |

---

### 12.2 개선 방향 요약

#### ✅ 1. Controller 완전 분리
```java
// ❌ 현재: View와 API 혼재
@Controller
public class OrderController {
    @PostMapping("/order/preview")  // View 반환
    public String preview(...) { return "order/create"; }

    @PostMapping("/order/create")   // JSON 반환
    @ResponseBody
    public OrderCreateResponse create(...) { ... }
}

// ✅ 개선: 완전 분리
@Controller
@RequestMapping("/orders")
public class OrderViewController {
    @GetMapping("/preview")
    public String showPreview(...) { return "order/preview"; }
}

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(...) { ... }
}
```

---

#### ✅ 2. RESTful HTTP 메서드 사용
```java
// ❌ 현재
POST /api/admin/products/{id}/update   // 수정
POST /api/admin/products/{id}/delete   // 삭제

// ✅ 개선
PUT    /api/admin/products/{id}        // 전체 수정
PATCH  /api/admin/products/{id}        // 부분 수정
DELETE /api/admin/products/{id}        // 삭제
```

---

#### ✅ 3. 표준 응답 형식
```java
// ❌ 현재: 여러 형식
Map<String, Object> response = new HashMap<>();
response.put("success", true);
return response;

OrderCreateResponse.success(orderId);

return new UserResponse(...);

// ✅ 개선: 통일된 형식
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
}

// 사용
return ResponseEntity.ok(ApiResponse.success(orderResponse));
```

---

#### ✅ 4. 리소스 중심 설계
```java
// ❌ 현재: 동사 중심, 불명확한 계층
POST /order/create
GET  /order/my-coupons
GET  /order/my-points

// ✅ 개선: 리소스 중심, 명확한 계층
POST   /api/orders                    # 주문 리소스
GET    /api/coupons                   # 쿠폰 리소스 (독립)
GET    /api/points                    # 포인트 리소스 (독립)
GET    /api/users/me/coupons          # 내 쿠폰 (사용자 하위)
```

---

#### ✅ 5. 일관된 경로 규칙
```
모든 API:           /api/**
리소스는 복수형:     /api/products, /api/orders, /api/users
ID 파라미터:        /api/products/{id}
하위 리소스:        /api/products/{id}/reviews
상태 변경:          /api/orders/{id}/cancel (PATCH)
액션:              /api/coupons/{id}/issue (POST)
```

---

#### ✅ 6. 쿼리 파라미터 활용
```java
// ❌ 현재: 경로로 정렬 지정
GET /product/category/{categoryId}/price-asc
GET /product/category/{categoryId}/price-desc
GET /product/category/{categoryId}/rating-desc

// ✅ 개선: 쿼리 파라미터
GET /api/products?category=1&sort=price,asc
GET /api/products?category=1&sort=price,desc
GET /api/products?category=1&sort=rating,desc
GET /api/products?category=1&minPrice=1000&maxPrice=50000
```

---

### 12.3 마이그레이션 전략

#### Phase 1: API 추가 (기존 유지) - 1개월
```
1. 새로운 RESTful API 엔드포인트 추가
2. 기존 API는 @Deprecated 표시하고 유지
3. 프론트엔드는 점진적으로 새 API 사용
```

#### Phase 2: View Controller 분리 - 1개월
```
1. View Controller와 API Controller 완전 분리
2. Thymeleaf는 View Controller만 사용
3. AJAX/Fetch는 API Controller만 사용
```

#### Phase 3: 기존 API 제거 - 1개월
```
1. @Deprecated API 사용처 확인
2. 모두 전환 완료 후 제거
3. 문서 업데이트
```

---

### 12.4 체크리스트

#### API 설계 체크리스트
- [ ] `/api` prefix 존재하는가?
- [ ] 리소스명이 복수형인가? (`/products`, `/orders`)
- [ ] HTTP 메서드가 적절한가? (POST=생성, PUT=전체수정, PATCH=부분수정, DELETE=삭제)
- [ ] 경로에 동사가 없는가? (`/create`, `/update` 제거)
- [ ] 응답 형식이 표준화되어 있는가? (`ApiResponse<T>`)
- [ ] 리소스 계층이 논리적인가?
- [ ] 쿼리 파라미터를 활용하는가? (정렬, 필터, 페이징)
- [ ] View와 API가 분리되어 있는가?

#### 응답 체크리스트
- [ ] 성공 시 `{ "success": true, "data": ... }`
- [ ] 실패 시 `{ "success": false, "message": "...", "errorCode": "..." }`
- [ ] HTTP 상태 코드가 적절한가? (200, 201, 400, 404, 500 등)
- [ ] 페이징 응답에 `page` 정보 포함되는가?

---

## 13. 참고 자료

### RESTful API 설계 원칙
- **리소스 중심**: 동사가 아닌 명사 사용
- **HTTP 메서드 활용**: GET, POST, PUT, PATCH, DELETE
- **계층 구조**: `/api/products/{id}/reviews`
- **쿼리 파라미터**: 필터링, 정렬, 페이징
- **상태 코드**: 의미 있는 HTTP 상태 코드 사용
- **버전 관리**: `/api/v1/products`

### 좋은 API 설계 사례
```
✅ GET    /api/products                    # 상품 목록
✅ POST   /api/products                    # 상품 생성
✅ GET    /api/products/{id}               # 상품 상세
✅ PUT    /api/products/{id}               # 상품 수정
✅ DELETE /api/products/{id}               # 상품 삭제
✅ GET    /api/products?category=1&sort=price,asc  # 필터링/정렬
✅ POST   /api/products/{id}/reviews       # 리뷰 작성
✅ PATCH  /api/orders/{id}/cancel          # 주문 취소
```

### 나쁜 API 설계 사례
```
❌ POST  /order/create                     # 동사 사용
❌ POST  /api/admin/products/{id}/update   # POST로 수정
❌ POST  /api/admin/products/{id}/delete   # POST로 삭제
❌ GET   /order/my-coupons                 # 리소스 계층 부적절
❌ GET   /product/category/1/price-asc     # 정렬을 경로로
```

---

**문서 버전**: 1.0
**최종 수정일**: 2025-10-14
**작성자**: Claude Code Analysis