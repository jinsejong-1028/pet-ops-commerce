package com.petopscommerce.domain.inventory.entity;

/**
 * - Location 유형
 * - 보관 위치와 피킹 후 출고 대기 위치를 구분
 */
public enum LocationType {
    /** - 대기장 location */
    STAGE,

    /** - 일반 보관 location */
    NOMAL,

    /** - 피킹 완료 후 출고 전 대기 location */
    PICKTO
}
