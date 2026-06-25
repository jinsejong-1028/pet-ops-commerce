package com.petopscommerce.global.businessnumber.entity;

/**
 * - 업무 번호 채번 범위
 * - 전역/회원별/창고별 번호판 분리 기준
 */
public enum BusinessNumberScopeType {
    /** - 전체 공유 번호 */
    GLOBAL,

    /** - 회원별 번호 */
    MEMBER,

    /** - 창고별 번호 */
    WAREHOUSE
}
