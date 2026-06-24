package com.petopscommerce.global.audit;

import com.petopscommerce.global.security.LoginMember;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * - 로그인 사용자 audit 제공자
 * - SecurityContext에서 LoginMember ID 추출
 */
@Component
public class LoginMemberAuditorAware implements AuditorAware<Long> {

    /**
     * - 현재 audit 작업자 조회
     *
     * @return 로그인 회원 ID
     */
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) { // 인증 정보 없음
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginMember loginMember) { // JWT 인증 사용자
            return Optional.of(loginMember.id());
        }

        return Optional.empty();
    }
}
