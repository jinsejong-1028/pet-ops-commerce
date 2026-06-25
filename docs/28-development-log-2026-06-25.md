# Development Log - 2026-06-25 QueryDSL

이 문서는 재고 QueryDSL 검색 전환 완료와 다음 주문 도메인 작업 인수인계를 정리합니다.

## 작업 요약

재고 현재고 조회의 동적 검색 조건을 `@Query` 기반 JPQL 문자열에서 QueryDSL custom repository 구조로 전환했습니다.

핵심 흐름:

```text
@Query 기반 현재고 검색
-> StockSearchCondition 추가
-> StockRepositoryCustom 추가
-> StockRepositoryImpl 추가
-> QueryDSL BooleanExpression 조건 조합
-> API URL/응답 구조 유지
```

## 완료된 작업

브랜치:

```text
feature/inventory-querydsl-search
```

완료 내용:

- QueryDSL JPA 의존성 추가
- QueryDSL annotation processor 설정 추가
- `StockSearchCondition` 추가
- `StockRepositoryCustom` 추가
- `StockRepositoryImpl` 추가
- `StockRepository`의 `@Query` 제거
- `StockService`에서 검색 조건 DTO 생성 후 custom repository 호출
- Service 테스트 수정
- context smoke test에 `StockRepository` mock 추가
- QueryDSL 전환 문서 작성

## 검증

실행한 검증:

```powershell
.\gradlew.bat test --console=plain
```

결과:

```text
BUILD SUCCESSFUL
```

확인한 내용:

- QStock 생성
- Service 테스트 통과
- Controller 테스트 통과
- 전체 테스트 통과
- 기존 `http/inventory-api.http` 수동 확인에서 API URL/응답 구조 유지 확인

## 병합 중 특이사항

`docs/update-inventory-handoff`에서 먼저 갱신된 `docs/25-inventory-domain.md`와 QueryDSL 브랜치의 같은 문서 변경이 겹쳐 PR에서 문서 충돌이 발생했습니다.

처리 결과:

- 재고 도메인 기본 설명 유지
- QueryDSL 검색 전환 설명 유지
- 다음 작업 후보는 재고 수량 변경 흐름으로 정리
- 후보 브랜치 `feature/inventory-stock-workflow` 기록

## 다음 작업

다음 브랜치:

```text
feature/order-domain
```

권장 1차 범위:

- `orders` Entity 작성
- `order_items` Entity 작성
- `OrderStatus` enum 작성
- `OrderRepository` 작성
- `OrderItemRepository` 작성
- 주문 생성 요청/응답 DTO 작성
- `OrderService` 작성
- `OrderController` 작성
- Service/Controller 테스트 작성
- `http/order-api.http` 작성
- 주문 도메인 문서 작성

1차 주문 생성에서는 재고 차감까지 바로 들어가지 않고 아래까지만 처리합니다.

```text
회원 존재 확인
상품 존재 확인
상품 판매상태 확인
주문 금액 계산
주문/주문상품 저장
```

재고 할당, PICK, 출고는 별도 브랜치에서 진행합니다.

```text
feature/inventory-stock-workflow
```

## 다음 세션 시작 기준

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: git status clean, origin/main 동기화 완료
다음 작업: feature/order-domain
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

시작 명령:

```powershell
cd C:\pet-ops-commerce
git checkout main
git pull
git checkout -b feature/order-domain
git status
```