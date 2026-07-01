# Project Progress

이 문서는 PetOps Commerce 프로젝트의 완료, 진행, 예정 작업을 한눈에 보기 위한 상태판입니다.

## 현재 상태

| 항목 | 상태 |
|---|---|
| 기준 날짜 | 2026-07-01 |
| 로컬 경로 | `C:\pet-ops-commerce` |
| 원격 저장소 | `https://github.com/jinsejong-1028/pet-ops-commerce` |
| 현재 브랜치 | `main` |
| Git 상태 | `feature/sales-order-warehouse-confirm-flow` PR merge 완료, clean |
| 현재 DB | Docker PostgreSQL 16 |
| 마지막 완료 작업 | `feature/sales-order-warehouse-confirm-flow` PR merge |
| 다음 추천 작업 | `chore/openapi-docs` |

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

## 현재 진행 작업

현재 진행 중인 기능 브랜치는 없습니다.

마지막 완료 흐름:

```text
feature/sales-order-warehouse-confirm-flow
```

완료 내용:

- 판매 주문에 출고 창고를 먼저 지정하는 `PATCH /api/v1/admin/sales-orders/{salesOrderId}` 추가
- 판매 주문 확정 API에서 요청 body 제거
- 확정 시 `sales_orders.warehouse_id` 필수 검증 후 출고 주문 생성
- `customer_orders.confirmed_at` 저장
- `customer_order_items.status` 추가 및 확정/취소 상태 동기화
- `OrderItem`을 공통 audit 상속 구조로 정리해 `updated_by` 자동 기록

유지할 주의사항:

- 기존 migration은 수정하지 않고 새 `V6__add_sales_order_warehouse_and_customer_order_status.sql` migration을 추가했습니다.
- `V6`는 이미 로컬 DB에 적용된 이력이 있으므로 이후 주석/공백 변경도 금지합니다.
- PostgreSQL은 `ALTER TABLE ADD COLUMN`으로 물리 컬럼 순서를 중간에 지정할 수 없습니다. 컬럼 순서는 Entity/문서의 논리 순서와 신규 `create table` 작성 시점에서 관리합니다.
- migration에는 기존 데이터 보정용 `update/delete/임시 insert`를 넣지 않았습니다.
- Codex는 Gradle, Docker, bootRun 명령을 직접 실행하지 않고 사용자에게 명령만 안내합니다.

## 다음 추천 작업

### 1. API 문서화

브랜치 후보:

```text
chore/openapi-docs
```

목표:

- Swagger/OpenAPI 설정
- Health/Member/Auth/Product/Inventory/Order API 문서화
- 관리자 workflow API 문서화 기준 수립

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

`main`은 판매 주문 창고 지정/확정 흐름 merge가 완료된 clean 상태입니다.

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: feature/sales-order-warehouse-confirm-flow PR merge 완료, git status clean
다음 작업: chore/openapi-docs
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

첫 확인 항목:

- `git checkout main`
- `git pull`
- `git checkout -b chore/openapi-docs`
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
