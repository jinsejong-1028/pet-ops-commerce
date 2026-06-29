# Spring Boot 프로젝트 골격 생성

## 1. 이번 단계에서 구현하는 것

이번 단계에서는 PetOps Commerce의 실제 Java/Spring Boot 프로젝트 골격을 생성합니다.

아직 회원, 상품, 주문 같은 본 기능을 구현하지 않습니다. 먼저 애플리케이션이 어떤 구조로 시작되는지 만들고, 가장 작은 API가 정상적으로 존재하는 상태를 목표로 합니다.

생성 대상은 다음과 같습니다.

- Gradle 기반 Spring Boot 프로젝트
- Java 21 설정
- Spring Boot 3.5.15 설정
- 기본 package 구조
- 공통 응답 객체
- Health API
- PostgreSQL 연결 설정 초안
- Docker Compose 초안
- 기본 테스트 구조

## 2. 왜 먼저 골격을 만드는가

큰 서비스를 만들 때 바로 주문, 재고, 결제부터 구현하면 구조가 쉽게 흐트러집니다.

그래서 먼저 아래 기준을 고정합니다.

- 코드는 어디에 둘 것인가
- API 요청은 어느 class에서 받을 것인가
- 비즈니스 로직은 어느 계층에서 처리할 것인가
- DB 접근은 어느 계층으로 분리할 것인가
- 공통 응답과 예외는 어떻게 관리할 것인가
- 나중에 Redis, Message Queue, Batch가 들어올 공간은 어디인가

이 단계는 집을 짓기 전에 뼈대와 배관 위치를 잡는 작업에 가깝습니다.

## 3. 핵심 용어 설명

### Spring Boot

Spring으로 웹 애플리케이션을 빠르게 만들 수 있게 도와주는 프레임워크입니다. 서버 실행, 기본 설정, 라이브러리 연결을 많이 자동화해줍니다.

### Gradle

Java 프로젝트의 빌드 도구입니다. 필요한 라이브러리를 내려받고, 컴파일하고, 테스트하고, 실행 가능한 jar 파일을 만드는 일을 담당합니다.

### Gradle Wrapper

PC에 Gradle이 직접 설치되어 있지 않아도 프로젝트에 포함된 `gradlew`로 정해진 Gradle 버전을 사용할 수 있게 해주는 파일입니다.

### Controller

외부 HTTP 요청을 처음 받는 계층입니다. 예를 들어 `GET /api/v1/health` 요청이 오면 Controller가 받습니다.

### Service

실제 업무 규칙을 처리하는 계층입니다. 주문 생성, 재고 차감, 쿠폰 검증 같은 비즈니스 로직이 여기에 위치합니다.

### Repository

DB와 통신하는 계층입니다. JPA를 사용하면 SQL을 직접 많이 쓰지 않아도 Entity를 저장하고 조회할 수 있습니다.

### Domain

서비스의 핵심 개념을 표현하는 영역입니다. 회원, 상품, 주문, 재고, 쿠폰 같은 것들이 도메인입니다.

## 4. package 구조

초기 package 구조는 아래처럼 시작합니다.

```text
com.petopscommerce
  PetOpsCommerceApplication.java
  global
    response
  domain
    health
    member
    product
    order
    stock
    coupon
```

처음에는 `health`만 실제 코드가 있고, 다른 도메인은 빈 package 또는 이후 단계에서 추가합니다.

## 5. 왜 domain별로 나누는가

이 프로젝트는 모듈러 모놀리스로 시작합니다.

모듈러 모놀리스는 서버는 하나지만 내부 코드를 도메인별로 나누는 방식입니다. 처음부터 주문 서버, 재고 서버, 회원 서버처럼 여러 서버로 나누지는 않습니다.

대신 package 경계를 미리 잘 잡아둡니다.

```text
domain.member
domain.product
domain.order
domain.stock
domain.coupon
```

이렇게 해두면 나중에 프로젝트가 커졌을 때 특정 도메인을 별도 서버로 분리하기 쉬워집니다.

