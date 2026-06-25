package com.petopscommerce.global.businessnumber.service;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberRange;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberRule;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberSequence;
import com.petopscommerce.global.businessnumber.repository.BusinessNumberSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * - 업무 번호 구간 할당기
 * - DB row lock으로 중복 없는 번호 구간 확보
 */
@Service
public class BusinessNumberRangeAllocator {

    private final BusinessNumberSequenceRepository sequenceRepository;

    /**
     * - 생성자 주입
     *
     * @param sequenceRepository 업무 번호 구간 DB 접근 객체
     */
    public BusinessNumberRangeAllocator(BusinessNumberSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * - 번호 구간 할당
     * - sequence row를 생성 보장한 뒤 잠금 조회하여 nextValue를 이동
     *
     * @param rule 업무 번호 규칙
     * @param scopeKey 채번 범위 key
     * @param sequencePeriod 초기화 주기 key
     * @return 할당된 번호 구간
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessNumberRange allocate(BusinessNumberRule rule, String scopeKey, String sequencePeriod) {
        sequenceRepository.ensureSequence(rule.getCode(), scopeKey, sequencePeriod);

        BusinessNumberSequence sequence = sequenceRepository.findForUpdate(rule.getCode(), scopeKey, sequencePeriod)
                .orElseThrow(() -> new IllegalStateException("business number sequence not found"));

        return sequence.allocate(rule.getAllocationSize());
    }
}
