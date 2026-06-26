package com.petopscommerce.domain.inventory.dto;

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
public record AllocateStockRequest(
        @NotNull Long stockId,
        @NotNull Long orderId,
        @NotNull Long orderItemId,
        @NotNull @Positive Integer quantity,
        @Size(max = 500) String reason
) {
}
