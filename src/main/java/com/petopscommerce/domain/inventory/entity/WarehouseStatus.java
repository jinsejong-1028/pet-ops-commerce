package com.petopscommerce.domain.inventory.entity;

/**
 * - 창고 상태
 * - 재고 운영에 사용할 수 있는 창고인지 구분
 */
public enum WarehouseStatus {
    /** - 사용 가능 창고 */
    ACTIVE,

    /** - 사용 중지 창고 */
    INACTIVE
}
