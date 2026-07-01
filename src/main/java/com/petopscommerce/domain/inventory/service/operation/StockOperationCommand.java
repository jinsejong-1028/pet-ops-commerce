package com.petopscommerce.domain.inventory.service.operation;

import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;

/**
 * - 재고 수량 변경 command
 * - 업무명이 아니라 source/target stock과 AVAILABLE/WORKING bucket 증감으로 작업을 표현
 *
 * @param job 재고 작업 헤더
 * @param source 차감 대상
 * @param target 증가 대상
 * @param quantity 처리 수량, 항상 양수
 * @param sourceMovement source 기준 원장 기록 계획
 * @param targetMovement target 기준 원장 기록 계획
 * @param reason 작업 사유
 */
public record StockOperationCommand(
        StockJob job,
        StockOperationTarget source,
        StockOperationTarget target,
        Integer quantity,
        StockMovementPlan sourceMovement,
        StockMovementPlan targetMovement,
        String reason
) {

    /**
     * - 현재고 증가 command
     * - 입고처럼 stock key 기준으로 target 현재고를 찾거나 생성할 때 사용
     */
    public static StockOperationCommand increase(StockJob job, Long productId, Long warehouseId, Long locationId, Long lotId, StockQuantityBucket targetBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                null,
                StockOperationTarget.stockKey(productId, warehouseId, locationId, lotId, targetBucket),
                quantity,
                null,
                StockMovementPlan.positive(movementType),
                reason
        );
    }

    /**
     * - 기존 현재고 증가 command
     * - 수동 조정 증가처럼 stockId 기준으로 target 현재고를 찾을 때 사용
     */
    public static StockOperationCommand increaseExisting(StockJob job, Long stockId, StockQuantityBucket targetBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                null,
                StockOperationTarget.stock(stockId, targetBucket),
                quantity,
                null,
                StockMovementPlan.positive(movementType),
                reason
        );
    }

    /**
     * - 기존 현재고 차감 command
     * - 수동 조정 차감이나 출고처럼 target이 없는 작업에 사용
     */
    public static StockOperationCommand decrease(StockJob job, Long stockId, StockQuantityBucket sourceBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                StockOperationTarget.stock(stockId, sourceBucket),
                null,
                quantity,
                StockMovementPlan.negative(movementType),
                null,
                reason
        );
    }

    /**
     * - source 차감 후 target 증가 command
     * - 일반 이동, PICK, LOT 변경처럼 from/to가 모두 있는 작업에 사용
     */
    public static StockOperationCommand move(StockJob job, Long fromStockId, StockQuantityBucket sourceBucket, Long toLocationId, Long toLotId, StockQuantityBucket targetBucket, Integer quantity, StockMovementType outMovementType, StockMovementType inMovementType, String reason) {
        return new StockOperationCommand(
                job,
                StockOperationTarget.stock(fromStockId, sourceBucket),
                StockOperationTarget.derivedStockKey(toLocationId, toLotId, targetBucket),
                quantity,
                StockMovementPlan.negative(outMovementType),
                StockMovementPlan.positive(inMovementType),
                reason
        );
    }

    /**
     * - 같은 stock 내부 bucket 전환 command
     * - 할당처럼 AVAILABLE을 WORKING으로 전환할 때 사용
     */
    public static StockOperationCommand convertBucket(StockJob job, Long stockId, StockQuantityBucket sourceBucket, StockQuantityBucket targetBucket, Integer quantity, StockMovementType movementType, String reason) {
        return new StockOperationCommand(
                job,
                StockOperationTarget.stock(stockId, sourceBucket),
                StockOperationTarget.stock(stockId, targetBucket),
                quantity,
                StockMovementPlan.positive(movementType),
                null,
                reason
        );
    }

    /**
     * - 수량 변경 대상 stock 표현
     * - stockId가 있으면 기존 stock을 사용하고, 없으면 stock key로 조회/생성
     */
    public record StockOperationTarget(
            Long stockId,
            Long productId,
            Long warehouseId,
            Long locationId,
            Long lotId,
            StockQuantityBucket bucket
    ) {

        public static StockOperationTarget stock(Long stockId, StockQuantityBucket bucket) {
            return new StockOperationTarget(stockId, null, null, null, null, bucket);
        }

        public static StockOperationTarget stockKey(Long productId, Long warehouseId, Long locationId, Long lotId, StockQuantityBucket bucket) {
            return new StockOperationTarget(null, productId, warehouseId, locationId, lotId, bucket);
        }

        public static StockOperationTarget derivedStockKey(Long locationId, Long lotId, StockQuantityBucket bucket) {
            return new StockOperationTarget(null, null, null, locationId, lotId, bucket);
        }
    }

    /**
     * - stock movement 기록 계획
     * - 수량 변경 방향과 movement 원장 수량 부호를 분리하기 위해 사용
     */
    public record StockMovementPlan(
            StockMovementType movementType,
            int quantitySign
    ) {

        public static StockMovementPlan positive(StockMovementType movementType) {
            return new StockMovementPlan(movementType, 1);
        }

        public static StockMovementPlan negative(StockMovementType movementType) {
            return new StockMovementPlan(movementType, -1);
        }
    }
}
