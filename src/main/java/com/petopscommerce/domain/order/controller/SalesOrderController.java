package com.petopscommerce.domain.order.controller;

import com.petopscommerce.domain.order.dto.SalesOrderResponse;
import com.petopscommerce.domain.order.dto.UpdateSalesOrderRequest;
import com.petopscommerce.domain.order.service.SalesOrderService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 관리자 판매 주문 API 진입점
 * - 판매 주문 수정/확정/취소 요청 처리
 */
@Tag(name = "Sales Order", description = "관리자 판매 주문 창고 지정, 확정, 취소 API")
@SecurityRequirement(name = "bearerAuth")
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
     * - 판매 주문 수정
     *
     * @param salesOrderId 판매 주문 ID
     * @param request 판매 주문 수정 요청
     * @return 수정된 판매 주문 응답
     */
    @Operation(summary = "판매 주문 출고 창고 지정", description = "판매 주문 확정 전에 출고에 사용할 창고를 지정합니다.")
    @PatchMapping("/{salesOrderId}")
    public ApiResponse<SalesOrderResponse> updateSalesOrder(
            @Parameter(description = "판매 주문 ID", example = "1") @PathVariable Long salesOrderId,
            @Valid @RequestBody UpdateSalesOrderRequest request
    ) {
        return ApiResponse.ok(salesOrderService.updateSalesOrder(salesOrderId, request));
    }

    /**
     * - 판매 주문 확정
     *
     * @param salesOrderId 판매 주문 ID
     * @return 확정된 판매 주문과 생성된 출고 주문 응답
     */
    @Operation(summary = "판매 주문 확정", description = "판매 주문과 고객 주문을 확정하고 지정된 창고 기준으로 출고 주문을 생성합니다.")
    @PostMapping("/{salesOrderId}/confirm")
    public ApiResponse<SalesOrderResponse> confirmSalesOrder(
            @Parameter(description = "판매 주문 ID", example = "1") @PathVariable Long salesOrderId
    ) {
        return ApiResponse.ok(salesOrderService.confirmSalesOrder(salesOrderId));
    }

    /**
     * - 판매 주문 취소
     *
     * @param salesOrderId 판매 주문 ID
     * @return 취소된 판매 주문 응답
     */
    @Operation(summary = "판매 주문 취소", description = "판매 주문, 고객 주문, 관련 품목 상태를 취소 처리합니다.")
    @PostMapping("/{salesOrderId}/cancel")
    public ApiResponse<SalesOrderResponse> cancelSalesOrder(
            @Parameter(description = "판매 주문 ID", example = "1") @PathVariable Long salesOrderId
    ) {
        return ApiResponse.ok(salesOrderService.cancelSalesOrder(salesOrderId));
    }
}