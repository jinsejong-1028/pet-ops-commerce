package com.petopscommerce.domain.member.dto;

import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.domain.member.entity.MemberStatus;

import java.time.LocalDateTime;

/**
 * - 회원 응답 DTO
 * - passwordHash 응답 제외
 *
 * @param id 회원 ID
 * @param email 로그인 이메일
 * @param name 회원 이름
 * @param role 회원 권한
 * @param status 회원 상태
 * @param createdAt 생성 일시
 */
public record MemberResponse(
        Long id,
        String email,
        String name,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param member 회원 Entity
     * @return 회원 응답 DTO
     */
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}