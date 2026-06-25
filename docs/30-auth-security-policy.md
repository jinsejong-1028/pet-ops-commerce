# Auth Security Policy

## 작업 목적

인증과 인가 기준을 정리합니다.

이번 작업은 활성 상태가 아닌 회원의 로그인 차단이 유지되는지 확인하고, API가 늘어날 때마다 `SecurityConfig`에 로그인 API를 한 줄씩 추가하지 않도록 URL 권한 정책을 그룹 기준으로 정리합니다.

## 로그인 상태 검증

회원 상태는 아래 값을 사용합니다.

```text
ACTIVE
SUSPENDED
WITHDRAWN
```

로그인 성공은 `ACTIVE` 회원만 가능합니다.

```text
ACTIVE
-> 이메일/비밀번호 일치 시 JWT 발급

SUSPENDED, WITHDRAWN
-> 403 member is not active
```

주문, 장바구니, 재고 같은 개별 도메인은 회원 상태를 다시 검증하지 않습니다.
회원 상태 검증은 인증 도메인의 책임입니다.

## URL 권한 정책

Spring Security의 `authorizeHttpRequests`는 위에서부터 순서대로 요청 matcher를 확인합니다.
따라서 공개 API와 관리자 API처럼 명확한 예외를 먼저 선언하고, 마지막에 기본 인증 정책을 둡니다.

현재 기준:

| 구분 | URL | 권한 |
|---|---|---|
| 오류 처리 | `/error` | 공개 |
| Health | `/api/v1/health` | 공개 |
| 로그인 | `POST /api/v1/auth/login` | 공개 |
| 회원가입 | `POST /api/v1/members` | 공개 |
| 회원 조회 | `GET /api/v1/members/*` | 임시 공개 |
| 상품 카테고리 조회 | `GET /api/v1/product-categories` | 공개 |
| 상품 조회 | `GET /api/v1/products`, `GET /api/v1/products/*` | 공개 |
| 관리자 API | `/api/v1/admin/**` | `ADMIN`, `OPERATOR` |
| 상품/카테고리 생성 | `POST /api/v1/product-categories`, `POST /api/v1/products` | `ADMIN`, `OPERATOR` |
| 그 외 API | `anyRequest()` | 로그인 필요 |

## 변경 이유

기존 방식은 API가 추가될 때마다 아래처럼 `SecurityConfig`에 계속 추가해야 했습니다.

```text
POST /api/v1/orders -> authenticated
POST /api/v1/cart -> authenticated
POST /api/v1/coupons -> authenticated
```

이 방식은 API가 늘어날수록 누락되기 쉽습니다.

이번 기준에서는 공개 API와 관리자 API만 명시하고, 나머지는 기본적으로 로그인 필요 API로 처리합니다.

```text
공개 API는 permitAll
관리자 API는 ADMIN/OPERATOR
그 외 API는 authenticated
```

## 검증 방법

자동 테스트:

```powershell
.\gradlew.bat test --console=plain
```

확인 항목:

- 활성 상태가 아닌 회원 로그인 시 403 응답
- 일반 회원의 상품 생성 API 접근 차단
- 일반 회원의 관리자 재고 API 접근 차단
- 공개 상품 조회 API 유지
- 로그인 사용자 주문 생성 API 유지
