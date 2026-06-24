package com.petopscommerce.domain.auth.service;

import com.petopscommerce.domain.auth.dto.LoginRequest;
import com.petopscommerce.domain.auth.dto.LoginResponse;
import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.entity.MemberStatus;
import com.petopscommerce.domain.member.repository.MemberRepository;
import com.petopscommerce.global.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 인증 Service
 * - 로그인 검증과 JWT 발급 담당
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * - 생성자 주입
     *
     * @param memberRepository 회원 DB 접근 객체
     * @param passwordEncoder 비밀번호 해시 검증 도구
     * @param jwtTokenProvider JWT 발급 도구
     */
    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * - 로그인
     * - 이메일/비밀번호 검증 후 access token 발급
     *
     * @param request 로그인 요청
     * @return 로그인 응답
     */
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid email or password"));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) { // 조건: 비밀번호 불일치 / 결과: 401 반환
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid email or password");
        }

        if (member.getStatus() != MemberStatus.ACTIVE) { // 조건: 활성 회원 아님 / 결과: 403 반환
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "member is not active");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);

        return LoginResponse.bearer(
                accessToken,
                jwtTokenProvider.getAccessTokenExpiresIn(),
                member.getId(),
                member.getEmail(),
                member.getRole()
        );
    }
}