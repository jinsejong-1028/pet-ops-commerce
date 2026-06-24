# 개발 로드맵

이 문서는 PetOps Commerce 프로젝트의 전체 개발 순서와 현재 진행 상태를 정리합니다.

## 상태 기준

| 상태 | 의미 |
|---|---|
| 완료 | PR merge와 로컬/원격 브랜치 정리 완료 |
| 진행 중 | 현재 작업 브랜치에서 진행 중 |
| 진행 예정 | 다음 작업 후보 |
| 예정 | 아직 시작하지 않은 작업 |
| 보류 | 현재 범위에서 제외한 작업 |

## 현재 요약

| 구분 | 현재 상태 |
|---|---|
| 현재 브랜치 | `docs/defer-admin-action-log` |
| Git 상태 | 문서 보류 작업 진행 중 |
| 마지막 완료 작업 | `feature/audit-user-tracking` |
| 다음 추천 작업 | `feature/inventory-domain` |

## Phase 0. 설계 - 완료

- 요구사항 정의
- 아키텍처 설계
- ERD 초안 작성
- API 목록 작성
- 기술 스택 확정
- PostgreSQL/MySQL 차이와 비용 전략 정리
- B2C 단일 운영사 상품 판매 모델 정리

## Phase 1. 프로젝트 골격 - 완료

- Spring Boot 프로젝트 생성
- Gradle 설정
- GitHub 저장소 연결
- Git 브랜치/PR workflow 문서화
- Health API 추가
- Spring Security 기본 설정
- Docker Compose PostgreSQL 구성
- JPA/Flyway 기본 설정
- Flyway 초기 migration 추가
- ERD와 Flyway schema 문서화
- IntelliJ HTTP Client 수동 API 테스트 흐름 추가

## Phase 2. 핵심 도메인 - 진행 중

완료:

- `feature/member-domain`
  - 회원 Entity
  - 회원 Repository
  - 회원 Service
  - 회원 생성/조회 API
  - 회원 Controller/Service 테스트

- `feature/product-domain`
  - 상품 카테고리 Entity
  - 상품 Entity
  - 상품 카테고리 생성/조회 API
  - 상품 생성/조회/목록 API
  - 상품 Controller/Service 테스트

진행 예정:

1. `feature/inventory-domain`
2. `feature/order-domain`
3. `feature/payment-domain`
4. `feature/coupon-domain`

목표:

- JPA Entity 작성
- Repository 작성
- Service 계층 작성
- Controller/API 작성
- 테스트 작성
- Flyway schema와 Entity validate 흐름 확인

## Phase 3. 공통 API 구조 - 완료

완료:

- `ApiResponse<T>` 공통 응답 구조
- `GlobalExceptionHandler` 전역 예외 처리
- `ResponseStatusException` 실패 응답 통일
- validation error 응답 통일
- Spring Security 예외 응답 처리 보완

예정:

- API 에러 코드 세분화
- 도메인별 custom exception 도입
- Swagger/OpenAPI 설정

## Phase 4. 인증과 Audit - 부분 완료

완료:

- `feature/auth-jwt-login`
  - JWT 로그인 API
  - access token 발급
  - JWT 인증 필터
  - `LoginMember` 기반 로그인 사용자 표현

- `feature/audit-user-tracking`
  - JPA Auditing 설정
  - `created_by`, `updated_by` 자동 입력
  - `created_at`, `updated_at` 공통 관리
  - 상품/카테고리 생성 API 인증 필요 처리

보류:

- 관리자 작업 로그
- 관리자 작업 IP/User-Agent 저장
- 관리자 작업 성공/실패 이력 저장

보류 이유:

- `created_by`, `updated_by`는 이미 핵심 데이터에 반영되어 있습니다.
- IP/User-Agent는 데이터의 작성자라기보다 요청 환경 정보입니다.
- 상품/회원/재고 테이블에 직접 넣기보다 `admin_action_logs` 같은 운영 로그 테이블로 분리하는 편이 유지보수에 좋습니다.

추후 브랜치:

```text
feature/admin-action-log
```

## Phase 5. JPA와 DB 설계 강화 - 예정

- 연관관계 정리
- N+1 문제 재현과 해결
- QueryDSL 검색 API
- 인덱스 적용 전후 비교
- 트랜잭션 경계 문서화

## Phase 6. Redis - 예정

- 상품 목록 캐시
- 토큰 블랙리스트
- 재고 차감 분산락
- 캐시 적용 전후 성능 비교

## Phase 7. Message Queue - 예정

- 주문 생성 이벤트 발행
- 재고 차감/알림 비동기 처리
- 중복 이벤트 처리와 멱등성 설계
- 실패 이벤트 재처리 전략 문서화

## Phase 8. Batch - 예정

- 일별 매출 집계
- 일별 재고 스냅샷
- 오래된 운영 이벤트 아카이빙
- Batch Job 실행 결과 문서화

## Phase 9. 서버 분리와 로드밸런싱 - 예정

- API 서버 3개 컨테이너 실행
- Nginx reverse proxy 구성
- health check
- 요청 분산 검증

## Phase 10. 모니터링 - 예정

- Actuator endpoint 구성
- Prometheus metrics 수집
- Grafana dashboard 구성
- 장애 상황 예시와 대응 문서화

## Phase 11. AI/데이터 확장 - 예정

- 고객 응대 초안 생성 Mock
- 운영 이벤트 요약
- Python FastAPI AI Service 분리
- LLM API 연동 후보 정리

## Phase 12. 포트폴리오 정리 - 예정

- README 고도화
- 아키텍처 의사결정 기록
- 성능 테스트 결과
- 트러블슈팅 기록
- 회고 문서 작성