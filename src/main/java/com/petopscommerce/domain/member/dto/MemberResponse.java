package com.petopscommerce.domain.member.dto;

import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.domain.member.entity.MemberStatus;

import java.time.LocalDateTime;

/**
 * 회원 응답 DTO입니다.
 * 보안상 passwordHash는 응답에 포함하지 않습니다.
 */
public record MemberResponse(
        Long id,
        String email,
        String name,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt
) {

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