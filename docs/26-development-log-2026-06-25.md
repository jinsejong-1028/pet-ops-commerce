# Development Log - 2026-06-25

이 문서는 재고 도메인 완료 내용과 다음 QueryDSL 전환 작업을 인수인계하기 위해 작성합니다.

## 작업 요약

재고 도메인은 단순 현재고 조회가 아니라, 이후 관리자 화면의 할당, PICK, 출고 흐름을 담을 수 있는 구조로 정리했습니다.

핵심 흐름:

```text
재고 도메인 구현
-> locations 테이블 추가
-> total_quantity / working_quantity / available_quantity 구조 반영
-> STORAGE에서 PICKTO로 이동하는 PICK 개념 정리
-> 현재고 조회 API 추가
-> QueryDSL 전환은 별도 브랜치로 분리
```

## 완료된 주요 작업

### 재고 도메인

브랜치:

```text
feature/inventory-domain
```

완료 내용:

- `locations` 테이블 추가
- `stocks.quantity`를 `stocks.total_quantity`로 변경
- `stocks.working_quantity` 추가
- `stocks.location_id` 추가
- `Warehouse`, `Location`, `Lot`, `Stock` Entity 추가
- 재고 Repository 추가
- 현재고 Service/Controller 추가
- 현재고 목록/단건 조회 API 추가
- Service/Controller 테스트 추가
- `http/inventory-api.http` 추가
- 재고 도메인 문서 작성

### 재고 수량 모델

현재고는 아래 기준으로 해석합니다.

```text
total_quantity      해당 location에 실제 존재하는 총수량
working_quantity    할당/피킹/출고 작업 중이라 판매 가능하지 않은 수량
available_quantity  total_quantity - working_quantity
```

예시:

```text
total_quantity: 100
working_quantity: 3
available_quantity: 97
```

### Location과 PICKTO

`locations`는 창고 안의 실제 보관/작업 위치입니다.

```text
warehouses
  -> locations
       -> stocks
```

location 유형:

| 유형 | 의미 |
|---|---|
| `STORAGE` | 일반 보관 location |
| `PICKTO` | 피킹 완료 후 출고 전 대기 location |

PICK은 단순 상태 변경이 아니라 동일 창고 안에서 `STORAGE` location의 재고를 `PICKTO` location으로 이동한 상태로 봅니다.

```text
할당
- STORAGE location에서 주문 재고를 찜
- total_quantity 유지
- working_quantity 증가
- available_quantity 감소

PICK
- STORAGE location에서 PICKTO location으로 재고 이동
- STORAGE location: total_quantity 감소, working_quantity 감소
- PICKTO location: total_quantity 증가, working_quantity 증가

출고
- PICKTO location에서 실제 출고
- PICKTO location: total_quantity 감소, working_quantity 감소
```

## QueryDSL 전환을 분리한 이유

재고 조회는 관리자 화면에서 조건이 계속 늘어날 가능성이 높습니다.

예상 조건:

```text
productId
warehouseId
locationId
lotId
locationType
lotStatus
availableQuantity > 0
safetyQuantity 이하
유효기간 임박
입고일자 범위
정렬
페이징
```

현재 1차 구현은 `@Query`로 동적 조건을 처리합니다.
다음 작업에서는 QueryDSL로 전환해 검색 조건을 확장하기 쉬운 구조로 바꿉니다.

## 다음 작업

다음 브랜치:

```text
feature/inventory-querydsl-search
```

목표:

- QueryDSL 의존성과 annotation processor 설정 추가
- `StockRepositoryCustom` 추가
- `StockRepositoryImpl` 추가
- `StockSearchCondition` 추가
- 기존 `@Query` 기반 `findStocks` 제거
- 현재고 조회 테스트 유지/보강
- QueryDSL 전환 내용을 `25-inventory-domain.md` 또는 별도 문서에 반영

## 다음 세션 시작 기준

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: feature/inventory-querydsl-search
현재 상태: git status clean, origin/feature/inventory-querydsl-search 동기화 완료
다음 작업: Inventory QueryDSL 검색 전환
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

확인 명령:

```powershell
cd C:\pet-ops-commerce
git checkout feature/inventory-querydsl-search
git status
```

## 검증 기준

QueryDSL 전환 후 기본 검증:

```powershell
.\gradlew.bat test --console=plain
```

DB migration 포함 확인이 필요하면:

```powershell
docker compose ps
.\gradlew.bat bootRun
```

HTTP Client 확인:

```text
http/inventory-api.http
```