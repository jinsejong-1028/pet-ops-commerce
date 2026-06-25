package com.petopscommerce.global.businessnumber.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * - 업무 번호 구간 Entity
 * - business_number_sequences 테이블 매핑
 */
@Entity
@Table(name = "business_number_sequences")
@EntityListeners(AuditingEntityListener.class)
public class BusinessNumberSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    /**
     * - 채번 범위 key
     * - GLOBAL, MEMBER:1, WAREHOUSE:3처럼 번호판을 분리
     */
    @Column(name = "scope_key", nullable = false, length = 100)
    private String scopeKey;

    /**
     * - 초기화 주기 key
     * - DAILY면 yyyyMMdd, NONE이면 ALL
     */
    @Column(name = "sequence_period", nullable = false, length = 30)
    private String sequencePeriod;

    /**
     * - 다음에 할당할 구간 시작값
     */
    @Column(name = "next_value", nullable = false)
    private Long nextValue;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BusinessNumberSequence() {
        // JPA 기본 생성자
    }

    private BusinessNumberSequence(String ruleCode, String scopeKey, String sequencePeriod, Long nextValue) {
        this.ruleCode = ruleCode;
        this.scopeKey = scopeKey;
        this.sequencePeriod = sequencePeriod;
        this.nextValue = nextValue;
    }

    /**
     * - 신규 업무 번호 구간 생성
     *
     * @param ruleCode 업무 번호 규칙 코드
     * @param scopeKey 채번 범위 key
     * @param sequencePeriod 초기화 주기 key
     * @return 신규 업무 번호 구간
     */
    public static BusinessNumberSequence create(String ruleCode, String scopeKey, String sequencePeriod) {
        return new BusinessNumberSequence(ruleCode, scopeKey, sequencePeriod, 1L);
    }

    /**
     * - 번호 구간 확보
     * - nextValue를 allocationSize만큼 앞으로 이동
     *
     * @param allocationSize 확보할 구간 크기
     * @return 확보된 번호 구간
     */
    public BusinessNumberRange allocate(Integer allocationSize) {
        Long start = nextValue;
        Long end = start + allocationSize - 1;
        nextValue = end + 1;

        return new BusinessNumberRange(start, end);
    }

    public Long getId() {
        return id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getScopeKey() {
        return scopeKey;
    }

    public String getSequencePeriod() {
        return sequencePeriod;
    }

    public Long getNextValue() {
        return nextValue;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
