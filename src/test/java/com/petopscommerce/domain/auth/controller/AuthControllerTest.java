package com.petopscommerce.domain.auth.controller;

import com.petopscommerce.domain.auth.dto.LoginRequest;
import com.petopscommerce.domain.auth.dto.LoginResponse;
import com.petopscommerce.domain.auth.service.AuthService;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.global.config.SecurityConfig;
import com.petopscommerce.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - 인증 Controller 테스트
 * - 로그인 API 공통 응답 검증
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    /**
     * - 로그인 성공 응답 검증
     */
    @Test
    @DisplayName("로그인 API는 access token 공통 응답을 반환한다")
    void login() throws Exception {
        LoginResponse response = LoginResponse.bearer(
                "access-token",
                3600L,
                1L,
                "user@example.com",
                MemberRole.MEMBER
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.memberId").value(1L))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    /**
     * - validation 실패 응답 검증
     */
    @Test
    @DisplayName("로그인 입력값 검증 실패 시 400 공통 실패 응답을 반환한다")
    void loginValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"invalid-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").isString());
    }
}