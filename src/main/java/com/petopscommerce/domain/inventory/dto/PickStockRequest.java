package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "재고 PICK 요청")
public record PickStockRequest(
        @Schema(description = "재고 작업 ID", example = "1")
        @NotNull Long jobId,

        @Schema(description = "PICKTO Location ID", example = "2")
        @NotNull Long picktoLocationId,

        @Schema(description = "PICK 수량", example = "2")
        @NotNull @Positive Integer quantity,

        @Schema(description = "작업 사유", example = "pick allocated stock")
        @Size(max = 500) String reason
) {
}