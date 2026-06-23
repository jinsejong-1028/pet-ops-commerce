# Flyway Initial Schema

이 문서는 `chore/flyway-initial-schema` 브랜치에서 추가한 최초 DB migration을 정리합니다.

## 목표

- Flyway로 DB schema를 버전 관리합니다.
- PostgreSQL에 최초 테이블 구조를 생성합니다.
- JPA `ddl-auto=validate` 전략의 기준 DB 구조를 만듭니다.
- 회원, 반려동물, 상품, LOT, 재고, 주문, 결제, 쿠폰의 최소 도메인 구조를 잡습니다.

## Flyway란

Flyway는 DB 변경 이력을 SQL 파일로 관리하는 migration 도구입니다.

이 프로젝트에서는 별도 서버나 컨테이너가 아닙니다.
Spring Boot가 실행될 때 애플리케이션 안에서 동작합니다.

동작 흐름:

```text
Spring Boot 실행
PostgreSQL 연결
classpath:db/migration 확인
아직 적용되지 않은 V*.sql 실행
flyway_schema_history에 적용 기록 저장
```

## 추가 파일

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

파일명 의미:

```text
V1 = 첫 번째 schema 버전
__ = Flyway 파일명 구분자
initial_schema = 최초 schema 생성 작업
```

## JPA와 Flyway 역할 분리

현재 설정:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

역할:

| 구분 | 역할 |
|---|---|
| Flyway | 테이블 생성/변경 SQL 실행 |
| JPA | Entity와 DB 구조 매핑 |
| validate | Entity와 DB 구조 일치 여부 검증 |

JPA가 테이블을 자동 생성하지 않기 때문에 DB 구조는 Flyway SQL로 관리합니다.

## DB FK 제약을 걸지 않는 이유

이번 schema에서는 DB 레벨 foreign key constraint를 걸지 않습니다.

대신 `member_id`, `product_id`, `order_id` 같은 컬럼과 인덱스로 논리 관계를 표현합니다.

이유:

- 운영 데이터 보정이 쉬움
- 대량 배치 처리 시 유연함
- 서비스 로직에서 관계 검증 가능
- FK 검증 비용과 잠금 영향을 줄일 수 있음

주의점:

- DB가 잘못된 참조를 자동으로 막아주지 않음
- 애플리케이션 코드와 테스트가 더 중요함

## 생성 테이블

| 테이블 | 역할 |
|---|---|
| `members` | 회원 |
| `pet_profiles` | 회원의 반려동물 프로필 |
| `product_categories` | 상품 카테고리 |
| `products` | 판매 상품 |
| `lots` | 상품별 LOT 관리 정보 |
| `warehouses` | 창고 |
| `stocks` | 상품/창고/LOT별 현재 재고 |
| `stock_histories` | 재고 변경 이력 |
| `orders` | 주문 |
| `order_items` | 주문 상품 |
| `payments` | 결제 |
| `coupons` | 쿠폰 마스터 |
| `order_events` | 주문 이벤트 이력 |

## LOT 설계

`lots` 테이블은 특정 업무 컬럼명 대신 `lot1` ~ `lot5` 형태를 사용합니다.

| 컬럼 | 의미 |
|---|---|
| `lot1` | LOT 주요 식별값 |
| `lot2` | 보조 LOT 정보 |
| `lot3` | 유효기간 |
| `lot4` | 입고일자 |
| `lot5` | 기타 관리값 |

`lot3`, `lot4`는 날짜 검색과 정렬을 고려해 `date` 타입으로 둡니다.

## 재고 설계

`stocks`는 상품, 창고, LOT 조합으로 현재 재고를 관리합니다.

```text
product_id + warehouse_id + lot_id
```

위 조합에 unique 제약을 둡니다.

의미:

```text
같은 상품, 같은 창고, 같은 LOT의 현재 재고 row는 하나만 존재합니다.
```

`version` 컬럼은 이후 재고 차감 동시성 제어에 사용할 수 있습니다.

## 공통 감사 컬럼

주요 테이블에는 아래 컬럼을 둡니다.

```text
created_at
created_by
updated_at
updated_by
```

의미:

- `created_at`: 생성 시각
- `created_by`: 생성자
- `updated_at`: 수정 시각
- `updated_by`: 수정자

`created_by`, `updated_by`는 초기 schema에서 FK를 걸지 않습니다.
시스템, 배치, 관리자 작업을 유연하게 기록하기 위해서입니다.

## 이번 범위에서 제외한 것

- 장바구니
- 회원 쿠폰 발급 이력
- 주문 쿠폰 적용 이력
- 정산
- 검색/추천
- 광고
- 메시지 큐 기반 비동기 처리

위 항목은 이후 기능 브랜치에서 확장합니다.

## 검증 방법

DB 컨테이너 상태 확인:

```powershell
docker compose ps
```

테스트 실행:

```powershell
.\gradlew.bat test
```

서버 실행:

```powershell
.\gradlew.bat bootRun
```

Health API 확인:

```text
http://localhost:8080/api/v1/health
```

## 주의사항

기존 Docker volume에 이미 테이블이 있으면 migration 결과가 깨끗한 DB 기준과 다를 수 있습니다.

DB를 완전히 초기화하는 명령은 데이터를 삭제합니다.
따라서 기본 검증에서는 사용하지 않습니다.
