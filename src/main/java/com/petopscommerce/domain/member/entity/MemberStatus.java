package com.petopscommerce.domain.member.entity;

/**
 * - 회원 상태
 * - 운영 제어 기준
 */
public enum MemberStatus {
    /** - 정상 회원 */
    ACTIVE,

    /** - 정지 회원 */
    SUSPENDED,

    /** - 탈퇴 회원 */
    WITHDRAWN
}