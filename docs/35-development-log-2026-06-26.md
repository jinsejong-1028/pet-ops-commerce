# Development Log - 2026-06-26

## 작업 브랜치

```text
feature/inventory-admin-stock-command
```

## 작업 내용

- 관리자 창고 생성 API를 추가했습니다.
- 관리자 location 생성 API를 추가했습니다.
- 입고 확정 흐름을 고려한 현재고 생성/증가 API를 추가했습니다.
- LOT 업무번호 `LOT00000001` 형식 생성을 위해 `BusinessNumberType.LOT`을 추가했습니다.
- `lots.lot_key`를 추가하고 기존 `product_id + lot1` unique 제약을 제거했습니다.
- `stocks.available_quantity` 컬럼을 추가해 총수량/가용수량/작업수량을 모두 저장하도록 변경했습니다.
- `stock_movements`는 처리 수량과 처리 후 총수량 snapshot 중심의 원장 구조로 정리했습니다.
- 가용수량 기준 location 간 재고 이동 API를 추가했습니다.
- 수동 재고 조정은 `ADJUST` 단일 타입과 signed quantity로 처리하도록 정리했습니다.
- `StockOperationService`를 추가해 입고, 할당, PICK, 출고, 조정의 현재고 수량 변경과 원장 저장 책임을 공통화했습니다.
- PICKTO 이동 시 도착 현재고가 없으면 0 row를 만들지 않고 실제 PICK 수량으로 신규 작업 현재고를 생성하도록 수정했습니다.
- 재고 증감 공통 기준을 `StockQuantityBucket`과 signed delta 방식으로 정리했습니다.

## 설계 메모

일반 재고 증감과 location 이동은 항상 가용수량 기준으로 처리합니다.
작업수량은 이미 주문 할당 또는 PICKTO 이동 상태에 잡힌 수량이므로 수동 조정이나 일반 이동 대상에서 제외합니다.

```text
total_quantity = available_quantity + working_quantity
```

입고성 현재고 생성은 같은 LOT/current stock이 있으면 신규 row를 만들지 않고 기존 현재고를 증가시킵니다.

재고 이동은 from/to location의 signed delta로 처리합니다.
도착 현재고가 없을 때 `quantity = 0` row를 먼저 만들지 않고, 실제 이동 수량만큼 바로 생성합니다.
음수 delta인데 현재고가 없거나 수량이 부족하면 재고 부족 오류로 처리합니다.

## 검증

```powershell
.\gradlew.bat compileJava --console=plain
git diff --check
HTTP Client 수동 테스트
```## 정리한 실수 방지 기준

이번 작업 중 다음 기준을 명확히 했습니다.

- `BusinessNumberType.LOT`처럼 공통 업무번호 생성기가 기본 rule을 자동 생성하는 경우, 별도 seed migration을 추가하지 않습니다.
- 로컬 개발 DB에 잘못 들어간 location type 같은 데이터는 사용자가 직접 보정하고, 불필요한 데이터 보정 migration은 추가하지 않습니다.
- Flyway migration은 schema 변경이나 모든 환경에 필요한 기준 데이터에만 사용합니다.
- 기존 적용 migration은 checksum 문제를 피하기 위해 수정하지 않고, 필요한 schema 변경만 새 migration으로 추가합니다.

필요 시 로컬 DB 데이터 보정은 아래처럼 직접 실행합니다.

```sql
update locations
set location_type = 'NORMAL'
where location_type = 'STORAGE';
```