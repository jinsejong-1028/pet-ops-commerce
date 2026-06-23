package com.petopscommerce.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * - 회원 생성 요청 DTO
 * - 원본 비밀번호 요청 전용 객체
 *
 * @param email 로그인 이메일
 * @param password 원본 비밀번호
 * @param name 회원 이름
 */
public record CreateMemberRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 255, message = "email must be 255 characters or less")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be 100 characters or less")
        String name
) {
}