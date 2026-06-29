# PetOps Commerce Docs

이 폴더는 PetOps Commerce 프로젝트의 설계, 구현 기록, API, 도메인 문서를 성격별로 정리합니다.

## 문서 구조

```text
docs/
  planning/      프로젝트 계획, 요구사항, 로드맵, 진행 현황
  architecture/  아키텍처, ERD, 기술 스택, DB 전략
  api/           API 목록과 HTTP Client 테스트 흐름
  infra/         Spring Boot, Docker, Flyway, 인프라 구성
  workflow/      Git/PR 작업 흐름
  domains/       Health, Member, Product, Inventory, Order 도메인
  common/        인증, 공통 응답, audit, 업무 번호 같은 공통 기능
  logs/          날짜별 개발 로그
```

## Planning

- [구현 계획](planning/01-implementation-plan.md)
- [프로젝트 개요](planning/02-project-overview.md)
- [요구사항](planning/03-requirements.md)
- [로드맵](planning/04-roadmap.md)
- [프로젝트 진행 현황](planning/05-project-progress.md)

## Architecture

- [아키텍처](architecture/01-architecture.md)
- [ERD](architecture/02-erd.md)
- [기술 스택](architecture/03-tech-stack.md)
- [DB 선택과 비용 전략](architecture/04-database-and-cost-strategy.md)

## API

- [API 목록](api/01-api-spec.md)
- [API 테스트 흐름](api/02-api-test-workflow.md)

## Infra

- [로드밸런싱 계획](infra/01-infra-load-balancing.md)
- [Spring Boot 프로젝트 설정](infra/02-spring-boot-project-setup.md)
- [Docker PostgreSQL](infra/03-docker-postgres.md)
- [Flyway Schema](infra/04-flyway-schema.md)

## Workflow

- [Git Workflow](workflow/01-git-workflow.md)

## Domains

- [Health API](domains/01-health-api.md)
- [Member Domain](domains/02-member-domain.md)
- [Product Domain](domains/03-product-domain.md)
- [Inventory Domain](domains/04-inventory-domain.md)
- [Inventory QueryDSL Search](domains/05-inventory-querydsl-search.md)
- [Order Domain](domains/06-order-domain.md)

## Common

- [Security Error Response](common/01-security-error-response.md)
- [Common API Response](common/02-common-api-response.md)
- [Auth JWT Login](common/03-auth-jwt-login.md)
- [Audit User Tracking](common/04-audit-user-tracking.md)
- [Auth Security Policy](common/05-auth-security-policy.md)
- [Business Number Generator](common/06-business-number-generator.md)

## Logs

- [2026-06-22](logs/2026-06-22.md)
- [2026-06-23](logs/2026-06-23.md)
- [2026-06-24](logs/2026-06-24.md)
- [2026-06-25](logs/2026-06-25.md)
- [2026-06-26](logs/2026-06-26.md)
- [2026-06-29](logs/2026-06-29.md)