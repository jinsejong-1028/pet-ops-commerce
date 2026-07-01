package com.petopscommerce.domain.order.dto;

import com.petopscommerce.domain.order.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * - 주문 상품 응답 DTO
 *
 * @param id 주문 상품 ID
 * @param productId 상품 ID
 * @param quantity 주문 수량
 * @param unitPrice 주문 당시 상품 단가
 * @param lineAmount 주문 라인 금액
 */
@Schema(description = "주문 상품 응답")
public record OrderItemResponse(
        @Schema(description = "주문 상품 ID", example = "1")
        Long id,

        @Schema(description = "상품 ID", example = "1")
        Long productId,

        @Schema(description = "주문 수량", example = "2")
        Integer quantity,

        @Schema(description = "주문 당시 상품 단가", example = "32000")
        Integer unitPrice,

        @Schema(description = "주문 라인 금액", example = "64000")
        Integer lineAmount
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param orderItem 주문 상품 Entity
     * @return 주문 상품 응답 DTO
     */
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getLineAmount()
        );
    }
}