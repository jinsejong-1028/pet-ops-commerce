package com.petopscommerce.domain.inventory.entity;

/**
 * - 재고 작업 참조 유형
 * - 주문, 입고, 수동 작업처럼 작업 발생 원천을 구분
 */
public enum StockReferenceType {
    /** - 판매 주문 */
    SALES_ORDER,
    /** - 입고 예정/확정 주문 */
    INBOUND_ORDER,
    /** - 수동 재고 조정 */
    ADJUSTMENT,
    /** - 수동 location 이동 */
    TRANSFER,
    /** - 별도 참조 없는 수동 작업 */
    MANUAL
}