## 6. Health API를 먼저 만드는 이유

처음 만들 API는 아래처럼 단순합니다.

```text
GET /api/v1/health
```

이 API는 기능적으로는 별것 없어 보이지만 중요합니다.

- 서버가 실행되는지 확인할 수 있습니다.
- Nginx 로드밸런싱에서 각 API 서버 상태 확인에 사용할 수 있습니다.
- 배포 후 모니터링과 연결하기 쉽습니다.
- 처음 만든 Controller, 응답 구조, 테스트 구조를 검증할 수 있습니다.

## 7. 이번 단계의 검증 방법

현재 PC에서 `java`, `gradle`, `docker`가 PATH에 잡히지 않아 실행 검증은 다음 단계에서 환경 설정 후 진행합니다.

이번 단계에서는 아래를 검증합니다.

- 프로젝트 파일이 생성되었는지
- Gradle Wrapper가 포함되었는지
- `build.gradle`에 Spring Boot 3.5.15와 Java 21 설정이 있는지
- Health API 코드가 있는지
- PostgreSQL Docker Compose 초안이 있는지
- 문서 링크가 README에 연결되었는지

환경 준비 후에는 아래 명령으로 검증합니다.

```bash
./gradlew test
./gradlew bootRun
```

Windows PowerShell에서는 다음처럼 실행합니다.

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

## 8. 포트폴리오 문장

> 프로젝트 초기에 Spring Boot와 Gradle 기반 백엔드 골격을 구성하고, 도메인별 package 구조를 먼저 잡았습니다. 기능 구현 전에 공통 응답, Health API, PostgreSQL 연결 준비, Docker Compose 초안을 구성해 이후 주문, 재고, 인증, 캐시, 메시징 기능을 안정적으로 확장할 수 있도록 했습니다.

## 9. 다음 단계

다음 단계에서는 개발 환경을 확인합니다.

- Java 21 설치 또는 PATH 설정
- Docker Desktop 설치 또는 PATH 설정
- Gradle Wrapper로 테스트 실행
- PostgreSQL 컨테이너 실행
- Health API 실제 호출
## 10. 이번 단계 결과

이번 단계에서 실제로 추가한 파일은 다음과 같습니다.

| 파일 | 역할 |
|---|---|
| `src/main/java/com/petopscommerce/global/response/ApiResponse.java` | 모든 API가 공통 형식으로 응답하기 위한 wrapper |
| `src/main/java/com/petopscommerce/domain/health/controller/HealthController.java` | 첫 번째 API인 Health API |
| `src/main/java/com/petopscommerce/domain/health/dto/HealthResponse.java` | Health API 응답 데이터 |
| `src/main/java/com/petopscommerce/global/config/SecurityConfig.java` | Spring Security 기본 설정 |
| `src/test/java/com/petopscommerce/domain/health/controller/HealthControllerTest.java` | Health API 테스트 |
| `docker-compose.yml` | 로컬 PostgreSQL 실행 초안 |
| `src/main/resources/application.properties` | PostgreSQL, JPA, Flyway, Actuator 설정 |

### 이번 단계에서 배운 것

- Spring Security를 추가하면 기본적으로 API가 보호됩니다.
- 그래서 로그인 없이 확인해야 하는 Health API는 명시적으로 `permitAll` 처리해야 합니다.
- JPA와 Flyway를 넣으면 애플리케이션은 DB 연결을 필요로 합니다.
- 로컬 개발에서는 PostgreSQL을 직접 설치하지 않고 Docker Compose로 띄울 수 있습니다.
- `ApiResponse`를 먼저 만들면 이후 회원, 상품, 주문 API의 응답 모양을 통일할 수 있습니다.

### 실행 검증 보류 사유

현재 환경에서 `java`, `gradle`, `docker` 명령이 PATH에 잡히지 않았습니다. 그래서 이번 단계에서는 파일 생성과 정적 구조 확인까지만 진행하고, 실제 실행 검증은 Java 21과 Docker Desktop 준비 후 진행합니다.