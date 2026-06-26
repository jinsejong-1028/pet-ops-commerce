# Development Log - 2026-06-26 Business Number Concurrency

이 문서는 업무 번호 구간 할당 동시성 검증 작업을 정리합니다.

## 작업 요약

업무 번호 구간 할당이 여러 요청에서 동시에 실행되어도 같은 번호 구간을 중복 확보하지 않는지 검증했습니다.

핵심 흐름:

```text
20개 스레드 동시 시작
-> 같은 rule_code + scope_key + sequence_period row 접근
-> PostgreSQL PESSIMISTIC_WRITE row lock
-> 각 스레드가 서로 다른 구간 확보
-> next_value가 다음 시작값으로 이동
```

## 완료한 작업

브랜치:

```text
test/business-number-concurrency
```

완료 내용:

- Testcontainers 의존성 추가
- PostgreSQL Testcontainer 기반 통합 테스트 추가
- `BusinessNumberRangeAllocatorConcurrencyTest` 추가
- `allocationSize = 1` 테스트 rule로 DB row lock 경합 상황 구성
- 20개 스레드가 동시에 같은 sequence row에 구간 할당 요청
- 할당 시작값 `1~20` 중복 없음 검증
- `business_number_sequences.next_value = 21` 검증
- 업무 번호 생성기 문서와 프로젝트 진행 문서 갱신

## 설계 판단

이번 검증은 PowerShell 수동 호출이 아니라 테스트 코드로 작성했습니다.
동시성 제어는 반복 검증 가능해야 하고, PostgreSQL row lock 동작은 H2 같은 인메모리 DB로 대체하기 어렵기 때문입니다.

테스트 편의를 위해 실무 API를 추가하지 않았습니다.
테스트는 기존 `BusinessNumberRangeAllocator.allocate()` public method를 그대로 사용합니다.

## 검증

신규 테스트 단독 실행:

```powershell
.\gradlew.bat test --tests "com.petopscommerce.global.businessnumber.service.BusinessNumberRangeAllocatorConcurrencyTest" --console=plain
```

전체 테스트:

```powershell
.\gradlew.bat test --console=plain
```

결과:

```text
BUILD SUCCESSFUL
```

## 다음 작업

다음 브랜치 후보:

```text
feature/inventory-stock-workflow
```

목표:

- 재고 할당 API 작성
- PICKTO location 이동 API 작성
- 출고 처리 API 작성
- 재고 변경 이력 저장
- 재고 수량 변경 동시성 충돌 검토

## 다음 세션 시작 기준

```text
프로젝트: C:\pet-ops-commerce
현재 브랜치: main
현재 상태: git status clean, origin/main 동기화 완료
다음 작업: feature/inventory-stock-workflow
작업 방식: 사용자가 명령 실행, Codex는 설명/수정 전 승인 후 진행
petops-portfolio-workflow skill 기준으로 진행
```

시작 명령:

```powershell
cd C:\pet-ops-commerce
git checkout main
git pull
git checkout -b feature/inventory-stock-workflow
git status
```
