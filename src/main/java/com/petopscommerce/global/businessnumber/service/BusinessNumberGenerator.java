package com.petopscommerce.global.businessnumber.service;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberRange;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberResetCycle;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberRule;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberScopeType;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.repository.BusinessNumberRuleRepository;
import org.springframework.stereotype.Service;

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
    private final Map<BusinessNumberCacheKey, BusinessNumberRangeCursor> rangeCache = new ConcurrentHashMap<>();

    /**
     * - 생성자 주입
     *
     * @param ruleRepository 업무 번호 규칙 DB 접근 객체
     * @param rangeAllocator 업무 번호 구간 할당기
     */
    public BusinessNumberGenerator(BusinessNumberRuleRepository ruleRepository, BusinessNumberRangeAllocator rangeAllocator) {
        this.ruleRepository = ruleRepository;
        this.rangeAllocator = rangeAllocator;
    }

    /**
     * - 전역 업무 번호 생성
     *
     * @param type 업무 번호 유형
     * @return 생성된 업무 번호
     */
    public String generate(BusinessNumberType type) {
        return generate(type, null, LocalDateTime.now());
    }

    /**
     * - 범위별 업무 번호 생성
     *
     * @param type 업무 번호 유형
     * @param scopeId 회원 ID, 창고 ID 같은 범위 식별자
     * @return 생성된 업무 번호
     */
    public String generate(BusinessNumberType type, Long scopeId) {
        return generate(type, scopeId, LocalDateTime.now());
    }

    /**
     * - 기준 일시를 지정해 업무 번호 생성
     * - 테스트와 배치 재처리에서 사용
     *
     * @param type 업무 번호 유형
     * @param scopeId 회원 ID, 창고 ID 같은 범위 식별자
     * @param now 기준 일시
     * @return 생성된 업무 번호
     */
    public String generate(BusinessNumberType type, Long scopeId, LocalDateTime now) {
        BusinessNumberRule rule = ruleRepository.findByCodeAndEnabledTrue(type.name())
                .orElseThrow(() -> new IllegalStateException("business number rule not found"));

        String scopeKey = createScopeKey(rule, scopeId);
        String sequencePeriod = createSequencePeriod(rule.getResetCycle(), now);
        BusinessNumberCacheKey cacheKey = new BusinessNumberCacheKey(rule.getCode(), scopeKey, sequencePeriod);
        Long sequenceValue = nextSequenceValue(cacheKey, rule);

        return formatBusinessNumber(rule, sequenceValue, now);
    }

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

    private BusinessNumberRangeCursor allocateRange(BusinessNumberRule rule, BusinessNumberCacheKey cacheKey) {
        BusinessNumberRange range = rangeAllocator.allocate(rule, cacheKey.scopeKey(), cacheKey.sequencePeriod());
        return new BusinessNumberRangeCursor(range.start(), range.end());
    }

    private String createScopeKey(BusinessNumberRule rule, Long scopeId) {
        if (rule.getScopeType() == BusinessNumberScopeType.GLOBAL) {
            return GLOBAL_SCOPE_KEY;
        }

        if (scopeId == null) {
            throw new IllegalArgumentException("scopeId is required");
        }

        return rule.getScopeType().name() + ":" + scopeId;
    }

    private String createSequencePeriod(BusinessNumberResetCycle resetCycle, LocalDateTime now) {
        return switch (resetCycle) {
            case NONE -> ALL_PERIOD;
            case DAILY -> now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            case MONTHLY -> now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            case YEARLY -> now.format(DateTimeFormatter.ofPattern("yyyy"));
        };
    }

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
