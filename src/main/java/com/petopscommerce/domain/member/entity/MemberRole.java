package com.petopscommerce.domain.member.entity;

/**
 * - 회원 권한
 * - 인증/인가 접근 범위 기준
 */
public enum MemberRole {
    /** - 일반 회원 */
    MEMBER,

    /** - 운영 담당자 */
    OPERATOR,

    /** - 관리자 */
    ADMIN
}