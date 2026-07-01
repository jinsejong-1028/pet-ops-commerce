package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.entity.StockMovement;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;
import com.petopscommerce.domain.inventory.repository.StockMovementRepository;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import com.petopscommerce.domain.inventory.service.operation.StockOperationCommand;
import com.petopscommerce.domain.inventory.service.operation.StockOperationCommand.StockMovementPlan;
import com.petopscommerce.domain.inventory.service.operation.StockOperationCommand.StockOperationTarget;
import com.petopscommerce.domain.inventory.service.operation.StockOperationResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 재고 수량 변경 공통 서비스
 * - source/target/bucket command를 AVAILABLE/WORKING 증감 primitive로 처리
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
     * - 업무별 switch 없이 source 차감, target 증가, movement 저장 순서로 처리
     *
     * @param command 재고 수량 변경 command
     * @return 변경 결과
     */
    public StockOperationResult execute(StockOperationCommand command) {
        validateCommand(command);

        Stock sourceStock = resolveSourceStock(command.source());

        // 단계 1: source가 있으면 bucket 기준 차감
        // 결과: 차감 재고가 부족하면 Entity 검증 예외를 API 400으로 변환
        if (command.source() != null) {
            applyDecrease(sourceStock, command.source().bucket(), command.quantity());
        }

        // 단계 2: target이 있으면 bucket 기준 증가
        // 결과: target stock이 없으면 양수 수량으로 신규 생성하고, 있으면 기존 row에 병합
        Stock targetStock = null;
        if (command.target() != null) {
            targetStock = resolveTargetAndIncrease(command.target(), sourceStock, command.quantity());
        }

        // 단계 3: movement 기록
        // 결과: 수량 변경 후 stock total snapshot이 원장에 저장됨
        if (command.sourceMovement() != null) {
            createMovement(command, sourceStock, command.sourceMovement(), sourceStock, targetStock);
        }
        if (command.targetMovement() != null) {
            createMovement(command, targetStock, command.targetMovement(), sourceStock, targetStock);
        }

        return new StockOperationResult(sourceStock, targetStock);
    }

    private void validateCommand(StockOperationCommand command) {
        validateRequired(command, "stock operation command is required");
        validateRequired(command.job(), "stock job is required");
        validatePositiveQuantity(command.quantity());

        if (command.source() == null && command.target() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source or target is required");
        }
    }

    private Stock resolveSourceStock(StockOperationTarget source) {
        if (source == null) {
            return null;
        }

        validateRequired(source.stockId(), "source stock is required");
        validateRequired(source.bucket(), "source quantity bucket is required");

        return getStockForUpdate(source.stockId());
    }

    private Stock resolveTargetAndIncrease(StockOperationTarget target, Stock sourceStock, Integer quantity) {
        validateRequired(target.bucket(), "target quantity bucket is required");

        if (target.stockId() != null) {
            Stock targetStock = resolveExistingTargetStock(target.stockId(), sourceStock);
            applyIncrease(targetStock, target.bucket(), quantity);
            return targetStock;
        }

        Long productId = resolveTargetProductId(target, sourceStock);
        Long warehouseId = resolveTargetWarehouseId(target, sourceStock);
        Long locationId = resolveTargetLocationId(target, sourceStock);
        Long lotId = resolveTargetLotId(target, sourceStock);

        return applyIncreaseToStockKey(productId, warehouseId, locationId, lotId, quantity, target.bucket());
    }

    private Stock resolveExistingTargetStock(Long targetStockId, Stock sourceStock) {
        if (sourceStock != null && sourceStock.getId().equals(targetStockId)) {
            return sourceStock;
        }

        return getStockForUpdate(targetStockId);
    }

    private Long resolveTargetProductId(StockOperationTarget target, Stock sourceStock) {
        if (target.productId() != null) {
            return target.productId();
        }
        validateRequired(sourceStock, "source stock is required to derive target product");
        return sourceStock.getProductId();
    }

    private Long resolveTargetWarehouseId(StockOperationTarget target, Stock sourceStock) {
        if (target.warehouseId() != null) {
            return target.warehouseId();
        }
        validateRequired(sourceStock, "source stock is required to derive target warehouse");
        return sourceStock.getWarehouseId();
    }

    private Long resolveTargetLocationId(StockOperationTarget target, Stock sourceStock) {
        if (target.locationId() != null) {
            return target.locationId();
        }
        validateRequired(sourceStock, "source stock is required to derive target location");
        return sourceStock.getLocationId();
    }

    private Long resolveTargetLotId(StockOperationTarget target, Stock sourceStock) {
        if (target.lotId() != null) {
            return target.lotId();
        }
        validateRequired(sourceStock, "source stock is required to derive target lot");
        return sourceStock.getLotId();
    }

    /**
     * - stock key 기준 target 현재고 증가
     * - row가 없으면 양수 수량으로 신규 생성하고, 있으면 bucket 기준 증가
     */
    private Stock applyIncreaseToStockKey(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, StockQuantityBucket bucket) {
        validatePositiveQuantity(quantity);
        validateRequired(productId, "target product is required");
        validateRequired(warehouseId, "target warehouse is required");
        validateRequired(locationId, "target location is required");
        validateRequired(lotId, "target lot is required");
        validateRequired(bucket, "target quantity bucket is required");

        return stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
                .map(stock -> {
                    applyIncrease(stock, bucket, quantity);
                    return stock;
                })
                .orElseGet(() -> createStockByPositiveQuantity(productId, warehouseId, locationId, lotId, quantity, bucket));
    }

    /**
     * - 신규 현재고 생성
     * - target 증가 작업에서만 신규 row를 만들 수 있음
     */
    private Stock createStockByPositiveQuantity(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, StockQuantityBucket bucket) {
        try {
            return stockRepository.saveAndFlush(createStock(productId, warehouseId, locationId, lotId, quantity, bucket));
        } catch (DataIntegrityViolationException exception) {
            return retryIncreaseAfterDuplicate(productId, warehouseId, locationId, lotId, quantity, bucket, exception);
        }
    }

    /**
     * - 동시 신규 생성 충돌 재시도
     * - 같은 현재고를 다른 요청이 먼저 만들었으면 다시 잠금 조회 후 증가 처리
     */
    private Stock retryIncreaseAfterDuplicate(Long productId, Long warehouseId, Long locationId, Long lotId, Integer quantity, StockQuantityBucket bucket, DataIntegrityViolationException exception) {
        Stock stock = stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
                .orElseThrow(() -> exception);
        applyIncrease(stock, bucket, quantity);
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

    private void applyIncrease(Stock stock, StockQuantityBucket bucket, Integer quantity) {
        validateRequired(bucket, "quantity bucket is required");
        applyStockChange(() -> {
            if (bucket == StockQuantityBucket.WORKING) {
                increaseWorking(stock, quantity);
                return;
            }

            increaseAvailable(stock, quantity);
        });
    }

    private void applyDecrease(Stock stock, StockQuantityBucket bucket, Integer quantity) {
        validateRequired(bucket, "quantity bucket is required");
        applyStockChange(() -> {
            if (bucket == StockQuantityBucket.WORKING) {
                decreaseWorking(stock, quantity);
                return;
            }

            decreaseAvailable(stock, quantity);
        });
    }

    /** - AVAILABLE 수량 증가 */
    private void increaseAvailable(Stock stock, Integer quantity) {
        stock.applyAvailableDelta(quantity);
    }

    /** - AVAILABLE 수량 차감 */
    private void decreaseAvailable(Stock stock, Integer quantity) {
        stock.applyAvailableDelta(-quantity);
    }

    /** - WORKING 수량 증가 */
    private void increaseWorking(Stock stock, Integer quantity) {
        stock.applyWorkingDelta(quantity, "working stock is not enough");
    }

    /** - WORKING 수량 차감 */
    private void decreaseWorking(Stock stock, Integer quantity) {
        stock.applyWorkingDelta(-quantity, "working stock is not enough");
    }

    /**
     * - 재고 이동 원장 생성
     */
    private void createMovement(StockOperationCommand command, Stock stock, StockMovementPlan movementPlan, Stock sourceStock, Stock targetStock) {
        validateRequired(stock, "movement stock is required");
        validateRequired(movementPlan.movementType(), "movement type is required");
        validateMovementQuantitySign(movementPlan.quantitySign());

        StockMovementType movementType = movementPlan.movementType();
        Integer movementQuantity = command.quantity() * movementPlan.quantitySign();

        stockMovementRepository.save(StockMovement.create(
                command.job(),
                stock,
                movementType,
                resolveFromLocationId(movementType, stock, sourceStock),
                resolveToLocationId(movementType, stock, targetStock),
                movementQuantity,
                command.reason()
        ));
    }

    private Long resolveFromLocationId(StockMovementType movementType, Stock stock, Stock sourceStock) {
        if (movementType == StockMovementType.RECEIVE_IN) {
            return null;
        }

        if (movementType == StockMovementType.ADJUST
                || movementType == StockMovementType.ALLOCATE
                || movementType == StockMovementType.SHIP_OUT) {
            return stock.getLocationId();
        }

        return sourceStock != null ? sourceStock.getLocationId() : null;
    }

    private Long resolveToLocationId(StockMovementType movementType, Stock stock, Stock targetStock) {
        if (movementType == StockMovementType.RECEIVE_IN) {
            return stock.getLocationId();
        }

        if (movementType == StockMovementType.ADJUST
                || movementType == StockMovementType.ALLOCATE
                || movementType == StockMovementType.SHIP_OUT) {
            return null;
        }

        return targetStock != null ? targetStock.getLocationId() : null;
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

    private void validateRequired(Object value, String message) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateMovementQuantitySign(int quantitySign) {
        if (quantitySign != 1 && quantitySign != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "movement quantity sign is invalid");
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
