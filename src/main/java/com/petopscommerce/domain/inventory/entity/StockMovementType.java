package com.petopscommerce.domain.inventory.entity;

/**
 * - 재고 이동 유형
 * - stock_movements 원장의 수량 증감 사유를 구분
 */
public enum StockMovementType {
    /** - 주문 출고를 위한 재고 할당 */
    ALLOCATE,
    /** - 보관 location에서 PICKTO로 빠지는 이동 */
    PICK_OUT,
    /** - PICKTO location으로 들어오는 이동 */
    PICK_IN,
    /** - PICKTO에서 실제 출고되는 차감 */
    SHIP_OUT,
    /** - 재고 조정 증가 */
    ADJUST_IN,
    /** - 재고 조정 차감 */
    ADJUST_OUT,
    /** - location 이동 출발 */
    TRANSFER_OUT,
    /** - location 이동 도착 */
    TRANSFER_IN,
    /** - 입고 증가 */
    RECEIVE_IN,
    /** - 입고 취소 차감 */
    RECEIVE_CANCEL_OUT
}
