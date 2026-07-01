# Inventory Domain

## 작업 목적

재고 도메인의 첫 구조를 구현합니다.

이번 작업은 단순 수량 조회가 아니라, 이후 관리자 화면에서 사용할 할당, PICK, 출고 흐름을 담을 수 있는 재고 모델을 먼저 잡는 것이 목적입니다.

```text
창고
-> location
-> 상품/LOT 현재고
-> 총수량, 작업수량, 가용수량
```

## 구현 범위

이번 브랜치에서는 아래를 구현합니다.

- `locations` 테이블 추가
- `stocks.quantity`를 `stocks.total_quantity`로 변경
- `stocks.working_quantity` 추가
- `stocks.location_id` 추가
- 창고, location, LOT, 현재고 Entity 추가
- 현재고 Repository/Service/Controller 추가
- 현재고 목록/단건 조회 API 추가
- Service/Controller 테스트 추가
- IntelliJ HTTP Client 요청 파일 추가

## 재고 수량 기준

현재고는 아래 세 값을 기준으로 해석합니다.

| 항목 | 의미 |
|---|---|
| `total_quantity` | 해당 location에 실제 존재하는 총수량 |
| `working_quantity` | 할당/피킹/출고 작업 중이라 판매 가능하지 않은 수량 |
| `available_quantity` | 일반 증감/이동/할당에 사용할 수 있는 가용수량 |

예시:

```text
total_quantity: 100
working_quantity: 3
available_quantity: 97
```

## Location 설계

`locations`는 창고 안의 실제 재고 위치입니다.

```text
warehouses
  -> locations
       -> stocks
```

location 유형은 아래처럼 시작합니다.

| 유형 | 의미 |
|---|---|
| `STAGE` | 입고 대기장 location |
| `NORMAL` | 일반 보관 location |
| `PICKTO` | 피킹 완료 후 출고 전 대기 location |

## 할당, PICK, 출고 흐름

이번 브랜치에서는 수량 변경 API를 만들지 않고, 아래 흐름을 담을 수 있는 테이블과 조회 구조를 먼저 만듭니다.

```text
할당
- NORMAL location에서 주문에 사용할 재고를 찜
- total_quantity 유지
- working_quantity 증가
- available_quantity 감소

PICK
- NORMAL location에서 PICKTO location으로 재고 이동
- NORMAL location: total_quantity 감소, working_quantity 감소
- PICKTO location: total_quantity 증가, working_quantity 증가
- 창고 전체 총수량은 유지

출고
- PICKTO location에서 실제 출고
- PICKTO location: total_quantity 감소, working_quantity 감소
- 창고 전체 총수량 감소
```

예시:

```text
초기
A-01-03: total 100, working 0, available 100
PICKTO:  total 0,   working 0, available 0

할당 3개
A-01-03: total 100, working 3, available 97
PICKTO:  total 0,   working 0, available 0

PICK
A-01-03: total 97, working 0, available 97
PICKTO:  total 3,  working 3, available 0

출고
A-01-03: total 97, working 0, available 97
PICKTO:  total 0,  working 0, available 0
```

## 권한 기준

재고 API의 운영 목표 권한은 `OPERATOR` 이상입니다.
다만 현재 프로젝트는 역할별 matcher를 아직 세분화하지 않았으므로, 이번 구현에서는 로그인 인증이 있는 사용자만 접근하도록 제한합니다.
역할별 권한 제한은 추후 Admin/Operator 인가 작업에서 분리합니다.

## API 목록

### 현재고 목록 조회

```text
GET /api/v1/admin/stocks
```

조건 조회:

```text
GET /api/v1/admin/stocks?productId=1&warehouseId=1&locationId=1
```

### 현재고 단건 조회

```text
GET /api/v1/admin/stocks/{stockId}
```

## 응답 구조

공통 API 응답 구조를 사용합니다.

```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "warehouseId": 1,
    "locationId": 1,
    "lotId": 1,
    "totalQuantity": 100,
    "workingQuantity": 3,
    "availableQuantity": 97
  },
  "message": "OK"
}
```

## DB 관계 설계

