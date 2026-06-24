package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.service.StockService;
import com.petopscommerce.global.response.ApiResponse;
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
    @GetMapping
    public ApiResponse<List<StockResponse>> getStocks(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long locationId
    ) {
        return ApiResponse.ok(stockService.getStocks(productId, warehouseId, locationId));
    }

    /**
     * - 현재고 단건 조회
     *
     * @param stockId 현재고 ID
     * @return 현재고 단건 공통 응답
     */
    @GetMapping("/{stockId}")
    public ApiResponse<StockResponse> getStock(@PathVariable Long stockId) {
        return ApiResponse.ok(stockService.getStock(stockId));
    }
}
