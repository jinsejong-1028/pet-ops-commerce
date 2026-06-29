# Order Domain

## 작업 목적

주문 도메인은 고객이 상품을 구매하는 주문을 생성합니다.
현재 `orders`는 고객 주문을 의미하고, 관리자 판매 주문과 창고 출고 지시는 별도 workflow 테이블로 분리합니다.

```text
JWT 로그인 사용자
-> POST /api/v1/orders
-> 로그인된 memberId 추출
-> 상품 존재 검증
-> 상품 판매상태 ON_SALE 검증
-> 상품 현재 가격으로 주문 금액 계산
-> orders 저장
-> order_items 저장
-> 주문 응답 반환
```

## 구현 범위

초기 주문 도메인에서는 아래를 구현했습니다.

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

## 현재 Schema 기준 보정

이후 migration 정리로 주문 주변 schema가 확장되었습니다.

```text
orders
= 고객 주문

order_deliveries
= 고객 주문 배송 정보

sales_orders
= 관리자가 고객 주문을 확정한 내부 판매 주문

shipment_orders
= 판매 주문을 바탕으로 창고가 처리할 출고 지시

purchase_orders
= 운영사가 공급사에 넣는 구매 발주

receiving_orders
= 구매 발주를 바탕으로 창고가 처리할 입고 지시
```

현재 주문 생성 API는 아직 배송 정보와 판매/출고 지시를 자동 생성하지 않습니다.
이 연결은 후속 `feature/order-fulfillment-workflow`에서 구현합니다.

## 제외 범위

초기 주문 생성에서는 아래 작업을 하지 않습니다.

- 활성 상태가 아닌 회원 검증
- 배송 정보 저장
- 판매 주문 생성
- 출고 지시 생성
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

## DB 관계 설계

현재 프로젝트는 DB foreign key constraint를 걸지 않습니다.

```text
DB FK 제약 없음
orders.member_id 컬럼 있음
order_items.order_id 컬럼 있음
order_items.product_id 컬럼 있음
Service에서 상품 존재 여부 검증
```

`orders.member_id`는 고객 주문의 소유자를 표현합니다.
`created_by`는 audit 컬럼으로, 누가 row를 생성했는지 기록하는 기술/운영 추적 값입니다.
따라서 고객 주문의 업무 주체는 `member_id`로 유지합니다.

## 주문번호 생성 기준

주문번호는 공통 업무 번호 생성기에서 생성합니다.

```text
BusinessNumberGenerator
-> ORDER rule
-> ORD-yyyyMMdd-000001
```

번호 규칙과 현재 순번은 DB에서 관리하고, 애플리케이션은 번호 구간을 한 번에 확보해 사용합니다.
상세 설계는 [Business Number Generator](../common/06-business-number-generator.md)에 정리합니다.

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

응답 예시:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNo": "ORD-20260629-000001",
    "memberId": 5,
    "status": "CREATED",
    "totalAmount": 50000,
    "discountAmount": 0,
    "paymentAmount": 50000,
    "orderedAt": "2026-06-29T10:00:00",
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

## 후속 업무 흐름

출고 흐름:

```text
orders
-> sales_orders
-> shipment_orders
-> stock_jobs(reference_type = SHIPMENT_ORDER)
-> stock_movements
```

입고 흐름:

```text
purchase_orders
-> receiving_orders
-> stock_jobs(reference_type = RECEIVING_ORDER)
-> stock_movements
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

주문 도메인 이후에는 판매/출고/입고 workflow를 연결합니다.

```text
feature/order-fulfillment-workflow
```