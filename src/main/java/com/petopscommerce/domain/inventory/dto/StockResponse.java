package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.Stock;

import java.time.LocalDateTime;

/**
 * - 현재고 응답 DTO
 *
 * @param id 현재고 ID
 * @param productId 상품 ID
 * @param warehouseId 창고 ID
 * @param locationId location ID
 * @param lotId LOT ID
 * @param totalQuantity 총수량
 * @param workingQuantity 작업수량
 * @param availableQuantity 가용수량
 * @param safetyQuantity 안전재고 수량
 * @param createdAt 생성 일시
 */
public record StockResponse(
        Long id,
        Long productId,
        Long warehouseId,
        Long locationId,
        Long lotId,
        Integer totalQuantity,
        Integer workingQuantity,
        Integer availableQuantity,
        Integer safetyQuantity,
        LocalDateTime createdAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param stock 현재고 Entity
     * @return 현재고 응답 DTO
     */
    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getProductId(),
                stock.getWarehouseId(),
                stock.getLocationId(),
                stock.getLotId(),
                stock.getTotalQuantity(),
                stock.getWorkingQuantity(),
                stock.getAvailableQuantity(),
                stock.getSafetyQuantity(),
                stock.getCreatedAt()
        );
    }
}
