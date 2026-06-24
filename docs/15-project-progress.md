# Project Progress

이 문서는 PetOps Commerce 프로젝트의 완료, 진행, 예정 작업을 한눈에 보기 위한 상태판입니다.

## 현재 상태

| 항목 | 상태 |
|---|---|
| 기준 날짜 | 2026-06-24 |
| 로컬 경로 | `C:\pet-ops-commerce` |
| 원격 저장소 | `https://github.com/jinsejong-1028/pet-ops-commerce` |
| 현재 브랜치 | `docs/update-session-handoff` |
| Git 상태 | `main` 최신화 후 세션 인수인계 문서/skill 정리 중 |
| 현재 DB | Docker PostgreSQL 16 |
| 마지막 완료 작업 | `docs/defer-admin-action-log` |
| 다음 추천 작업 | `feature/inventory-domain` |

## 완료 작업

| 순서 | 브랜치 | 작업 | 결과 문서 |
|---:|---|---|---|
| 1 | `main` | Spring Boot 초기 프로젝트 생성 | `09-spring-boot-project-setup.md` |
| 2 | `docs/git-workflow` | Git 브랜치/PR workflow 정리 | `10-git-workflow.md` |
| 3 | `feature/health-api` | Health API 추가 | `11-health-api.md` |
| 4 | `chore/docker-postgres` | Docker PostgreSQL 구성 | `12-docker-postgres.md` |
| 5 | `chore/document-app-properties` | `application.properties` 주석 정리 | `12-docker-postgres.md` |
| 6 | `docs/restore-planning-docs` | 초기 설계 문서 00~09 복구 | `13-development-log-2026-06-22.md` |
| 7 | `chore/flyway-initial-schema` | Flyway 초기 schema 추가 | `14-flyway-initial-schema.md` |
| 8 | `docs/update-project-progress` | 프로젝트 진행 현황/로드맵 최신화 | `15-project-progress.md`, `16-development-log-2026-06-23.md` |
| 9 | `chore/add-schema-comments` | 초기 schema SQL 개발자 주석 추가 | `14-flyway-initial-schema.md` |
| 10 | `feature/member-domain` | 회원 Entity/Repository/Service/Controller/API 테스트 추가 | `17-member-domain.md` |
| 11 | `chore/add-http-client-requests` | IntelliJ HTTP Client API 테스트 흐름 추가 | `18-api-test-workflow.md` |
| 12 | `fix/security-error-response` | 예외 응답이 403으로 바뀌는 문제 수정 | `19-security-error-response.md` |
| 13 | `docs/standardize-member-comments` | member 도메인 JavaDoc bullet 스타일 통일 | `petops-portfolio-workflow` skill 반영 |
| 14 | `feature/common-api-response` | 공통 API 응답/전역 예외 처리 추가 | `20-common-api-response.md` |
| 15 | `feature/product-domain` | 상품 카테고리/상품 Entity, Service, API, 테스트 추가 | `21-product-domain.md` |
| 16 | `docs/clarify-commerce-model` | B2C 단일 운영사 상품 판매 모델 명확화 | `00-project-overview.md`, `01-requirements.md`, `03-erd.md` |
| 17 | `feature/auth-jwt-login` | JWT 로그인 API와 인증 필터 추가 | `22-auth-jwt-login.md` |
| 18 | `feature/audit-user-tracking` | JPA Auditing 기반 created_by/updated_by 자동 입력 | `23-audit-user-tracking.md` |
| 19 | `docs/defer-admin-action-log` | 관리자 작업 로그 보류 결정과 추후 범위 정리 | `15-project-progress.md`, `07-roadmap.md` |

## 현재 진행 작업

현재 진행 중인 작업은 다음 세션 인수인계 기준 정리입니다.

```text
docs/update-session-handoff
```

목표:

- 오늘까지의 작업 흐름을 개발 로그로 정리
- 프로젝트 진행 현황과 로드맵을 현재 상태에 맞게 정리
- `petops-portfolio-workflow` skill을 최신 작업 방식에 맞게 개선
- 다음 세션이 같은 방식으로 `feature/inventory-domain`을 시작할 수 있게 기준 고정

## 다음 추천 작업

### 1. 재고 도메인

브랜치:

```text
feature/inventory-domain
```

목표:

- 창고 Entity 작성
- lot Entity 작성
- 재고 Entity 작성
- 상품별 재고 조회 API 작성
- lot 기준 재고 관리 구조 검증
- 상품/lot/재고 관계를 FK 제약 없이 애플리케이션에서 검증

