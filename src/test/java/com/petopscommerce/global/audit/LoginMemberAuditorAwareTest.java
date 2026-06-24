package com.petopscommerce.global.audit;

import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.global.security.LoginMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * - 로그인 사용자 audit 제공자 테스트
 * - SecurityContext 기반 사용자 ID 추출 검증
 */
class LoginMemberAuditorAwareTest {

    private final LoginMemberAuditorAware auditorAware = new LoginMemberAuditorAware();

    /**
     * - 테스트 후 SecurityContext 정리
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * - 로그인 사용자 ID 반환 검증
     */
    @Test
    @DisplayName("LoginMember 인증 정보가 있으면 회원 ID를 audit 값으로 반환한다")
    void getCurrentAuditorWithLoginMember() {
        LoginMember loginMember = new LoginMember(1L, "admin@example.com", MemberRole.ADMIN);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginMember,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).contains(1L);
    }

    /**
     * - 비로그인 상태 빈 값 검증
     */
    @Test
    @DisplayName("인증 정보가 없으면 audit 값을 반환하지 않는다")
    void getCurrentAuditorWithoutAuthentication() {
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).isEmpty();
    }
}