현재 프로젝트는 DB foreign key constraint를 걸지 않습니다.

```text
DB FK 제약 없음
product_id, warehouse_id, location_id, lot_id 컬럼 있음
Service에서 관계 검증
```

이번 조회 API는 기존 데이터를 읽는 범위이므로 별도 관계 검증이 필요하지 않습니다.
추후 입고, 할당, PICK, 출고 API를 만들 때 아래 검증을 Service에 추가합니다.

```text
productId 존재 여부
warehouseId 존재 여부
locationId 존재 여부
location.warehouseId와 요청 warehouseId 일치 여부
lotId 존재 여부
lot.productId와 요청 productId 일치 여부
```


## QueryDSL 검색 전환

현재고 목록 조회는 QueryDSL custom repository로 전환했습니다.

기존 1차 구현은 `@Query`에서 아래처럼 null 조건을 직접 처리했습니다.

```text
:param is null or field = :param
```

이번 전환 후에는 검색 조건을 `StockSearchCondition`으로 묶고, 조건별 `BooleanExpression`을 조합합니다.

```text
StockService
-> StockSearchCondition
-> StockRepository.searchStocks(condition)
-> StockRepositoryImpl
-> QueryDSL BooleanExpression 조합
```

이 구조를 쓰면 관리자 재고 검색 조건이 늘어날 때 Repository method가 폭발하지 않고, 조건 메서드를 하나씩 추가할 수 있습니다.

추가된 구조:

```text
StockSearchCondition
StockRepositoryCustom
StockRepositoryImpl
```

QueryDSL Q class는 Gradle annotation processor가 생성합니다.
생성 파일은 `build/generated/sources/annotationProcessor/java/main` 아래에 생기며 Git에는 커밋하지 않습니다.


## 재고 작업/이동 원장 구조

재고 수량 변경은 `stock_jobs`와 `stock_movements`로 관리합니다.
기존 단일 수량 이력 테이블은 `total_quantity`, `working_quantity` 구조와 맞지 않아 제거하고, 이동 원장 구조로 대체합니다.

```text
stock_jobs
= 작업 헤더, 현재 상태와 완료/삭제 시각

stock_movements
= 재고 증감/이동 원장, append-only 이력
```

`stock_jobs`는 출고처럼 단계가 있는 작업의 현재 상태를 관리합니다.
입고성 재고 반영, 수동 조정, 일반 location 이동처럼 별도 단계가 없는 작업도 같은 jobNo로 movement를 묶되, 생성과 동시에 `completed_at`을 기록합니다.

```text
job_no
job_type
warehouse_id
reference_type
reference_id
status
reason
completed_at
deleted_at
```

`job_type`은 재고 작업 자체의 유형입니다.
`reference_type`과 `reference_id`는 출고 지시, 입고 지시처럼 재고 작업을 발생시킨 외부 업무가 있을 때만 사용합니다.
따라서 직접 입고, 수동 조정, 일반 location 이동처럼 외부 참조 업무가 없으면 둘 다 `null`로 둡니다.
외부 업무 참조 기준:

```text
출고 지시 기반 작업: reference_type = SHIPMENT_ORDER, reference_id = shipment_orders.id
입고 지시 기반 작업: reference_type = RECEIVING_ORDER, reference_id = receiving_orders.id
직접 입고/수동 조정/일반 이동: reference_type = null, reference_id = null
```

`stock_movements`는 실제 stock row의 수량 변화 내역을 누적합니다.
`quantity`는 이번 movement의 처리 수량이고, `total_quantity`는 movement 처리 후 해당 stock row의 총수량 snapshot입니다.
`available_quantity`, `working_quantity`는 현재고 상태 컬럼이므로 원장에는 저장하지 않습니다.

```text
movement_type
from_location_id
to_location_id
product_id
lot_id
quantity
total_quantity
```

위치 기준은 아래처럼 해석합니다.

