# Business Number Generator

## 작업 목적

주문번호처럼 사용자가 확인하는 업무 번호를 공통 구조로 생성합니다.

기존 주문번호는 `OrderService` 안에서 UUID 일부를 붙여 만들었습니다.
이번 작업에서는 번호 규칙과 현재 순번을 DB에서 관리하고, 애플리케이션은 번호 구간을 한 번에 확보해 메모리에서 소비하도록 변경합니다.

```text
업무 번호 규칙
-> 번호 구간 확보
-> 메모리에서 순번 소비
-> 업무 번호 생성
```

## 구현 범위

- `business_number_rules` 테이블 추가
- `business_number_sequences` 테이블 추가
- 주문번호 기본 rule seed 추가
- `BusinessNumberType` 기본 rule 정보 추가
- 기본 rule 자동 보장 로직 추가
- 업무 번호 Entity/Repository 추가
- `BusinessNumberRangeAllocator` 추가
- `BusinessNumberGenerator` 추가
- `OrderService` 주문번호 생성 로직 교체
- 업무 번호 생성기 테스트 추가
- 주문 Service 테스트 수정
- 업무 번호 구간 할당 동시성 통합 테스트 추가

## 번호 규칙 테이블

`business_number_rules`는 번호의 모양을 관리합니다.
BusinessNumberType도 기본 rule 값을 가지고 있어, rule이 없으면 generator가 기본 rule을 생성합니다.

| 컬럼 | 의미 |
|---|---|
| `code` | 번호 유형, 예: `ORDER` |
| `prefix` | 번호 앞부분, 예: `ORD` |
| `date_format` | 번호에 표시할 날짜 포맷 |
| `sequence_width` | 순번 자리수 |
| `reset_cycle` | 순번 초기화 주기 |
| `scope_type` | 순번 공유 범위 |
| `separator` | 번호 조각 구분자 |
| `allocation_size` | 한 번에 확보할 번호 구간 크기 |
| `enabled` | 사용 여부 |

주문번호 기본 규칙:

```text
code: ORDER
prefix: ORD
date_format: yyyyMMdd
sequence_width: 6
reset_cycle: DAILY
scope_type: GLOBAL
separator: -
allocation_size: 100
```

생성 결과:

```text
ORD-20260625-000001
ORD-20260625-000002
```

## 번호 구간 테이블

`business_number_sequences`는 다음에 할당할 번호 구간의 시작값을 관리합니다.

| 컬럼 | 의미 |
|---|---|
| `rule_code` | 번호 규칙 코드 |
| `scope_key` | 번호판 범위 |
| `sequence_period` | 초기화 주기 key |
| `next_value` | 다음에 할당할 구간 시작값 |
| `version` | 낙관적 잠금 version |

unique 기준:

```text
rule_code + scope_key + sequence_period
```

예:

```text
rule_code: ORDER
scope_key: GLOBAL
sequence_period: 20260625
next_value: 101
```

이 값은 `ORDER` 번호의 `GLOBAL` 번호판에서 `20260625` 기간에는 다음 구간이 101부터 시작한다는 뜻입니다.

## 구간 할당 방식

매 주문마다 DB row를 잠그고 1씩 증가시키면 같은 row에 요청이 몰릴 수 있습니다.

```text
주문 100건
-> DB row lock 100번
```

이번 구조는 `allocation_size`만큼 번호 구간을 한 번에 확보합니다.

```text
allocation_size: 100

DB에서 1~100 확보
-> 애플리케이션 메모리에서 1, 2, 3 사용
-> 100까지 사용 후 다시 DB에서 101~200 확보
```

결과:

```text
주문 100건
-> DB row lock 1번
```

번호 구간 확보는 `REQUIRES_NEW` 트랜잭션으로 별도 커밋합니다.
주문 저장 트랜잭션이 나중에 실패해도 이미 확보한 번호 구간은 되돌리지 않습니다.
그래야 애플리케이션 메모리의 번호 구간과 DB의 `next_value`가 어긋나 중복이 생기는 위험을 줄일 수 있습니다.
이 방식은 중복 없는 번호를 만들면서 DB lock 빈도를 줄입니다.
다만 서버 재시작이나 트랜잭션 rollback으로 사용하지 않은 번호가 비는 것은 허용합니다.

```text
중복 없는 번호
!=
빈틈 없는 번호
```

커머스 주문번호는 보통 빈틈 없는 번호보다 중복 없는 번호가 더 중요합니다.

## Scope 기준

`scope_type`은 번호판을 나누는 기준입니다.

```text
GLOBAL
-> 모든 요청이 같은 번호판 공유

MEMBER
-> 회원별 번호판 분리

WAREHOUSE
-> 창고별 번호판 분리
```

예:

