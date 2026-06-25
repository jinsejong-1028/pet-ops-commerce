package com.petopscommerce.global.businessnumber.service;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberRange;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberResetCycle;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberRule;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberScopeType;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.repository.BusinessNumberRuleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * - 업무 번호 생성기 테스트
 * - 번호 포맷/구간 할당 캐시 동작 검증
 */
@ExtendWith(MockitoExtension.class)
class BusinessNumberGeneratorTest {

    @Mock
    private BusinessNumberRuleRepository ruleRepository;

    @Mock
    private BusinessNumberRangeAllocator rangeAllocator;

    @InjectMocks
    private BusinessNumberGenerator businessNumberGenerator;

    /**
     * - ORDER 번호 포맷 검증
     */
    @Test
    @DisplayName("ORDER 규칙으로 날짜와 0 padding 순번을 포함한 번호를 생성한다")
    void generateOrderNumber() {
        BusinessNumberRule rule = BusinessNumberRule.create(
                "ORDER",
                "ORD",
                "yyyyMMdd",
                6,
                BusinessNumberResetCycle.DAILY,
                BusinessNumberScopeType.GLOBAL,
                "-",
                100
        );
        LocalDateTime now = LocalDateTime.of(2026, 6, 25, 10, 0);

        when(ruleRepository.findByCodeAndEnabledTrue("ORDER")).thenReturn(Optional.of(rule));
        when(rangeAllocator.allocate(rule, "GLOBAL", "20260625")).thenReturn(new BusinessNumberRange(1L, 100L));

        String businessNumber = businessNumberGenerator.generate(BusinessNumberType.ORDER, null, now);

        assertThat(businessNumber).isEqualTo("ORD-20260625-000001");
    }

    /**
     * - 할당 구간 캐시 검증
     */
    @Test
    @DisplayName("할당받은 번호 구간을 모두 쓰기 전에는 DB 구간을 다시 요청하지 않는다")
    void useAllocatedRangeBeforeRequestingNextRange() {
        BusinessNumberRule rule = BusinessNumberRule.create(
                "ORDER",
                "ORD",
                "yyyyMMdd",
                6,
                BusinessNumberResetCycle.DAILY,
                BusinessNumberScopeType.GLOBAL,
                "-",
                2
        );
        LocalDateTime now = LocalDateTime.of(2026, 6, 25, 10, 0);

        when(ruleRepository.findByCodeAndEnabledTrue("ORDER")).thenReturn(Optional.of(rule));
        when(rangeAllocator.allocate(rule, "GLOBAL", "20260625"))
                .thenReturn(new BusinessNumberRange(1L, 2L))
                .thenReturn(new BusinessNumberRange(3L, 4L));

        String first = businessNumberGenerator.generate(BusinessNumberType.ORDER, null, now);
        String second = businessNumberGenerator.generate(BusinessNumberType.ORDER, null, now);
        String third = businessNumberGenerator.generate(BusinessNumberType.ORDER, null, now);

        assertThat(first).isEqualTo("ORD-20260625-000001");
        assertThat(second).isEqualTo("ORD-20260625-000002");
        assertThat(third).isEqualTo("ORD-20260625-000003");
        verify(rangeAllocator, times(2)).allocate(rule, "GLOBAL", "20260625");
    }
}
