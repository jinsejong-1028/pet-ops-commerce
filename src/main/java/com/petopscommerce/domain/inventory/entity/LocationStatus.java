package com.petopscommerce.domain.inventory.entity;

/**
 * - Location 상태
 * - 재고 보관/작업 위치 사용 가능 여부 구분
 */
public enum LocationStatus {
    /** - 사용 가능 location */
    ACTIVE,

    /** - 사용 중지 location */
    INACTIVE
}
