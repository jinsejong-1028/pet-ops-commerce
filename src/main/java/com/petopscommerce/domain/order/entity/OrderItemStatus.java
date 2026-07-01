package com.petopscommerce.domain.order.entity;

/**
 * - 고객 주문 품목 상태
 * - 고객 주문 header와 함께 품목 단위 진행 상태를 관리
 */
public enum OrderItemStatus {
    /** - 고객 주문 품목 생성 */
    CREATED,

    /** - 판매 주문 확정으로 고객 주문 품목도 확정 */
    CONFIRMED,

    /** - 판매 주문 취소로 고객 주문 품목도 취소 */
    CANCELED
}