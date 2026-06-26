package com.petopscommerce.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * - 재고 출고 요청 DTO
 *
 * @param jobId 재고 작업 ID
 * @param quantity 출고 수량
 * @param reason 작업 사유
 */
public record OutboundStockRequest(
        @NotNull Long jobId,
        @NotNull @Positive Integer quantity,
        @Size(max = 500) String reason
) {
}
