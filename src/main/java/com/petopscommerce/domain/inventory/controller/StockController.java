package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.service.StockService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * - 현재고 API 진입점
 * - location 단위 현재고 조회 요청 처리
 */
@Tag(name = "Inventory", description = "창고, location, 현재고와 재고 작업 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/stocks")
public class StockController {

    private final StockService stockService;

    /**
     * - 생성자 주입
     *
     * @param stockService 현재고 비즈니스 로직
     */
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * - 현재고 목록 조회
     * - productId, warehouseId, locationId 조건 선택 적용
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @return 현재고 목록 공통 응답
     */
    @Operation(summary = "현재고 목록 조회", description = "상품, 창고, location 조건으로 location 단위 현재고를 조회합니다.")
    @GetMapping
    public ApiResponse<List<StockResponse>> getStocks(
            @Parameter(description = "상품 ID", example = "1") @RequestParam(required = false) Long productId,
            @Parameter(description = "창고 ID", example = "1") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "Location ID", example = "1") @RequestParam(required = false) Long locationId
    ) {
        return ApiResponse.ok(stockService.getStocks(productId, warehouseId, locationId));
    }

    /**
     * - 현재고 단건 조회
     *
     * @param stockId 현재고 ID
     * @return 현재고 단건 공통 응답
     */
    @Operation(summary = "현재고 단건 조회", description = "현재고 ID로 총수량, 작업수량, 가용수량을 조회합니다.")
    @GetMapping("/{stockId}")
    public ApiResponse<StockResponse> getStock(
            @Parameter(description = "현재고 ID", example = "1") @PathVariable Long stockId
    ) {
        return ApiResponse.ok(stockService.getStock(stockId));
    }
}