package com.petopscommerce.domain.auth.controller;

import com.petopscommerce.domain.auth.dto.LoginRequest;
import com.petopscommerce.domain.auth.dto.LoginResponse;
import com.petopscommerce.domain.auth.service.AuthService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 인증 API 진입점
 * - 로그인 요청 처리
 */
@Tag(name = "Auth", description = "로그인과 JWT 발급 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * - 생성자 주입
     *
     * @param authService 인증 비즈니스 로직
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * - 로그인
     * - 성공 시 JWT access token 반환
     *
     * @param request 로그인 요청
     *                - email: 로그인 이메일
     *                - password: 원본 비밀번호
     * @return 로그인 공통 응답
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT access token을 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}