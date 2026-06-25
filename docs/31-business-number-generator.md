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
- 업무 번호 Entity/Repository 추가
- `BusinessNumberRangeAllocator` 추가
- `BusinessNumberGenerator` 추가
- `OrderService` 주문번호 생성 로직 교체
- 업무 번호 생성기 테스트 추가
- 주문 Service 테스트 수정

## 번호 규칙 테이블

`business_number_rules`는 번호의 모양을 관리합니다.

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

```text
OrderService
-> BusinessNumberGenerator.generate(ORDER)
-> ORD-20260625-000001
```

이렇게 하면 결제번호, 출고번호, 재고이동번호도 같은 생성기를 재사용할 수 있습니다.

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
