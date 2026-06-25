package com.petopscommerce.domain.order.entity;

/**
 * - 주문 상태
 * - 주문 생성 이후 결제/출고 흐름의 기준값
 */
public enum OrderStatus {
    /** - 주문 생성 */
    CREATED,

    /** - 결제 완료 */
    PAID,

    /** - 출고 처리 중 */
    SHIPPING,

    /** - 주문 완료 */
    COMPLETED,

    /** - 주문 취소 */
    CANCELED
}
