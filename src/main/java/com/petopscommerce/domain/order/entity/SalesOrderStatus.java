package com.petopscommerce.domain.order.entity;

/**
 * - 판매 주문 상태
 * - 고객 주문을 운영 주문으로 확정했는지 관리
 */
public enum SalesOrderStatus {
    /** - 생성됨 */
    CREATED,

    /** - 판매 주문 확정 */
    CONFIRMED,

    /** - 판매 주문 취소 */
    CANCELED
}