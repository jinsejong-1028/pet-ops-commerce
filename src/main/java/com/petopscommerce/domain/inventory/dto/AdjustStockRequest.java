package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * - 수동 재고 조정 요청 DTO
 * - quantity 부호로 증가/차감을 구분
 *
 * @param stockId 현재고 ID
 * @param quantity 조정 수량, 양수는 증가/음수는 차감
 * @param reason 조정 사유
 */
@Schema(description = "수동 재고 조정 요청")
public record AdjustStockRequest(
        @Schema(description = "현재고 ID", example = "1")
        @NotNull
        Long stockId,

        @Schema(description = "조정 수량, 양수는 증가/음수는 차감", example = "-2")
        @NotNull
        Integer quantity,

        @Schema(description = "조정 사유", example = "inventory count correction")
        @Size(max = 500)
        String reason
) {
}