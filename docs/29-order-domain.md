# Order Domain

## 작업 목적

주문 도메인의 첫 API를 구현합니다.

이번 작업은 결제나 재고 차감까지 확장하지 않고, 로그인 회원이 판매 중 상품으로 주문을 생성하는 기본 흐름을 먼저 만듭니다.

```text
JWT 로그인 사용자
-> 주문 생성 요청
-> 상품 존재 검증
-> 상품 판매상태 검증
-> 주문 금액 계산
-> orders 저장
-> order_items 저장
```

## 구현 범위

이번 브랜치에서는 아래를 구현합니다.

- `Order` Entity
- `OrderItem` Entity
- `OrderStatus` enum
- `OrderRepository`
- `OrderItemRepository`
- 주문 생성 요청/응답 DTO
- `OrderService`
- `OrderController`
- 주문 생성 Service 테스트
- 주문 생성 Controller 테스트
- IntelliJ HTTP Client 요청 파일

## 제외 범위

이번 브랜치에서는 아래 작업을 하지 않습니다.

- 활성 상태가 아닌 회원 검증
- 재고 할당
- PICKTO location 이동
- 출고 처리
- 결제 승인
- 쿠폰 할인

회원 상태 검증은 인증 도메인의 책임입니다.
`SUSPENDED`, `WITHDRAWN` 회원은 로그인 단계에서 차단합니다.

```text
fix/auth-member-status-check
```

재고 수량 변경은 주문 도메인 이후 별도 브랜치에서 진행합니다.

```text
feature/inventory-stock-workflow
```

## DB 관계 설계

주문 테이블은 초기 schema에 이미 존재합니다.

```text
orders
order_items
payments
order_events
```

이번 작업은 새 migration을 추가하지 않고, 기존 `orders`, `order_items` 테이블에 맞춰 Entity를 추가합니다.

현재 프로젝트는 DB foreign key constraint를 걸지 않습니다.

```text
DB FK 제약 없음
orders.member_id 컬럼 있음
order_items.order_id 컬럼 있음
order_items.product_id 컬럼 있음
Service에서 상품 존재 여부 검증
```

## 주문 금액 기준

주문 상품은 상품 현재 가격을 주문 당시 가격으로 복사해서 저장합니다.

| 항목 | 의미 |
|---|---|
| `unit_price` | 주문 당시 상품 단가 |
| `quantity` | 주문 수량 |
| `line_amount` | `unit_price * quantity` |
| `total_amount` | 주문 상품 금액 합계 |
| `discount_amount` | 할인 금액, 쿠폰 전까지 0 |
| `payment_amount` | 결제 대상 금액 |

상품 가격이 나중에 바뀌어도 과거 주문 금액이 바뀌지 않도록 `order_items.unit_price`와 `order_items.line_amount`를 저장합니다.

## API 목록

### 주문 생성

```text
POST /api/v1/orders
```

인증:

```text
Authorization: Bearer {accessToken}
```

요청:

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

응답:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNo": "ORD-20260625100000-ABCDEF12",
    "memberId": 5,
    "status": "CREATED",
    "totalAmount": 50000,
    "discountAmount": 0,
    "paymentAmount": 50000,
    "orderedAt": "2026-06-25T10:00:00",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "quantity": 2,
        "unitPrice": 25000,
        "lineAmount": 50000
      }
    ]
  },
  "message": "OK"
}
```

## 검증 방법

자동 테스트:

```powershell
.\gradlew.bat test --console=plain
```

서버 실행 후 IntelliJ HTTP Client로 확인합니다.

```powershell
.\gradlew.bat bootRun
```

확인 파일:

```text
http/order-api.http
```

## 다음 작업 후보

주문 도메인 이후에는 재고 수량 변경 흐름을 연결합니다.

```text
feature/inventory-stock-workflow
```

후속 흐름:

```text
주문 생성
-> 재고 할당
-> PICKTO location 이동
-> 출고
```
