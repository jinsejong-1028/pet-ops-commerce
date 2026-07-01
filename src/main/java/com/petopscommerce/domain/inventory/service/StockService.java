package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.AdjustStockRequest;
import com.petopscommerce.domain.inventory.dto.AllocateStockRequest;
import com.petopscommerce.domain.inventory.dto.ChangeLotRequest;
import com.petopscommerce.domain.inventory.dto.OutboundStockRequest;
import com.petopscommerce.domain.inventory.dto.PickStockRequest;
import com.petopscommerce.domain.inventory.dto.ReceiveStockRequest;
import com.petopscommerce.domain.inventory.dto.StockJobResponse;
import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.dto.StockSearchCondition;
import com.petopscommerce.domain.inventory.dto.TransferStockRequest;
import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.entity.LocationStatus;
import com.petopscommerce.domain.inventory.entity.LocationType;
import com.petopscommerce.domain.inventory.entity.Lot;
import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.entity.StockJobStatus;
import com.petopscommerce.domain.inventory.entity.StockMovement;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import com.petopscommerce.domain.inventory.entity.StockQuantityBucket;
import com.petopscommerce.domain.inventory.repository.LocationRepository;
import com.petopscommerce.domain.inventory.repository.LotRepository;
import com.petopscommerce.domain.inventory.repository.StockJobRepository;
import com.petopscommerce.domain.inventory.repository.StockMovementRepository;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import com.petopscommerce.domain.inventory.repository.WarehouseRepository;
import com.petopscommerce.domain.inventory.service.operation.StockOperationCommand;
import com.petopscommerce.domain.inventory.service.operation.StockOperationResult;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.repository.OrderItemRepository;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.domain.product.repository.ProductRepository;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.service.BusinessNumberGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * - 현재고 facade 비즈니스 로직
 * - 조회, 재고 명령, 출고 workflow 검증과 job 생성을 한 곳에서 조율
 */
