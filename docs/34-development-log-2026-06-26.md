# Development Log - 2026-06-26 Inventory Stock Workflow

이 문서는 재고 작업/이동 원장 구조와 할당, PICKTO 이동, 출고 API 구현 내용을 정리합니다.

## 작업 요약

기존 `stock_histories`를 제거하고 `stock_jobs + stock_movements` 구조로 재고 작업과 재고 이동 이력을 분리했습니다.

```text
stock_jobs
-> 작업 헤더와 현재 상태

stock_movements
-> 재고 증감/이동 원장
```

## 완료한 작업

브랜치:

```text
feature/inventory-stock-workflow
```

완료 내용:

- V4 migration 추가
- `stock_histories` 제거
- `stock_jobs` 테이블 추가
- `stock_movements` 테이블 추가
- `StockJob`, `StockMovement` Entity 추가
- 재고 작업/이동 enum 추가
- `Stock` 수량 변경 메서드 추가
- `StockWorkflowService` 추가
- `StockWorkflowController` 추가
- 재고 할당 API 추가
- PICKTO 이동 API 추가
- 출고 API 추가
- `http/inventory-api.http` 요청 예시 추가
- 재고 도메인 문서 갱신

## 수량 처리 기준

할당:

```text
total_quantity 유지
working_quantity 증가
```

PICKTO 이동:

```text
source stock: total_quantity 감소, working_quantity 감소
PICKTO stock: total_quantity 증가, working_quantity 증가
```

출고:

```text
PICKTO stock: total_quantity 감소, working_quantity 감소
```

## 검증

실행한 검증:

```powershell
.\gradlew.bat test --console=plain
```

결과:

```text
BUILD SUCCESSFUL
```

## 다음 작업

다음 브랜치 후보:

```text
chore/openapi-docs
```

목표:

- Swagger/OpenAPI 설정
- Health/Member/Auth/Product/Inventory/Order API 문서화
- 재고 workflow API 문서화

## 다음 세션 시작 기준

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: git status clean, origin/main 동기화 완료
다음 작업: chore/openapi-docs
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```
