# Security Error Response

## 작업 목적

회원 API에서 중복 이메일이나 입력값 오류가 발생했을 때 원래 상태 코드가 유지되도록 수정합니다.

## 발생한 문제

`POST /api/v1/members`에서 중복 이메일을 보내면 서비스 계층은 `409 Conflict`를 발생시킵니다.

하지만 실제 IntelliJ HTTP Client에서는 `403 Forbidden`이 내려왔습니다.

## 원인

Spring Boot는 예외 응답을 만들 때 내부적으로 `/error` 경로를 사용합니다.

Spring Security는 일반 요청뿐 아니라 error dispatch 요청에도 적용됩니다.

기존 설정에서는 `/error`가 허용되어 있지 않아서, 원래의 `409` 또는 `400` 응답이 Security에 의해 `403`으로 바뀌었습니다.

## 수정 내용

`SecurityConfig`에서 error dispatch와 `/error` 경로를 허용했습니다.

```java
// Service/Controller 예외가 /error 처리 과정에서 403으로 바뀌지 않도록 허용합니다.
.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
.requestMatchers("/error").permitAll()
```

## 기대 결과

- 중복 이메일: `409 Conflict`
- validation 오류: `400 Bad Request`
- 회원 생성 성공: `201 Created`
- 회원 조회 성공: `200 OK`

## 검증 방법

```powershell
.\gradlew.bat test
```

서버를 이미 켜둔 상태라면 코드를 반영하기 위해 `bootRun`을 종료하고 다시 실행합니다.

```powershell
.\gradlew.bat bootRun
```

IntelliJ HTTP Client에서 아래 요청을 다시 실행합니다.

- `Create member`
- `Duplicate email`
- `Validation error`