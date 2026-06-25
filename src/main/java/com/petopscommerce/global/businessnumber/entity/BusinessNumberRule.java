package com.petopscommerce.global.businessnumber.entity;

import com.petopscommerce.global.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * - 업무 번호 규칙 Entity
 * - business_number_rules 테이블 매핑
 */
@Entity
@Table(name = "business_number_rules")
public class BusinessNumberRule extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 업무 번호 유형 코드
     * - ORDER, PAYMENT 같은 generator 조회 기준
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 30)
    private String prefix;

    /**
     * - 번호에 표시할 날짜 포맷
     * - null이면 날짜 조각을 만들지 않음
     */
    @Column(name = "date_format", length = 30)
    private String dateFormat;

    /**
     * - 순번 자리수
     * - 부족한 자리는 0으로 채움
     */
    @Column(name = "sequence_width", nullable = false)
    private Integer sequenceWidth;

    /**
     * - 순번 초기화 주기
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_cycle", nullable = false, length = 30)
    private BusinessNumberResetCycle resetCycle;

    /**
     * - 순번 공유 범위
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 30)
    private BusinessNumberScopeType scopeType;

    @Column(nullable = false, length = 10)
    private String separator;

    /**
     * - 한 번에 확보할 번호 구간 크기
     * - DB row lock 횟수를 줄이기 위한 값
     */
    @Column(name = "allocation_size", nullable = false)
    private Integer allocationSize;

    @Column(nullable = false)
    private Boolean enabled;

    protected BusinessNumberRule() {
        // JPA 기본 생성자
    }

    private BusinessNumberRule(String code, String prefix, String dateFormat, Integer sequenceWidth, BusinessNumberResetCycle resetCycle, BusinessNumberScopeType scopeType, String separator, Integer allocationSize, Boolean enabled) {
        this.code = code;
        this.prefix = prefix;
        this.dateFormat = dateFormat;
        this.sequenceWidth = sequenceWidth;
        this.resetCycle = resetCycle;
        this.scopeType = scopeType;
        this.separator = separator;
        this.allocationSize = allocationSize;
        this.enabled = enabled;
    }

    /**
     * - 신규 업무 번호 규칙 생성
     *
     * @param code 업무 번호 유형 코드
     * @param prefix 번호 prefix
     * @param dateFormat 날짜 포맷
     * @param sequenceWidth 순번 자리수
     * @param resetCycle 초기화 주기
     * @param scopeType 채번 범위
     * @param separator 구분자
     * @param allocationSize 구간 할당 크기
     * @return 신규 업무 번호 규칙
     */
    public static BusinessNumberRule create(String code, String prefix, String dateFormat, Integer sequenceWidth, BusinessNumberResetCycle resetCycle, BusinessNumberScopeType scopeType, String separator, Integer allocationSize) {
        return new BusinessNumberRule(code, prefix, dateFormat, sequenceWidth, resetCycle, scopeType, separator, allocationSize, true);
    }

    /**
     * - 업무 번호 유형의 기본 규칙 생성
     *
     * @param type 업무 번호 유형
     * @return 신규 업무 번호 규칙
     */
    public static BusinessNumberRule create(BusinessNumberType type) {
        return new BusinessNumberRule(
                type.name(),
                type.getDefaultPrefix(),
                type.getDefaultDateFormat(),
                type.getDefaultSequenceWidth(),
                type.getDefaultResetCycle(),
                type.getDefaultScopeType(),
                type.getDefaultSeparator(),
                type.getDefaultAllocationSize(),
                true
        );
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public Integer getSequenceWidth() {
        return sequenceWidth;
    }

    public BusinessNumberResetCycle getResetCycle() {
        return resetCycle;
    }

    public BusinessNumberScopeType getScopeType() {
        return scopeType;
    }

    public String getSeparator() {
        return separator;
    }

    public Integer getAllocationSize() {
        return allocationSize;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}