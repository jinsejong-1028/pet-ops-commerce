# Common API Response

## 작업 목적

API 성공/실패 응답 모양을 통일합니다.

## 적용 전 문제

회원 API는 `MemberResponse`를 바로 반환했습니다.

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

이 방식은 API가 늘어날수록 응답 구조가 제각각이 될 수 있습니다.

## 적용 후 구조

성공 응답은 `success`, `data`, `message` 구조로 반환합니다.

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com"
  },
  "message": "OK"
}
```

실패 응답도 같은 구조를 사용합니다.

```json
{
  "success": false,
  "data": null,
  "message": "email already exists"
}
```

## 추가한 클래스

### ApiResponse

- API 공통 응답 DTO
- 성공/실패 응답 모양 통일
- 성공 시 `ApiResponse.ok(data)` 사용
- 실패 시 `ApiResponse.error(message)` 사용

### GlobalExceptionHandler

- 전역 예외 처리기
- `ResponseStatusException` 처리
- `MethodArgumentNotValidException` 처리
- 예상하지 못한 예외는 `500 internal server error`로 처리

## 회원 API 변경

`MemberController`는 이제 `MemberResponse`를 직접 반환하지 않습니다.

서비스에서 받은 값을 `ApiResponse`로 감싸서 반환합니다.

```java
return ApiResponse.ok(memberService.createMember(request));
```

## 예외 응답 흐름

중복 이메일은 `MemberService`에서 `ResponseStatusException`을 발생시킵니다.

`GlobalExceptionHandler`가 이 예외를 잡아 공통 실패 응답으로 변환합니다.

```text
MemberService
-> ResponseStatusException
-> GlobalExceptionHandler
-> ApiResponse.error(message)
```

## 기대 결과

- 회원 생성 성공: `201 Created`, `success=true`
- 회원 조회 성공: `200 OK`, `success=true`
- 중복 이메일: `409 Conflict`, `success=false`
- 없는 회원: `404 Not Found`, `success=false`
- validation 오류: `400 Bad Request`, `success=false`

## 검증 방법

```powershell
.\gradlew.bat test --console=plain
```

서버 실행 후 IntelliJ HTTP Client로 확인합니다.

```powershell
.\gradlew.bat bootRun
```

확인 파일은 아래입니다.

```text
http/member-api.http
```