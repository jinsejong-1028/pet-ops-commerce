package com.petopscommerce.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * - 재고 PICK 요청 DTO
 *
 * @param jobId 재고 작업 ID
 * @param picktoLocationId PICKTO location ID
 * @param quantity PICK 수량
 * @param reason 작업 사유
 */
public record PickStockRequest(
        @NotNull Long jobId,
        @NotNull Long picktoLocationId,
        @NotNull @Positive Integer quantity,
        @Size(max = 500) String reason
) {
}
