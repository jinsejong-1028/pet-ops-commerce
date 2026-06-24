package com.petopscommerce.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * - 로그인 요청 DTO
 *
 * @param email 로그인 이메일
 * @param password 원본 비밀번호
 */
public record LoginRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "password is required")
        String password
) {
}