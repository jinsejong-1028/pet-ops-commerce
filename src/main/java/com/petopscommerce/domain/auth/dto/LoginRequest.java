package com.petopscommerce.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * - 로그인 요청 DTO
 *
 * @param email 로그인 이메일
 * @param password 원본 비밀번호
 */
@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "로그인 이메일", example = "admin@petops.com")
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @Schema(description = "원본 비밀번호", example = "password1234")
        @NotBlank(message = "password is required")
        String password
) {
}