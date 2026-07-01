package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.Stock;
import io.swagger.v3.oas.annotations.media.Schema;

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
 * @param createdAt 생성 일시
 */
@Schema(description = "현재고 응답")
public record StockResponse(
        @Schema(description = "현재고 ID", example = "1")
        Long id,

        @Schema(description = "상품 ID", example = "1")
        Long productId,

        @Schema(description = "창고 ID", example = "1")
        Long warehouseId,

        @Schema(description = "Location ID", example = "1")
        Long locationId,

        @Schema(description = "LOT ID", example = "1")
        Long lotId,

        @Schema(description = "총수량", example = "100")
        Integer totalQuantity,

        @Schema(description = "작업수량", example = "3")
        Integer workingQuantity,

        @Schema(description = "가용수량", example = "97")
        Integer availableQuantity,

        @Schema(description = "생성 일시", example = "2026-07-01T10:00:00")
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
                stock.getCreatedAt()
        );
    }
}