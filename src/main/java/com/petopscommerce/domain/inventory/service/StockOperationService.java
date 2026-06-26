package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.entity.StockMovement;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;
import com.petopscommerce.domain.inventory.repository.StockMovementRepository;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 재고 수량 변경 공통 서비스
 * - 현재고 잠금, 수량 증감, stock_movements 원장 저장 담당
 */
@Service
@Transactional
public class StockOperationService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    /**
     * - 생성자 주입
     *
     * @param stockRepository 현재고 DB 접근 객체
     * @param stockMovementRepository 재고 이동 원장 DB 접근 객체
     */
    public StockOperationService(StockRepository stockRepository, StockMovementRepository stockMovementRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * - 재고 수량 변경을 위한 잠금 조회
     * - 같은 stock row에 대한 동시 작업을 순서대로 처리하기 위해 사용
     *
     * @param stockId 현재고 ID
     * @return 잠금 조회된 현재고
     */
    public Stock getStockForUpdate(Long stockId) {
        return stockRepository.findWithLockById(stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));
    }

    /**
     * - 입고/초기 재고 증가
     * - 같은 product/warehouse/location/lot 현재고가 있으면 증가하고, 없으면 신규 생성
     *
     * @param job 재고 작업 헤더
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param quantity 입고 수량
     * @param safetyQuantity 안전재고 수량
     * @param reason 입고 사유
     * @return 변경된 현재고
     */
    public Stock receive(StockJob job, Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, Integer safetyQuantity, String reason) {
        // 단계 1: 가용수량 증가 delta를 현재고에 반영
        // 결과: 동일 현재고가 있으면 증가하고, 없으면 입고 수량만큼 신규 row 생성
        Stock stock = applyStockDelta(productId, warehouseId, locationId, lotId, quantity, StockQuantityBucket.AVAILABLE, safetyQuantity);

        // 단계 2: 입고 원장 저장
        // 결과: RECEIVE_IN movement에 total/available 증가량 기록
        stockMovementRepository.save(StockMovement.create(
                job,
                stock,
                StockMovementType.RECEIVE_IN,
                null,
                locationId,
                quantity,
                quantity,
                quantity,
                0,
                reason
        ));

        return stock;
    }

    /**
     * - 재고 할당
     * - 실제 location 이동 없이 가용수량을 작업수량으로 전환
     *
     * @param job 재고 작업 헤더
     * @param stock 잠금 조회된 현재고
     * @param quantity 할당 수량
     * @param reason 할당 사유
     * @return 변경된 현재고
     */
    public Stock reserve(StockJob job, Stock stock, Integer quantity, String reason) {
        applyStockChange(() -> stock.allocate(quantity));

        stockMovementRepository.save(StockMovement.create(
                job,
                stock,
                StockMovementType.ALLOCATE,
                stock.getLocationId(),
                stock.getLocationId(),
                quantity,
                0,
                -quantity,
                quantity,
                reason
        ));

        return stock;
    }

    /**
     * - location 간 가용 재고 이동
     * - 작업수량은 건드리지 않고 source/target의 total/available만 변경
     *
     * @param job 재고 작업 헤더
     * @param sourceStock 잠금 조회된 출발 현재고
     * @param toLocationId 도착 location ID
     * @param quantity 이동 수량
     * @param reason 이동 사유
     * @return 도착 location 현재고
     */
    public Stock moveAvailableToLocation(StockJob job, Stock sourceStock, Long toLocationId, Integer quantity, String reason) {
        // 단계 1: 출발 location의 가용수량 차감
        // 결과: 가용수량이 부족하면 BAD_REQUEST로 중단
        applyStockChange(() -> sourceStock.applyAvailableDelta(-quantity));

        // 단계 2: 도착 location의 가용수량 증가
        // 결과: 도착 현재고가 없으면 0 row가 아니라 이동 수량만큼 신규 row 생성
        Stock targetStock = applyStockDelta(
                sourceStock.getProductId(),
                sourceStock.getWarehouseId(),
                toLocationId,
                sourceStock.getLotId(),
                quantity,
                StockQuantityBucket.AVAILABLE,
                sourceStock.getSafetyQuantity()
        );

        stockMovementRepository.save(StockMovement.create(
                job,
                sourceStock,
                StockMovementType.TRANSFER_OUT,
                sourceStock.getLocationId(),
                toLocationId,
                -quantity,
                -quantity,
                -quantity,
                0,
                reason
        ));
        stockMovementRepository.save(StockMovement.create(
                job,
                targetStock,
                StockMovementType.TRANSFER_IN,
                sourceStock.getLocationId(),
                toLocationId,
                quantity,
                quantity,
                quantity,
                0,
                reason
        ));

        return targetStock;
    }

    /**
     * - PICKTO 작업 이동
     * - source 작업수량을 PICKTO 작업수량으로 이동
     *
     * @param job 재고 작업 헤더
     * @param sourceStock 잠금 조회된 원천 현재고
     * @param toLocationId PICKTO location ID
     * @param quantity 이동 수량
     * @param reason PICK 사유
     * @return PICKTO 현재고
     */
    public Stock moveWorkingToLocation(StockJob job, Stock sourceStock, Long toLocationId, Integer quantity, String reason) {
        // 단계 1: 출발 location의 작업수량 차감
        // 결과: 할당된 작업수량이 부족하면 BAD_REQUEST로 중단
        applyStockChange(() -> sourceStock.applyWorkingDelta(-quantity, "picked stock is not enough"));

        // 단계 2: PICKTO location의 작업수량 증가
        // 결과: PICKTO 현재고가 없으면 0 row가 아니라 PICK 수량만큼 신규 row 생성
        Stock targetStock = applyStockDelta(
                sourceStock.getProductId(),
                sourceStock.getWarehouseId(),
                toLocationId,
                sourceStock.getLotId(),
                quantity,
                StockQuantityBucket.WORKING,
                sourceStock.getSafetyQuantity()
        );

        stockMovementRepository.save(StockMovement.create(
                job,
                sourceStock,
                StockMovementType.PICK_OUT,
                sourceStock.getLocationId(),
                toLocationId,
                -quantity,
                -quantity,
                0,
                -quantity,
                reason
        ));
        stockMovementRepository.save(StockMovement.create(
                job,
                targetStock,
                StockMovementType.PICK_IN,
                sourceStock.getLocationId(),
                toLocationId,
                quantity,
                quantity,
                0,
                quantity,
                reason
        ));

        return targetStock;
    }

    /**
     * - PICKTO 출고 차감
     * - 작업수량으로 잡힌 재고를 실제 창고 밖으로 차감
     *
     * @param job 재고 작업 헤더
     * @param stock 출고 대상 현재고
     * @param quantity 출고 수량
     * @param reason 출고 사유
     * @return 변경된 현재고
     */
    public Stock issueWorking(StockJob job, Stock stock, Integer quantity, String reason) {
        applyStockChange(() -> stock.applyWorkingDelta(-quantity, "shipping stock is not enough"));

        stockMovementRepository.save(StockMovement.create(
                job,
                stock,
                StockMovementType.SHIP_OUT,
                stock.getLocationId(),
                null,
                -quantity,
                -quantity,
                0,
                -quantity,
                reason
        ));

        return stock;
    }

    /**
     * - 수동 재고 조정
     * - 양수는 증가, 음수는 가용수량 기준 차감
     *
     * @param job 재고 작업 헤더
     * @param stock 조정 대상 현재고
     * @param signedQuantity 부호가 있는 조정 수량
     * @param reason 조정 사유
     * @return 변경된 현재고
     */
    public Stock adjust(StockJob job, Stock stock, Integer signedQuantity, String reason) {
        applyStockChange(() -> stock.applyAvailableDelta(signedQuantity));

        stockMovementRepository.save(StockMovement.create(
                job,
                stock,
                StockMovementType.ADJUST,
                stock.getLocationId(),
                stock.getLocationId(),
                signedQuantity,
                signedQuantity,
                signedQuantity,
                0,
                reason
        ));

        return stock;
    }

    /**
     * - 현재고 row에 수량 delta 반영
     * - row가 없으면 양수 delta일 때만 신규 생성하고, 음수 delta는 재고 부족으로 처리
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param quantityDelta 부호가 있는 변경 수량
     * @param bucket 변경 대상 수량 유형
     * @param safetyQuantity 신규 생성 시 안전재고 수량
     * @return 변경된 현재고
     */
    private Stock applyStockDelta(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantityDelta, StockQuantityBucket bucket, Integer safetyQuantity) {
        validateNonZeroDelta(quantityDelta);

        // 단계 1: 동일 현재고 row를 잠금 조회
        // 결과: row가 있으면 해당 row에 delta만 반영
        return stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
                .map(stock -> {
                    applyDeltaToStock(stock, quantityDelta, bucket);
                    return stock;
                })
                .orElseGet(() -> createStockByPositiveDelta(productId, warehouseId, locationId, lotId, quantityDelta, bucket, safetyQuantity));
    }

    /**
     * - 신규 현재고 생성
     * - 재고 row가 없을 때 양수 delta만 신규 row로 만들 수 있음
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param quantityDelta 양수 변경 수량
     * @param bucket 생성할 수량 유형
     * @param safetyQuantity 안전재고 수량
     * @return 신규 현재고
     */
    private Stock createStockByPositiveDelta(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantityDelta, StockQuantityBucket bucket, Integer safetyQuantity) {
        if (quantityDelta < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stock is not enough");
        }

        try {
            return stockRepository.saveAndFlush(createStock(productId, warehouseId, locationId, lotId, quantityDelta, bucket, safetyQuantity));
        } catch (DataIntegrityViolationException exception) {
            return retryApplyDeltaAfterDuplicate(productId, warehouseId, locationId, lotId, quantityDelta, bucket, exception);
        }
    }

    /**
     * - 동시 신규 생성 충돌 재시도
     * - 같은 현재고를 다른 요청이 먼저 만들었으면 다시 잠금 조회 후 delta 반영
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param quantityDelta 부호가 있는 변경 수량
     * @param bucket 변경 대상 수량 유형
     * @param exception 원래 DB unique 충돌 예외
     * @return 변경된 현재고
     */
    private Stock retryApplyDeltaAfterDuplicate(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantityDelta, StockQuantityBucket bucket, DataIntegrityViolationException exception) {
        Stock stock = stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
                .orElseThrow(() -> exception);
        applyDeltaToStock(stock, quantityDelta, bucket);
        return stock;
    }

    /**
     * - 수량 유형별 신규 현재고 생성
     * - AVAILABLE은 가용수량으로, WORKING은 작업수량으로 시작
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param quantity 생성 수량
     * @param bucket 생성할 수량 유형
     * @param safetyQuantity 안전재고 수량
     * @return 신규 현재고
     */
    private Stock createStock(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, StockQuantityBucket bucket, Integer safetyQuantity) {
        if (bucket == StockQuantityBucket.WORKING) {
            return Stock.createWorking(productId, warehouseId, locationId, lotId, quantity, safetyQuantity);
        }

        return Stock.create(productId, warehouseId, locationId, lotId, quantity, safetyQuantity);
    }

    /**
     * - 기존 현재고에 delta 반영
     * - Entity 검증 예외는 API 응답용 BAD_REQUEST로 변환
     *
     * @param stock 변경 대상 현재고
     * @param quantityDelta 부호가 있는 변경 수량
     * @param bucket 변경 대상 수량 유형
     */
    private void applyDeltaToStock(Stock stock, Integer quantityDelta, StockQuantityBucket bucket) {
        applyStockChange(() -> {
            if (bucket == StockQuantityBucket.WORKING) {
                stock.applyWorkingDelta(quantityDelta, "working stock is not enough");
                return;
            }

            stock.applyAvailableDelta(quantityDelta);
        });
    }

    private void validateNonZeroDelta(Integer quantityDelta) {
        if (quantityDelta == null || quantityDelta == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must not be zero");
        }
    }

    private void applyStockChange(Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}