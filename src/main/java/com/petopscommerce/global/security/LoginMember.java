package com.petopscommerce.global.security;

import com.petopscommerce.domain.member.entity.MemberRole;

/**
 * - 인증된 회원 Principal
 * - SecurityContext에 저장되는 현재 로그인 사용자 정보
 *
 * @param id 회원 ID
 * @param email 로그인 이메일
 * @param role 회원 권한
 */
public record LoginMember(
        Long id,
        String email,
        MemberRole role
) {
}