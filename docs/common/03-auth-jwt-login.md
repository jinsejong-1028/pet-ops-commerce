# Auth JWT Login

## 작업 목적

로그인 API와 JWT access token 발급 흐름을 추가합니다.

다음 도메인 작업 전에 인증 기반을 먼저 만드는 이유는 아래와 같습니다.

- `SecurityConfig`에 임시 `permitAll` 설정이 계속 쌓이는 문제를 줄입니다.
- 서버가 현재 요청 사용자를 알 수 있게 만듭니다.
- 다음 브랜치에서 `created_by`, `updated_by`를 로그인 회원 ID로 채울 수 있습니다.
- 상품 등록 같은 운영자 기능을 이후 `ADMIN` 권한으로 제한할 수 있습니다.

## 구현 범위

이번 브랜치에서는 아래를 구현합니다.

- 로그인 요청 DTO
- 로그인 응답 DTO
- 인증 Service
- 인증 Controller
- JWT access token 발급
- JWT Bearer token 검증 설정
- SecurityContext에 저장할 `LoginMember` principal
- IntelliJ HTTP Client 요청 파일
- Controller/Service 테스트

## 로그인 흐름

```text
POST /api/v1/auth/login
-> AuthController
-> AuthService
-> MemberRepository.findByEmail(email)
-> PasswordEncoder.matches(rawPassword, passwordHash)
-> JwtTokenProvider.createAccessToken(member)
-> ApiResponse<LoginResponse> 반환
```

## JWT 인증 흐름

```text
Authorization: Bearer {accessToken}
-> Spring Security OAuth2 Resource Server
-> JwtDecoder로 서명/만료 검증
-> JWT claim 추출
-> LoginMember(id, email, role) 생성
-> SecurityContext 저장
```

## API

### 로그인

```text
POST /api/v1/auth/login
```

요청:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "memberId": 1,
    "email": "user@example.com",
    "role": "MEMBER"
  },
  "message": "OK"
}
```

## 설정

`application.properties`에 로컬 개발용 JWT 설정을 추가합니다.

```properties
app.jwt.secret=${APP_JWT_SECRET:...}
app.jwt.issuer=${APP_JWT_ISSUER:pet-ops-commerce}
app.jwt.access-token-expires-in=${APP_JWT_ACCESS_TOKEN_EXPIRES_IN:3600}
```

운영 환경에서는 반드시 `APP_JWT_SECRET` 환경변수로 secret을 교체합니다.

## 이번 브랜치에서 하지 않는 것

- refresh token
- 로그아웃/토큰 블랙리스트
- 관리자 권한 세부 제한
- `created_by`, `updated_by` 자동 입력

위 항목은 인증 기반이 안정화된 뒤 별도 브랜치에서 진행합니다.

## 검증 방법

```powershell
.\gradlew.bat test --console=plain
```

서버 실행 후 HTTP Client로 확인합니다.

```powershell
.\gradlew.bat bootRun
```

확인 파일:

```text
http/auth-api.http
```