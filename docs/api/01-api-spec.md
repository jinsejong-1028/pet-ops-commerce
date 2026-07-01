# API 목록

## 공통 규칙

- Base URL: `/api/v1`
- 인증: `Authorization: Bearer {accessToken}`
- 응답 포맷은 공통 wrapper를 사용합니다.
- 관리자 API는 `/admin/**` 경로를 사용합니다.
- 현재 권한은 인증 사용자 중심이며, 운영 목표는 `ADMIN`, `OPERATOR`, `MEMBER` 역할로 세분화하는 것입니다.


## OpenAPI / Swagger UI

서버 실행 후 브라우저에서 자동 생성 API 문서를 확인할 수 있습니다.

| 항목 | URL | 설명 |
|---|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` | API 목록, 요청/응답 모델, 인증 입력 화면 |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` | 도구 연동용 OpenAPI 3 JSON 명세 |

JWT가 필요한 API는 Swagger UI 오른쪽 상단 `Authorize` 버튼에 아래 형식으로 access token을 입력한 뒤 호출합니다.

```text
Bearer {accessToken}
```

현재 Swagger UI에는 실제 Controller로 구현된 Health, Auth, Member, Product, Inventory, Order, Sales Order API가 표시됩니다.

## 인증

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/auth/login` | 로그인 | Guest |

## 회원/반려동물

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/members` | 회원 생성 | Guest |
| GET | `/members/{memberId}` | 회원 단건 조회 | Guest 임시 |
| GET | `/members/me` | 내 정보 조회 | Member 예정 |
| PATCH | `/members/me` | 내 정보 수정 | Member 예정 |
| GET | `/pets` | 내 반려동물 목록 | Member 예정 |
| POST | `/pets` | 반려동물 등록 | Member 예정 |

## 상품

상품은 단일 운영사가 등록하고 사용자가 구매하는 B2C 상품 카탈로그입니다.

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/product-categories` | 상품 카테고리 생성 | Authenticated |
| GET | `/product-categories` | 상품 카테고리 목록 조회 | Guest |
| POST | `/products` | 상품 생성 | Authenticated |
| GET | `/products` | 상품 목록 조회 | Guest |
| GET | `/products/{productId}` | 상품 상세 조회 | Guest |

## 고객 주문

`customer_orders`는 고객이 생성한 주문 테이블입니다.
배송 정보는 `customer_order_deliveries`로 분리해 관리합니다.

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/orders` | 고객 주문 생성 | Member |
| GET | `/orders` | 내 주문 목록 | Member 예정 |
| GET | `/orders/{orderId}` | 주문 상세 | Member 예정 |
| POST | `/orders/{orderId}/cancel` | 주문 취소 | Member 예정 |

현재 주문 생성 요청은 상품과 수량 중심입니다.
배송 정보 입력은 `customer_order_deliveries` 적용 브랜치에서 요청 DTO에 확장합니다.

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

## 판매/출고/구매/입고 업무 목표 API

아래 API는 현재 schema 기준의 목표 흐름입니다.
고객 주문 생성 시 판매 주문은 CREATED 상태로 자동 생성됩니다. 운영자는 판매 주문에 출고 창고를 지정한 뒤 확정합니다. 판매 주문 확정 API는 고객 주문을 확정하고 지정된 창고로 출고 주문을 생성합니다. 출고 할당/피킹/출고 확정과 구매/입고 API는 후속 브랜치에서 진행합니다.

### 판매 주문

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| PATCH | `/admin/sales-orders/{salesOrderId}` | 판매 주문 출고 창고 지정 | Operator |
| POST | `/admin/sales-orders/{salesOrderId}/confirm` | 판매 주문 확정 및 출고 주문 생성 | Operator |
| POST | `/admin/sales-orders/{salesOrderId}/cancel` | 판매 주문 취소 | Operator |
| GET | `/admin/sales-orders` | 판매 주문 목록 조회 | Operator 예정 |


