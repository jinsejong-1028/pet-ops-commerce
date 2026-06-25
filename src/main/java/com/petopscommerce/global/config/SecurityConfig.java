package com.petopscommerce.global.config;

import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.global.security.LoginMember;
import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * - Spring Security 설정
 * - 공개 API와 JWT 인증 흐름 정의
 */
@Configuration
public class SecurityConfig {

    /**
     * - HTTP 보안 필터 설정
     * - JWT Bearer token 인증 사용
     *
     * @param http Spring Security HTTP 설정 객체
     * @param jwtAuthenticationConverter JWT를 인증 객체로 변환하는 converter
     * @return Security filter chain
     * @throws Exception Security 설정 실패
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Service/Controller 예외가 /error 처리 과정에서 403으로 바뀌지 않도록 허용합니다.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        // TODO: Auth 기능 안정화 후 회원 조회는 /members/me 중심으로 잠급니다.
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/*").permitAll()
                        // 조회 API는 비로그인 사용자도 상품 탐색이 가능하도록 공개합니다.
                        .requestMatchers(HttpMethod.GET, "/api/v1/product-categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/*").permitAll()
                        // 운영/관리 API는 관리자 또는 운영 담당자만 접근합니다.
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/product-categories", "/api/v1/products").hasAnyRole("ADMIN", "OPERATOR")
                        // 그 외 API는 로그인 사용자만 접근합니다.
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    /**
     * - JWT 인증 객체 변환기
     * - JWT claim을 LoginMember principal로 변환
     *
     * @return JWT 인증 converter
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Long memberId = Long.valueOf(jwt.getSubject());
            String email = jwt.getClaimAsString("email");
            MemberRole role = MemberRole.valueOf(jwt.getClaimAsString("role"));
            LoginMember loginMember = new LoginMember(memberId, email, role);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

            return new UsernamePasswordAuthenticationToken(loginMember, jwt.getTokenValue(), authorities);
        };
    }

    /**
     * - JWT 서명 secret key
     * - HS256 대칭키 방식 사용
     *
     * @param secret 설정 파일 또는 환경변수 secret
     * @return JWT secret key
     */
    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * - JWT encoder
     * - 로그인 성공 시 토큰 서명에 사용
     *
     * @param secretKey JWT secret key
     * @return JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /**
     * - JWT decoder
     * - 요청 Bearer token 검증에 사용
     *
     * @param secretKey JWT secret key
     * @return JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * - 비밀번호 원문을 DB에 저장하지 않기 위해 BCrypt 기반 해시 도구를 Bean으로 등록합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
