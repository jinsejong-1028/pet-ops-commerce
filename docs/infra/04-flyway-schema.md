# Flyway Schema

이 문서는 현재 DB migration 기준을 정리합니다.
초기에는 `V1__initial_schema.sql`만 있었지만, 현재는 재고 location, 업무 번호, 재고 원장, 주문 fulfillment workflow까지 migration으로 관리합니다.

## 목표

- Flyway로 DB schema를 버전 관리합니다.
- PostgreSQL에 현재 프로젝트 기준 테이블 구조를 생성합니다.
- JPA `ddl-auto=validate` 전략의 기준 DB 구조를 만듭니다.
- 회원, 상품, LOT, 재고, 주문, 판매/출고/구매/입고 workflow의 최소 도메인 구조를 잡습니다.

## 현재 Migration 목록

```text
V1__initial_schema.sql
V2__create_inventory_locations.sql
V3__add_business_number_rules.sql
V4__add_stock_jobs_and_movements.sql
V5__create_order_fulfillment_workflow.sql
```

`chore/squash-inventory-migrations` 이후에는 Docker DB reset을 전제로 중간 실험용 migration을 정리했습니다.
새 환경에서는 위 5개 migration만 순서대로 적용하면 현재 schema가 만들어집니다.

## V1 Initial Schema

| 테이블 | 역할 |
|---|---|
| `members` | 회원 |
| `pet_profiles` | 회원의 반려동물 프로필 |
| `product_categories` | 상품 카테고리 |
| `products` | 판매 상품 |
| `lots` | 상품별 LOT 관리 정보 |
| `warehouses` | 창고 |
| `stocks` | 상품/창고/location/LOT별 현재고 |
| `customer_orders` | 고객 주문 |
| `customer_order_deliveries` | 고객 주문 배송 정보 |
| `customer_order_items` | 고객 주문 상품 |
| `payments` | 결제 |
| `coupons` | 쿠폰 마스터 |
| `customer_order_events` | 고객 주문 이벤트 이력 |

## V2 Inventory Locations

`V2__create_inventory_locations.sql`은 창고 내부 위치를 관리하는 `locations` 테이블을 생성합니다.

| location_type | 의미 |
|---|---|
| `STAGE` | 입고 대기 location |
| `NORMAL` | 일반 보관 location |
| `PICKTO` | 피킹 완료 후 출고 전 대기 location |

## V3 Business Number

| 테이블 | 역할 |
|---|---|
| `business_number_rules` | 번호 포맷, 초기화 주기, 구간 크기 관리 |
| `business_number_sequences` | rule/scope/period별 다음 구간 시작값 관리 |

업무 번호 rule은 `BusinessNumberType` 기본값을 통해 자동 생성할 수 있습니다.
따라서 LOT 같은 추가 번호 rule을 migration seed로 강제 삽입하지 않습니다.

## V4 Stock Jobs And Movements

| 테이블 | 역할 |
|---|---|
| `stock_jobs` | 출고/입고/조정/이동 같은 재고 작업의 헤더와 현재 상태 |
| `stock_movements` | 재고 증감/이동 append-only 원장 |

`stock_movements.quantity`는 이번 처리 수량입니다.
`stock_movements.total_quantity`는 처리 후 해당 stock row의 총수량 snapshot입니다.
가용수량과 작업수량은 현재 상태인 `stocks`에서 관리하고, movement 원장에는 저장하지 않습니다.

## V5 Order Fulfillment Workflow

| 테이블 | 역할 |
|---|---|
| `sales_orders` | 고객 주문 생성 시 자동 생성되는 내부 판매 주문, `order_date`로 판매 주문 업무일자 관리 |
| `sales_order_items` | 판매 주문 품목 snapshot |
| `shipment_orders` | 판매 주문 기반 창고 출고 지시 |
| `shipment_order_items` | 출고 지시 품목과 할당/피킹/출고 수량 |
| `purchase_orders` | 운영사가 공급사에 넣는 구매 발주 |
| `purchase_order_items` | 구매 발주 품목 |
| `receiving_orders` | 구매 발주 기반 창고 입고 지시 |
| `receiving_order_items` | 입고 지시 품목과 입고 수량/LOT 예정 정보 |

## DB FK 제약을 걸지 않는 이유

현재 schema에서는 DB 레벨 foreign key constraint를 걸지 않습니다.
대신 `member_id`, `product_id`, `customer_order_id`, `warehouse_id` 같은 컬럼과 인덱스로 논리 관계를 표현합니다.

이유:

- 운영 데이터 보정이 쉬움
- 대량 배치 처리 시 유연함
- 서비스 로직에서 관계 검증 가능
- FK 검증 비용과 잠금 영향을 줄일 수 있음

주의점:

- DB가 잘못된 참조를 자동으로 막아주지 않음
- 애플리케이션 코드와 테스트가 더 중요함

## LOT 설계

`lots` 테이블은 사용자가 확인할 수 있는 `lot_key`와 업무 확장용 `lot1` ~ `lot5`를 함께 사용합니다.

| 컬럼 | 의미 |
|---|---|
| `lot_key` | LOT 업무 번호, 예: `LOT00000001` |
| `lot1` | LOT 주요 식별값 |
| `lot2` | 보조 LOT 정보 |
| `lot3` | 유효기간 |
| `lot4` | 입고일자 |
| `lot5` | 기타 관리값 |

`lot1`은 unique가 아닙니다.
같은 LOT 표시값이라도 상품, 유효기간, 입고일자 등 다른 관리 정보와 조합해 사용할 수 있습니다.

## 재고 설계

`stocks`는 상품, 창고, location, LOT 조합으로 현재고를 관리합니다.

```text
product_id + warehouse_id + location_id + lot_id
```

| 컬럼 | 의미 |
|---|---|
| `total_quantity` | 해당 location에 실제 존재하는 총수량 |
| `available_quantity` | 입고/이동/조정/할당에 사용할 수 있는 가용수량 |
| `working_quantity` | 할당/피킹/출고 작업 중인 수량 |
| `version` | 현재고 동시 수정 제어용 JPA version |

## 공통 감사 컬럼

주요 테이블에는 아래 컬럼을 공통으로 둡니다.

```text
created_at
created_by
updated_at
updated_by
deleted_at
```

`created_by`, `updated_by`는 초기 schema에서 FK를 걸지 않습니다.
시스템, 배치, 관리자 작업을 유연하게 기록하기 위해서입니다.

## 검증 방법

```powershell
docker compose ps
.\gradlew.bat test --console=plain
.\gradlew.bat bootRun
```

## 주의사항

기존 Docker volume에 이미 오래된 migration 결과가 있으면 현재 migration 파일과 충돌할 수 있습니다.
`chore/squash-inventory-migrations` 이후의 깨끗한 schema 검증은 Docker DB reset을 전제로 합니다.
DB reset은 데이터를 삭제하므로 실행 전에 반드시 백업 필요 여부를 확인합니다.