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
| `available_quantity` | `total_quantity - working_quantity`로 계산하는 가용수량 |

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
| `STORAGE` | 일반 보관 location |
| `PICKTO` | 피킹 완료 후 출고 전 대기 location |

## 할당, PICK, 출고 흐름

이번 브랜치에서는 수량 변경 API를 만들지 않고, 아래 흐름을 담을 수 있는 테이블과 조회 구조를 먼저 만듭니다.

```text
할당
- STORAGE location에서 주문에 사용할 재고를 찜
- total_quantity 유지
- working_quantity 증가
- available_quantity 감소

PICK
- STORAGE location에서 PICKTO location으로 재고 이동
- STORAGE location: total_quantity 감소, working_quantity 감소
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
    "availableQuantity": 97,
    "safetyQuantity": 10
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
기존 `stock_histories`는 단일 수량 컬럼 기준 이력이어서 `total_quantity`, `working_quantity` 구조와 맞지 않아 제거하고, 이동 원장 구조로 대체합니다.

```text
stock_jobs
= 작업 헤더, 현재 상태

stock_movements
= 재고 증감/이동 원장, append-only 이력
```

`stock_jobs`는 출고, 입고, 조정, 이동 같은 작업 묶음의 현재 상태를 관리합니다.
운영 화면에서 "이 작업이 어디까지 진행됐는지" 확인하는 기준입니다.

```text
job_no
job_type
warehouse_id
reference_type
reference_id
status
reason
```

`stock_movements`는 실제 stock row의 수량 변화 내역을 누적합니다.
`quantity`는 해당 location 기준 증감 방향을 부호로 표현하고, `total_quantity_delta`, `working_quantity_delta`로 어떤 수량 컬럼이 바뀌었는지 남깁니다.

```text
movement_type
location_id
from_location_id
to_location_id
product_id
lot_id
quantity
total_quantity_delta
working_quantity_delta
```

예시:

```text
ALLOCATE
- quantity: 3
- total_quantity_delta: 0
- working_quantity_delta: +3

PICK_OUT
- quantity: -3
- total_quantity_delta: -3
- working_quantity_delta: -3

PICK_IN
- quantity: +3
- total_quantity_delta: +3
- working_quantity_delta: +3

SHIP_OUT
- quantity: -3
- total_quantity_delta: -3
- working_quantity_delta: -3
```

## 재고 작업 API

### 재고 할당

```text
POST /api/v1/admin/stocks/allocate
```

처리:

```text
STORAGE stock
- total_quantity 유지
- working_quantity 증가

stock_jobs
- SALES_SHIPMENT 작업 생성
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

## 다음 작업 후보

QueryDSL 전환 이후에는 현재 구조 위에 재고 수량 변경 흐름을 추가합니다.

```text
POST /api/v1/admin/stocks/allocate
POST /api/v1/admin/stocks/pick
POST /api/v1/admin/stocks/outbound
POST /api/v1/admin/stocks/adjustment
```

후보 브랜치:

```text
feature/inventory-stock-workflow
```
