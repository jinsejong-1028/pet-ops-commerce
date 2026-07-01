package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.CreateWarehouseRequest;
import com.petopscommerce.domain.inventory.dto.WarehouseResponse;
import com.petopscommerce.domain.inventory.service.WarehouseService;
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
 * - 창고 API 진입점
 * - 관리자 창고 생성 요청 처리
 */
@Tag(name = "Inventory", description = "창고, location, 현재고와 재고 작업 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * - 생성자 주입
     *
     * @param warehouseService 창고 비즈니스 로직
     */
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    /**
     * - 창고 생성 API
     *
     * @param request 창고 생성 요청
     * @return 창고 응답
     */
    @Operation(summary = "창고 생성", description = "관리자가 재고를 보관할 창고를 생성합니다.")
    @PostMapping
    public ApiResponse<WarehouseResponse> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        return ApiResponse.ok(warehouseService.createWarehouse(request));
    }
}