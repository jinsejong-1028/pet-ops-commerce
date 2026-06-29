# Development Log - 2026-06-29

## 작업 브랜치

```text
chore/squash-inventory-migrations
```

## 작업 내용

- Docker DB reset을 전제로 재고 관련 migration 이력을 최종 schema 기준으로 정리했습니다.
- `V1__initial_schema.sql`에 LOT, 현재고 최종 구조를 직접 반영했습니다.
- `V2`는 location 테이블 생성 전용 migration으로 정리했습니다.
- `V4`는 stock_jobs, stock_movements 최종 구조를 직접 생성하도록 정리했습니다.
- 데이터 보정용 `V5`, cleanup 임시 테이블 방식의 `V6`, reference 보정용 `V7`을 제거했습니다.
- 모든 create table의 audit 컬럼 순서를 `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at` 기준으로 정리했습니다.
- 고객 주문 배송정보를 `order_deliveries`로 분리했습니다.
- 판매 확정 주문 `sales_orders`, 창고 출고 지시 `shipment_orders`를 추가했습니다.
- 공급사 구매 발주 `purchase_orders`, 창고 입고 지시 `receiving_orders`를 추가했습니다.
- 출고 수량 진행률은 `shipment_order_items`, 입고 확정 수량은 `receiving_order_items`에서 관리하도록 schema를 추가했습니다.

## 설계 메모

이번 작업은 운영 DB migration 방식이 아니라 포트폴리오 개발 초기의 schema 정리 작업입니다.
이미 적용된 DB에서는 checksum이 달라지므로 Docker volume reset 후 새 DB에 처음부터 적용해야 합니다.

```text
cleanup 임시 테이블 생성 금지
로컬 데이터 보정용 migration 제거
최종 schema는 create table에 직접 반영
```

## 검증

```powershell
.\gradlew.bat compileJava --console=plain
.\gradlew.bat test --console=plain
```

## Docker DB reset 필요

이번 브랜치 적용 후 로컬 DB는 기존 Flyway 이력과 맞지 않으므로 reset이 필요합니다.

```powershell
docker compose down -v
docker compose up -d
.\gradlew.bat bootRun
```