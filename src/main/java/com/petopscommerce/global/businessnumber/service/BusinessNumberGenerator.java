package com.petopscommerce.global.businessnumber.service;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberRange;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberResetCycle;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberRule;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberScopeType;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.repository.BusinessNumberRuleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * - 업무 번호 생성기
 * - DB에서 번호 구간을 확보하고 메모리에서 순번을 소비
 */
@Service
public class BusinessNumberGenerator {

    private static final String GLOBAL_SCOPE_KEY = "GLOBAL";
    private static final String ALL_PERIOD = "ALL";

    private final BusinessNumberRuleRepository ruleRepository;
    private final BusinessNumberRangeAllocator rangeAllocator;
    private final Clock clock;
    private final Map<BusinessNumberCacheKey, BusinessNumberRangeCursor> rangeCache = new ConcurrentHashMap<>();

    /**
     * - 생성자 주입
     *
     * @param ruleRepository 업무 번호 규칙 DB 접근 객체
     * @param rangeAllocator 업무 번호 구간 할당기
     * @param clock 기준 시각 제공 객체
     */
    public BusinessNumberGenerator(BusinessNumberRuleRepository ruleRepository, BusinessNumberRangeAllocator rangeAllocator, Clock clock) {
        this.ruleRepository = ruleRepository;
        this.rangeAllocator = rangeAllocator;
        this.clock = clock;
    }

    /**
     * - 업무 번호 생성
     * - 호출자는 업무 번호 유형만 지정하고, 기준 시각/규칙/순번 관리는 생성기가 담당
     *
     * @param type 업무 번호 유형
     * @return 생성된 업무 번호
     */
    public String generate(BusinessNumberType type) {
        // 현재 시각 확정
        LocalDateTime now = LocalDateTime.now(clock);

        // 활성 rule 조회, 없으면 type 기본값으로 rule 생성
        BusinessNumberRule rule = getOrCreateRule(type);

        // scope/period 기준으로 같은 번호판을 공유할 cache key 생성
        String scopeKey = createScopeKey(rule);
        String sequencePeriod = createSequencePeriod(rule.getResetCycle(), now);
        BusinessNumberCacheKey cacheKey = new BusinessNumberCacheKey(rule.getCode(), scopeKey, sequencePeriod);

        // 메모리 구간 또는 DB 신규 구간에서 다음 순번 확보
        Long sequenceValue = nextSequenceValue(cacheKey, rule);

        // prefix/date/sequence 규칙으로 최종 번호 생성
        return formatBusinessNumber(rule, sequenceValue, now);
    }

    /**
     * - 업무 번호 규칙 조회
     * - 기본 규칙이 없으면 BusinessNumberType 기준으로 생성
     *
     * @param type 업무 번호 유형
     * @return 활성 업무 번호 규칙
     */
    private BusinessNumberRule getOrCreateRule(BusinessNumberType type) {
        return ruleRepository.findByCodeAndEnabledTrue(type.name())
                .orElseGet(() -> createDefaultRule(type));
    }

    /**
     * - 업무 번호 기본 규칙 생성
     * - 동시 생성 충돌이 발생하면 이미 생성된 규칙을 다시 조회
     *
     * @param type 업무 번호 유형
     * @return 생성 또는 재조회된 활성 업무 번호 규칙
     */
    private BusinessNumberRule createDefaultRule(BusinessNumberType type) {
        try {
            return ruleRepository.save(BusinessNumberRule.create(type));
        } catch (DataIntegrityViolationException exception) {
            return ruleRepository.findByCodeAndEnabledTrue(type.name())
                    .orElseThrow(() -> new IllegalStateException("business number rule not found"));
        }
    }

    /**
     * - 다음 순번 확보
     * - 메모리 구간을 먼저 소비하고, 구간이 소진되면 DB에서 새 구간을 할당
     *
     * @param cacheKey 번호판 식별 key
     * @param rule 업무 번호 규칙
     * @return 다음 순번
     */
    private Long nextSequenceValue(BusinessNumberCacheKey cacheKey, BusinessNumberRule rule) {
        while (true) {
            BusinessNumberRangeCursor cursor = rangeCache.computeIfAbsent(cacheKey, ignored -> allocateRange(rule, cacheKey));
            Long sequenceValue = cursor.next();

            if (sequenceValue != null) {
                return sequenceValue;
            }

            rangeCache.remove(cacheKey, cursor);
        }
    }

    /**
     * - DB sequence row를 통해 신규 번호 구간 확보
     * - 매 요청 DB 접근을 줄이기 위해 할당된 구간은 메모리에서 소비
     *
     * @param rule 업무 번호 규칙
     * @param cacheKey 번호판 식별 key
     * @return 메모리에서 소비할 번호 구간 cursor
     */
    private BusinessNumberRangeCursor allocateRange(BusinessNumberRule rule, BusinessNumberCacheKey cacheKey) {
        BusinessNumberRange range = rangeAllocator.allocate(rule, cacheKey.scopeKey(), cacheKey.sequencePeriod());
        return new BusinessNumberRangeCursor(range.start(), range.end());
    }

    /**
     * - 업무 번호 범위 key 생성
     * - 현재는 전체 주문이 같은 번호판을 공유하는 GLOBAL 범위만 지원
     *
     * @param rule 업무 번호 규칙
     * @return 번호판 범위 key
     */
    private String createScopeKey(BusinessNumberRule rule) {
        // 조건: 전체 공통 번호판
        // 결과: 모든 요청이 GLOBAL 순번을 공유
        if (rule.getScopeType() == BusinessNumberScopeType.GLOBAL) {
            return GLOBAL_SCOPE_KEY;
        }

        throw new IllegalStateException("scoped business number requires scope resolver");
    }

    /**
     * - resetCycle에 따라 순번을 공유할 기간 key 생성
     *
     * @param resetCycle 순번 초기화 주기
     * @param now 기준 시각
     * @return 순번 기간 key
     */
    private String createSequencePeriod(BusinessNumberResetCycle resetCycle, LocalDateTime now) {
        return switch (resetCycle) {
            case NONE -> ALL_PERIOD;
            case DAILY -> now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            case MONTHLY -> now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            case YEARLY -> now.format(DateTimeFormatter.ofPattern("yyyy"));
        };
    }

    /**
     * - prefix, date, sequence를 separator로 조립
     *
     * @param rule 업무 번호 규칙
     * @param sequenceValue 확보된 순번
     * @param now 기준 시각
     * @return 최종 업무 번호
     */
    private String formatBusinessNumber(BusinessNumberRule rule, Long sequenceValue, LocalDateTime now) {
        List<String> parts = new ArrayList<>();
        parts.add(rule.getPrefix());

        if (rule.getDateFormat() != null && !rule.getDateFormat().isBlank()) {
            parts.add(now.format(DateTimeFormatter.ofPattern(rule.getDateFormat())));
        }

        parts.add(String.format("%0" + rule.getSequenceWidth() + "d", sequenceValue));

        return String.join(rule.getSeparator(), parts);
    }

    private record BusinessNumberCacheKey(
            String ruleCode,
            String scopeKey,
            String sequencePeriod
    ) {
    }

    private static class BusinessNumberRangeCursor {

        private final AtomicLong current;
        private final Long end;

        private BusinessNumberRangeCursor(Long start, Long end) {
            this.current = new AtomicLong(start);
            this.end = end;
        }

        private Long next() {
            Long next = current.getAndIncrement();

            if (next > end) {
                return null;
            }

            return next;
        }
    }
}
