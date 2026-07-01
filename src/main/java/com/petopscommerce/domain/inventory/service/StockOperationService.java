package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.entity.Stock;
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
 * - 상품/창고/location/LOT key로 현재고를 찾고 AVAILABLE/WORKING 증감 primitive로 처리
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
     * - command key로 source/target 현재고를 찾고 차감, 증가, movement 저장 순서로 처리
     *
     * @param command 재고 수량 변경 command
     * @return 변경 결과
     */
    public StockOperationResult execute(StockOperationCommand command) {
        validateCommand(command);

        Stock sourceStock = null;
        if (command.sourceBucket() != null) {
            sourceStock = getStockForUpdate(command.productId(), command.warehouseId(), command.fromLocationId(), command.fromLotId());
            applyDecrease(sourceStock, command.sourceBucket(), command.quantity());
        }

        Stock targetStock = null;
        if (command.targetBucket() != null) {
            targetStock = increaseTargetStock(command, sourceStock);
        }

        if (command.sourceMovementType() != null) {
            createMovement(command, sourceStock, command.sourceMovementType(), sourceMovementQuantity(command), sourceStock, targetStock);
        }
        if (command.targetMovementType() != null) {
            createMovement(command, targetStock, command.targetMovementType(), command.quantity(), sourceStock, targetStock);
        }

        return new StockOperationResult(sourceStock, targetStock);
    }

    private void validateCommand(StockOperationCommand command) {
        validateRequired(command, "stock operation command is required");
        validateRequired(command.job(), "stock job is required");
        validateRequired(command.productId(), "product id is required");
        validateRequired(command.warehouseId(), "warehouse id is required");
        validatePositiveQuantity(command.quantity());

        if (command.sourceBucket() == null && command.targetBucket() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source or target bucket is required");
        }

        if (command.sourceBucket() != null) {
            validateRequired(command.fromLocationId(), "source location is required");
            validateRequired(command.fromLotId(), "source lot is required");
        }

        if (command.targetBucket() != null) {
            validateRequired(command.toLocationId(), "target location is required");
            validateRequired(command.toLotId(), "target lot is required");
        }
    }

    /**
     * - target 현재고 증가 또는 생성
     * - 기존 row면 bucket 기준 증가하고, 신규 row면 처리 수량이 반영된 row로 생성
     */
    private Stock increaseTargetStock(StockOperationCommand command, Stock sourceStock) {
        if (isSameStockKey(command, sourceStock)) {
            applyIncrease(sourceStock, command.targetBucket(), command.quantity());
            return sourceStock;
        }

        return stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(
                        command.productId(),
                        command.warehouseId(),
                        command.toLocationId(),
                        command.toLotId()
                )
                .map(stock -> {
                    applyIncrease(stock, command.targetBucket(), command.quantity());
                    return stock;
                })
                .orElseGet(() -> createTargetStock(command));
    }

    private boolean isSameStockKey(StockOperationCommand command, Stock sourceStock) {
        return sourceStock != null
                && sourceStock.getProductId().equals(command.productId())
                && sourceStock.getWarehouseId().equals(command.warehouseId())
                && sourceStock.getLocationId().equals(command.toLocationId())
                && sourceStock.getLotId().equals(command.toLotId());
    }

    /**
     * - target 현재고 신규 생성
     * - 같은 key를 다른 요청이 먼저 만들면 다시 잠금 조회 후 기존 row를 사용
     */
    private Stock createTargetStock(StockOperationCommand command) {
        try {
            return stockRepository.saveAndFlush(createStock(command));
        } catch (DataIntegrityViolationException exception) {
            Stock stock = stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(
                            command.productId(),
                            command.warehouseId(),
                            command.toLocationId(),
                            command.toLotId()
                    )
                    .orElseThrow(() -> exception);
            applyIncrease(stock, command.targetBucket(), command.quantity());
            return stock;
        }
    }

    /**
     * - target bucket 기준 신규 현재고 생성
     */
    private Stock createStock(StockOperationCommand command) {
        if (command.targetBucket() == StockQuantityBucket.WORKING) {
            return Stock.createWorking(command.productId(), command.warehouseId(), command.toLocationId(), command.toLotId(), command.quantity());
        }

        return Stock.create(command.productId(), command.warehouseId(), command.toLocationId(), command.toLotId(), command.quantity());
    }

    private void applyIncrease(Stock stock, StockQuantityBucket bucket, Integer quantity) {
        try {
            if (bucket == StockQuantityBucket.WORKING) {
                increaseWorking(stock, quantity);
                return;
            }

            increaseAvailable(stock, quantity);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    private void applyDecrease(Stock stock, StockQuantityBucket bucket, Integer quantity) {
        try {
            if (bucket == StockQuantityBucket.WORKING) {
                decreaseWorking(stock, quantity);
                return;
            }

            decreaseAvailable(stock, quantity);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
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

    private Integer sourceMovementQuantity(StockOperationCommand command) {
        if (command.sourceMovementType() == StockMovementType.ALLOCATE) {
            return command.quantity();
        }

        return -command.quantity();
    }

    /**
     * - 재고 이동 원장 생성
     */
    private void createMovement(StockOperationCommand command, Stock stock, StockMovementType movementType, Integer movementQuantity, Stock sourceStock, Stock targetStock) {
        validateRequired(stock, "movement stock is required");
        validateRequired(movementType, "movement type is required");

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
     * - 재고 수량 변경을 위한 key 기반 잠금 조회
     */
    private Stock getStockForUpdate(Long productId, Long warehouseId, Long locationId, Long lotId) {
        return stockRepository.findWithLockByProductIdAndWarehouseIdAndLocationIdAndLotId(productId, warehouseId, locationId, lotId)
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
}
