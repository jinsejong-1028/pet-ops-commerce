package com.petopscommerce.domain.member.dto;

import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.domain.member.entity.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "회원 응답")
public record MemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "로그인 이메일", example = "member@petops.com")
        String email,

        @Schema(description = "회원 이름", example = "홍길동")
        String name,

        @Schema(description = "회원 권한", example = "MEMBER")
        MemberRole role,

        @Schema(description = "회원 상태", example = "ACTIVE")
        MemberStatus status,

        @Schema(description = "생성 일시", example = "2026-07-01T10:00:00")
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