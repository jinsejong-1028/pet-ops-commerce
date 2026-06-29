# 개발 로드맵

이 문서는 PetOps Commerce 프로젝트의 전체 개발 순서와 현재 진행 상태를 정리합니다.

## 현재 요약

| 구분 | 현재 상태 |
|---|---|
| 기준 날짜 | 2026-06-29 |
| 현재 브랜치 | `docs/reorganize-documentation` |
| Git 상태 | 문서 폴더 구조 개편과 README 추가 중 |
| 마지막 완료 작업 | `docs/update-current-project-docs` |
| 다음 추천 작업 | `feature/order-fulfillment-workflow` |

## Phase 0. 설계 - 완료

- 요구사항 정의
- 아키텍처 설계
- ERD 작성
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
- Flyway migration 추가
- IntelliJ HTTP Client 수동 API 테스트 흐름 추가

## Phase 2. 핵심 도메인 - 완료

- 회원 도메인
- 상품/카테고리 도메인
- JWT 로그인과 인증 필터
- Audit user tracking
- 공통 API 응답/전역 예외 처리
- 주문 생성 도메인
- 재고 조회 도메인
- QueryDSL 기반 재고 검색
- 업무 번호 생성기
- 재고 작업/이동 원장
- 관리자 재고 명령 API
- location type `NORMAL` 오타 수정

## Phase 3. 인증과 권한 - 부분 완료

완료:

- JWT 로그인 API
- access token 발급
- JWT 인증 필터
- 비활성 회원 로그인 차단
- `/api/v1/admin/**` 인증 필요 정책 정리

진행 예정:

- `ADMIN`, `OPERATOR`, `MEMBER` 역할별 인가 세분화
- `/members/me` 중심 회원 조회 정책 정리
- 관리자 API 접근 정책 정리

## Phase 4. 재고와 주문 운영 흐름 - 진행 예정

현재 DB schema는 고객 주문 이후의 운영 workflow를 준비해 둔 상태입니다.

출고 흐름:

```text
orders
-> sales_orders
-> shipment_orders
-> stock_jobs(reference_type = SHIPMENT_ORDER)
-> stock_movements
```

입고 흐름:

```text
purchase_orders
-> receiving_orders
-> stock_jobs(reference_type = RECEIVING_ORDER)
-> stock_movements
```

추천 브랜치:

```text
feature/order-fulfillment-workflow
```

목표:

- 고객 주문을 판매 주문으로 확정
- 판매 주문 기반 출고 지시 생성
- 출고 지시 품목별 할당/피킹/출고 수량 관리
- 구매 발주와 입고 지시 생성
- 입고 지시 확정 시 LOT/현재고 반영
- 기존 재고 operation과 출고/입고 workflow 연결

## Phase 5. API 문서화 - 진행 예정

추천 브랜치:

```text
chore/openapi-docs
```

목표:

- Swagger/OpenAPI 설정
- Health/Member/Auth/Product/Inventory/Order API 문서화
- 관리자 API 문서화 기준 수립

## Phase 6. JPA와 DB 설계 강화 - 예정

- 연관관계 정리
- N+1 문제 재현과 해결
- QueryDSL 검색 조건 확장
- 인덱스 적용 전후 비교
- 트랜잭션 경계 문서화

## Phase 7. Redis - 예정

- 상품 목록 캐시
- 토큰 블랙리스트
- 재고 차감 분산락
- 캐시 적용 전후 성능 비교

## Phase 8. Message Queue - 예정

- 주문 생성 이벤트 발행
- 재고 차감/알림 비동기 처리
- 중복 이벤트 처리와 멱등성 설계
- 실패 이벤트 재처리 전략 문서화

## Phase 9. Batch - 예정

- 일별 매출 집계
- 일별 재고 스냅샷
- 오래된 운영 이벤트 아카이빙
- Batch Job 실행 결과 문서화

## Phase 10. 서버 분리와 로드밸런싱 - 예정

- API 서버 3개 컨테이너 실행
- Nginx reverse proxy 구성
- health check
- 요청 분산 검증

## Phase 11. 모니터링 - 예정

- Actuator endpoint 구성
- Prometheus metrics 수집
- Grafana dashboard 구성
- 장애 상황 예시와 대응 문서화

## Phase 12. AI/데이터 확장 - 예정

- 고객 응대 초안 생성 Mock
- 운영 이벤트 요약
- Python FastAPI AI Service 분리
- LLM API 연동 후보 정리

## Phase 13. 포트폴리오 정리 - 예정

- README 고도화
- 아키텍처 의사결정 기록
- 성능 테스트 결과
- 트러블슈팅 기록
- 회고 문서 작성