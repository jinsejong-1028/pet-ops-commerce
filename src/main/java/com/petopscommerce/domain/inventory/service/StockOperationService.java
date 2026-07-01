package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.entity.StockMovement;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;
import com.petopscommerce.domain.inventory.repository.StockMovementRepository;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import com.petopscommerce.domain.inventory.service.operation.StockOperationCommand;
import com.petopscommerce.domain.inventory.service.operation.StockOperationResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 재고 수량 변경 공통 서비스
 * - 외부에는 execute(command) 하나만 열고, 현재고 잠금/증감/원장 저장을 내부에서 처리
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
     * - 재고 수량 작업 실행 단일 진입점
     * - command의 from/to/bucket 조합으로 입고, 할당, 이동, PICK, 출고, 조정, LOT 변경을 처리
     *
     * @param command 재고 수량 변경 command
     * @return 변경 결과
     */
    public StockOperationResult execute(StockOperationCommand command) {
        validateRequired(command, "stock operation command is required");
        validateRequired(command.job(), "stock job is required");
        validateRequired(command.operationType(), "stock operation type is required");

        switch (command.operationType()) {
            case RECEIVE:
                return executeReceive(command);
            case ALLOCATE:
                return executeAllocate(command);
            case TRANSFER:
                return executeTransfer(command);
            case PICK:
                return executePick(command);
            case SHIP:
                return executeShip(command);
            case ADJUST:
                return executeAdjust(command);
            case LOT_CHANGE:
                return executeLotChange(command);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported stock operation type");
        }
    }

    /**
     * - 입고/초기 재고 증가
     * - from 없이 target 현재고를 생성하거나 증가
     */
    private StockOperationResult executeReceive(StockOperationCommand command) {
        validateRequired(command.productId(), "product id is required");
        validateRequired(command.warehouseId(), "warehouse id is required");
        validateRequired(command.toLocationId(), "target location is required");
        validateRequired(command.toLotId(), "target lot is required");
        validatePositiveQuantity(command.quantity());

        Stock targetStock = applyStockDelta(
                command.productId(),
                command.warehouseId(),
                command.toLocationId(),
                command.toLotId(),
                command.quantity(),
                command.toBucket()
        );
        createMovement(command, targetStock, StockMovementType.RECEIVE_IN, null, command.toLocationId(), command.quantity());

        return new StockOperationResult(null, targetStock);
    }

    /**
     * - 재고 할당
     * - 같은 stock 내부에서 total은 유지하고 available을 working으로 전환
     */
    private StockOperationResult executeAllocate(StockOperationCommand command) {
        validateRequired(command.fromStockId(), "source stock is required");
        validatePositiveQuantity(command.quantity());

        Stock sourceStock = getStockForUpdate(command.fromStockId());
        applyStockChange(() -> sourceStock.allocate(command.quantity()));
        createMovement(command, sourceStock, StockMovementType.ALLOCATE, sourceStock.getLocationId(), null, command.quantity());

        return new StockOperationResult(sourceStock, null);
    }

    /**
     * - location 간 가용 재고 이동
     */
    private StockOperationResult executeTransfer(StockOperationCommand command) {
        validateRequired(command.toLocationId(), "target location is required");
        return executeMove(command, StockMovementType.TRANSFER_OUT, StockMovementType.TRANSFER_IN, "available stock is not enough");
    }

    /**
     * - PICKTO 작업수량 이동
     */
    private StockOperationResult executePick(StockOperationCommand command) {
        validateRequired(command.toLocationId(), "target location is required");
        return executeMove(command, StockMovementType.PICK_OUT, StockMovementType.PICK_IN, "picked stock is not enough");
    }

    /**
     * - LOT 속성 변경
     * - 같은 location에서 lot_id가 다른 현재고로 available 수량을 이동
     */
    private StockOperationResult executeLotChange(StockOperationCommand command) {
        validateRequired(command.toLotId(), "target lot is required");
        return executeMove(command, StockMovementType.LOT_CHANGE_OUT, StockMovementType.LOT_CHANGE_IN, "available stock is not enough");
    }

    /**
     * - from 현재고 차감 후 to 현재고 생성/증가
     */
    private StockOperationResult executeMove(StockOperationCommand command, StockMovementType outType, StockMovementType inType, String shortageMessage) {
        validateRequired(command.fromStockId(), "source stock is required");
        validatePositiveQuantity(command.quantity());

        Stock sourceStock = getStockForUpdate(command.fromStockId());
        Long targetLocationId = command.toLocationId() != null ? command.toLocationId() : sourceStock.getLocationId();
        Long targetLotId = command.toLotId() != null ? command.toLotId() : sourceStock.getLotId();

        // 단계 1: 출발 현재고 차감
        // 결과: 부족하면 Entity 검증 예외를 API 400으로 변환
        applyDeltaToStock(sourceStock, -command.quantity(), command.fromBucket(), shortageMessage);

        // 단계 2: 도착 현재고 생성 또는 증가
        // 결과: 동일 target 현재고가 있으면 병합되고, 없으면 신규 row 생성
        Stock targetStock = applyStockDelta(
                sourceStock.getProductId(),
                sourceStock.getWarehouseId(),
                targetLocationId,
                targetLotId,
                command.quantity(),
                command.toBucket()
        );

        createMovement(command, sourceStock, outType, sourceStock.getLocationId(), targetLocationId, -command.quantity());
        createMovement(command, targetStock, inType, sourceStock.getLocationId(), targetLocationId, command.quantity());

        return new StockOperationResult(sourceStock, targetStock);
    }

    /**
     * - PICKTO 출고 차감
     */
    private StockOperationResult executeShip(StockOperationCommand command) {
        validateRequired(command.fromStockId(), "source stock is required");
        validatePositiveQuantity(command.quantity());

        Stock sourceStock = getStockForUpdate(command.fromStockId());
        applyDeltaToStock(sourceStock, -command.quantity(), command.fromBucket(), "shipping stock is not enough");
        createMovement(command, sourceStock, StockMovementType.SHIP_OUT, sourceStock.getLocationId(), null, -command.quantity());

        return new StockOperationResult(sourceStock, null);
    }

    /**
     * - 수동 재고 조정
     * - quantity 부호로 증가/차감을 구분
     */
    private StockOperationResult executeAdjust(StockOperationCommand command) {
        validateRequired(command.fromStockId(), "source stock is required");
        validateNonZeroDelta(command.quantity());

        Stock sourceStock = getStockForUpdate(command.fromStockId());
        applyDeltaToStock(sourceStock, command.quantity(), command.fromBucket(), "available stock is not enough");
        createMovement(command, sourceStock, StockMovementType.ADJUST, sourceStock.getLocationId(), null, command.quantity());

        return new StockOperationResult(sourceStock, null);
    }

    /**
     * - 현재고 row에 수량 delta 반영
     * - row가 없으면 양수 delta일 때만 신규 생성하고, 음수 delta는 재고 부족으로 처리
     */
    private Stock applyStockDelta(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantityDelta, StockQuantityBucket bucket) {
        validateNonZeroDelta(quantityDelta);
        validateRequired(bucket, "target quantity bucket is required");

        return stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
                .map(stock -> {
                    applyDeltaToStock(stock, quantityDelta, bucket, "stock is not enough");
                    return stock;
                })
                .orElseGet(() -> createStockByPositiveDelta(productId, warehouseId, locationId, lotId, quantityDelta, bucket));
    }

    /**
     * - 신규 현재고 생성
     * - 재고 row가 없을 때 양수 delta만 신규 row로 만들 수 있음
     */
    private Stock createStockByPositiveDelta(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantityDelta, StockQuantityBucket bucket) {
        if (quantityDelta < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stock is not enough");
        }

        try {
            return stockRepository.saveAndFlush(createStock(productId, warehouseId, locationId, lotId, quantityDelta, bucket));
        } catch (DataIntegrityViolationException exception) {
            return retryApplyDeltaAfterDuplicate(productId, warehouseId, locationId, lotId, quantityDelta, bucket, exception);
        }
    }

    /**
     * - 동시 신규 생성 충돌 재시도
     * - 같은 현재고를 다른 요청이 먼저 만들었으면 다시 잠금 조회 후 delta 반영
     */
    private Stock retryApplyDeltaAfterDuplicate(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantityDelta, StockQuantityBucket bucket, DataIntegrityViolationException exception) {
        Stock stock = stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
                .orElseThrow(() -> exception);
        applyDeltaToStock(stock, quantityDelta, bucket, "stock is not enough");
        return stock;
    }

    /**
     * - 수량 유형별 신규 현재고 생성
     */
    private Stock createStock(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, StockQuantityBucket bucket) {
        if (bucket == StockQuantityBucket.WORKING) {
            return Stock.createWorking(productId, warehouseId, locationId, lotId, quantity);
        }

        return Stock.create(productId, warehouseId, locationId, lotId, quantity);
    }

    /**
     * - 기존 현재고에 delta 반영
     * - Entity 검증 예외는 API 응답용 BAD_REQUEST로 변환
     */
    private void applyDeltaToStock(Stock stock, Integer quantityDelta, StockQuantityBucket bucket, String shortageMessage) {
        validateRequired(bucket, "quantity bucket is required");
        applyStockChange(() -> {
            if (bucket == StockQuantityBucket.WORKING) {
                stock.applyWorkingDelta(quantityDelta, shortageMessage);
                return;
            }

            stock.applyAvailableDelta(quantityDelta);
        });
    }

    /**
     * - 재고 이동 원장 생성
     */
    private void createMovement(StockOperationCommand command, Stock stock, StockMovementType movementType, Long fromLocationId, Long toLocationId, Integer quantity) {
        stockMovementRepository.save(StockMovement.create(
                command.job(),
                stock,
                movementType,
                fromLocationId,
                toLocationId,
                quantity,
                command.reason()
        ));
    }

    /**
     * - 재고 수량 변경을 위한 잠금 조회
     */
    private Stock getStockForUpdate(Long stockId) {
        return stockRepository.findWithLockById(stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));
    }

    private void validatePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
    }

    private void validateNonZeroDelta(Integer quantityDelta) {
        if (quantityDelta == null || quantityDelta == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must not be zero");
        }
    }

    private void validateRequired(Object value, String message) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
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