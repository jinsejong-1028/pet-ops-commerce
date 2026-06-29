# Audit User Tracking

## 목적

이번 작업은 `created_by`, `updated_by`를 자동 입력하기 위한 작업입니다.

상품, 카테고리, 회원처럼 DB에 저장되는 Entity는 누가 만들었고 누가 수정했는지 추적할 수 있어야 합니다.
운영 서비스에서는 장애 분석, 관리자 작업 이력 확인, 데이터 변경 추적에 audit 정보가 자주 사용됩니다.

## 이번 작업의 핵심

기존 방식은 Entity마다 `@PrePersist`, `@PreUpdate`를 두고 `createdAt`, `updatedAt`을 직접 넣는 구조였습니다.

이번 작업에서는 이 반복 코드를 공통 `BaseAuditEntity`로 옮겼습니다.

```text
Member
Product
ProductCategory
        ↓ extends
BaseAuditEntity
```

## JPA Auditing이란?

JPA Auditing은 Entity가 저장되거나 수정될 때 공통 audit 필드를 자동으로 채워주는 Spring Data JPA 기능입니다.

이번 프로젝트에서는 아래 값을 자동 관리합니다.

```text
created_at
created_by
updated_at
updated_by
```

## 동작 흐름

로그인 후 상품을 생성하면 흐름은 아래처럼 이어집니다.

```text
1. 사용자가 로그인
2. JWT access token 발급
3. 상품 생성 요청에 Authorization: Bearer token 전달
4. Spring Security가 JWT 검증
5. SecurityContext에 LoginMember 저장
6. JPA가 Product 저장
7. JPA Auditing이 현재 사용자 ID 요청
8. LoginMemberAuditorAware가 LoginMember.id 반환
9. created_by, updated_by 자동 입력
```

## 추가된 파일

```text
src/main/java/com/petopscommerce/global/audit/BaseAuditEntity.java
src/main/java/com/petopscommerce/global/audit/LoginMemberAuditorAware.java
src/main/java/com/petopscommerce/global/config/JpaAuditingConfig.java
src/test/java/com/petopscommerce/global/audit/LoginMemberAuditorAwareTest.java
```

## BaseAuditEntity

`BaseAuditEntity`는 여러 Entity가 공통으로 사용하는 audit 필드를 가집니다.

```text
createdAt
createdBy
updatedAt
updatedBy
```

Entity마다 같은 필드를 반복 작성하지 않기 위해 `@MappedSuperclass`를 사용합니다.

## LoginMemberAuditorAware

`LoginMemberAuditorAware`는 현재 로그인한 사용자의 ID를 JPA Auditing에 알려주는 클래스입니다.

```text
SecurityContext
→ Authentication
→ principal
→ LoginMember
→ LoginMember.id()
```

인증 정보가 없으면 빈 값을 반환합니다.
회원가입처럼 로그인 없이 가능한 API에서는 `created_by`가 비어 있을 수 있습니다.

## JpaAuditingConfig

`@EnableJpaAuditing`으로 JPA Auditing을 활성화합니다.

```java
@EnableJpaAuditing(auditorAwareRef = "loginMemberAuditorAware")
```

이 설정이 없으면 `@CreatedBy`, `@LastModifiedBy`, `@CreatedDate`, `@LastModifiedDate`가 동작하지 않습니다.

## 보안 정책 변경

상품/카테고리 생성은 이제 로그인 JWT가 필요합니다.

```text
POST /api/v1/product-categories
POST /api/v1/products
```

조회 API는 B2C 사용자에게 공개될 수 있으므로 계속 로그인 없이 접근 가능합니다.

```text
GET /api/v1/product-categories
GET /api/v1/products
GET /api/v1/products/{productId}
```

## 왜 Service에서 직접 넣지 않았나?

서비스에서 직접 처리하면 모든 생성/수정 메서드마다 아래 코드가 반복됩니다.

```java
product.setCreatedBy(loginMember.id());
product.setUpdatedBy(loginMember.id());
```

Entity가 많아질수록 빠뜨리기 쉽고, 수정자 갱신도 일관성이 떨어집니다.
그래서 실무에서는 공통 audit 처리는 JPA Auditing이나 별도 공통 인프라로 빼는 경우가 많습니다.

## 검증 방법

자동 테스트:

```powershell
.\gradlew.bat test
```

수동 확인:

```powershell
docker compose ps
.\gradlew.bat bootRun
```

HTTP Client 확인:

```text
http/product-api.http
```

먼저 로그인 요청을 실행한 뒤 상품/카테고리 생성 요청을 실행합니다.

## 주의점

현재는 권한을 `authenticated`까지만 확인합니다.
즉 로그인한 회원이면 상품/카테고리 생성 요청이 가능합니다.

관리자만 상품을 등록할 수 있게 하는 기능은 다음 작업에서 `ADMIN` 권한 제한으로 분리하는 것이 좋습니다.
