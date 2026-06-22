# 개발 로드맵

## Phase 0. 설계

- 요구사항 정의
- 아키텍처 설계
- ERD 초안 작성
- API 목록 작성
- 기술 스택 확정

## Phase 1. 프로젝트 골격

- Spring Boot 프로젝트 생성
- Gradle 설정
- 공통 응답/예외 구조
- Swagger 설정
- Docker Compose DB 구성
- Flyway 초기 마이그레이션

## Phase 2. 핵심 도메인

- 회원/로그인/JWT
- 상품/카테고리
- 장바구니
- 주문 생성
- 재고 차감/복구

## Phase 3. JPA와 DB 설계 강화

- 연관관계 정리
- N+1 문제 재현과 해결
- QueryDSL 검색 API
- 인덱스 적용 전후 비교
- 트랜잭션 경계 문서화

## Phase 4. Redis

- 상품 목록 캐시
- 토큰 블랙리스트
- 재고 차감 분산락
- 캐시 적용 전후 성능 비교

## Phase 5. Message Queue

- 주문 생성 이벤트 발행
- 재고 차감/알림 비동기 처리
- 중복 이벤트 처리와 멱등성 설계
- 실패 이벤트 재처리 전략 문서화

## Phase 6. Batch

- 일별 매출 집계
- 일별 재고 스냅샷
- 오래된 운영 이벤트 아카이빙
- Batch Job 실행 결과 문서화

## Phase 7. 서버 분리와 로드밸런싱

- API 서버 3개 컨테이너 실행
- Nginx reverse proxy 구성
- health check
- 요청 분산 검증

## Phase 8. 모니터링

- Actuator endpoint 구성
- Prometheus metrics 수집
- Grafana dashboard 구성
- 장애 상황 예시와 대응 문서화

## Phase 9. AI/데이터 확장

- 고객 응대 초안 생성 Mock
- 운영 이벤트 요약
- Python FastAPI AI Service 분리
- LLM API 연동 후보 정리

## Phase 10. 포트폴리오 정리

- README 고도화
- 아키텍처 의사결정 기록
- 성능 테스트 결과
- 트러블슈팅 기록
- 회고 문서 작성