학습 포인트:

- 재고 도메인 모델링
- lot 기반 상세 재고 관리
- JPA Repository 조회 메서드
- 트랜잭션 경계
- 재고 수량 변경 전 동시성 고려 지점 정리

### 2. 주문 도메인

브랜치:

```text
feature/order-domain
```

목표:

- 주문 Entity 작성
- 주문 상품 Entity 작성
- 주문 생성 API 작성
- 주문 생성 시 상품/회원/재고 검증 흐름 설계

### 3. API 문서화

브랜치:

```text
chore/openapi-docs
```

목표:

- Swagger/OpenAPI 설정
- Health/Member/Auth/Product API 문서화
- 이후 API 추가 시 문서 자동 확인 흐름 적용

## 보류 작업

| 작업 | 보류 이유 | 추후 브랜치 후보 |
|---|---|---|
| 관리자 작업 로그 | 핵심 CRUD와 audit user tracking을 먼저 안정화한 뒤 운영 추적 기능으로 분리 | `feature/admin-action-log` |
| 관리자 작업 IP/User-Agent 저장 | `created_by`, `updated_by`와 성격이 다르므로 별도 로그 테이블에서 관리 | `feature/admin-action-log` |
| 장바구니 | 상품/회원/재고 기본 구조 이후 진행 | `feature/cart-domain` |
| 회원 쿠폰 발급 이력 | 쿠폰 정책 확정 후 진행 | `feature/coupon-domain` |
| 주문 쿠폰 적용 이력 | 주문/쿠폰 도메인 이후 진행 | `feature/order-coupon` |
| Redis | 핵심 CRUD 이후 성능/동시성 학습 단계에서 진행 | `chore/redis-cache` |
| Message Queue | 주문 이벤트 구조 이후 진행 | `feature/order-event-queue` |
| Batch | 주문/재고 데이터가 쌓인 뒤 진행 | `feature/batch-jobs` |
| Nginx 로드밸런싱 | API 서버가 안정화된 뒤 진행 | `chore/nginx-load-balancing` |
| 모니터링 | Actuator endpoint 정리 후 진행 | `chore/monitoring` |

## 관리자 작업 로그 보류 기준

관리자 작업 로그는 아래 정보를 남기기 위한 운영 고도화 기능입니다.

```text
누가 어떤 관리자 작업을 했는지
어떤 대상에 작업했는지
요청 IP와 User-Agent가 무엇이었는지
작업 성공/실패 여부가 무엇이었는지
```

현재 `feature/audit-user-tracking`으로 `created_by`, `updated_by`는 이미 자동 입력됩니다. 따라서 상품 생성자와 수정자는 추적할 수 있습니다.

다만 IP, User-Agent, 작업 성공/실패, 요청 경로 같은 정보는 상품 테이블에 직접 넣기보다 `admin_action_logs` 같은 별도 로그 테이블로 분리하는 것이 좋습니다.

나중에 구현할 때 예상 범위:

```text
admin_action_logs Flyway migration 추가
AdminActionLog Entity 추가
AdminActionLogRepository 추가
AdminActionLogService 추가
요청 IP/User-Agent 추출 유틸 추가
상품 생성/수정/삭제 흐름에서 로그 저장
관리자 작업 로그 문서 추가
```

## 다음 세션 시작 기준

다음 세션에서 아래 상태로 시작하면 됩니다.

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: git status clean, origin/main 동기화 완료
다음 작업: feature/inventory-domain
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
```

시작 명령:

```powershell
cd C:\pet-ops-commerce
git checkout main
git pull
git checkout -b feature/inventory-domain
git status
```

## 검증 기준

기본 검증:

```powershell
.\gradlew.bat test --console=plain
```

DB 포함 검증:

```powershell
docker compose up -d
docker compose ps
.\gradlew.bat bootRun
```

API 수동 확인:

```text
http/member-api.http
http/auth-api.http
http/product-api.http
```

브라우저 확인:

```text
http://localhost:8080/api/v1/health
```

## 작업 원칙

- 모든 작업은 브랜치 단위로 진행합니다.
- 사용자가 Git/Docker/Gradle 명령을 직접 실행합니다.
- Codex는 파일 읽기, 분석, 설명, 승인 후 수정만 담당합니다.
- 구현과 함께 문서를 남깁니다.
- 코드 주석은 짧은 bullet형 JavaDoc을 기본으로 합니다.
- PR merge 후 로컬/원격 브랜치를 정리합니다.