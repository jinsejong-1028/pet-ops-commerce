package com.petopscommerce.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * - OpenAPI 문서 설정
 * - Swagger UI에서 API 정보와 JWT 인증 스키마를 제공합니다.
 */
@Configuration
public class OpenApiConfig {

    /**
     * - PetOps Commerce API 문서 기본 정보
     * - Bearer JWT 인증 스키마를 Swagger UI Authorize 버튼에 연결합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI petOpsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PetOps Commerce API")
                        .version("0.0.1")
                        .description("PetOps Commerce 포트폴리오 API 명세"))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}