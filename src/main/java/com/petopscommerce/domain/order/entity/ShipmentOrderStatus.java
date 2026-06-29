package com.petopscommerce.domain.order.entity;

/**
 * - 출고 주문 상태
 * - 창고 출고 작업의 현재 단계를 관리
 */
public enum ShipmentOrderStatus {
    /** - 출고 지시 생성 */
    CREATED,

    /** - 재고 할당 완료 */
    ALLOCATED,

    /** - PICKTO location 피킹 완료 */
    PICKED,

    /** - 출고 확정 */
    SHIPPED,

    /** - 출고 취소 */
    CANCELED
}