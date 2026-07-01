package com.petopscommerce.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * - 주문 생성 요청 DTO
 *
 * @param items 주문 상품 목록
 */
@Schema(description = "주문 생성 요청")
public record CreateOrderRequest(
        @Schema(description = "주문 상품 목록")
        @NotEmpty(message = "items is required")
        List<@Valid CreateOrderItemRequest> items
) {
}