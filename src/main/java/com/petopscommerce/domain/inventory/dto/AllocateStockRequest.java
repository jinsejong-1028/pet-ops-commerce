package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * - 재고 할당 요청 DTO
 *
 * @param stockId 할당 대상 현재고 ID
 * @param orderId 판매 주문 ID
 * @param orderItemId 주문 상품 ID
 * @param quantity 할당 수량
 * @param reason 작업 사유
 */
@Schema(description = "재고 할당 요청")
public record AllocateStockRequest(
        @Schema(description = "할당 대상 현재고 ID", example = "1")
        @NotNull Long stockId,

        @Schema(description = "판매 주문 ID", example = "1")
        @NotNull Long orderId,

        @Schema(description = "주문 상품 ID", example = "1")
        @NotNull Long orderItemId,

        @Schema(description = "할당 수량", example = "2")
        @NotNull @Positive Integer quantity,

        @Schema(description = "작업 사유", example = "allocate for shipment")
        @Size(max = 500) String reason
) {
}