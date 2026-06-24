package com.petopscommerce.global.security;

import com.petopscommerce.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * - JWT 발급 담당
 * - 로그인 성공 시 access token 생성
 */
@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenExpiresIn;

    /**
     * - 생성자 주입
     *
     * @param jwtEncoder JWT 서명 encoder
     * @param issuer 토큰 발급자
     * @param accessTokenExpiresIn access token 만료 초
     */
    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-expires-in}") long accessTokenExpiresIn
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    /**
     * - access token 생성
     * - subject에는 회원 ID 저장
     *
     * @param member 로그인 회원
     * @return JWT access token
     */
    public String createAccessToken(Member member) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpiresIn);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(member.getId().toString())
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * - access token 만료 초 반환
     *
     * @return 만료 시간 초
     */
    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }
}