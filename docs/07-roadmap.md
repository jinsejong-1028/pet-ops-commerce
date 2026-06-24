# 개발 로드맵

이 문서는 PetOps Commerce 프로젝트의 전체 개발 순서와 현재 진행 상태를 정리합니다.

## 상태 기준

| 상태 | 의미 |
|---|---|
| 완료 | PR merge와 로컬/원격 브랜치 정리 완료 |
| 진행 예정 | 다음 작업 후보 |
| 예정 | 아직 시작하지 않은 작업 |
| 보류 | 현재 범위에서 제외한 작업 |

## 현재 요약

| 구분 | 현재 상태 |
|---|---|
| 현재 브랜치 | `main` |
| Git 상태 | `origin/main` 동기화, working tree clean |
| 마지막 완료 작업 | `feature/common-api-response` |
| 다음 추천 작업 | `feature/product-domain` |

## Phase 0. 설계 - 완료

- 요구사항 정의
- 아키텍처 설계
- ERD 초안 작성
- API 목록 작성
- 기술 스택 확정
- PostgreSQL/MySQL 차이와 비용 전략 정리

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

다음 추천 순서:

1. `feature/product-domain`
2. `feature/inventory-domain`
3. `feature/order-domain`
4. `feature/payment-domain`
5. `feature/coupon-domain`

목표:

- JPA Entity 작성
- Repository 작성
- Service 계층 작성
- Controller/API 작성
- 테스트 작성
- Flyway schema와 Entity validate 흐름 확인

## Phase 3. 공통 API 구조 - 부분 완료

완료:

- `ApiResponse<T>` 공통 응답 구조
- `GlobalExceptionHandler` 전역 예외 처리
- `ResponseStatusException` 실패 응답 통일
- validation error 응답 통일

예정:

- API 에러 코드 세분화
- 도메인별 custom exception 도입
- Swagger/OpenAPI 설정

## Phase 4. JPA와 DB 설계 강화 - 예정

- 연관관계 정리
- N+1 문제 재현과 해결
- QueryDSL 검색 API
- 인덱스 적용 전후 비교
- 트랜잭션 경계 문서화

## Phase 5. Redis - 예정

- 상품 목록 캐시
- 토큰 블랙리스트
- 재고 차감 분산락
- 캐시 적용 전후 성능 비교

## Phase 6. Message Queue - 예정

- 주문 생성 이벤트 발행
- 재고 차감/알림 비동기 처리
- 중복 이벤트 처리와 멱등성 설계
- 실패 이벤트 재처리 전략 문서화

## Phase 7. Batch - 예정

- 일별 매출 집계
- 일별 재고 스냅샷
- 오래된 운영 이벤트 아카이빙
- Batch Job 실행 결과 문서화

## Phase 8. 서버 분리와 로드밸런싱 - 예정

- API 서버 3개 컨테이너 실행
- Nginx reverse proxy 구성
- health check
- 요청 분산 검증

## Phase 9. 모니터링 - 예정

- Actuator endpoint 구성
- Prometheus metrics 수집
- Grafana dashboard 구성
- 장애 상황 예시와 대응 문서화

## Phase 10. AI/데이터 확장 - 예정

- 고객 응대 초안 생성 Mock
- 운영 이벤트 요약
- Python FastAPI AI Service 분리
- LLM API 연동 후보 정리

## Phase 11. 포트폴리오 정리 - 예정

- README 고도화
- 아키텍처 의사결정 기록
- 성능 테스트 결과
- 트러블슈팅 기록
- 회고 문서 작성