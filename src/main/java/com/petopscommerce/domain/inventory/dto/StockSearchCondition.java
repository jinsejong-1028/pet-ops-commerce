package com.petopscommerce.domain.inventory.dto;

/**
 * - 현재고 검색 조건 DTO
 * - QueryDSL 동적 조건 조합에 사용
 *
 * @param productId 상품 ID
 * @param warehouseId 창고 ID
 * @param locationId location ID
 * @param includeZero 0수량 현재고 포함 여부
 */
public record StockSearchCondition(
        Long productId,
        Long warehouseId,
        Long locationId,
        Boolean includeZero
) {
}