# API 목록 초안

## 공통 규칙

- Base URL: `/api/v1`
- 인증: `Authorization: Bearer {accessToken}`
- 응답 포맷은 공통 wrapper를 사용한다.
- 목록 조회 API는 `page`, `size`, `sort`를 지원한다.

## 인증

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/auth/signup` | 회원가입 | Guest |
| POST | `/auth/login` | 로그인 | Guest |
| POST | `/auth/refresh` | 토큰 재발급 | Member |
| POST | `/auth/logout` | 로그아웃 | Member |

## 회원/반려동물

> 회원 도메인 1차에서는 공통 응답 wrapper 적용 전까지 원본 DTO 응답을 사용합니다.


| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/members/me` | 내 정보 조회 | Member |
| PATCH | `/members/me` | 내 정보 수정 | Member |
| GET | `/pets` | 내 반려동물 목록 | Member |
| POST | `/pets` | 반려동물 등록 | Member |
| PATCH | `/pets/{petId}` | 반려동물 수정 | Member |
| DELETE | `/pets/{petId}` | 반려동물 삭제 | Member |

## 상품

상품은 단일 운영사가 등록하고 사용자가 구매하는 B2C 상품 카탈로그입니다.
현재 1차 구현에서는 인증/관리자 기능 전 단계이므로 학습 검증을 위해 `/products`, `/product-categories` 생성 API를 임시로 열어둡니다.
운영 단계에서는 상품 등록/수정 API를 Admin 권한으로 잠글 예정입니다.

### 현재 구현 API

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/product-categories` | 상품 카테고리 생성 | Guest 임시 |
| GET | `/product-categories` | 상품 카테고리 목록 조회 | Guest |
| POST | `/products` | 상품 생성 | Guest 임시 |
| GET | `/products` | 상품 목록 조회 | Guest |
| GET | `/products/{productId}` | 상품 상세 조회 | Guest |

### 운영 목표 API

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/products` | 상품 목록 조회 | Guest |
| GET | `/products/{productId}` | 상품 상세 조회 | Guest |
| POST | `/admin/products` | 상품 등록 | Admin |
| PATCH | `/admin/products/{productId}` | 상품 수정 | Admin |
| PATCH | `/admin/products/{productId}/status` | 판매 상태 변경 | Admin |

## 장바구니

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/cart/items` | 장바구니 조회 | Member |
| POST | `/cart/items` | 장바구니 추가 | Member |
| PATCH | `/cart/items/{cartItemId}` | 수량 변경 | Member |
| DELETE | `/cart/items/{cartItemId}` | 항목 삭제 | Member |

## 주문

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/orders` | 주문 생성 | Member |
| GET | `/orders` | 내 주문 목록 | Member |
| GET | `/orders/{orderId}` | 주문 상세 | Member |
| POST | `/orders/{orderId}/cancel` | 주문 취소 | Member |
| GET | `/admin/orders` | 주문 관리 목록 | Operator |
| PATCH | `/admin/orders/{orderId}/status` | 주문 상태 변경 | Operator |

## 재고

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/admin/stocks` | 재고 목록 조회 | Operator |
| POST | `/admin/stocks/inbound` | 입고 처리 | Admin |
| POST | `/admin/stocks/outbound` | 출고 처리 | Admin |
| POST | `/admin/stocks/adjustment` | 재고 조정 | Admin |
| GET | `/admin/stocks/{stockId}/histories` | 재고 이력 조회 | Operator |

## 쿠폰

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/coupons/my` | 내 쿠폰 목록 | Member |
| POST | `/admin/coupons` | 쿠폰 생성 | Admin |
| POST | `/admin/coupons/{couponId}/issue` | 쿠폰 발급 | Admin |

## 운영/AI

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/admin/events` | 운영 이벤트 조회 | Operator |
| POST | `/admin/ai/support-draft` | 고객 응대 초안 생성 | Operator |
| GET | `/admin/reports/daily-sales` | 일별 매출 리포트 | Admin |
| GET | `/admin/reports/stock-snapshot` | 재고 스냅샷 리포트 | Admin |

