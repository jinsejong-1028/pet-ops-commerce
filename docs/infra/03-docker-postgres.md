# Docker PostgreSQL

이 문서는 `chore/docker-postgres` 브랜치에서 추가한 로컬 PostgreSQL 실행 환경을 정리합니다.

## 목표

- Windows에 PostgreSQL을 직접 설치하지 않고 Docker로 DB 실행
- Spring Boot가 로컬 PostgreSQL에 연결되도록 설정
- `bootRun` 실행 시 DB 설정 누락으로 실패하던 문제 해결
- 이후 JPA, Flyway, 회원/상품/주문 도메인 구현을 위한 DB 기반 마련

## 추가 파일

| 파일 | 역할 |
|---|---|
| `docker-compose.yml` | PostgreSQL 컨테이너 실행 설정 |
| `application.properties` | Spring Boot DB/JPA/Flyway 연결 설정 |
| `docs/infra/03-docker-postgres.md` | Docker PostgreSQL 구성 설명 문서 |

## docker-compose.yml 구조

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: petops-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: petops
      POSTGRES_USER: petops
      POSTGRES_PASSWORD: petops
```

의미:

- `postgres`: Compose에서 관리할 서비스 이름
- `image`: 사용할 PostgreSQL Docker 이미지
- `container_name`: 실행될 컨테이너 이름
- `ports`: 내 PC의 5432 포트를 컨테이너의 5432 포트와 연결
- `POSTGRES_DB`: 생성할 DB 이름
- `POSTGRES_USER`: DB 접속 계정
- `POSTGRES_PASSWORD`: 로컬 개발용 비밀번호

## volume 설정

```yaml
volumes:
  - petops-postgres-data:/var/lib/postgresql/data
```

PostgreSQL 데이터는 컨테이너 내부의 `/var/lib/postgresql/data`에 저장됩니다.

컨테이너를 삭제해도 데이터를 유지하기 위해 Docker volume을 사용합니다.

volume이 없으면 컨테이너를 지울 때 DB 데이터도 함께 사라질 수 있습니다.

## healthcheck 설정

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U petops -d petops"]
```

PostgreSQL이 실제로 접속 가능한 상태인지 Docker가 주기적으로 확인합니다.

`docker compose ps`에서 `healthy`가 보이면 DB가 준비된 상태입니다.

## Spring Boot 연결 설정

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/petops
spring.datasource.username=petops
spring.datasource.password=petops
spring.datasource.driver-class-name=org.postgresql.Driver
```

Spring Boot는 위 설정을 보고 로컬 PostgreSQL에 접속합니다.

연결 흐름:

```text
Spring Boot
↓ localhost:5432
Docker port mapping
↓
PostgreSQL container
↓
petops database
```

## 환경변수 기본값

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/petops}
```

의미:

- `SPRING_DATASOURCE_URL` 환경변수가 있으면 그 값을 사용
- 없으면 `jdbc:postgresql://localhost:5432/petops`를 기본값으로 사용

로컬 개발에서는 기본값을 쓰고, 배포 환경에서는 환경변수로 DB 접속 정보를 바꿀 수 있습니다.

## JPA 설정

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

- `ddl-auto=validate`: Entity와 DB 테이블이 맞는지 검증만 합니다.
- `open-in-view=false`: 웹 요청이 끝난 뒤까지 DB 세션을 열어두지 않습니다.

테이블 생성은 Hibernate 자동 생성보다 Flyway SQL migration으로 관리할 예정입니다.

## Flyway 설정

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

Flyway는 DB 변경 이력을 SQL 파일로 관리하는 도구입니다.

아직 migration 파일이 없기 때문에 지금은 DB 연결과 Flyway 초기 구동 여부만 확인합니다.

## 없으면 어떻게 되는가

`docker-compose.yml`이 없으면 다음 명령에서 실패합니다.

```powershell
docker compose ps
```

오류:

```text
no configuration file provided: not found
```

`application.properties`에 DB 설정이 없으면 `bootRun`에서 실패합니다.

오류:

```text
Failed to configure a DataSource: 'url' attribute is not specified
```

## 실행 순서

PostgreSQL 컨테이너 실행:

```powershell
docker compose up -d
```

상태 확인:

```powershell
docker compose ps
```

테스트:

```powershell
.\gradlew.bat test
```

서버 실행:

```powershell
.\gradlew.bat bootRun
```

Health API 확인:

```text
http://localhost:8080/api/v1/health
```

## 주의사항

- `petops/petops` 계정과 비밀번호는 로컬 개발용입니다.
- 운영 환경에서는 비밀번호를 코드에 직접 적지 않고 환경변수나 Secret Manager를 사용해야 합니다.
- 5432 포트를 이미 다른 PostgreSQL이 사용 중이면 포트 충돌이 발생할 수 있습니다.