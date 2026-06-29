# Health API

이 문서는 `feature/health-api` 브랜치에서 추가한 서버 상태 확인 API를 정리합니다.

## 목표

- Spring Boot 서버가 정상 실행 중인지 확인할 수 있는 최소 API 추가
- 이후 Docker, 로드밸런싱, 모니터링, 배포 확인의 기준점 마련
- 기능 구현 전에 Controller, DTO, 테스트 흐름을 작게 경험

## API

```text
GET /api/v1/health
```

응답 예시:

```json
{
  "status": "UP",
  "application": "pet-ops-commerce"
}
```

## 구현 파일

| 파일 | 역할 |
|---|---|
| `HealthController.java` | `/api/v1/health` 요청을 받는 Controller |
| `HealthResponse.java` | Health API 응답 데이터 구조 |
| `SecurityConfig.java` | Health API는 로그인 없이 접근 가능하도록 설정 |
| `HealthControllerTest.java` | Health API 응답 검증 테스트 |
| `PetOpsCommerceApplicationTests.java` | DB 없이 기본 Spring context 테스트 가능하도록 임시 설정 |

## 동작 흐름

```text
브라우저 또는 클라이언트가 GET /api/v1/health 요청
↓
Spring Security가 /api/v1/health 요청을 허용
↓
HealthController.health() 실행
↓
HealthResponse("UP", "pet-ops-commerce") 반환
↓
Spring Boot가 Java 객체를 JSON으로 변환
```

## 왜 필요한가

Health API는 단순한 확인용 API이지만 운영 환경에서 중요합니다.

- 서버가 살아있는지 확인
- 로드밸런서가 정상 서버만 트래픽을 보내도록 판단
- 모니터링 시스템이 장애 여부를 판단
- 배포 후 API 서버가 정상 기동되었는지 확인

## 이번 브랜치에서 제외한 것

- 공통 응답 구조 `ApiResponse`
- Actuator health 설정
- Docker PostgreSQL 설정
- DB 연결 상태까지 포함한 health check

위 항목은 이후 별도 브랜치에서 단계적으로 추가합니다.

## 검증 방법

테스트 실행:

```powershell
.\gradlew.bat test
```

서버 실행 후 브라우저 확인:

```text
http://localhost:8080/api/v1/health
```