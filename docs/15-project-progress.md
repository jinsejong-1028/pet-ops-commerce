# Project Progress

이 문서는 PetOps Commerce 프로젝트의 완료, 진행, 예정 작업을 한눈에 보기 위한 상태판입니다.

## 현재 상태

| 항목 | 상태 |
|---|---|
| 기준 날짜 | 2026-06-23 |
| 로컬 경로 | `C:\pet-ops-commerce` |
| 원격 저장소 | `https://github.com/jinsejong-1028/pet-ops-commerce` |
| 현재 브랜치 | `main` |
| Git 상태 | `origin/main` 동기화, working tree clean |
| 현재 DB | Docker PostgreSQL 16 |
| 다음 추천 작업 | `feature/member-domain` |

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

## 현재 진행 작업

현재 진행 중인 기능 브랜치는 없습니다.

```text
main clean
```

## 다음 추천 작업

### 1. 회원 도메인

브랜치:

```text
feature/member-domain
```

목표:

- `members` 테이블과 매핑되는 JPA Entity 작성
- 회원 Repository 작성
- 회원 생성/조회 Service 작성
- 회원 API 초안 작성
- JPA Entity와 Flyway schema의 `validate` 흐름 확인

학습 포인트:

- JPA Entity
- Repository
- Service 계층
- Controller 계층
- validation
- 테스트

### 2. 공통 API 응답

브랜치:

```text
feature/common-api-response
```

목표:

- API 응답 형식 통일
- success/error 응답 구조 분리
- validation error 응답 기준 마련

### 3. 상품 도메인

브랜치:

```text
feature/product-domain
```

목표:

- 상품 카테고리와 상품 Entity 작성
- 상품 목록/상세 API 작성
- sale_status 기반 조회 조건 추가

## 보류 작업

| 작업 | 보류 이유 |
|---|---|
| 장바구니 | 주문/회원 기본 구조 이후 진행 |
| 회원 쿠폰 발급 이력 | 쿠폰 정책 확정 후 진행 |
| 주문 쿠폰 적용 이력 | 주문/쿠폰 도메인 이후 진행 |
| Redis | 핵심 CRUD 이후 성능/동시성 학습 단계에서 진행 |
| Message Queue | 주문 이벤트 구조 이후 진행 |
| Batch | 주문/재고 데이터가 쌓인 뒤 진행 |
| Nginx 로드밸런싱 | API 서버가 안정화된 뒤 진행 |
| 모니터링 | Actuator endpoint 정리 후 진행 |

## 검증 기준

기본 검증:

```powershell
.\gradlew.bat test
```

DB 포함 검증:

```powershell
docker compose up -d
docker compose ps
.\gradlew.bat bootRun
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
- PR merge 후 로컬/원격 브랜치를 정리합니다.
