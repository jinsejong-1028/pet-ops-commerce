package com.petopscommerce.domain.inventory.service.operation;

/**
 * - 재고 수량 작업 유형
 * - 외부 업무가 StockOperationService에 전달하는 수량 변경 의도
 */
public enum StockOperationType {
    /** - 입고/초기 재고 생성 */
    RECEIVE,
    /** - 같은 stock 내부에서 가용수량을 작업수량으로 전환 */
    ALLOCATE,
    /** - location 간 가용 재고 이동 */
    TRANSFER,
    /** - 보관 location 작업수량을 PICKTO 작업수량으로 이동 */
    PICK,
    /** - PICKTO 작업수량 출고 차감 */
    SHIP,
    /** - 수동 재고 조정 */
    ADJUST,
    /** - 같은 location 안에서 LOT 속성 변경 */
    LOT_CHANGE
}