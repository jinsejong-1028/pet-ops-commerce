package com.petopscommerce.global.businessnumber.repository;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * - 업무 번호 규칙 DB 접근 객체
 * - business_number_rules 테이블 CRUD/조회 담당
 */
public interface BusinessNumberRuleRepository extends JpaRepository<BusinessNumberRule, Long> {

    /**
     * - 활성 업무 번호 규칙 조회
     *
     * @param code 업무 번호 유형 코드
     * @return 활성 업무 번호 규칙 Optional
     */
    Optional<BusinessNumberRule> findByCodeAndEnabledTrue(String code);
}
