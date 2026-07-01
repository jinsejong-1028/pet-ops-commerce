package com.petopscommerce.domain.inventory.service.operation;

import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;

/**
 * - 재고 수량 변경 command
 * - from/to 위치와 수량 bucket을 하나의 입력 모델로 표현
 *
 * @param job 재고 작업 헤더
 * @param operationType 재고 작업 유형
 * @param fromStockId 출발 현재고 ID
 * @param productId 신규 생성성 작업의 상품 ID
 * @param warehouseId 신규 생성성 작업의 창고 ID
 * @param toLocationId 도착 location ID
 * @param toLotId 도착 LOT ID
 * @param quantity 처리 수량, ADJUST만 부호 허용
 * @param fromBucket 출발 수량 bucket
 * @param toBucket 도착 수량 bucket
 * @param reason 작업 사유
 */
public record StockOperationCommand(
        StockJob job,
        StockOperationType operationType,
        Long fromStockId,
        Long productId,
        Long warehouseId,
        Long toLocationId,
        Long toLotId,
        Integer quantity,
        StockQuantityBucket fromBucket,
        StockQuantityBucket toBucket,
        String reason
) {

    public static StockOperationCommand receive(StockJob job, Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.RECEIVE, null, productId, warehouseId, locationId, lotId, quantity, null, StockQuantityBucket.AVAILABLE, reason);
    }

    public static StockOperationCommand allocate(StockJob job, Long stockId, Integer quantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.ALLOCATE, stockId, null, null, null, null, quantity, StockQuantityBucket.AVAILABLE, StockQuantityBucket.WORKING, reason);
    }

    public static StockOperationCommand transfer(StockJob job, Long fromStockId, Long toLocationId, Integer quantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.TRANSFER, fromStockId, null, null, toLocationId, null, quantity, StockQuantityBucket.AVAILABLE, StockQuantityBucket.AVAILABLE, reason);
    }

    public static StockOperationCommand pick(StockJob job, Long fromStockId, Long toLocationId, Integer quantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.PICK, fromStockId, null, null, toLocationId, null, quantity, StockQuantityBucket.WORKING, StockQuantityBucket.WORKING, reason);
    }

    public static StockOperationCommand ship(StockJob job, Long stockId, Integer quantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.SHIP, stockId, null, null, null, null, quantity, StockQuantityBucket.WORKING, null, reason);
    }

    public static StockOperationCommand adjust(StockJob job, Long stockId, Integer signedQuantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.ADJUST, stockId, null, null, null, null, signedQuantity, StockQuantityBucket.AVAILABLE, null, reason);
    }

    public static StockOperationCommand changeLot(StockJob job, Long fromStockId, Long toLotId, Integer quantity, String reason) {
        return new StockOperationCommand(job, StockOperationType.LOT_CHANGE, fromStockId, null, null, null, toLotId, quantity, StockQuantityBucket.AVAILABLE, StockQuantityBucket.AVAILABLE, reason);
    }
}