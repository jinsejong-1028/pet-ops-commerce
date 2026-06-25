package com.petopscommerce.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * - 주문 생성 요청 DTO
 *
 * @param items 주문 상품 목록
 */
public record CreateOrderRequest(
        @NotEmpty(message = "items is required")
        List<@Valid CreateOrderItemRequest> items
) {
}
