package com.petopscommerce.domain.auth.dto;

import com.petopscommerce.domain.member.entity.MemberRole;

/**
 * - 로그인 응답 DTO
 * - accessToken은 이후 인증 요청에 사용
 *
 * @param accessToken JWT access token
 * @param tokenType 인증 헤더 타입
 * @param expiresIn 만료 시간 초
 * @param memberId 로그인 회원 ID
 * @param email 로그인 이메일
 * @param role 회원 권한
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long memberId,
        String email,
        MemberRole role
) {

    /**
     * - Bearer 토큰 응답 생성
     *
     * @param accessToken JWT access token
     * @param expiresIn 만료 시간 초
     * @param memberId 로그인 회원 ID
     * @param email 로그인 이메일
     * @param role 회원 권한
     * @return 로그인 응답
     */
    public static LoginResponse bearer(String accessToken, long expiresIn, Long memberId, String email, MemberRole role) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, memberId, email, role);
    }
}