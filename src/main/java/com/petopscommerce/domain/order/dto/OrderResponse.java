package com.petopscommerce.domain.order.dto;

import com.petopscommerce.domain.order.entity.Order;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.entity.OrderStatus;

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
public record OrderResponse(
        Long id,
        String orderNo,
        Long memberId,
        OrderStatus status,
        Integer totalAmount,
        Integer discountAmount,
        Integer paymentAmount,
        LocalDateTime orderedAt,
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
