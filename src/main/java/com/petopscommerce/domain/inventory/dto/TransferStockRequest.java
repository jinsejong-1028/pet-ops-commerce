package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Location 간 가용 재고 이동 요청")
public record TransferStockRequest(
        @Schema(description = "출발 현재고 ID", example = "1")
        @NotNull
        Long fromStockId,

        @Schema(description = "도착 Location ID", example = "2")
        @NotNull
        Long toLocationId,

        @Schema(description = "이동 수량", example = "10")
        @NotNull
        @Positive
        Integer quantity,

        @Schema(description = "이동 사유", example = "move to picking zone")
        @Size(max = 500)
        String reason
) {
}