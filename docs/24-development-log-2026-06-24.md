# Development Log - 2026-06-24

이 문서는 2026-06-24 작업 흐름과 다음 세션 인수인계 내용을 정리합니다.

## 작업 요약

오늘은 상품 도메인 이후 운영 흐름에 필요한 인증, audit, 보류 작업 기준을 정리했습니다.

핵심 흐름:

```text
상품 도메인 구현
→ B2C 단일 운영사 상품 판매 모델 정리
→ JWT 로그인 구현
→ 로그인 사용자 기반 audit 자동 입력
→ 관리자 작업 로그는 별도 운영 로그로 보류
→ 다음 작업을 재고 도메인으로 확정
```

## 완료된 주요 작업

### 상품 도메인

브랜치:

```text
feature/product-domain
```

완료 내용:

- 상품 카테고리 Entity 작성
- 상품 Entity 작성
- 상품 카테고리 생성/조회 API 작성
- 상품 생성/조회/목록 API 작성
- Controller/Service 테스트 작성
- HTTP Client 기반 수동 API 확인 흐름 사용

학습 내용:

- `record` DTO 생성자와 `from()` 변환 흐름
- `static factory method` 사용 이유
- JPA Repository method name query
- `stream().map(...).toList()` 변환 흐름
- 분기 조건 주석 스타일 협의

### B2C commerce 모델 정리

브랜치:

```text
docs/clarify-commerce-model
```

결정 내용:

- 사용자가 상품을 등록하는 marketplace 모델이 아닙니다.
- 회사가 상품을 등록하고 사용자가 구매하는 B2C commerce 모델입니다.
- 따라서 상품에 vendor/store 소유자는 현재 두지 않습니다.
- 상품 등록자는 audit의 `created_by`, `updated_by`로 추적합니다.

### JWT 로그인

브랜치:

```text
feature/auth-jwt-login
```

완료 내용:

- 로그인 API 추가
- JWT access token 발급
- JWT 인증 필터 추가
- `LoginMember(id, email, role)` 구조 추가
- HTTP Client 로그인 요청 추가

학습 내용:

- 로그인 후 서비스 로직에서 현재 사용자 정보가 필요한 이유
- `SecurityContext`에 인증 사용자가 저장되는 흐름
- 같은 계정의 여러 기기 로그인은 일반 B2C에서는 허용 가능
- 동일 기기 중복 로그인은 프론트엔드 토큰 교체/저장 정책으로 처리 가능

### Audit user tracking

브랜치:

```text
feature/audit-user-tracking
```

완료 내용:

- `BaseAuditEntity` 추가
- `JpaAuditingConfig` 추가
- `LoginMemberAuditorAware` 추가
- `created_at`, `updated_at`, `created_by`, `updated_by` 공통 관리
- 상품/카테고리 생성 API를 로그인 필요로 변경
- HTTP Client 상품 생성 요청에 `Authorization` 헤더 추가

학습 내용:

- 기존 `@PrePersist`, `@PreUpdate` 직접 처리 방식과 JPA Auditing 방식 차이
- `@CreatedDate`, `@LastModifiedDate`는 JPA Auditing이 시간 자동 입력
- `@CreatedBy`, `@LastModifiedBy`는 `AuditorAware`가 현재 사용자 ID 제공
- `@EnableJpaAuditing(auditorAwareRef = "loginMemberAuditorAware")`가 연결 지점

### 관리자 작업 로그 보류

브랜치:

```text
docs/defer-admin-action-log
```

결정 내용:

- IP/User-Agent는 `created_by`, `updated_by`와 성격이 다릅니다.
- 데이터 작성자는 audit 필드로 관리합니다.
- 요청 환경 정보는 별도 운영 로그 테이블로 분리합니다.
- 관리자 작업 로그는 추후 `feature/admin-action-log`에서 진행합니다.

추후 예상 범위:

```text
admin_action_logs Flyway migration 추가
AdminActionLog Entity 추가
AdminActionLogRepository 추가
AdminActionLogService 추가
요청 IP/User-Agent 추출 유틸 추가
상품 생성/수정/삭제 흐름에서 로그 저장
관리자 작업 로그 문서 추가
```

## 현재 프로젝트 상태

```text
프로젝트: C:\pet-ops-commerce
현재 기준 브랜치: main
현재 완료 작업: docs/defer-admin-action-log
현재 진행 작업: docs/update-session-handoff
다음 추천 작업: feature/inventory-domain
```

## 다음 세션 시작 문구

다음 세션에서는 아래처럼 시작하면 됩니다.

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: git status clean, origin/main 동기화 완료
다음 작업: feature/inventory-domain
작업 방식: 내가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

## 다음 세션 첫 명령

사용자가 직접 실행합니다.

```powershell
cd C:\pet-ops-commerce
git checkout main
git pull
git checkout -b feature/inventory-domain
git status
```

## feature/inventory-domain 예상 목표

- `warehouses` Entity 작성
- `lots` Entity 작성
- `stocks` Entity 작성
- Repository 작성
- 재고 조회 Service 작성
- 재고 조회 Controller 작성
- 테스트 작성
- 문서 작성

## 재고 도메인에서 특히 설명할 개념

- 재고와 상품의 관계
- lot이 왜 필요한지
- FK 제약 없이 `product_id`, `lot_id`를 다루는 방식
- DB 제약과 애플리케이션 검증의 차이
- 재고 수량 변경 시 트랜잭션이 필요한 이유
- 주문 도메인으로 이어지는 재고 차감 흐름

## 작업 방식 유지 기준

- 사용자가 Git/Docker/Gradle 명령을 직접 실행합니다.
- Codex는 먼저 작업 목적, 예상 파일, 필요성, 위험, 검증 방법을 설명합니다.
- 사용자가 `진행`, `수정해줘`, `적용`처럼 승인하면 파일을 수정합니다.
- 작업 후 PR 제목, PR 본문, merge 후 정리 명령을 제공합니다.
- 코드와 SQL에는 주석을 남깁니다.
- 복잡한 분기는 `조건`, `결과` 2줄 주석을 사용합니다.