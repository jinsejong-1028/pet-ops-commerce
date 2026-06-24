package com.petopscommerce.domain.inventory.entity;

/**
 * - LOT 상태
 * - 유효기간/운영 제한에 따른 재고 사용 가능 여부 구분
 */
public enum LotStatus {
    /** - 사용 가능 LOT */
    ACTIVE,

    /** - 유효기간 만료 LOT */
    EXPIRED,

    /** - 운영상 사용 제한 LOT */
    BLOCKED
}
