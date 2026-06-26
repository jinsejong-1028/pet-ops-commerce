package com.petopscommerce.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * - location 간 가용 재고 이동 요청 DTO
 *
 * @param fromStockId 출발 현재고 ID
 * @param toLocationId 도착 location ID
 * @param quantity 이동 수량
 * @param reason 이동 사유
 */
public record TransferStockRequest(
        @NotNull
        Long fromStockId,
        @NotNull
        Long toLocationId,
        @NotNull
        @Positive
        Integer quantity,
        @Size(max = 500)
        String reason
) {
}