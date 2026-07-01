package com.petopscommerce.domain.inventory.entity;

/**
 * - 재고 작업 유형
 * - 작업 헤더가 어떤 업무 흐름인지 구분
 */
public enum StockJobType {
    /** - 판매 주문 출고 작업 */
    SALES_SHIPMENT,
    /** - 수동 재고 조정 작업 */
    ADJUSTMENT,
    /** - location 간 재고 이동 작업 */
    TRANSFER,
    /** - LOT 속성 변경 작업 */
    LOT_CHANGE,
    /** - 입고 작업 */
    INBOUND
}