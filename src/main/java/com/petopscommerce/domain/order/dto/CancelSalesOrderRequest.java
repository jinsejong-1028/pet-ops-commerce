package com.petopscommerce.domain.order.dto;

import jakarta.validation.constraints.Size;

/**
 * - 판매 주문 취소 요청 DTO
 * - 판매 주문과 원천 고객 주문을 함께 취소할 때 사용
 *
 * @param reason 취소 사유
 */
public record CancelSalesOrderRequest(
        @Size(max = 500) String reason
) {
}