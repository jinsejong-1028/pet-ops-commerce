# PostgreSQL 선택과 비용 전략

## 1. 결정 사항

PetOps Commerce의 기본 관계형 데이터베이스는 PostgreSQL로 진행합니다.

기존 실무 경험은 PHP/MySQL 중심이지만, 이번 포트폴리오에서는 PostgreSQL을 사용해 새로운 RDBMS를 익히고 Java/Spring Boot, JPA, QueryDSL, 데이터 분석 확장성과 함께 보여주는 방향으로 잡습니다.

## 2. PostgreSQL을 선택한 이유

| 이유 | 설명 |
|---|---|
| 채용 공고 대응 | 최근 백엔드 공고에서 PostgreSQL, MySQL, MariaDB 등 RDBMS 설계 경험을 폭넓게 요구합니다. PostgreSQL 경험을 추가하면 기존 MySQL 경험과 함께 RDBMS 적응력을 보여줄 수 있습니다. |
| Spring/JPA 학습 | JPA, 트랜잭션, 인덱스, 락, 페이징, 동적 쿼리 학습에 적합합니다. |
| 데이터/AI 확장 | 운영 데이터 분석, 리포트, 추천, AI 기능 확장 시 PostgreSQL 생태계와 잘 맞습니다. |
| 포트폴리오 메시지 | 익숙한 MySQL만 반복하지 않고, 목적에 맞는 새 DB를 선택하고 학습했다는 메시지를 줄 수 있습니다. |

## 3. MySQL 경험과 연결해서 어필하는 방법

이 프로젝트에서 PostgreSQL을 사용한다고 해서 기존 MySQL 경험이 사라지는 것은 아닙니다. 오히려 아래처럼 설명할 수 있습니다.

> 기존 실무에서는 PHP/MySQL 환경에서 주문, 재고, 운영 데이터를 다뤘고, 이 포트폴리오에서는 같은 RDBMS 설계 경험을 Java/Spring Boot와 PostgreSQL 환경으로 확장했습니다. MySQL에서 익힌 테이블 설계, 인덱스, SQL 튜닝, 트랜잭션 감각을 PostgreSQL에서도 비교하며 적용했습니다.

면접에서는 아래 포인트를 강조합니다.

- MySQL 실무 경험이 있어 RDBMS 기본기는 이미 갖고 있다.
- PostgreSQL을 새로 선택해 DB별 차이를 학습했다.
- JPA를 사용하더라도 DB 인덱스, 트랜잭션, 락, 실행 계획을 의식한다.
- DB 제품이 달라져도 도메인 모델링과 SQL 사고를 이어갈 수 있다.

## 4. PostgreSQL과 MySQL의 차이 학습 포인트

| 항목 | MySQL | PostgreSQL | 프로젝트에서 볼 내용 |
|---|---|---|---|
| Auto Increment | `AUTO_INCREMENT` | `GENERATED AS IDENTITY`, sequence | JPA ID 생성 전략 |
| JSON | JSON 타입 지원 | JSON/JSONB 강점 | 운영 이벤트 확장 후보 |
| 트랜잭션 | InnoDB 기준으로 강력함 | MVCC 기반 동시성 강점 | 주문/재고 동시성 테스트 |
| 인덱스 | B-Tree 중심으로 많이 사용 | B-Tree 외 다양한 인덱스 확장 | 검색 API 성능 개선 |
| SQL 문법 | 일부 MySQL 전용 문법 존재 | 표준 SQL 친화적 | QueryDSL과 native query 사용 기준 |
| 운영 경험 | PHP/Laravel 생태계와 친숙 | Java/Spring, 데이터 분석 확장에 적합 | 새 DB 학습 기록 |

## 5. 무료 우선 개발 전략

초기 개발은 최대한 비용이 들지 않는 로컬 환경에서 진행합니다.

| 영역 | 무료 우선 선택 |
|---|---|
| API 서버 | 로컬 Java/Spring Boot 실행 |
| DB | Docker Compose PostgreSQL 컨테이너 |
| Redis | Docker Compose Redis 컨테이너 |
| Message Queue | Docker Compose RabbitMQ 또는 Kafka 컨테이너 |
| Nginx | Docker Compose Nginx 컨테이너 |
| 모니터링 | 로컬 Prometheus/Grafana 컨테이너 |
| 테스트 | JUnit5, Testcontainers |
| 문서 | Markdown, Mermaid |