@Service
@Transactional
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final LotRepository lotRepository;
    private final StockJobRepository stockJobRepository;
    private final StockMovementRepository stockMovementRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BusinessNumberGenerator businessNumberGenerator;
    private final StockOperationService stockOperationService;
    private final Clock clock;

    /**
     * - 생성자 주입
     */
    public StockService(
            StockRepository stockRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            LocationRepository locationRepository,
            LotRepository lotRepository,
            StockJobRepository stockJobRepository,
            StockMovementRepository stockMovementRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            BusinessNumberGenerator businessNumberGenerator,
            StockOperationService stockOperationService,
            Clock clock
    ) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.lotRepository = lotRepository;
        this.stockJobRepository = stockJobRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.businessNumberGenerator = businessNumberGenerator;
        this.stockOperationService = stockOperationService;
        this.clock = clock;
    }

    /**
     * - 현재고 목록 조회
     * - 기본적으로 0수량 row는 제외하고, includeZero=true일 때만 포함
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param includeZero 0수량 포함 여부
     * @return 현재고 응답 목록
     */
    @Transactional(readOnly = true)
    public List<StockResponse> getStocks(Long productId, Long warehouseId, Long locationId) {
        return getStocks(productId, warehouseId, locationId, false);
    }

    /**
     * - 현재고 목록 조회
     * - includeZero=true이면 0수량 row까지 포함
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param includeZero 0수량 포함 여부
     * @return 현재고 응답 목록
     */
    @Transactional(readOnly = true)
    public List<StockResponse> getStocks(Long productId, Long warehouseId, Long locationId, boolean includeZero) {
        StockSearchCondition condition = new StockSearchCondition(productId, warehouseId, locationId, includeZero);

        return stockRepository.searchStocks(condition).stream()
                .map(StockResponse::from)
                .toList();
    }

    /**
     * - 현재고 단건 조회
     * - 없으면 404 응답
     *
     * @param stockId 현재고 ID
     * @return 현재고 단건 응답
     */
    @Transactional(readOnly = true)
    public StockResponse getStock(Long stockId) {
        return StockResponse.from(getStockEntity(stockId));
    }

    /**
     * - 입고성 현재고 생성/증가
     * - 기준정보와 LOT를 확인한 뒤 공통 수량 엔진에 RECEIVE command를 전달
     *
     * @param request 입고성 현재고 생성 요청
     * @return 변경된 현재고 응답
     */
    public StockResponse receiveStock(ReceiveStockRequest request) {
        // 단계 1: 상품/창고/location 존재와 소속 관계 검증
        // 결과: 존재하지 않는 기준정보로 현재고가 생성되는 것을 차단
        validateProduct(request.productId());
        validateWarehouse(request.warehouseId());
        Location location = getActiveLocation(request.locationId(), request.warehouseId());

        // 단계 2: LOT 조회 또는 생성
        // 결과: 같은 LOT 속성은 같은 lot_id로 재사용
        LocalDate receivedDate = resolveReceivedDate(request.lot4());
        Lot lot = getOrCreateLot(request.productId(), request.lot1(), request.lot2(), request.lot3(), receivedDate, request.lot5());

        // 단계 3: job 생성 후 수량 엔진 실행
        // 결과: 현재고 생성/증가와 RECEIVE_IN movement 저장
        StockJob stockJob = saveJob(StockJob.createInbound(nextStockJobNo(), request.warehouseId(), request.reason(), LocalDateTime.now(clock)));
        StockOperationResult result = stockOperationService.execute(StockOperationCommand.increase(
                stockJob,
                request.productId(),
                request.warehouseId(),
                location.getId(),
                lot.getId(),
                StockQuantityBucket.AVAILABLE,
                request.quantity(),
                StockMovementType.RECEIVE_IN,
                request.reason()
        ));

        return StockResponse.from(result.changedStock());
    }

    /**
     * - location 간 가용 재고 이동
     * - 출발 현재고 검증 후 공통 수량 엔진에 TRANSFER command를 전달
     *
     * @param request location 간 재고 이동 요청
     * @return 도착 location 현재고 응답
     */
    public StockResponse transferStock(TransferStockRequest request) {
        Stock sourceStock = getStockEntity(request.fromStockId());
        Location toLocation = getActiveLocation(request.toLocationId(), sourceStock.getWarehouseId());

        if (sourceStock.getLocationId().equals(toLocation.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source and target location must be different");
        }

        StockJob stockJob = saveJob(StockJob.createTransfer(nextStockJobNo(), sourceStock.getWarehouseId(), request.reason(), LocalDateTime.now(clock)));
        StockOperationResult result = stockOperationService.execute(StockOperationCommand.move(
                stockJob,
                sourceStock.getProductId(),
                sourceStock.getWarehouseId(),
                sourceStock.getLocationId(),
                toLocation.getId(),
                sourceStock.getLotId(),
                sourceStock.getLotId(),
                StockQuantityBucket.AVAILABLE,
                StockQuantityBucket.AVAILABLE,
                request.quantity(),
                StockMovementType.TRANSFER_OUT,
                StockMovementType.TRANSFER_IN,
                request.reason()
        ));

        return StockResponse.from(result.changedStock());
    }

    /**
     * - LOT 속성 변경
     * - 같은 location에서 기존 LOT 재고를 새 LOT 재고로 이동하고 target이 있으면 병합
     *
     * @param request LOT 속성 변경 요청
     * @return 변경 후 LOT 현재고 응답
     */
    public StockResponse changeLot(ChangeLotRequest request) {
        Stock sourceStock = getStockEntity(request.stockId());
        LocalDate receivedDate = resolveReceivedDate(request.lot4());
        Lot targetLot = getOrCreateLot(sourceStock.getProductId(), request.lot1(), request.lot2(), request.lot3(), receivedDate, request.lot5());

        if (sourceStock.getLotId().equals(targetLot.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "target lot must be different from source lot");
        }

        StockJob stockJob = saveJob(StockJob.createLotChange(nextStockJobNo(), sourceStock.getWarehouseId(), request.reason(), LocalDateTime.now(clock)));
        StockOperationResult result = stockOperationService.execute(StockOperationCommand.move(
                stockJob,
                sourceStock.getProductId(),
                sourceStock.getWarehouseId(),
                sourceStock.getLocationId(),
                sourceStock.getLocationId(),
                sourceStock.getLotId(),
                targetLot.getId(),
                StockQuantityBucket.AVAILABLE,
                StockQuantityBucket.AVAILABLE,
                request.quantity(),
                StockMovementType.LOT_CHANGE_OUT,
                StockMovementType.LOT_CHANGE_IN,
                request.reason()
        ));

        return StockResponse.from(result.changedStock());
    }

    /**
     * - 수동 재고 조정
     * - quantity 부호로 증가/차감을 구분하고 공통 수량 엔진에 ADJUST command를 전달
     *
     * @param request 수동 재고 조정 요청
     * @return 변경된 현재고 응답
     */
    public StockResponse adjustStock(AdjustStockRequest request) {
        if (request.quantity() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must not be zero");
        }

        Stock stock = getStockEntity(request.stockId());
        StockJob stockJob = saveJob(StockJob.createAdjustment(nextStockJobNo(), stock.getWarehouseId(), request.reason(), LocalDateTime.now(clock)));
        StockOperationCommand command = request.quantity() > 0
                ? StockOperationCommand.increase(
                        stockJob,
                        stock.getProductId(),
                        stock.getWarehouseId(),
                        stock.getLocationId(),
                        stock.getLotId(),
                        StockQuantityBucket.AVAILABLE,
                        request.quantity(),
                        StockMovementType.ADJUST,
                        request.reason()
                )
                : StockOperationCommand.decrease(
                        stockJob,
                        stock.getProductId(),
                        stock.getWarehouseId(),
                        stock.getLocationId(),
                        stock.getLotId(),
                        StockQuantityBucket.AVAILABLE,
                        Math.abs(request.quantity()),
                        StockMovementType.ADJUST,
                        request.reason()
                );
        StockOperationResult result = stockOperationService.execute(command);

        return StockResponse.from(result.changedStock());
    }

    /**
     * - 재고 할당
     * - 주문/주문상품 검증 후 같은 stock 안에서 available을 working으로 전환
     *
     * @param request 재고 할당 요청
     * @return 재고 작업 응답
     */
    public StockJobResponse allocate(AllocateStockRequest request) {
        // 단계 1: 재고와 주문 품목 검증
        // 결과: 다른 상품 재고를 주문상품에 잘못 할당하는 것을 차단
        Stock sourceStock = getStockEntity(request.stockId());
        OrderItem orderItem = getOrderItem(request.orderId(), request.orderItemId());
        validateOrderItemProduct(sourceStock, orderItem);

        // 단계 2: 출고 작업 job 생성 후 수량 엔진 실행
        // 결과: total은 유지하고 available 감소, working 증가, ALLOCATE movement 저장
        StockJob stockJob = saveJob(StockJob.createSalesShipment(nextStockJobNo(), sourceStock.getWarehouseId(), request.orderId(), request.reason()));
        stockOperationService.execute(StockOperationCommand.convertBucket(
                stockJob,
                sourceStock.getProductId(),
                sourceStock.getWarehouseId(),
                sourceStock.getLocationId(),
                sourceStock.getLotId(),
                StockQuantityBucket.AVAILABLE,
                StockQuantityBucket.WORKING,
                request.quantity(),
                StockMovementType.ALLOCATE,
                request.reason()
        ));

        return StockJobResponse.from(stockJob);
    }

    /**
     * - PICKTO 이동
     * - 할당 movement에서 원천 재고를 찾고 PICKTO location으로 작업수량을 이동
     *
     * @param request 재고 PICK 요청
     * @return 재고 작업 응답
     */
    public StockJobResponse pick(PickStockRequest request) {
        // 단계 1: job 상태와 최초 할당 이력 검증
        // 결과: 할당 완료된 작업만 PICKTO 이동 가능
        StockJob stockJob = getJob(request.jobId());
        validateJobStatus(stockJob, StockJobStatus.ALLOCATED);
        StockMovement allocationMovement = getMovement(stockJob.getId(), StockMovementType.ALLOCATE);
        Stock allocatedStock = getStockEntity(allocationMovement.getStockId());

        // 단계 2: PICKTO location 검증 후 수량 엔진 실행
        // 결과: PICK_OUT/PICK_IN 한 쌍의 movement와 PICKTO 현재고 생성 또는 증가
        Location picktoLocation = getPicktoLocation(request.picktoLocationId(), stockJob.getWarehouseId());
        stockOperationService.execute(StockOperationCommand.move(
                stockJob,
                allocatedStock.getProductId(),
                allocatedStock.getWarehouseId(),
                allocatedStock.getLocationId(),
                picktoLocation.getId(),
                allocatedStock.getLotId(),
                allocatedStock.getLotId(),
                StockQuantityBucket.WORKING,
                StockQuantityBucket.WORKING,
                request.quantity(),
                StockMovementType.PICK_OUT,
                StockMovementType.PICK_IN,
                request.reason()
        ));

        // 단계 3: job 상태 변경
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
        // 단계 1: job 상태와 PICK_IN 이력 검증
        // 결과: PICKTO 이동이 끝난 작업만 실제 출고 가능
        StockJob stockJob = getJob(request.jobId());
        validateJobStatus(stockJob, StockJobStatus.PICKED);
        StockMovement pickInMovement = getMovement(stockJob.getId(), StockMovementType.PICK_IN);
        Stock picktoStock = getStockEntity(pickInMovement.getStockId());
        getPicktoLocation(picktoStock.getLocationId(), stockJob.getWarehouseId());

        // 단계 2: 수량 엔진 실행 후 job 완료 처리
        // 결과: PICKTO total/working 감소와 SHIP_OUT movement 저장
        stockOperationService.execute(StockOperationCommand.decrease(
                stockJob,
                picktoStock.getProductId(),
                picktoStock.getWarehouseId(),
                picktoStock.getLocationId(),
                picktoStock.getLotId(),
                StockQuantityBucket.WORKING,
                request.quantity(),
                StockMovementType.SHIP_OUT,
                request.reason()
        ));
        stockJob.markShipped(LocalDateTime.now(clock));

        return StockJobResponse.from(stockJob);
    }

    private Stock getStockEntity(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));
    }

    private void validateProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        }
    }

    private void validateWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "warehouse not found");
        }
    }

    private Location getActiveLocation(Long locationId, Long warehouseId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "location not found"));

        if (location.getStatus() != LocationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "location is not active");
        }

        if (!warehouseId.equals(location.getWarehouseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "location warehouse does not match request warehouse");
        }

        return location;
    }

    private Location getPicktoLocation(Long picktoLocationId, Long warehouseId) {
        Location location = locationRepository.findByIdAndLocationTypeAndStatus(picktoLocationId, LocationType.PICKTO, LocationStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pickto location not found"));

        if (!warehouseId.equals(location.getWarehouseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pickto location warehouse does not match stock job");
        }

        return location;
    }

    private Lot getOrCreateLot(Long productId, String lot1, String lot2, LocalDate lot3, LocalDate lot4, String lot5) {
        return lotRepository.findByProductIdAndLot1AndLot2AndLot3AndLot4AndLot5(productId, lot1, lot2, lot3, lot4, lot5)
                .orElseGet(() -> lotRepository.save(Lot.create(
                        businessNumberGenerator.generate(BusinessNumberType.LOT),
                        productId,
                        lot1,
                        lot2,
                        lot3,
                        lot4,
                        lot5
                )));
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

    private StockJob saveJob(StockJob stockJob) {
        return stockJobRepository.save(stockJob);
    }

    private String nextStockJobNo() {
        return businessNumberGenerator.generate(BusinessNumberType.STOCK_MOVE);
    }

    private LocalDate resolveReceivedDate(LocalDate receivedDate) {
        return receivedDate != null ? receivedDate : LocalDate.now(clock);
    }
}