```text
RECEIVE_IN: from_location_id null, to_location_id 입고 location
ADJUST: from_location_id 조정 location, to_location_id null
ALLOCATE: from_location_id 할당 location, to_location_id null
TRANSFER_OUT/TRANSFER_IN: from_location_id 출발 location, to_location_id 도착 location
PICK_OUT/PICK_IN: from_location_id 보관 location, to_location_id PICKTO location
SHIP_OUT: from_location_id PICKTO location, to_location_id null
```

예시:

```text
ALLOCATE
- quantity: 3
- total_quantity: 100

PICK_OUT
- quantity: -3
- total_quantity: 97

PICK_IN
- quantity: +3
- total_quantity: 3

SHIP_OUT
- quantity: -3
- total_quantity: 0
```
## 재고 작업 API

### 재고 할당

```text
POST /api/v1/admin/stocks/allocate
```

처리:

```text
NORMAL stock
- total_quantity 유지
- working_quantity 증가

stock_jobs
- SHIPMENT_ORDER 기반 작업 생성
- status = ALLOCATED

stock_movements
- ALLOCATE 원장 생성
```

### PICKTO 이동

```text
POST /api/v1/admin/stocks/pick
```

처리:

```text
source stock
- total_quantity 감소
- working_quantity 감소

PICKTO stock
- total_quantity 증가
- working_quantity 증가

stock_jobs
- status = PICKED

stock_movements
- PICK_OUT 원장 생성
- PICK_IN 원장 생성
```

### 출고

```text
POST /api/v1/admin/stocks/outbound
```

처리:

```text
PICKTO stock
- total_quantity 감소
- working_quantity 감소

stock_jobs
- status = SHIPPED

stock_movements
- SHIP_OUT 원장 생성
```
## 검증 방법

자동 테스트:

```powershell
.\gradlew.bat test --console=plain
```

DB migration 포함 확인:

```powershell
docker compose ps
.\gradlew.bat bootRun
```

HTTP Client 확인:

```text
http/inventory-api.http
```

## 현재 재고 API 요약

현재 재고 도메인은 조회, 입고성 현재고 생성, 수동 조정, location 이동, LOT 속성 변경, 할당, PICK, 출고 API를 제공합니다.

```text
GET  /api/v1/admin/stocks
GET  /api/v1/admin/stocks?includeZero=true
GET  /api/v1/admin/stocks/{stockId}
POST /api/v1/admin/warehouses
POST /api/v1/admin/locations
POST /api/v1/admin/stocks
POST /api/v1/admin/stocks/transfer
POST /api/v1/admin/stocks/change-lot
POST /api/v1/admin/stocks/adjust
POST /api/v1/admin/stocks/allocate
POST /api/v1/admin/stocks/pick
POST /api/v1/admin/stocks/outbound
```

후속 작업에서는 이 API를 출고 지시(`shipment_orders`)와 입고 지시(`receiving_orders`) workflow에 연결합니다.

## Stock Controller / Service 책임 정리

`/api/v1/admin/stocks` 계열 API는 `StockController` 하나를 단일 HTTP 진입점으로 사용합니다.
Controller는 `StockService` facade만 호출하고, 업무 검증과 job 생성, workflow 상태 변경은 `StockService`에서 조율합니다.

```text
StockController
-> StockService: 기준정보 검증, LOT 생성/조회, stock_jobs 생성, workflow 상태 변경
-> StockOperationService.execute(command): 현재고 잠금, 수량 증감, stock_movements 저장
```

이 구조는 같은 base URL을 여러 Controller가 나누어 갖는 혼선을 줄이고, 외부 업무가 재고 수량 엔진을 여러 public 메서드로 직접 호출하지 않도록 책임 경계를 단순화합니다.

## 관리자 재고 명령 API

창고, location, 입고성 현재고 생성, 수동 조정 API를 추가했습니다.
입고성 현재고 생성은 추후 입고 화면에서 입고 확정 시 사용할 수 있는 흐름을 기준으로 설계했습니다.

```text
POST /api/v1/admin/warehouses
POST /api/v1/admin/locations
POST /api/v1/admin/stocks
POST /api/v1/admin/stocks/transfer
POST /api/v1/admin/stocks/change-lot
POST /api/v1/admin/stocks/adjust
```

### 입고성 현재고 생성/증가

