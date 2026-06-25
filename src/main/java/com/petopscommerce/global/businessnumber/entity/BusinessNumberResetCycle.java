package com.petopscommerce.global.businessnumber.entity;

/**
 * - 업무 번호 초기화 주기
 * - sequence period 계산 기준
 */
public enum BusinessNumberResetCycle {
    /** - 초기화 없음 */
    NONE,

    /** - 일별 초기화 */
    DAILY,

    /** - 월별 초기화 */
    MONTHLY,

    /** - 연별 초기화 */
    YEARLY
}
