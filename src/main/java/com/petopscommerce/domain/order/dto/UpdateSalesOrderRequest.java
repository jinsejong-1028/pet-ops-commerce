package com.petopscommerce.domain.order.dto;

import jakarta.validation.constraints.NotNull;

/**
 * - 판매 주문 수정 요청 DTO
 * - 판매 주문 확정 전 출고 창고를 지정
 *
 * @param warehouseId 출고 창고 ID
 */
public record UpdateSalesOrderRequest(
        @NotNull(message = "warehouseId is required")
        Long warehouseId
) {
}