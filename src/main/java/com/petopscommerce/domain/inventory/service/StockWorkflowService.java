package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.AllocateStockRequest;
import com.petopscommerce.domain.inventory.dto.OutboundStockRequest;
import com.petopscommerce.domain.inventory.dto.PickStockRequest;
import com.petopscommerce.domain.inventory.dto.StockJobResponse;
import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.entity.LocationStatus;
import com.petopscommerce.domain.inventory.entity.LocationType;
import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.entity.StockJobStatus;
import com.petopscommerce.domain.inventory.entity.StockMovement;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.repository.LocationRepository;
import com.petopscommerce.domain.inventory.repository.StockJobRepository;
import com.petopscommerce.domain.inventory.repository.StockMovementRepository;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.repository.OrderItemRepository;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.service.BusinessNumberGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 재고 작업 비즈니스 로직
 * - 할당, PICKTO 이동, 출고 수량 변경과 원장 저장 담당
 */
@Service
@Transactional
public class StockWorkflowService {

    private final StockRepository stockRepository;
    private final LocationRepository locationRepository;
    private final StockJobRepository stockJobRepository;
    private final StockMovementRepository stockMovementRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BusinessNumberGenerator businessNumberGenerator;

    /**
     * - 생성자 주입
     */
    public StockWorkflowService(StockRepository stockRepository, LocationRepository locationRepository, StockJobRepository stockJobRepository, StockMovementRepository stockMovementRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, BusinessNumberGenerator businessNumberGenerator) {
        this.stockRepository = stockRepository;
        this.locationRepository = locationRepository;
        this.stockJobRepository = stockJobRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.businessNumberGenerator = businessNumberGenerator;
    }

