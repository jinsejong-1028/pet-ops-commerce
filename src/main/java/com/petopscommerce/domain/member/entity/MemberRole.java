package com.petopscommerce.domain.member.entity;

/**
 * 회원 권한은 인증/인가 기능에서 접근 범위를 판단하는 기준입니다.
 */
public enum MemberRole {
    MEMBER,
    OPERATOR,
    ADMIN
}