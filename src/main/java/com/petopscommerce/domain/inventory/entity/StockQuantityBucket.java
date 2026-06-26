package com.petopscommerce.domain.inventory.entity;

/**
 * - 현재고 수량 변경 대상
 * - 같은 stock row 안에서도 가용수량과 작업수량의 책임을 분리
 */
public enum StockQuantityBucket {
    /** - 일반 입고, 조정, 이동에 사용할 수 있는 가용수량 */
    AVAILABLE,

    /** - 할당, PICK, 출고 작업 중인 작업수량 */
    WORKING
}