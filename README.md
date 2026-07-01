# PetOps Commerce

반려동물 커머스 운영을 가정한 Spring Boot 포트폴리오 프로젝트입니다.
회원, 상품, 주문, 재고 도메인을 구현하고, 창고/location/LOT 기반 재고 관리와 주문 이후 판매/출고/입고 workflow로 확장 가능한 구조를 설계합니다.

## 핵심 목표

- 회원, 상품, 주문, 재고 도메인 구현
- 창고/location/LOT 기반 현재고 관리
- 재고 할당, PICKTO 이동, 출고 흐름 설계
- 고객 주문 이후 판매 주문, 출고 지시, 구매 발주, 입고 지시로 확장 가능한 구조 설계
- PostgreSQL, Flyway, JPA, QueryDSL 기반 백엔드 설계 학습

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- QueryDSL
- PostgreSQL
- Flyway
- Docker
- Gradle

## 주요 문서

- [문서 인덱스](docs/README.md)
- [프로젝트 개요](docs/planning/02-project-overview.md)
- [요구사항](docs/planning/03-requirements.md)
- [ERD](docs/architecture/02-erd.md)
- [API 목록](docs/api/01-api-spec.md)
- [개발 로드맵](docs/planning/04-roadmap.md)
- [진행 현황](docs/planning/05-project-progress.md)

## 실행 기준

Docker PostgreSQL 상태 확인:

```powershell
docker compose ps
```

테스트 실행:

```powershell
.\gradlew.bat test --console=plain
```

서버 실행:

```powershell
.\gradlew.bat bootRun
```

Health API:

```text
GET http://localhost:8080/api/v1/health
```

## 현재 다음 작업 후보

```text
feature/shipment-stock-workflow
```

출고 지시(`shipment_orders`)를 기준으로 재고 할당, PICKTO 이동, 출고 확정을 연결하고 `shipment_order_items`의 처리 수량을 함께 갱신하는 작업입니다.