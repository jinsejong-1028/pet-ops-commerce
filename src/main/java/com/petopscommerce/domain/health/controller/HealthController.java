package com.petopscommerce.domain.health.controller;

import com.petopscommerce.domain.health.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "서비스 상태 확인 API")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "Health check", description = "애플리케이션 기동 상태와 서비스명을 확인합니다.")
    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", "pet-ops-commerce");
    }
}