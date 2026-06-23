package com.petopscommerce.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원 생성 요청 DTO입니다.
 * Controller 밖으로 원본 비밀번호가 새지 않도록 요청 전용 객체로 분리합니다.
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