# ERD 초안

## 핵심 엔티티

```mermaid
erDiagram
  MEMBER ||--o{ PET_PROFILE : owns
  MEMBER ||--o{ CART_ITEM : has
  MEMBER ||--o{ ORDER : places
  MEMBER ||--o{ MEMBER_COUPON : owns

  PRODUCT_CATEGORY ||--o{ PRODUCT : contains
  PRODUCT ||--o{ CART_ITEM : selected
  PRODUCT ||--o{ ORDER_ITEM : ordered
  PRODUCT ||--o{ STOCK : stocked

  WAREHOUSE ||--o{ STOCK : holds
  STOCK ||--o{ STOCK_HISTORY : records

  ORDER ||--o{ ORDER_ITEM : contains
  ORDER ||--o| PAYMENT : paid_by
  ORDER ||--o{ ORDER_EVENT : records

  COUPON ||--o{ MEMBER_COUPON : issued
  COUPON ||--o{ ORDER_COUPON : applied
  ORDER ||--o{ ORDER_COUPON : uses

  MEMBER {
    bigint id PK
    string email UK
    string password_hash
    string name
    string role
    string status
    datetime created_at
  }

  PET_PROFILE {
    bigint id PK
    bigint member_id FK
    string name
    string species
    date birth_date
    string allergy_notes
  }

  PRODUCT {
    bigint id PK
    bigint category_id FK
    string name
    int price
    string sale_status
    datetime created_at
  }

  WAREHOUSE {
    bigint id PK
    string name
    string code UK
    string status
  }

  STOCK {
    bigint id PK
    bigint product_id FK
    bigint warehouse_id FK
    int quantity
    int safety_quantity
    bigint version
  }

  STOCK_HISTORY {
    bigint id PK
    bigint stock_id FK
    string change_type
    int quantity
    int before_quantity
    int after_quantity
    string reason
    datetime created_at
  }

  ORDER {
    bigint id PK
    bigint member_id FK
    string order_no UK
    string status
    int total_amount
    int discount_amount
    int payment_amount
    datetime ordered_at
  }

  ORDER_ITEM {
    bigint id PK
    bigint order_id FK
    bigint product_id FK
    int quantity
    int unit_price
    int line_amount
  }

  PAYMENT {
    bigint id PK
    bigint order_id FK
    string payment_key
    string status
    int amount
    datetime approved_at
  }

  COUPON {
    bigint id PK
    string name
    string discount_type
    int discount_value
    int min_order_amount
    datetime starts_at
    datetime ends_at
  }
```

## 초기 인덱스 후보

| 테이블 | 인덱스 | 목적 |
|---|---|---|
| member | email | 로그인 |
| product | category_id, sale_status | 상품 목록 필터 |
| stock | product_id, warehouse_id | 상품/창고별 재고 조회 |
| stock_history | stock_id, created_at | 재고 이력 조회 |
| orders | member_id, ordered_at | 회원 주문 내역 |
| orders | order_no | 주문 단건 조회 |
| order_event | order_id, created_at | 주문 이벤트 추적 |

## 동시성 검토 대상

- 주문 생성 시 재고 차감
- 주문 취소 시 재고 복구
- 쿠폰 중복 사용 방지
- 결제 승인 이벤트 중복 수신 방지