```text
GLOBAL
user1 -> ORD-20260625-000001
user2 -> ORD-20260625-000002

MEMBER
user1 -> EXM-000001
user2 -> EXM-000001
user1 -> EXM-000002
```

## 주문 도메인 적용

`OrderService`는 더 이상 직접 주문번호를 만들지 않습니다.
개발자는 업무 번호 유형만 넘기고, 기준 시각과 rule 보장은 generator 내부에서 처리합니다.
기준 시각은 `Clock` bean에서 가져오므로 테스트에서는 고정 Clock으로 날짜를 제어할 수 있습니다.

```text
OrderService
-> BusinessNumberGenerator.generate(ORDER)
-> ORD-20260625-000001
```

이렇게 하면 결제번호, 출고번호, 재고이동번호도 같은 생성기를 재사용할 수 있습니다.

## 생성기 책임 정리

`BusinessNumberGenerator`의 외부 호출 API는 업무 번호 유형만 받습니다.

```text
BusinessNumberGenerator.generate(ORDER)
```

서비스 계층은 기준 시각, scope key, sequence period, rule 생성 여부를 직접 알 필요가 없습니다.
이 정보는 생성기 내부에서 처리합니다.

정리된 책임:

- 서비스는 업무 번호 유형만 전달
- 생성기는 `Clock` 기준 현재 시각 확정
- 생성기는 활성 rule 조회 또는 기본 rule 생성
- 생성기는 scope/period 기준 cache key 생성
- 생성기는 메모리 구간 또는 DB 신규 구간에서 순번 확보
- 생성기는 prefix/date/sequence 규칙으로 최종 번호 생성

테스트를 위해 실무에서 사용하지 않는 `generate(type, now)` 같은 public API는 두지 않습니다.
시간 제어가 필요한 테스트는 `Clock` bean을 고정 Clock으로 교체해 처리합니다.

## 수동 DB 확인 결과

Docker PostgreSQL에서 실제 주문번호 생성 결과를 확인했습니다.

```sql
select code, prefix, date_format, sequence_width, reset_cycle, scope_type, allocation_size
from business_number_rules;
```

확인 결과:

```text
ORDER / ORD / yyyyMMdd / 6 / DAILY / GLOBAL / 100
```

주문 생성 결과:

```text
ORD-20260625-000001
ORD-20260625-000002
ORD-20260625-000101
ORD-20260625-000102
```

`allocation_size`가 100이므로 서버 재실행 후 메모리에 남은 `3~100` 구간은 사라지고, 다음 DB 구간인 `101~200`을 확보하는 것이 정상입니다.

`business_number_sequences` 확인 결과:

```text
rule_code: ORDER
scope_key: GLOBAL
sequence_period: 20260625
next_value: 201
version: 2
```

`next_value = 201`은 다음 구간 할당 시 `201~300`부터 확보한다는 뜻입니다.
`version`은 JPA `@Version` 동시성 제어 값이며, 업무 번호 자체의 현재값이 아닙니다.

## 동시성 통합 테스트

현재 구현은 `BusinessNumberSequenceRepository.findForUpdate()`에서 `PESSIMISTIC_WRITE`를 사용합니다.
따라서 같은 `rule_code + scope_key + sequence_period` row에 여러 요청이 동시에 접근하면 PostgreSQL이 한 트랜잭션씩 순서대로 구간을 할당합니다.

이 동작은 Testcontainers PostgreSQL 기반 통합 테스트로 검증합니다.
H2 같은 인메모리 DB는 PostgreSQL row lock 동작과 차이가 있을 수 있으므로, 실제 PostgreSQL 컨테이너를 테스트 중 자동으로 띄웁니다.

테스트 대상:

```text
BusinessNumberRangeAllocatorConcurrencyTest
```

검증 흐름:

```text
allocationSize = 1인 테스트 rule 생성
20개 스레드 동시 시작
각 스레드가 같은 sequence row에 구간 할당 요청
PostgreSQL PESSIMISTIC_WRITE row lock으로 순차 처리
1~20 시작값이 중복 없이 할당되는지 확인
business_number_sequences.next_value = 21 확인
```

이 테스트는 운영 service API를 테스트 편의용으로 바꾸지 않습니다.
`BusinessNumberRangeAllocator`의 실제 public method와 PostgreSQL row lock을 그대로 사용합니다.

## 검증 방법

자동 테스트:

```powershell
.\gradlew.bat test --console=plain
```

확인 항목:

- ORDER 번호 포맷
- 날짜 조각 생성
- 0 padding 순번 생성
- 할당 구간 캐시 동작
- 주문 생성 시 공통 generator 사용
- rule이 없을 때 기본 rule 생성
- `Clock` 기반 기준 시각 사용
- PostgreSQL row lock 기반 구간 할당 동시성
