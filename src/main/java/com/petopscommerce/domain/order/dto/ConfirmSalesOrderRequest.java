package com.petopscommerce.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * - 판매 주문 확정 요청 DTO
 * - 판매 주문 확정 시 출고 주문을 생성할 창고와 예정일을 전달
 *
 * @param warehouseId 출고 창고 ID
 * @param scheduledShipDate 출고 예정일
 * @param reason 확정 사유
 */
public record ConfirmSalesOrderRequest(
        @NotNull Long warehouseId,
        LocalDate scheduledShipDate,
        @Size(max = 500) String reason
) {
}