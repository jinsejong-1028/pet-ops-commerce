package com.petopscommerce.domain.order.dto;

import com.petopscommerce.domain.order.entity.Order;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * - 주문 응답 DTO
 *
 * @param id 주문 ID
 * @param orderNo 주문 번호
 * @param memberId 주문 회원 ID
 * @param status 주문 상태
 * @param totalAmount 상품 합계 금액
 * @param discountAmount 할인 금액
 * @param paymentAmount 결제 대상 금액
 * @param orderedAt 주문 일시
 * @param items 주문 상품 목록
 */
@Schema(description = "주문 응답")
public record OrderResponse(
        @Schema(description = "주문 ID", example = "1")
        Long id,

        @Schema(description = "주문 번호", example = "CO-20260701-000001")
        String orderNo,

        @Schema(description = "주문 회원 ID", example = "1")
        Long memberId,

        @Schema(description = "주문 상태", example = "CREATED")
        OrderStatus status,

        @Schema(description = "상품 합계 금액", example = "64000")
        Integer totalAmount,

        @Schema(description = "할인 금액", example = "0")
        Integer discountAmount,

        @Schema(description = "결제 대상 금액", example = "64000")
        Integer paymentAmount,

        @Schema(description = "주문 일시", example = "2026-07-01T10:00:00")
        LocalDateTime orderedAt,

        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param order 주문 Entity
     * @param orderItems 주문 상품 Entity 목록
     * @return 주문 응답 DTO
     */
    public static OrderResponse from(Order order, List<OrderItem> orderItems) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getMemberId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getPaymentAmount(),
                order.getOrderedAt(),
                orderItems.stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}