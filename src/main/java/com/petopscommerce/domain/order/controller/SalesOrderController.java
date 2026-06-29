package com.petopscommerce.domain.order.controller;

import com.petopscommerce.domain.order.dto.CancelSalesOrderRequest;
import com.petopscommerce.domain.order.dto.ConfirmSalesOrderRequest;
import com.petopscommerce.domain.order.dto.SalesOrderResponse;
import com.petopscommerce.domain.order.service.SalesOrderService;
import com.petopscommerce.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 관리자 판매 주문 API 진입점
 * - 판매 주문 확정/취소 요청 처리
 */
@RestController
@RequestMapping("/api/v1/admin/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    /**
     * - 생성자 주입
     *
     * @param salesOrderService 판매 주문 비즈니스 로직
     */
    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    /**
     * - 판매 주문 확정
     *
     * @param salesOrderId 판매 주문 ID
     * @param request 판매 주문 확정 요청
     * @return 확정된 판매 주문과 생성된 출고 주문 응답
     */
    @PostMapping("/{salesOrderId}/confirm")
    public ApiResponse<SalesOrderResponse> confirmSalesOrder(
            @PathVariable Long salesOrderId,
            @Valid @RequestBody ConfirmSalesOrderRequest request
    ) {
        return ApiResponse.ok(salesOrderService.confirmSalesOrder(salesOrderId, request));
    }

    /**
     * - 판매 주문 취소
     *
     * @param salesOrderId 판매 주문 ID
     * @param request 판매 주문 취소 요청
     * @return 취소된 판매 주문 응답
     */
    @PostMapping("/{salesOrderId}/cancel")
    public ApiResponse<SalesOrderResponse> cancelSalesOrder(
            @PathVariable Long salesOrderId,
            @Valid @RequestBody CancelSalesOrderRequest request
    ) {
        return ApiResponse.ok(salesOrderService.cancelSalesOrder(salesOrderId, request));
    }
}