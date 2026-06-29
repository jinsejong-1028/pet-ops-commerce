package com.petopscommerce.domain.order.entity;

/**
 * - 판매 주문 품목 상태
 * - 판매 주문 확정 이후 품목 단위 진행 상태 관리
 */
public enum SalesOrderItemStatus {
    /** - 판매 주문 품목 생성 */
    CREATED,

    /** - 판매 주문 품목 확정 */
    CONFIRMED,

    /** - 판매 주문 품목 취소 */
    CANCELED
}