이 단계에서는 AWS를 사용하지 않습니다. 비용 없이 로컬에서 API 서버 3개, DB, Redis, MQ, Nginx를 모두 띄우고 로드밸런싱과 서버 분리 구조를 학습합니다.

## 6. 배포 전략

배포는 단계별로 진행합니다.

### 1단계. 로컬 Docker Compose

가장 먼저 로컬에서 전체 구조를 완성합니다.

- Spring Boot API 서버 1개 실행
- PostgreSQL 연결
- Redis 연결
- Nginx reverse proxy 구성
- API 서버 3개로 확장
- 로드밸런싱 확인

### 2단계. 무료 또는 무료에 가까운 외부 배포 후보 검토

포트폴리오 시연이 필요할 때 무료 호스팅 후보를 먼저 검토합니다.

- GitHub Actions는 CI 중심으로 사용
- API 서버는 무료 컨테이너 호스팅 후보 검토
- DB는 무료 PostgreSQL 제공 서비스 후보 검토
- 단, 무료 서비스는 sleep, 제한, 삭제 정책이 있을 수 있으므로 운영 포트폴리오의 최종 기준으로 고정하지 않습니다.

### 3단계. AWS 저비용 배포

AWS 배포는 포트폴리오 완성 후 선택적으로 진행합니다.

저비용 구성을 우선합니다.

- EC2 소형 인스턴스 1대에 Docker Compose 배포
- PostgreSQL은 초기에는 같은 EC2의 Docker 컨테이너로 실행
- 비용이 감당 가능할 때 RDS PostgreSQL로 분리
- 트래픽이 적은 포트폴리오 시연용으로만 운영
- 사용하지 않을 때는 서버를 중지하거나 제거

## 7. AWS 비용 주의 사항

AWS는 무료처럼 보여도 설정에 따라 과금될 수 있습니다. 배포 전에는 반드시 공식 가격 페이지와 Billing 알림을 확인합니다.

2026-06-22 확인 기준 AWS 공식 Free Tier 페이지는 신규 고객에게 최대 200달러 크레딧과 6개월 무료 플랜 구조를 안내하고 있습니다. RDS for PostgreSQL 가격 페이지는 신규 고객이 Free Tier에서 Single-AZ DB 750시간, gp2 스토리지 20GB, 자동 백업 20GB를 1년 동안 사용할 수 있다고 안내합니다.

하지만 실제 비용은 계정 유형, 리전, 인스턴스 타입, 스토리지, 네트워크, Free Tier 잔여 기간에 따라 달라집니다. 따라서 이 프로젝트 문서에서는 정확한 월 비용을 단정하지 않고, 배포 직전에 AWS Pricing Calculator로 다시 계산합니다.

참고 자료:

- AWS Free Tier: https://aws.amazon.com/free/
- Amazon RDS for PostgreSQL Pricing: https://aws.amazon.com/rds/postgresql/pricing/
- Amazon EC2 Pricing: https://aws.amazon.com/ec2/pricing/

## 8. 포트폴리오 설명 문장

> 기존에는 PHP/MySQL 환경에서 업무 데이터를 다뤘고, 이 프로젝트에서는 PostgreSQL을 선택해 Java/Spring Boot 환경에서 RDBMS 설계 경험을 확장했습니다. 초기 개발과 로드밸런싱 검증은 Docker Compose로 무료 로컬 환경에서 진행하고, 배포는 비용을 고려해 EC2 단일 서버 Docker Compose 구성부터 시작한 뒤 필요 시 RDS로 분리하는 단계적 전략을 세웠습니다.

## 9. 현재 결정

| 항목 | 결정 |
|---|---|
| 기본 DB | PostgreSQL |
| MySQL | 기존 실무 경험과 비교 학습 포인트로 문서화 |
| 초기 개발 환경 | 로컬 Docker Compose |
| 초기 배포 방향 | 무료 우선, 필요 시 AWS 저비용 구성 |
| AWS DB 구성 | 처음부터 RDS 고정이 아니라 EC2 Docker PostgreSQL 후 RDS 분리 검토 |
| 비용 원칙 | 무료/저비용 우선, 배포 전 공식 가격 재확인 |