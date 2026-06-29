# Project Progress

이 문서는 PetOps Commerce 프로젝트의 완료, 진행, 예정 작업을 한눈에 보기 위한 상태판입니다.

## 현재 상태

| 항목 | 상태 |
|---|---|
| 기준 날짜 | 2026-06-29 |
| 로컬 경로 | `C:\pet-ops-commerce` |
| 원격 저장소 | `https://github.com/jinsejong-1028/pet-ops-commerce` |
| 현재 브랜치 | `feature/order-fulfillment-workflow` |
| Git 상태 | 주문 fulfillment workflow 구현 중, 커밋 전 dirty 상태 |
| 현재 DB | Docker PostgreSQL 16 |
| 마지막 완료 작업 | `docs/reorganize-documentation` |
| 다음 추천 작업 | `feature/order-fulfillment-workflow` 마무리 점검 후 PR |

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

## 현재 진행 작업

현재 진행 중인 브랜치:

```text
feature/order-fulfillment-workflow
```

목표:

- 고객 주문 생성 시 `sales_orders`, `sales_order_items`를 `CREATED` 상태로 자동 생성
- 관리자/오퍼레이터가 판매 주문을 `confirm` 또는 `cancel` 처리
- 판매 주문 확정 시 `customer_orders`도 `CONFIRMED`로 변경
- 판매 주문 확정 시 `shipment_orders`, `shipment_order_items` 생성
- 출고 지시 기반 재고 할당/PICK/출고 workflow 연결 준비

현재 주의사항:

- 이번 브랜치는 사용자가 마지막 Docker DB reset 예외를 명시한 상태입니다.
- 이후 작업부터는 기존 migration 수정이 아니라 새 `V다음번호__...sql` migration 추가가 기본입니다.
- migration에는 기존 데이터 보정용 `update/delete/임시 insert`를 넣지 않습니다.
- 이전 세션에서 `src/test/**` 파일이 수정되었습니다. 다음 세션에서는 유지/원복 여부를 먼저 확인합니다.
- Codex는 Gradle, Docker, bootRun 명령을 직접 실행하지 않고 사용자에게 명령만 안내합니다.

## 다음 추천 작업

### 1. 주문 fulfillment workflow 구현

브랜치 후보:

```text
feature/order-fulfillment-workflow
```

목표:

- 고객 주문을 판매 주문으로 확정
- 판매 주문 기반 출고 지시 생성
- shipment item 수량 진행률 관리
- 출고 지시 기반 할당/PICK/출고 workflow 연결
- 구매 발주와 입고 지시 흐름 구현

### 2. API 문서화

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

현재 브랜치는 커밋 전 dirty 상태이므로, 새 세션에서는 먼저 변경 파일을 리뷰합니다.

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: feature/order-fulfillment-workflow
현재 상태: 주문 fulfillment workflow 구현 중, git status dirty
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

첫 확인 항목:

- `git status --short --branch`
- `src/test/**` 변경 유지/원복 여부
- `V5__create_order_fulfillment_workflow.sql`은 이번 마지막 reset 예외로 유지할지 확인
- `OrderService`의 판매 주문 자동 생성 흐름 리뷰
- `SalesOrderService`의 confirm/cancel 흐름 리뷰

## 검증 기준

문서 작업:

```powershell
git diff --check
```

코드 작업 기본 검증은 사용자가 직접 실행합니다:

```powershell
.\gradlew.bat test --console=plain
```