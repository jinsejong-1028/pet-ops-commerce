package com.petopscommerce.global.businessnumber.service;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberRange;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberResetCycle;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberRule;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberScopeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * - 업무 번호 구간 할당 동시성 통합 테스트
 * - Testcontainers PostgreSQL에서 실제 row lock 동작을 검증
 * - H2는 PostgreSQL 잠금 동작과 다를 수 있어 실제 PostgreSQL 컨테이너를 사용
 */
@Testcontainers
@SpringBootTest
class BusinessNumberRangeAllocatorConcurrencyTest {

    private static final int THREAD_COUNT = 20;
    private static final String RULE_CODE = "CONCURRENCY_ORDER";
    private static final String SCOPE_KEY = "GLOBAL";
    private static final String SEQUENCE_PERIOD = "20260626";

    /**
     * - 테스트 전용 PostgreSQL 컨테이너
     * - @ServiceConnection으로 Spring datasource에 자동 연결
     * - 애플리케이션 시작 시 Flyway migration이 이 컨테이너 DB에 적용됨
     */
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BusinessNumberRangeAllocator rangeAllocator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * - 같은 sequence row에 동시에 접근해도 서로 다른 번호 구간을 확보하는지 검증
     * - 성공 기준: 모든 스레드 완료, 예외 없음, 할당 시작값 중복 없음, nextValue 정상 이동
     */
    @Test
    @DisplayName("동시 구간 할당 시 PostgreSQL row lock으로 중복 없는 번호 구간을 확보한다")
    void allocateRangesConcurrentlyWithoutDuplicates() throws Exception {
        // 조건: 구간 크기 1
        // 결과: 스레드마다 DB row lock을 타도록 만들어 동시성 충돌 상황을 강하게 재현
        BusinessNumberRule rule = BusinessNumberRule.create(
                RULE_CODE,
                "TST",
                "yyyyMMdd",
                6,
                BusinessNumberResetCycle.DAILY,
                BusinessNumberScopeType.GLOBAL,
                "-",
                1
        );

        // 조건: 20개 작업 스레드
        // 결과: 같은 sequence row에 거의 동시에 할당 요청을 보낼 실행 환경 구성
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        Set<Long> allocatedStarts = ConcurrentHashMap.newKeySet();
        Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    // 조건: 스레드 준비 완료
                    // 결과: 모든 스레드가 출발선에 모일 때까지 대기
                    readyLatch.countDown();
                    startLatch.await();

                    // 조건: 같은 rule/scope/period
                    // 결과: 모든 스레드가 동일 business_number_sequences row에 접근
                    BusinessNumberRange range = rangeAllocator.allocate(rule, SCOPE_KEY, SEQUENCE_PERIOD);
                    allocatedStarts.add(range.start());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    failures.add(exception);
                } catch (RuntimeException exception) {
                    failures.add(exception);
                }
            });
        }

        // 조건: 모든 스레드가 준비됨
        // 결과: startLatch를 열어 동시에 allocate 호출 시작
        boolean ready = readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 조건: 동시 할당 완료 후 DB 상태
        // 결과: 다음 할당 시작값이 THREAD_COUNT + 1로 이동했는지 확인
        Long nextValue = jdbcTemplate.queryForObject(
                """
                        select next_value
                        from business_number_sequences
                        where rule_code = ?
                          and scope_key = ?
                          and sequence_period = ?
                        """,
                Long.class,
                RULE_CODE,
                SCOPE_KEY,
                SEQUENCE_PERIOD
        );

        // 성공 판단: 모든 스레드가 제한 시간 안에 출발선에 도착
        assertThat(ready).isTrue();
        // 성공 판단: 모든 스레드 작업이 제한 시간 안에 완료
        assertThat(finished).isTrue();
        // 성공 판단: 동시 할당 중 예외가 발생하지 않음
        assertThat(failures).isEmpty();
        // 성공 판단: 20개 요청이 20개의 서로 다른 시작값을 확보
        assertThat(allocatedStarts).hasSize(THREAD_COUNT);
        // 성공 판단: allocationSize가 1이므로 시작값이 1~20까지 빠짐없이 존재
        assertThat(allocatedStarts).containsExactlyInAnyOrderElementsOf(
                LongStream.rangeClosed(1, THREAD_COUNT)
                        .boxed()
                        .toList()
        );
        // 성공 판단: 1~20 할당 후 다음 구간 시작값은 21
        assertThat(nextValue).isEqualTo(THREAD_COUNT + 1L);
    }
}
