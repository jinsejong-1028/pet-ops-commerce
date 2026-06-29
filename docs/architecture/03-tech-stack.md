# 기술 스택

## 기본 백엔드

| 기술 | 선택 이유 |
|---|---|
| Java 21 | 최신 LTS 기반의 JVM 역량 증명 |
| Spring Boot 3.5.x | 기업 채용 요구와 안정성의 균형 |
| Gradle | Spring Boot 프로젝트 구성과 CI 연동 |
| Spring Web MVC | RESTful API 구현 |
| Spring Security | 인증/인가 구현 |
| JPA | ORM, 도메인 모델링, 트랜잭션 학습 |
| QueryDSL | 복잡한 검색 조건과 동적 쿼리 대응 |

## 데이터

| 기술 | 사용 위치 |
|---|---|
| PostgreSQL | 주문, 상품, 재고, 결제 등 핵심 데이터. MySQL 실무 경험과 비교하며 학습 |
| Redis | 캐시, 분산락, 토큰 블랙리스트 |
| Flyway | DB 마이그레이션 |

## DB 선택 기준

기본 DB는 PostgreSQL로 확정합니다. 기존 MySQL 실무 경험은 버리는 것이 아니라, PostgreSQL과 비교하며 RDBMS 설계 역량을 확장하는 근거로 사용합니다.

자세한 비교와 비용 전략은 [DB 선택과 비용 전략](04-database-and-cost-strategy.md)에 정리합니다.
## 분산 처리

| 기술 | 사용 위치 |
|---|---|
| Kafka 또는 RabbitMQ | 주문 생성, 재고 차감, 알림 이벤트 |
| Spring Batch | 일별 매출 집계, 재고 스냅샷 |
| Redisson | 재고 차감 분산락 |

## 운영/인프라

| 기술 | 사용 위치 |
|---|---|
| Docker Compose | 로컬 운영 환경 구성 |
| Nginx | 로드밸런싱과 reverse proxy |
| GitHub Actions | 테스트/빌드 자동화 |
| Actuator | 헬스 체크와 운영 endpoint |
| Prometheus/Grafana | 메트릭 수집과 시각화 |

## 테스트

| 기술 | 사용 위치 |
|---|---|
| JUnit5 | 단위/통합 테스트 |
| Mockito | 단위 테스트 mock |
| Testcontainers | 실제 DB/Redis/MQ 기반 통합 테스트 |
| k6 또는 JMeter | 부하 테스트 |

## AI 확장

| 기술 | 사용 위치 |
|---|---|
| Python FastAPI | AI 운영 보조 서비스 분리 후보 |
| OpenAI API | 고객 응대 초안, 운영 데이터 요약 |
| Pandas | 운영 데이터 분석 확장 후보 |

