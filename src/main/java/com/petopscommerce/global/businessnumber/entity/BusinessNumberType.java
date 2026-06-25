package com.petopscommerce.global.businessnumber.entity;

/**
 * - 업무 번호 유형
 * - 규칙 테이블 code와 매칭
 */
public enum BusinessNumberType {
    /** - 주문 번호 */
    ORDER,

    /** - 결제 번호 */
    PAYMENT,

    /** - 출고 번호 */
    SHIPMENT,

    /** - 재고 이동 번호 */
    STOCK_MOVE
}
