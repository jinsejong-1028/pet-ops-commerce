package com.petopscommerce.global.businessnumber.entity;

/**
 * - 업무 번호 유형
 * - 규칙 테이블 code와 기본 rule 기준값 관리
 */
public enum BusinessNumberType {
    /** - 주문 번호 */
    ORDER("ORD", "yyyyMMdd", 6, BusinessNumberResetCycle.DAILY, BusinessNumberScopeType.GLOBAL, "-", 100),

    /** - 결제 번호 */
    PAYMENT("PAY", "yyyyMMdd", 6, BusinessNumberResetCycle.DAILY, BusinessNumberScopeType.GLOBAL, "-", 100),

    /** - 출고 번호 */
    SHIPMENT("SHP", "yyyyMMdd", 6, BusinessNumberResetCycle.DAILY, BusinessNumberScopeType.GLOBAL, "-", 100),

    /** - 재고 이동 번호 */
    STOCK_MOVE("STM", "yyyyMMdd", 6, BusinessNumberResetCycle.DAILY, BusinessNumberScopeType.GLOBAL, "-", 100);

    private final String defaultPrefix;
    private final String defaultDateFormat;
    private final Integer defaultSequenceWidth;
    private final BusinessNumberResetCycle defaultResetCycle;
    private final BusinessNumberScopeType defaultScopeType;
    private final String defaultSeparator;
    private final Integer defaultAllocationSize;

    BusinessNumberType(
            String defaultPrefix,
            String defaultDateFormat,
            Integer defaultSequenceWidth,
            BusinessNumberResetCycle defaultResetCycle,
            BusinessNumberScopeType defaultScopeType,
            String defaultSeparator,
            Integer defaultAllocationSize
    ) {
        this.defaultPrefix = defaultPrefix;
        this.defaultDateFormat = defaultDateFormat;
        this.defaultSequenceWidth = defaultSequenceWidth;
        this.defaultResetCycle = defaultResetCycle;
        this.defaultScopeType = defaultScopeType;
        this.defaultSeparator = defaultSeparator;
        this.defaultAllocationSize = defaultAllocationSize;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public String getDefaultDateFormat() {
        return defaultDateFormat;
    }

    public Integer getDefaultSequenceWidth() {
        return defaultSequenceWidth;
    }

    public BusinessNumberResetCycle getDefaultResetCycle() {
        return defaultResetCycle;
    }

    public BusinessNumberScopeType getDefaultScopeType() {
        return defaultScopeType;
    }

    public String getDefaultSeparator() {
        return defaultSeparator;
    }

    public Integer getDefaultAllocationSize() {
        return defaultAllocationSize;
    }
}