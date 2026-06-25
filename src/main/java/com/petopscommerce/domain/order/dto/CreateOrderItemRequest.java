package com.petopscommerce.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * - 주문 상품 생성 요청 DTO
 *
 * @param productId 상품 ID
 * @param quantity 주문 수량
 */
public record CreateOrderItemRequest(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be greater than or equal to 1")
        Integer quantity
) {
}
