package com.petopscommerce.domain.order.dto;

import com.petopscommerce.domain.order.entity.OrderItem;

/**
 * - 주문 상품 응답 DTO
 *
 * @param id 주문 상품 ID
 * @param productId 상품 ID
 * @param quantity 주문 수량
 * @param unitPrice 주문 당시 상품 단가
 * @param lineAmount 주문 라인 금액
 */
public record OrderItemResponse(
        Long id,
        Long productId,
        Integer quantity,
        Integer unitPrice,
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