### 출고 지시

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/admin/shipment-orders` | 판매 주문 기반 출고 지시 생성 | Operator 예정 |
| POST | `/admin/shipment-orders/{shipmentOrderId}/allocate` | 출고 재고 할당 | Operator 예정 |
| POST | `/admin/shipment-orders/{shipmentOrderId}/pick` | PICKTO location 이동 | Operator 예정 |
| POST | `/admin/shipment-orders/{shipmentOrderId}/ship` | 출고 확정 | Operator 예정 |
| GET | `/admin/shipment-orders` | 출고 지시 목록 조회 | Operator 예정 |

출고 수량 진행률은 `shipment_order_items`에서 관리합니다.

```text
order_quantity
allocated_quantity
picked_quantity
shipped_quantity
```

### 구매 발주

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/admin/purchase-orders` | 공급사 구매 발주 생성 | Operator 예정 |
| POST | `/admin/purchase-orders/{purchaseOrderId}/confirm` | 구매 발주 확정 | Operator 예정 |
| POST | `/admin/purchase-orders/{purchaseOrderId}/cancel` | 구매 발주 취소 | Operator 예정 |
| GET | `/admin/purchase-orders` | 구매 발주 목록 조회 | Operator 예정 |

### 입고 지시

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/admin/receiving-orders` | 구매 발주 기반 입고 지시 생성 | Operator 예정 |
| POST | `/admin/receiving-orders/{receivingOrderId}/receive` | 입고 확정 | Operator 예정 |
| GET | `/admin/receiving-orders` | 입고 지시 목록 조회 | Operator 예정 |

입고 수량 진행률은 `receiving_order_items`에서 관리합니다.

```text
order_quantity
received_quantity
lot1~lot5
```

## 재고

현재 재고 API는 관리자 재고 테스트와 이후 입고/출고 workflow 연결을 위한 기반 API입니다.
재고 API의 HTTP 진입점은 `StockController`로 단일화하고, Controller는 `StockService` facade만 호출합니다. `StockService`는 기준정보 검증, job 생성, workflow 상태 변경을 조율하고, 실제 수량 변경과 movement 저장은 `StockOperationService.execute(command)` 단일 진입점으로 처리합니다.

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/admin/warehouses` | 창고 생성 | Operator |
| POST | `/admin/locations` | location 생성 | Operator |
| GET | `/admin/stocks` | location 단위 현재고 목록 조회, 기본 0수량 제외, `includeZero=true`로 포함 | Operator |
| GET | `/admin/stocks/{stockId}` | 현재고 단건 조회 | Operator |
| POST | `/admin/stocks` | LOT 생성/조회 후 입고성 현재고 생성 또는 증가 | Operator |
| POST | `/admin/stocks/transfer` | 가용수량 기준 location 재고 이동 | Operator |
| POST | `/admin/stocks/change-lot` | 같은 location 안에서 LOT 속성 변경 | Operator |
| POST | `/admin/stocks/adjust` | 수동 재고 조정 | Operator |
| POST | `/admin/stocks/allocate` | 출고 작업 재고 할당 | Operator |
| POST | `/admin/stocks/pick` | PICKTO location 이동 | Operator |
| POST | `/admin/stocks/outbound` | PICKTO 재고 출고 확정 | Operator |

현재고 응답에는 총수량, 가용수량, 작업수량을 함께 제공합니다.

```json
{
  "id": 1,
  "productId": 1,
  "warehouseId": 1,
  "locationId": 1,
  "lotId": 1,
  "totalQuantity": 100,
  "availableQuantity": 97,
  "workingQuantity": 3
}
```

## 쿠폰

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/coupons/my` | 내 쿠폰 목록 | Member 예정 |
| POST | `/admin/coupons` | 쿠폰 생성 | Admin 예정 |
| POST | `/admin/coupons/{couponId}/issue` | 쿠폰 발급 | Admin 예정 |

## 운영/AI

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/admin/events` | 운영 이벤트 조회 | Operator 예정 |
| POST | `/admin/ai/support-draft` | 고객 응대 초안 생성 | Operator 예정 |
| GET | `/admin/reports/daily-sales` | 일별 매출 리포트 | Admin 예정 |
| GET | `/admin/reports/stock-snapshot` | 재고 스냅샷 리포트 | Admin 예정 |
