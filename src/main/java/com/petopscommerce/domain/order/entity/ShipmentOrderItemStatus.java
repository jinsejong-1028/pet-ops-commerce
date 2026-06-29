package com.petopscommerce.domain.order.entity;

/**
 * - 출고 주문 품목 상태
 * - 품목별 할당/피킹/출고 진행 상태 관리
 */
public enum ShipmentOrderItemStatus {
    /** - 출고 품목 생성 */
    CREATED,

    /** - 출고 품목 할당 완료 */
    ALLOCATED,

    /** - 출고 품목 피킹 완료 */
    PICKED,

    /** - 출고 품목 출고 확정 */
    SHIPPED,

    /** - 출고 품목 취소 */
    CANCELED
}