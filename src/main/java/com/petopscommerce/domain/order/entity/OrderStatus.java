package com.petopscommerce.domain.order.entity;

/**
 * - 주문 상태
 * - 고객 주문 생성 이후 판매 확정/출고 흐름의 기준값
 */
public enum OrderStatus {
    /** - 고객 주문 생성 */
    CREATED,

    /** - 운영자가 판매 주문을 확정해 고객 주문도 확정됨 */
    CONFIRMED,

    /** - 결제 완료 */
    PAID,

    /** - 출고 처리 중 */
    SHIPPING,

    /** - 주문 완료 */
    COMPLETED,

    /** - 주문 취소 */
    CANCELED
}