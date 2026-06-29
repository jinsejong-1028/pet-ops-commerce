package com.petopscommerce.domain.inventory.entity;

/**
 * - 재고 작업 상태
 * - 작업 헤더의 현재 진행 또는 최종 상태 관리
 */
public enum StockJobStatus {
    /** - 작업 생성 */
    CREATED,
    /** - 재고 할당 완료 */
    ALLOCATED,
    /** - PICKTO 이동 완료 */
    PICKED,
    /** - 출고 완료 */
    SHIPPED,
    /** - 입고성 재고 반영 완료 */
    RECEIVED,
    /** - 수동 재고 조정 완료 */
    ADJUSTED,
    /** - location 재고 이동 완료 */
    TRANSFERRED,
    /** - 작업 취소 */
    CANCELED
}