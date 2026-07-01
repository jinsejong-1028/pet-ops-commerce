package com.petopscommerce.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * - 주문 상품 생성 요청 DTO
 *
 * @param productId 상품 ID
 * @param quantity 주문 수량
 */
@Schema(description = "주문 상품 생성 요청")
public record CreateOrderItemRequest(
        @Schema(description = "상품 ID", example = "1")
        @NotNull(message = "productId is required")
        Long productId,

        @Schema(description = "주문 수량", example = "2")
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be greater than or equal to 1")
        Integer quantity
) {
}