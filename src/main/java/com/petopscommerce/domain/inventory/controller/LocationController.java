package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.CreateLocationRequest;
import com.petopscommerce.domain.inventory.dto.LocationResponse;
import com.petopscommerce.domain.inventory.service.LocationService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * - Location API 진입점
 * - 관리자 location 생성 요청 처리
 */
@Tag(name = "Inventory", description = "창고, location, 현재고와 재고 작업 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/locations")
public class LocationController {

    private final LocationService locationService;

    /**
     * - 생성자 주입
     *
     * @param locationService location 비즈니스 로직
     */
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * - Location 생성 API
     *
     * @param request location 생성 요청
     * @return location 응답
     */
    @Operation(summary = "Location 생성", description = "창고 안의 재고 보관 위치를 생성합니다.")
    @PostMapping
    public ApiResponse<LocationResponse> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        return ApiResponse.ok(locationService.createLocation(request));
    }
}