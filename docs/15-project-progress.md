# Project Progress

이 문서는 PetOps Commerce 프로젝트의 완료, 진행, 예정 작업을 한눈에 보기 위한 상태판입니다.

## 현재 상태

| 항목 | 상태 |
|---|---|
| 기준 날짜 | 2026-06-29 |
| 로컬 경로 | `C:\pet-ops-commerce` |
| 원격 저장소 | `https://github.com/jinsejong-1028/pet-ops-commerce` |
| 현재 브랜치 | `docs/update-current-project-docs` |
| Git 상태 | 전체 docs 최신화 중 |
| 현재 DB | Docker PostgreSQL 16 |
| 마지막 완료 작업 | `fix/location-type-normal-typo` |
| 다음 추천 작업 | `feature/order-fulfillment-workflow` |

## 완료 작업

| 순서 | 브랜치 | 작업 | 결과 문서 |
|---:|---|---|---|
| 1 | `main` | Spring Boot 초기 프로젝트 생성 | `09-spring-boot-project-setup.md` |
| 2 | `docs/git-workflow` | Git 브랜치/PR workflow 정리 | `10-git-workflow.md` |
| 3 | `feature/health-api` | Health API 추가 | `11-health-api.md` |
| 4 | `chore/docker-postgres` | Docker PostgreSQL 구성 | `12-docker-postgres.md` |
| 5 | `chore/flyway-initial-schema` | Flyway 초기 schema 추가 | `14-flyway-initial-schema.md` |
| 6 | `feature/member-domain` | 회원 Entity/Repository/Service/Controller/API 테스트 추가 | `17-member-domain.md` |
| 7 | `feature/common-api-response` | 공통 API 응답/전역 예외 처리 추가 | `20-common-api-response.md` |
| 8 | `feature/product-domain` | 상품 카테고리/상품 Entity, Service, API, 테스트 추가 | `21-product-domain.md` |
| 9 | `feature/auth-jwt-login` | JWT 로그인 API와 인증 필터 추가 | `22-auth-jwt-login.md` |
| 10 | `feature/audit-user-tracking` | JPA Auditing 기반 audit user tracking 추가 | `23-audit-user-tracking.md` |
| 11 | `feature/inventory-domain` | location 단위 재고 도메인과 현재고 조회 API 추가 | `25-inventory-domain.md` |
| 12 | `feature/inventory-querydsl-search` | 현재고 동적 검색을 QueryDSL custom repository 구조로 전환 | `27-inventory-querydsl-search.md` |
| 13 | `feature/order-domain` | 주문/주문상품 도메인과 주문 생성 API 추가 | `29-order-domain.md` |
| 14 | `fix/auth-member-status-check` | 비활성 회원 로그인 차단과 URL 권한 정책 정리 | `30-auth-security-policy.md` |
| 15 | `refactor/business-number-generator` | DB 기반 업무 번호 생성기와 주문번호 공통화 추가 | `31-business-number-generator.md` |
| 16 | `refactor/business-number-generator-responsibility` | 업무 번호 생성기 책임 정리와 테스트용 API 제거 | `31-business-number-generator.md`, `32-development-log-2026-06-25.md` |
| 17 | `test/business-number-concurrency` | Testcontainers PostgreSQL 기반 업무 번호 구간 할당 동시성 검증 추가 | `31-business-number-generator.md`, `33-development-log-2026-06-26.md` |
| 18 | `feature/inventory-stock-workflow` | stock_jobs/stock_movements 기반 재고 할당, PICKTO 이동, 출고 API 추가 | `25-inventory-domain.md`, `34-development-log-2026-06-26.md` |
| 19 | `feature/inventory-admin-stock-command` | 창고/location/입고성 현재고 생성 API와 재고 수량 변경 공통 서비스 추가 | `25-inventory-domain.md`, `35-development-log-2026-06-26.md` |
| 20 | `refactor/inventory-ledger-schema-cleanup` | 재고 원장 컬럼명/순서와 job 완료 시각 정리 | `25-inventory-domain.md` |
| 21 | `chore/squash-inventory-migrations` | Docker DB reset 전제 재고/주문/출고/입고 workflow migration 최종 schema 정리 | `36-development-log-2026-06-29.md` |
| 22 | `fix/location-type-normal-typo` | location type 오타를 `NORMAL`로 수정 | `25-inventory-domain.md` |

## 현재 진행 작업

현재 진행 중인 브랜치:

```text
docs/update-current-project-docs
```

목표:

- ERD를 현재 migration 기준으로 최신화
- API 문서를 현재 구현 API와 목표 workflow API로 분리
- Flyway 문서를 V1~V5 기준으로 정리
- 재고 문서의 stock_jobs/stock_movements 기준 보정
- 주문 문서에 customer order, delivery, sales/shipment/purchase/receiving 흐름 반영
- 로드맵과 다음 작업 후보 최신화

문서 작업만 진행하므로 전체 `gradlew test`는 실행하지 않습니다.

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

현재 브랜치 PR merge 후 주문 fulfillment workflow 작업은 아래 상태로 시작하면 됩니다.

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: git status clean, origin/main 동기화 완료
다음 작업: feature/order-fulfillment-workflow
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
```

시작 명령:

```powershell
cd C:\pet-ops-commerce
git checkout main
git pull
git checkout -b feature/order-fulfillment-workflow
git status
```

## 검증 기준

문서 작업:

```powershell
git diff --check
```

코드 작업 기본 검증:

```powershell
.\gradlew.bat test --console=plain
```