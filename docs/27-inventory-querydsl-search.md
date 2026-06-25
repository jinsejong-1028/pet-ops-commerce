# Inventory QueryDSL Search

## 작업 목적

재고 관리자 조회 API의 동적 검색 조건을 QueryDSL 기반으로 전환합니다.

기존 `@Query` 방식은 조건이 적을 때는 간단하지만, 관리자 화면 검색 조건이 늘어나면 JPQL 문자열이 길어지고 조건 관리가 어려워집니다.
이번 작업에서는 검색 조건을 DTO로 묶고, 조건별 `BooleanExpression`을 조합하는 구조로 바꿉니다.

## 구현 범위

이번 브랜치에서는 아래를 구현합니다.

- QueryDSL JPA 의존성 추가
- QueryDSL annotation processor 설정 추가
- `StockSearchCondition` 추가
- `StockRepositoryCustom` 추가
- `StockRepositoryImpl` 추가
- `StockRepository`에서 `@Query` 제거
- `StockService`에서 검색 조건 DTO 생성 후 custom repository 호출
- Service 테스트 수정
- context smoke test에 `StockRepository` mock 추가

## 변경 전 구조

기존 구조는 Repository 인터페이스 안에 JPQL을 직접 작성했습니다.

```text
StockRepository
-> @Query
-> productId, warehouseId, locationId null 조건 처리
```

특징:

- 코드가 한 파일에 있어 처음 보기 쉽습니다.
- 조건이 늘어나면 JPQL 문자열이 길어집니다.
- `:param is null or ...` 조건이 반복됩니다.

## 변경 후 구조

변경 후에는 Spring Data JPA 기본 CRUD와 QueryDSL 조회 책임을 분리합니다.

```text
StockRepository
-> JpaRepository
-> StockRepositoryCustom

StockRepositoryImpl
-> QueryDSL 동적 조건 조회 구현
```

조회 흐름:

```text
StockController
-> StockService.getStocks(productId, warehouseId, locationId)
-> StockSearchCondition 생성
-> StockRepository.searchStocks(condition)
-> StockRepositoryImpl.searchStocks(condition)
-> BooleanExpression 조건 조합
```

## 핵심 코드 기준

검색 조건 DTO:

```text
StockSearchCondition
- productId
- warehouseId
- locationId
```

조건 메서드:

```text
productIdEq(productId)
warehouseIdEq(warehouseId)
locationIdEq(locationId)
```

각 조건 메서드는 값이 없으면 `null`을 반환합니다.
QueryDSL은 `where()`에 들어온 null 조건을 무시합니다.

## QueryDSL 설정

`build.gradle`에 아래 계열의 의존성을 추가했습니다.

```text
querydsl-jpa:5.1.0:jakarta
querydsl-apt:5.1.0:jakarta
jakarta.annotation-api
jakarta.persistence-api
```

Spring Boot 3는 Jakarta Persistence를 사용하므로 QueryDSL 의존성도 `jakarta` classifier를 사용합니다.

## 생성 파일 기준

QueryDSL Q class는 Gradle compile 단계에서 생성됩니다.

예시:

```text
build/generated/sources/annotationProcessor/java/main/.../QStock.java
```

이 파일은 빌드 산출물이므로 Git에 커밋하지 않습니다.

## 검증 방법

자동 테스트:

```powershell
.\gradlew.bat test --console=plain
```

확인한 내용:

- QStock 생성
- Service 테스트 통과
- Controller 테스트 통과
- 전체 테스트 통과

## 다음 작업 후보

QueryDSL 검색 구조 위에 관리자 재고 검색 조건을 확장할 수 있습니다.

후보 조건:

```text
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

재고 수량 변경 API는 별도 브랜치에서 진행합니다.

```text
feature/inventory-stock-workflow
```