    /**
     * - 재고 할당
     * - 보관 location의 총수량은 유지하고 작업수량만 증가
     *
     * @param request 재고 할당 요청
     * @return 재고 작업 응답
     */
    public StockJobResponse allocate(AllocateStockRequest request) {
        // 단계 1: 원천 재고 row를 잠금 조회
        // 결과: 같은 재고에 대한 동시 할당/피킹이 순서대로 처리됨
        Stock sourceStock = getStockForUpdate(request.stockId());

        // 단계 2: 주문/주문상품과 재고 상품 일치 여부 검증
        // 결과: 다른 상품 재고를 주문상품에 잘못 할당하는 것을 차단
        OrderItem orderItem = getOrderItem(request.orderId(), request.orderItemId());
        validateOrderItemProduct(sourceStock, orderItem);

        try {
            // 단계 3: 할당은 "찜" 상태이므로 실제 location 이동은 없음
            // 결과: 총수량은 유지하고 작업수량만 증가하여 가용수량이 감소
            sourceStock.allocate(request.quantity());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        // 단계 4: 출고 작업 단위 job 생성
        // 결과: 이후 PICK/출고가 같은 jobNo 기준으로 이어짐
        String jobNo = businessNumberGenerator.generate(BusinessNumberType.STOCK_MOVE);
        StockJob stockJob = stockJobRepository.save(StockJob.createSalesShipment(jobNo, sourceStock.getWarehouseId(), request.orderId(), request.reason()));

        // 단계 5: 할당 이력 저장
        // 결과: location 이동 없이 workingQuantityDelta만 +수량으로 남김
        stockMovementRepository.save(StockMovement.create(
                stockJob,
                sourceStock,
                StockMovementType.ALLOCATE,
                sourceStock.getLocationId(),
                sourceStock.getLocationId(),
                request.quantity(),
                0,
                request.quantity(),
                request.reason()
        ));

        return StockJobResponse.from(stockJob);
    }

    /**
     * - PICKTO 이동
     * - source stock에서 수량을 빼고 PICKTO stock으로 수량을 이동
     *
     * @param request 재고 PICK 요청
     * @return 재고 작업 응답
     */
    public StockJobResponse pick(PickStockRequest request) {
        // 단계 1: job 조회와 상태 검증
        // 결과: 할당 완료된 작업만 PICKTO 이동 가능
        StockJob stockJob = getJob(request.jobId());
        validateJobStatus(stockJob, StockJobStatus.ALLOCATED);

        // 단계 2: 최초 할당 이력에서 원천 재고를 찾음
        // 결과: 어떤 보관 location 재고를 PICK할지 결정
        StockMovement allocationMovement = getMovement(stockJob.getId(), StockMovementType.ALLOCATE);
        Stock sourceStock = getStockForUpdate(allocationMovement.getStockId());

        // 단계 3: 같은 창고의 활성 PICKTO location 검증
        // 결과: 창고를 건너뛰는 잘못된 PICK 이동을 차단
        Location picktoLocation = getPicktoLocation(request.picktoLocationId(), stockJob.getWarehouseId());

        // 단계 4: PICKTO location의 현재고 row 확보
        // 결과: 기존 row가 있으면 재사용하고, 없으면 0수량 row를 만든 뒤 입고처럼 증가
        Stock picktoStock = getOrCreatePicktoStock(sourceStock, picktoLocation.getId());

        try {
            // 단계 5: 원천 location에서 재고를 빼고 PICKTO location으로 더함
            // 결과: 같은 창고 안에서 총수량/작업수량이 source -> PICKTO로 이동
            sourceStock.pickOut(request.quantity());
            picktoStock.pickIn(request.quantity());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        // 단계 6: 원천 location 출고 이력 저장
        // 결과: source stock의 총수량/작업수량 감소가 원장에 남음
        stockMovementRepository.save(StockMovement.create(
                stockJob,
                sourceStock,
                StockMovementType.PICK_OUT,
                sourceStock.getLocationId(),
                picktoLocation.getId(),
                -request.quantity(),
                -request.quantity(),
                -request.quantity(),
                request.reason()
        ));

        // 단계 7: PICKTO location 입고 이력 저장
        // 결과: PICKTO stock의 총수량/작업수량 증가가 원장에 남음
        stockMovementRepository.save(StockMovement.create(
                stockJob,
                picktoStock,
                StockMovementType.PICK_IN,
                sourceStock.getLocationId(),
                picktoLocation.getId(),
                request.quantity(),
                request.quantity(),
                request.quantity(),
                request.reason()
        ));

        // 단계 8: job 상태를 PICKED로 변경
        // 결과: 이후 출고 API에서만 다음 단계 진행 가능
        stockJob.markPicked();

        return StockJobResponse.from(stockJob);
    }

    /**
     * - 출고 처리
     * - PICKTO stock의 총수량과 작업수량을 함께 감소
     *
     * @param request 출고 요청
     * @return 재고 작업 응답
     */
    public StockJobResponse outbound(OutboundStockRequest request) {
        // 단계 1: job 조회와 상태 검증
        // 결과: PICKTO 이동이 끝난 작업만 실제 출고 가능
        StockJob stockJob = getJob(request.jobId());
        validateJobStatus(stockJob, StockJobStatus.PICKED);

        // 단계 2: PICK_IN 이력에서 PICKTO 재고를 찾음
        // 결과: 출고 차감 대상이 보관 location이 아니라 PICKTO location으로 고정
        StockMovement pickInMovement = getMovement(stockJob.getId(), StockMovementType.PICK_IN);
        Stock picktoStock = getStockForUpdate(pickInMovement.getStockId());

        try {
            // 단계 3: PICKTO 재고를 실제로 창고 밖으로 내보냄
            // 결과: 총수량과 작업수량이 함께 감소하고 가용 재고로 돌아오지 않음
            picktoStock.shipOut(request.quantity());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }

        // 단계 4: 출고 이력 저장
        // 결과: toLocation 없이 PICKTO에서 외부로 빠진 수량을 원장에 남김
        stockMovementRepository.save(StockMovement.create(
                stockJob,
                picktoStock,
                StockMovementType.SHIP_OUT,
                picktoStock.getLocationId(),
                null,
                -request.quantity(),
                -request.quantity(),
                -request.quantity(),
                request.reason()
        ));

        // 단계 5: job 상태를 SHIPPED로 변경
        // 결과: 출고 작업이 완료 상태로 마감됨
        stockJob.markShipped();

        return StockJobResponse.from(stockJob);
    }

    private Stock getStockForUpdate(Long stockId) {
        return stockRepository.findWithLockById(stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));
    }

    private OrderItem getOrderItem(Long orderId, Long orderItemId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order item not found"));

        if (!orderId.equals(orderItem.getOrderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order item does not belong to order");
        }

        return orderItem;
    }

    private void validateOrderItemProduct(Stock stock, OrderItem orderItem) {
        if (!stock.getProductId().equals(orderItem.getProductId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stock product does not match order item");
        }
    }

    private StockJob getJob(Long jobId) {
        return stockJobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock job not found"));
    }

    private void validateJobStatus(StockJob stockJob, StockJobStatus expectedStatus) {
        if (stockJob.getStatus() != expectedStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid stock job status");
        }
    }

    private StockMovement getMovement(Long jobId, StockMovementType movementType) {
        return stockMovementRepository.findFirstByJobIdAndMovementTypeOrderByIdDesc(jobId, movementType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock movement not found"));
    }

    private Location getPicktoLocation(Long picktoLocationId, Long warehouseId) {
        Location location = locationRepository.findByIdAndLocationTypeAndStatus(picktoLocationId, LocationType.PICKTO, LocationStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pickto location not found"));

        if (!warehouseId.equals(location.getWarehouseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pickto location warehouse does not match stock job");
        }

        return location;
    }

    private Stock getOrCreatePicktoStock(Stock sourceStock, Long picktoLocationId) {
        return stockRepository.findByProductIdAndWarehouseIdAndLocationIdAndLotId(
                        sourceStock.getProductId(),
                        sourceStock.getWarehouseId(),
                        picktoLocationId,
                        sourceStock.getLotId()
                )
                .orElseGet(() -> stockRepository.save(Stock.create(
                        sourceStock.getProductId(),
                        sourceStock.getWarehouseId(),
                        picktoLocationId,
                        sourceStock.getLotId(),
                        0,
                        sourceStock.getSafetyQuantity()
                )));
    }
}