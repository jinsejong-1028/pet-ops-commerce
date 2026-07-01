package com.petopscommerce.domain.inventory.service.operation;

import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;

/**
 * - 재고 수량 변경 command
 * - stockId가 아니라 상품/창고/location/LOT key로 재고 모듈이 현재고를 찾도록 표현
 *
 * @param job 재고 작업 헤더
 * @param productId 상품 ID
 * @param warehouseId 창고 ID
 * @param fromLocationId 차감 기준 location ID
 * @param toLocationId 증가 기준 location ID
 * @param fromLotId 차감 기준 LOT ID
 * @param toLotId 증가 기준 LOT ID
 * @param quantity 처리 수량, 항상 양수
 * @param sourceBucket 차감 bucket
 * @param targetBucket 증가 bucket
 * @param sourceMovementType source 기준 원장 유형
 * @param targetMovementType target 기준 원장 유형
 * @param reason 작업 사유
 */
public record StockOperationCommand(
        StockJob job,
        Long productId,
        Long warehouseId,
        Long fromLocationId,
        Long toLocationId,
        Long fromLotId,
        Long toLotId,
        Integer quantity,
        StockQuantityBucket sourceBucket,
        StockQuantityBucket targetBucket,
        StockMovementType sourceMovementType,
        StockMovementType targetMovementType,
        String reason
) {

    /**
     * - 현재고 증가 command
     * - 입고나 조정 증가처럼 target 현재고를 찾거나 생성할 때 사용
     */
    public static StockOperationCommand increase(StockJob job, Long productId, Long warehouseId, Long locationId, Long lotId, StockQuantityBucket targetBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                productId,
                warehouseId,
                null,
                locationId,
                null,
                lotId,
                quantity,
                null,
                targetBucket,
                null,
                movementType,
                reason
        );
    }

    /**
     * - 현재고 차감 command
     * - 조정 차감이나 출고처럼 target이 없는 작업에 사용
     */
    public static StockOperationCommand decrease(StockJob job, Long productId, Long warehouseId, Long locationId, Long lotId, StockQuantityBucket sourceBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                productId,
                warehouseId,
                locationId,
                null,
                lotId,
                null,
                quantity,
                sourceBucket,
                null,
                movementType,
                null,
                reason
        );
    }

    /**
     * - source 차감 후 target 증가 command
     * - 일반 이동, PICK, LOT 변경처럼 from/to가 모두 있는 작업에 사용
     */
    public static StockOperationCommand move(StockJob job, Long productId, Long warehouseId, Long fromLocationId, Long toLocationId, Long fromLotId, Long toLotId, StockQuantityBucket sourceBucket, StockQuantityBucket targetBucket, Integer quantity, StockMovementType outMovementType, StockMovementType inMovementType, String reason) {
        return new StockOperationCommand(
                job,
                productId,
                warehouseId,
                fromLocationId,
                toLocationId,
                fromLotId,
                toLotId,
                quantity,
                sourceBucket,
                targetBucket,
                outMovementType,
                inMovementType,
                reason
        );
    }

    /**
     * - 같은 현재고 내부 bucket 전환 command
     * - 할당처럼 AVAILABLE을 WORKING으로 전환할 때 사용
     */
    public static StockOperationCommand convertBucket(StockJob job, Long productId, Long warehouseId, Long locationId, Long lotId, StockQuantityBucket sourceBucket, StockQuantityBucket targetBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                productId,
                warehouseId,
                locationId,
                locationId,
                lotId,
                lotId,
                quantity,
                sourceBucket,
                targetBucket,
                movementType,
                null,
                reason
        );
    }
}
