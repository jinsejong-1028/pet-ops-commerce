package com.petopscommerce.domain.auth.service;

import com.petopscommerce.domain.auth.dto.LoginRequest;
import com.petopscommerce.domain.auth.dto.LoginResponse;
import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.domain.member.entity.MemberStatus;
import com.petopscommerce.domain.member.repository.MemberRepository;
import com.petopscommerce.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * - 인증 Service 테스트
 * - 로그인 검증과 토큰 발급 흐름 확인
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    /**
     * - 로그인 성공 검증
     */
    @Test
    @DisplayName("로그인 성공 시 access token 응답을 반환한다")
    void login() {
        Member member = Member.create("user@example.com", "hashed-password", "홍길동");
        ReflectionTestUtils.setField(member, "id", 1L);

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(member)).thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpiresIn()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("user@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.role()).isEqualTo(MemberRole.MEMBER);
    }

    /**
     * - 없는 이메일 실패 검증
     */
    @Test
    @DisplayName("이메일이 없으면 401 예외를 반환한다")
    void loginWithMissingEmail() {
        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "password123")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * - 비밀번호 불일치 실패 검증
     */
    @Test
    @DisplayName("비밀번호가 틀리면 401 예외를 반환한다")
    void loginWithWrongPassword() {
        Member member = Member.create("user@example.com", "hashed-password", "홍길동");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong-password")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * - 비활성 회원 실패 검증
     */
    @Test
    @DisplayName("활성 상태가 아니면 403 예외를 반환한다")
    void loginWithInactiveMember() {
        Member member = Member.create("user@example.com", "hashed-password", "홍길동");
        ReflectionTestUtils.setField(member, "status", MemberStatus.SUSPENDED);

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "password123")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}