package com.petopscommerce.domain.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "서비스 상태 응답")
public record HealthResponse(
        @Schema(description = "서비스 상태", example = "UP")
        String status,

        @Schema(description = "애플리케이션 이름", example = "pet-ops-commerce")
        String application
) {
}