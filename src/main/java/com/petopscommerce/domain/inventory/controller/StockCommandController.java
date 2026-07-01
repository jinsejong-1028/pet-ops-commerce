package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.AdjustStockRequest;
import com.petopscommerce.domain.inventory.dto.ReceiveStockRequest;
import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.dto.TransferStockRequest;
import com.petopscommerce.domain.inventory.service.StockCommandService;
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
 * - 현재고 명령 API 진입점
 * - 입고성 현재고 생성/증가와 수동 조정 요청 처리
 */
@Tag(name = "Inventory", description = "창고, location, 현재고와 재고 작업 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/stocks")
public class StockCommandController {

    private final StockCommandService stockCommandService;

    /**
     * - 생성자 주입
     *
     * @param stockCommandService 현재고 명령 비즈니스 로직
     */
    public StockCommandController(StockCommandService stockCommandService) {
        this.stockCommandService = stockCommandService;
    }

    /**
     * - 입고성 현재고 생성/증가 API
     *
     * @param request 입고성 현재고 생성 요청
     * @return 현재고 응답
     */
    @Operation(summary = "입고성 현재고 생성 또는 증가", description = "상품, 창고, location, LOT 기준으로 현재고를 생성하거나 기존 현재고 수량을 증가시킵니다.")
    @PostMapping
    public ApiResponse<StockResponse> receiveStock(@Valid @RequestBody ReceiveStockRequest request) {
        return ApiResponse.ok(stockCommandService.receiveStock(request));
    }

    /**
     * - location 간 가용 재고 이동 API
     *
     * @param request 재고 이동 요청
     * @return 도착 location 현재고 응답
     */
    @Operation(summary = "Location 간 재고 이동", description = "출발 현재고의 가용수량을 도착 location으로 이동합니다.")
    @PostMapping("/transfer")
    public ApiResponse<StockResponse> transferStock(@Valid @RequestBody TransferStockRequest request) {
        return ApiResponse.ok(stockCommandService.transferStock(request));
    }

    /**
     * - 수동 재고 조정 API
     *
     * @param request 수동 재고 조정 요청
     * @return 현재고 응답
     */
    @Operation(summary = "수동 재고 조정", description = "현재고 수량을 운영자가 직접 증가 또는 차감합니다.")
    @PostMapping("/adjust")
    public ApiResponse<StockResponse> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        return ApiResponse.ok(stockCommandService.adjustStock(request));
    }
}