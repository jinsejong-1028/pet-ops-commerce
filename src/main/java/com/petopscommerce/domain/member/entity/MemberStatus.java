package com.petopscommerce.domain.member.entity;

/**
 * 회원 상태는 탈퇴, 정지 같은 운영 제어를 위해 별도 enum으로 관리합니다.
 */
public enum MemberStatus {
    ACTIVE,
    SUSPENDED,
    WITHDRAWN
}