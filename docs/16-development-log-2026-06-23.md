# Development Log - 2026-06-23

이 문서는 PetOps Commerce 프로젝트의 2026-06-23 진행 내용을 정리합니다.

## 시작 상태

- 현재 브랜치: `main`
- Git 상태: `origin/main` 동기화, working tree clean
- 다음 작업: `chore/flyway-initial-schema`
- 작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행

## 진행 브랜치

```text
chore/flyway-initial-schema
```

## 작업 내용

- Flyway 개념 설명
- Docker PostgreSQL과 Flyway 역할 분리 설명
- `V1__initial_schema.sql` 추가
- DB FK 제약 없이 논리 관계와 인덱스 중심으로 schema 작성
- `created_by`, `updated_by` 감사 컬럼 추가
- `lots` 테이블 추가
- `lots.lot1` ~ `lots.lot5` 구조 추가
- `lots.lot3`은 유효기간, `lots.lot4`는 입고일자로 문서화
- `stocks.lot_id` 추가
- `docs/03-erd.md` 보완
- `docs/14-flyway-initial-schema.md` 추가

## 생성/수정 파일

```text
docs/03-erd.md
docs/14-flyway-initial-schema.md
src/main/resources/db/migration/V1__initial_schema.sql
```

## Flyway 정리

Flyway는 별도 Docker 컨테이너가 아닙니다.

역할:

```text
Spring Boot 실행 시 SQL migration을 PostgreSQL에 적용
```

설정 위치:

```text
build.gradle
src/main/resources/application.properties
src/main/resources/db/migration/V1__initial_schema.sql
```

동작 흐름:

```text
bootRun
Spring Boot 실행
PostgreSQL 연결
Flyway가 db/migration 확인
V1__initial_schema.sql 실행
flyway_schema_history에 기록
```

## Docker 정리

Docker Compose는 PostgreSQL DB 서버를 실행합니다.

```powershell
docker compose up -d
```

의미:

```text
docker-compose.yml을 읽고 PostgreSQL 컨테이너를 백그라운드로 실행
```

상태 확인:

```powershell
docker compose ps
```

## 검증 결과

사용자 확인:

- `docker compose ps` 확인
- `./gradlew.bat test` 성공
- `./gradlew.bat bootRun` 성공
- 브라우저에서 `/api/v1/health` 확인

## Git 정리

- 원격 PR merge 완료
- 원격 브랜치 삭제 완료
- 로컬 브랜치 삭제 완료
- `git fetch --prune` 완료
- 최종 상태: `main`만 남고 working tree clean

## 최종 상태

```text
로컬 브랜치: main
원격 브랜치: origin/main
작업 트리: clean
```

## 다음 추천 작업

```text
feature/member-domain
```

이유:

- Flyway schema가 생겼으므로 JPA Entity를 만들기 좋은 시점입니다.
- `members` 테이블과 Java Entity 매핑을 통해 JPA 기본기를 학습할 수 있습니다.
- 이후 로그인/JWT, 주문, 쿠폰 기능으로 확장할 수 있습니다.
