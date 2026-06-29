# Member Domain

이 문서는 `feature/member-domain` 브랜치에서 추가한 회원 도메인 1차 구현을 정리합니다.

## 목표

- `members` 테이블과 Java Entity를 연결합니다.
- 회원 생성과 단건 조회 API를 구현합니다.
- 비밀번호 원문 대신 해시 값을 DB에 저장합니다.
- JPA Entity, Repository, Service, Controller의 역할을 작은 기능으로 학습합니다.

## 이번 범위

| 항목 | 내용 |
|---|---|
| Entity | `Member`가 `members` 테이블과 매핑됩니다. |
| Repository | `MemberRepository`가 DB 조회와 저장을 담당합니다. |
| Service | `MemberService`가 중복 이메일 검사와 비밀번호 해시 처리를 담당합니다. |
| Controller | `MemberController`가 HTTP 요청과 응답을 담당합니다. |
| Security | 회원 생성/단건 조회 API를 임시로 공개합니다. |

## API

### 회원 생성

```text
POST /api/v1/members
```

요청 예시:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

응답 예시:

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "MEMBER",
  "status": "ACTIVE",
  "createdAt": "2026-06-23T10:00:00"
}
```

### 회원 단건 조회

```text
GET /api/v1/members/{memberId}
```

응답 예시는 회원 생성 응답과 같습니다.

## 계층별 역할

```text
Client
↓
MemberController: HTTP 요청과 응답 처리
↓
MemberService: 회원 생성/조회 비즈니스 규칙 처리
↓
MemberRepository: members 테이블 접근
↓
PostgreSQL members table
```

## Entity란

Entity는 DB 테이블과 연결되는 Java 객체입니다.

이 프로젝트에서는 `Member` 클래스가 `members` 테이블을 표현합니다.

```text
Member.email        -> members.email
Member.passwordHash -> members.password_hash
Member.role         -> members.role
Member.status       -> members.status
```

JPA는 Entity 정보를 보고 Java 객체와 DB row 사이의 변환을 처리합니다.

## Repository란

Repository는 DB 접근을 담당하는 계층입니다.

PHP/MySQL에서 직접 SQL을 작성해 조회하던 작업 중 일부를 Spring Data JPA가 대신 만들어줍니다.

예시:

```java
boolean existsByEmail(String email);
```

위 메서드 이름만으로 Spring Data JPA가 이메일 중복 확인 쿼리를 생성합니다.

## Service란

Service는 비즈니스 규칙을 담당합니다.

이번 작업에서는 아래 규칙을 Service에 둡니다.

- 이미 사용 중인 이메일이면 409 오류 반환
- 비밀번호는 저장 전에 해시 처리
- 없는 회원 id를 조회하면 404 오류 반환

Controller에 규칙을 몰아넣지 않고 Service로 분리하면 이후 테스트와 유지보수가 쉬워집니다.

## 비밀번호 저장 방식

원본 비밀번호는 DB에 저장하지 않습니다.

```text
password -> PasswordEncoder -> password_hash
```

현재는 Spring Security의 `BCryptPasswordEncoder`를 사용합니다.

## 임시 공개 API인 이유

아직 로그인, JWT, 세션 인증을 구현하지 않았습니다.

그래서 학습과 검증을 위해 아래 API만 임시로 로그인 없이 접근 가능하게 열었습니다.

```text
POST /api/v1/members
GET /api/v1/members/{memberId}
```

Auth 기능을 추가하면 회원 조회는 `/members/me` 중심으로 바꾸고, 관리자 조회는 권한 검사를 붙일 예정입니다.

## 이번 범위에서 제외한 것

- 로그인
- JWT 발급
- refresh token
- `/members/me`
- 회원 정보 수정
- 공통 응답 wrapper `ApiResponse`
- 전역 예외 응답 형식 통일

위 항목은 이후 별도 브랜치에서 진행합니다.

## 검증 방법

테스트 실행:

```powershell
.\gradlew.bat test
```

DB 포함 서버 실행:

```powershell
docker compose ps
.\gradlew.bat bootRun
```

수동 API 확인:

```text
POST http://localhost:8080/api/v1/members
GET http://localhost:8080/api/v1/members/{memberId}
```

## 주의사항

기존 Docker DB에 이전 `V1__initial_schema.sql`이 이미 적용된 상태에서 migration 파일 내용이 바뀌었다면 Flyway checksum 오류가 발생할 수 있습니다.

이 경우 로컬 DB 초기화 여부는 별도로 판단합니다.