`POST /api/v1/admin/stocks`는 단순 stock row 생성 API가 아닙니다.
입고 확정처럼 LOT를 찾거나 생성하고, 동일 현재고가 있으면 기존 재고를 증가시킵니다.

```text
productId 검증
warehouseId 검증
locationId 검증
lot4 null이면 오늘 날짜 적용
productId + lot1~lot5 기준 LOT 조회
LOT 없으면 lot_key 채번 후 생성
productId + warehouseId + locationId + lotId 현재고 잠금 조회
현재고 있으면 total/available 증가
현재고 없으면 신규 stock 생성
stock_jobs INBOUND 생성
stock_movements RECEIVE_IN 저장
stock_jobs completed_at 기록
```

LOT 업무 번호는 공통 업무번호 생성기를 사용합니다. `BusinessNumberType.LOT` 기본값으로 rule이 자동 생성되므로 LOT rule은 migration seed로 넣지 않습니다.

```text
LOT00000001
LOT00000002
```

### 수동 재고 조정

`POST /api/v1/admin/stocks/adjust`는 `ADJUST` movement type을 하나만 사용하고, `quantity`의 부호로 증감 방향을 판단합니다.

`POST /api/v1/admin/stocks/transfer`는 가용수량 기준 location 이동이며, `TRANSFER_OUT`/`TRANSFER_IN` movement를 남깁니다.
`POST /api/v1/admin/stocks/change-lot`는 같은 location에서 기존 LOT 재고를 새 LOT 재고로 이동하며, `LOT_CHANGE_OUT`/`LOT_CHANGE_IN` movement를 남깁니다. 대상 LOT 현재고가 이미 있으면 해당 row로 병합하고, 없으면 신규 현재고를 생성합니다.

```text
quantity > 0: total_quantity 증가, available_quantity 증가
quantity < 0: available_quantity 기준 차감 검증 후 total_quantity 감소, available_quantity 감소
quantity = 0: 오류
```

작업수량은 이미 할당 또는 피킹 중인 재고이므로 수동 조정 대상이 아닙니다.

## StockOperationService 공통화

재고 수량 변경 책임은 `StockOperationService.execute(command)` 단일 public 메서드로 모았습니다.
입고, 할당, 일반 이동, PICK, 출고, 조정, LOT 변경은 모두 `StockOperationCommand`의 from/to 위치와 `AVAILABLE`/`WORKING` bucket 조합으로 표현합니다.

```text
RECEIVE: from 없음, target AVAILABLE 증가
ALLOCATE: 같은 stock 내부 AVAILABLE 감소 + WORKING 증가
TRANSFER: source AVAILABLE 감소 + target AVAILABLE 증가
PICK: source WORKING 감소 + PICKTO target WORKING 증가
SHIP: source WORKING 감소, target 없음
ADJUST: signed quantity 기준 source AVAILABLE 증감
LOT_CHANGE: 같은 location에서 source LOT AVAILABLE 감소 + target LOT AVAILABLE 증가
```

재고 이동은 from/to location과 lot의 수량 delta로 처리합니다.
도착 현재고가 없을 때도 `quantity = 0` row를 먼저 만들지 않고, 실제 이동 수량만큼 바로 생성합니다.
음수 delta인데 현재고가 없거나 수량이 부족하면 재고 부족 오류로 처리합니다.
현재고가 0이 되어도 row를 즉시 삭제하지 않고 보존하며, 목록 조회에서는 기본 제외하고 `includeZero=true` 요청에서만 포함합니다.

## 수량 컬럼 기준 변경

가용수량은 계산값으로만 두지 않고 `stocks.available_quantity` 컬럼으로 저장합니다.
운영 화면과 DB 조회에서 현재 재고 상태를 바로 확인하기 위한 선택입니다.

```text
total_quantity = working_quantity + available_quantity
available_quantity >= 0
working_quantity >= 0
total_quantity >= 0
```

`stock_movements`는 `quantity`와 처리 후 `total_quantity` snapshot만 저장하고, 가용수량/작업수량은 `stocks` 현재 상태로 관리합니다.
