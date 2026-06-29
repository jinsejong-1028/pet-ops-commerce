# API Test Workflow

이 문서는 PetOps Commerce 프로젝트에서 API를 어떻게 검증할지 정리합니다.

## 목표

- 자동 테스트와 수동 API 테스트의 역할을 분리합니다.
- IntelliJ HTTP Client 요청 파일을 Git에 남깁니다.
- Postman은 협업과 공유용 API Client로 사용합니다.
- PowerShell API 호출은 빠른 임시 확인용으로만 사용합니다.

## 테스트 도구 구분

| 도구 | 용도 |
|---|---|
| JUnit | Service, Controller 같은 코드 단위 자동 검증 |
| IntelliJ HTTP Client | 개발자가 IDE 안에서 API를 직접 호출해보는 수동 검증 |
| Postman | API 요청을 Collection으로 저장하고 협업자와 공유 |
| PowerShell/curl | 설치 없이 빠르게 확인하는 임시 검증 |

## IntelliJ HTTP Client

요청 파일 위치:

```text
http/member-api.http
```

이 파일은 Git에 포함됩니다.

장점:

- API 호출 예시를 코드처럼 버전 관리할 수 있습니다.
- 기능 브랜치에서 API 변경 내역과 요청 예시를 함께 리뷰할 수 있습니다.
- IntelliJ에서 각 요청 위의 실행 버튼으로 바로 호출할 수 있습니다.

## member-api.http 구성

| 요청 | 목적 |
|---|---|
| Health check | 서버 실행 상태 확인 |
| Create member | 회원 생성 API 확인 |
| Get member | 회원 단건 조회 API 확인 |
| Duplicate email | 중복 이메일 409 응답 확인 |
| Validation error | 요청값 검증 오류 확인 |

## 실행 전 준비

DB 컨테이너 확인:

```powershell
docker compose ps
```

DB가 꺼져 있으면 실행:

```powershell
docker compose up -d
```

Spring Boot 실행:

```powershell
.\gradlew.bat bootRun
```

IntelliJ에서는 `PetOpsCommerceApplication.java`의 실행 버튼으로 서버를 실행해도 됩니다.

## Postman 사용 기준

Postman은 아래 상황에서 사용합니다.

- API 요청을 Collection으로 묶어 공유할 때
- 환경변수로 local, dev, prod URL을 나눌 때
- 기획자, QA, 다른 개발자와 API 호출 예시를 공유할 때
- 인증 토큰을 저장하고 여러 요청에 재사용할 때

현재 프로젝트에서는 IntelliJ HTTP Client 파일을 먼저 Git에 남기고, 이후 API가 늘어나면 Postman Collection도 추가로 정리합니다.

## 실무형 검증 흐름

```text
1. JUnit으로 자동 테스트 실행
2. Spring Boot 서버 실행
3. IntelliJ HTTP Client로 API 수동 확인
4. 필요한 경우 Postman Collection으로 공유
5. PR 본문에 자동/수동 검증 결과 작성
```

## 주의사항

- `POST /api/v1/members`는 Auth 구현 전까지 임시 공개 API입니다.
- 같은 이메일로 회원 생성 요청을 반복하면 409 Conflict가 발생합니다.
- 로컬 DB를 초기화하면 기존 회원 데이터가 삭제됩니다.
- 공통 응답 wrapper가 아직 없으므로 validation 오류 응답 형태는 Spring 기본 응답입니다.