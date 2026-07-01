# Project Progress

이 문서는 PetOps Commerce 프로젝트의 완료, 진행, 예정 작업을 한눈에 보기 위한 상태판입니다.

## 현재 상태

| 항목 | 상태 |
|---|---|
| 기준 날짜 | 2026-07-01 |
| 로컬 경로 | `C:\pet-ops-commerce` |
| 원격 저장소 | `https://github.com/jinsejong-1028/pet-ops-commerce` |
| 현재 브랜치 | `refactor/stock-operation-delta-engine` |
| Git 상태 | 재고 수량 엔진을 source/target/bucket 델타 구조로 리팩토링 중 |
| 현재 DB | Docker PostgreSQL 16 |
| 마지막 완료 작업 | `refactor/stock-controller-consolidation` PR merge |
| 다음 추천 작업 | `feature/shipment-stock-workflow` |

## 완료 작업

| 순서 | 브랜치 | 작업 | 결과 문서 |
|---:|---|---|---|
| 1 | `main` | Spring Boot 초기 프로젝트 생성 | `docs/infra/02-spring-boot-project-setup.md` |
| 2 | `docs/git-workflow` | Git 브랜치/PR workflow 정리 | `docs/workflow/01-git-workflow.md` |
| 3 | `feature/health-api` | Health API 추가 | `docs/domains/01-health-api.md` |
| 4 | `chore/docker-postgres` | Docker PostgreSQL 구성 | `docs/infra/03-docker-postgres.md` |
| 5 | `chore/flyway-initial-schema` | Flyway 초기 schema 추가 | `docs/infra/04-flyway-schema.md` |
| 6 | `feature/member-domain` | 회원 Entity/Repository/Service/Controller/API 테스트 추가 | `docs/domains/02-member-domain.md` |
| 7 | `feature/common-api-response` | 공통 API 응답/전역 예외 처리 추가 | `docs/common/02-common-api-response.md` |
| 8 | `feature/product-domain` | 상품 카테고리/상품 Entity, Service, API, 테스트 추가 | `docs/domains/03-product-domain.md` |
| 9 | `feature/auth-jwt-login` | JWT 로그인 API와 인증 필터 추가 | `docs/common/03-auth-jwt-login.md` |
| 10 | `feature/audit-user-tracking` | JPA Auditing 기반 audit user tracking 추가 | `docs/common/04-audit-user-tracking.md` |
| 11 | `feature/inventory-domain` | location 단위 재고 도메인과 현재고 조회 API 추가 | `docs/domains/04-inventory-domain.md` |
| 12 | `feature/inventory-querydsl-search` | 현재고 동적 검색을 QueryDSL custom repository 구조로 전환 | `docs/domains/05-inventory-querydsl-search.md` |
| 13 | `feature/order-domain` | 주문/주문상품 도메인과 주문 생성 API 추가 | `docs/domains/06-order-domain.md` |
| 14 | `fix/auth-member-status-check` | 비활성 회원 로그인 차단과 URL 권한 정책 정리 | `docs/common/05-auth-security-policy.md` |
| 15 | `refactor/business-number-generator` | DB 기반 업무 번호 생성기와 주문번호 공통화 추가 | `docs/common/06-business-number-generator.md` |
| 16 | `refactor/business-number-generator-responsibility` | 업무 번호 생성기 책임 정리와 테스트용 API 제거 | `docs/common/06-business-number-generator.md`, `docs/logs/2026-06-25.md` |
| 17 | `test/business-number-concurrency` | Testcontainers PostgreSQL 기반 업무 번호 구간 할당 동시성 검증 추가 | `docs/common/06-business-number-generator.md`, `docs/logs/2026-06-26.md` |
| 18 | `feature/inventory-stock-workflow` | stock_jobs/stock_movements 기반 재고 할당, PICKTO 이동, 출고 API 추가 | `docs/domains/04-inventory-domain.md`, `docs/logs/2026-06-26.md` |
| 19 | `feature/inventory-admin-stock-command` | 창고/location/입고성 현재고 생성 API와 재고 수량 변경 공통 서비스 추가 | `docs/domains/04-inventory-domain.md`, `docs/logs/2026-06-26.md` |
| 20 | `refactor/inventory-ledger-schema-cleanup` | 재고 원장 컬럼명/순서와 job 완료 시각 정리 | `docs/domains/04-inventory-domain.md` |
| 21 | `chore/squash-inventory-migrations` | Docker DB reset 전제 재고/주문/출고/입고 workflow migration 최종 schema 정리 | `docs/logs/2026-06-29.md` |
| 22 | `fix/location-type-normal-typo` | location type 오타를 `NORMAL`로 수정 | `docs/domains/04-inventory-domain.md` |
| 23 | `docs/update-current-project-docs` | 현재 migration, API, 재고, 주문 workflow 기준으로 프로젝트 문서 최신화 | `docs/architecture/02-erd.md`, `docs/api/01-api-spec.md`, `docs/planning/05-project-progress.md` |
| 24 | `docs/reorganize-documentation` | 루트 README와 docs 하위 폴더 구조 정리 | `README.md`, `docs/README.md` |
| 25 | `feature/sales-order-warehouse-confirm-flow` | 판매 주문 창고 지정 후 확정/취소/출고 생성 흐름 정리 | `docs/api/01-api-spec.md`, `docs/architecture/02-erd.md`, `docs/domains/06-order-domain.md`, `docs/logs/2026-07-01.md` |
| 26 | `chore/openapi-docs` | Springdoc Swagger UI와 OpenAPI 문서화 추가 | `docs/api/01-api-spec.md`, `docs/logs/2026-07-01.md` |
| 27 | `refactor/stock-controller-consolidation` | StockController 단일 진입점, StockService facade, StockOperationService command 흐름 정리 | `docs/domains/04-inventory-domain.md`, `docs/api/01-api-spec.md`, `docs/logs/2026-07-01.md` |

