package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.AdjustStockRequest;
import com.petopscommerce.domain.inventory.dto.ReceiveStockRequest;
import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.dto.TransferStockRequest;
import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.entity.LocationStatus;
import com.petopscommerce.domain.inventory.entity.Lot;
import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.entity.StockJob;
import com.petopscommerce.domain.inventory.repository.LocationRepository;
import com.petopscommerce.domain.inventory.repository.LotRepository;
import com.petopscommerce.domain.inventory.repository.StockJobRepository;
import com.petopscommerce.domain.inventory.repository.WarehouseRepository;
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

/**
 * - 관리자 재고 명령 비즈니스 로직
 * - 입고성 현재고 생성/증가와 수동 재고 조정 담당
 */
@Service
@Transactional
public class StockCommandService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final LotRepository lotRepository;
    private final StockJobRepository stockJobRepository;
    private final BusinessNumberGenerator businessNumberGenerator;
    private final StockOperationService stockOperationService;
    private final Clock clock;

    /**
     * - 생성자 주입
     *
     * @param productRepository 상품 DB 접근 객체
     * @param warehouseRepository 창고 DB 접근 객체
     * @param locationRepository location DB 접근 객체
     * @param lotRepository LOT DB 접근 객체
     * @param stockJobRepository 재고 작업 헤더 DB 접근 객체
     * @param businessNumberGenerator 업무 번호 생성기
     * @param stockOperationService 재고 수량 변경 공통 서비스
     * @param clock 기준 날짜 제공 객체
     */
    public StockCommandService(ProductRepository productRepository, WarehouseRepository warehouseRepository, LocationRepository locationRepository, LotRepository lotRepository, StockJobRepository stockJobRepository, BusinessNumberGenerator businessNumberGenerator, StockOperationService stockOperationService, Clock clock) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.lotRepository = lotRepository;
        this.stockJobRepository = stockJobRepository;
        this.businessNumberGenerator = businessNumberGenerator;
        this.stockOperationService = stockOperationService;
        this.clock = clock;
    }

    /**
     * - 입고성 현재고 생성/증가
     * - LOT는 속성값으로 찾고 없으면 LOT 업무번호를 채번해 생성
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

        // 단계 2: 입고일자 기본값 확정 후 LOT 조회 또는 생성
        // 결과: 같은 LOT 속성은 같은 lot_id로 재사용
        LocalDate receivedDate = request.lot4() != null ? request.lot4() : LocalDate.now(clock);
        Lot lot = getOrCreateLot(request, receivedDate);

        // 단계 3: 입고 작업 job 생성
        // 결과: RECEIVE_IN movement를 같은 jobNo로 추적 가능
        String jobNo = businessNumberGenerator.generate(BusinessNumberType.STOCK_MOVE);
        StockJob stockJob = stockJobRepository.save(StockJob.createInbound(jobNo, request.warehouseId(), request.reason(), LocalDateTime.now(clock)));

        // 단계 4: 현재고 생성 또는 증가
        // 결과: stock row가 없으면 만들고, 있으면 total/available만 증가
        Stock stock = stockOperationService.receive(
                stockJob,
                request.productId(),
                request.warehouseId(),
                location.getId(),
                lot.getId(),
                request.quantity(),
                request.reason()
        );

        return StockResponse.from(stock);
    }

    /**
     * - 수동 재고 조정
     * - quantity 부호로 증가/차감을 구분하고 차감은 가용수량 기준으로 검증
     *
     * @param request 수동 재고 조정 요청
     * @return 변경된 현재고 응답
     */
    public StockResponse adjustStock(AdjustStockRequest request) {
        if (request.quantity() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must not be zero");
        }

        Stock stock = stockOperationService.getStockForUpdate(request.stockId());
        String jobNo = businessNumberGenerator.generate(BusinessNumberType.STOCK_MOVE);
        StockJob stockJob = stockJobRepository.save(StockJob.createAdjustment(jobNo, stock.getWarehouseId(), request.reason(), LocalDateTime.now(clock)));
        Stock adjustedStock = stockOperationService.adjust(stockJob, stock, request.quantity(), request.reason());

        return StockResponse.from(adjustedStock);
    }

    /**
     * - location 간 가용 재고 이동
     * - 출발 현재고의 availableQuantity를 기준으로 이동 가능 여부 검증
     *
     * @param request location 간 재고 이동 요청
     * @return 도착 location 현재고 응답
     */
    public StockResponse transferStock(TransferStockRequest request) {
        Stock sourceStock = stockOperationService.getStockForUpdate(request.fromStockId());
        Location toLocation = getActiveLocation(request.toLocationId(), sourceStock.getWarehouseId());

        if (sourceStock.getLocationId().equals(toLocation.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source and target location must be different");
        }

        String jobNo = businessNumberGenerator.generate(BusinessNumberType.STOCK_MOVE);
        StockJob stockJob = stockJobRepository.save(StockJob.createTransfer(jobNo, sourceStock.getWarehouseId(), request.reason(), LocalDateTime.now(clock)));
        Stock targetStock = stockOperationService.moveAvailableToLocation(stockJob, sourceStock, toLocation.getId(), request.quantity(), request.reason());

        return StockResponse.from(targetStock);
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

    private Lot getOrCreateLot(ReceiveStockRequest request, LocalDate receivedDate) {
        return lotRepository.findByProductIdAndLot1AndLot2AndLot3AndLot4AndLot5(
                        request.productId(),
                        request.lot1(),
                        request.lot2(),
                        request.lot3(),
                        receivedDate,
                        request.lot5()
                )
                .orElseGet(() -> lotRepository.save(Lot.create(
                        businessNumberGenerator.generate(BusinessNumberType.LOT),
                        request.productId(),
                        request.lot1(),
                        request.lot2(),
                        request.lot3(),
                        receivedDate,
                        request.lot5()
                )));
    }
}