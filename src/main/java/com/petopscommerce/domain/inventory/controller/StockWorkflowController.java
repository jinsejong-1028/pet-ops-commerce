package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.AllocateStockRequest;
import com.petopscommerce.domain.inventory.dto.OutboundStockRequest;
import com.petopscommerce.domain.inventory.dto.PickStockRequest;
import com.petopscommerce.domain.inventory.dto.StockJobResponse;
import com.petopscommerce.domain.inventory.service.StockWorkflowService;
import com.petopscommerce.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 재고 작업 API 진입점
 * - 할당, PICKTO 이동, 출고 요청 처리
 */
@RestController
@RequestMapping("/api/v1/admin/stocks")
public class StockWorkflowController {

    private final StockWorkflowService stockWorkflowService;

    /**
     * - 생성자 주입
     *
     * @param stockWorkflowService 재고 작업 비즈니스 로직
     */
    public StockWorkflowController(StockWorkflowService stockWorkflowService) {
        this.stockWorkflowService = stockWorkflowService;
    }

    /**
     * - 재고 할당 API
     *
     * @param request 재고 할당 요청
     * @return 재고 작업 응답
     */
    @PostMapping("/allocate")
    public ApiResponse<StockJobResponse> allocate(@Valid @RequestBody AllocateStockRequest request) {
        return ApiResponse.ok(stockWorkflowService.allocate(request));
    }

    /**
     * - 재고 PICK API
     *
     * @param request 재고 PICK 요청
     * @return 재고 작업 응답
     */
    @PostMapping("/pick")
    public ApiResponse<StockJobResponse> pick(@Valid @RequestBody PickStockRequest request) {
        return ApiResponse.ok(stockWorkflowService.pick(request));
    }

    /**
     * - 재고 출고 API
     *
     * @param request 재고 출고 요청
     * @return 재고 작업 응답
     */
    @PostMapping("/outbound")
    public ApiResponse<StockJobResponse> outbound(@Valid @RequestBody OutboundStockRequest request) {
        return ApiResponse.ok(stockWorkflowService.outbound(request));
    }
}