## 현재 진행 작업

현재 브랜치는 재고 수량 엔진 내부 구조 리팩토링용입니다.

```text
refactor/stock-operation-delta-engine
```

마지막 완료 흐름:

```text
refactor/stock-controller-consolidation
```

완료 내용:

- `/api/v1/admin/stocks` 계열 Controller를 `StockController` 하나로 단일화
- `StockCommandService`, `StockWorkflowService`를 제거하고 `StockService` facade로 통합
- `StockOperationService.execute(StockOperationCommand)` 단일 수량 엔진 구조로 정리
- operation command/result/type을 `service.operation` 패키지로 분리
- 0수량 stock row는 삭제하지 않고 유지하며 목록 조회 기본값에서는 제외
- `includeZero=true` 조회 옵션 추가
- LOT 속성 변경 API `POST /api/v1/admin/stocks/change-lot` 추가
- `StockOperationService` 내부 업무 타입 switch를 제거하고 4개 수량 primitive 조합으로 처리
- `LOT_CHANGE_OUT`, `LOT_CHANGE_IN` movement 이력 추가

유지할 주의사항:

- 기존 migration은 수정하지 않았습니다.
- `src/test/**`는 수정하지 않았습니다.
- 0수량 row는 force delete하지 않고 조회 조건으로 제어합니다.
- 재고 수량 엔진의 command/result는 DB 저장 Entity가 아니므로 `service.operation`에 둡니다.
- Codex는 Gradle, Docker, bootRun 명령을 직접 실행하지 않고 사용자에게 명령만 안내합니다.

## 다음 추천 작업

### 1. 출고 지시 기반 재고 workflow 연결

브랜치 후보:

```text
feature/shipment-stock-workflow
```

목표:

- 출고 지시(`shipment_orders`) 기준 재고 할당 API 설계
- `shipment_order_items.allocated_quantity`, `picked_quantity`, `shipped_quantity`와 stock job 연결
- PICKTO 이동과 출고 확정 API를 출고 지시 workflow에 연결
- 관리자 stock 테스트 API와 실제 출고 workflow API의 역할 분리
- HTTP 테스트 순서와 DB 확인 SQL 문서화

## 보류 작업

| 작업 | 보류 이유 | 추후 브랜치 후보 |
|---|---|---|
| 관리자 작업 로그 | 핵심 CRUD와 audit user tracking을 먼저 안정화한 뒤 운영 추적 기능으로 분리 | `feature/admin-action-log` |
| 장바구니 | 상품/회원/재고 기본 구조 이후 진행 | `feature/cart-domain` |
| 회원 쿠폰 발급 이력 | 쿠폰 정책 확정 후 진행 | `feature/coupon-domain` |
| 주문 쿠폰 적용 이력 | 주문/쿠폰 도메인 이후 진행 | `feature/order-coupon` |
| Redis | 핵심 CRUD 이후 성능/동시성 학습 단계에서 진행 | `chore/redis-cache` |
| Message Queue | 주문 이벤트 구조 이후 진행 | `feature/order-event-queue` |
| Batch | 주문/재고 데이터가 쌓인 뒤 진행 | `feature/batch-jobs` |
| Nginx 로드밸런싱 | API 서버가 안정화된 뒤 진행 | `chore/nginx-load-balancing` |
| 모니터링 | Actuator endpoint 정리 후 진행 | `chore/monitoring` |

## 다음 세션 시작 기준

`main`은 OpenAPI 문서화와 재고 operation 리팩토링 merge가 완료된 clean 상태입니다.

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: refactor/stock-controller-consolidation PR merge 완료, git status clean
다음 작업: feature/shipment-stock-workflow
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

첫 확인 항목:

- `git checkout main`
- `git pull`
- `git checkout -b feature/shipment-stock-workflow`
- `git status --short --branch`

## 검증 기준

문서 작업:

```powershell
git diff --check
```

코드 작업 기본 검증은 사용자가 직접 실행합니다:

```powershell
.\gradlew.bat test --console=plain
```
