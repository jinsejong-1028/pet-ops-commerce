package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.AdjustStockRequest;
import com.petopscommerce.domain.inventory.dto.AllocateStockRequest;
import com.petopscommerce.domain.inventory.dto.ChangeLotRequest;
import com.petopscommerce.domain.inventory.dto.OutboundStockRequest;
import com.petopscommerce.domain.inventory.dto.PickStockRequest;
import com.petopscommerce.domain.inventory.dto.ReceiveStockRequest;
import com.petopscommerce.domain.inventory.dto.StockJobResponse;
import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.dto.TransferStockRequest;
import com.petopscommerce.domain.inventory.service.StockService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * - 현재고 API 단일 진입점
 * - 조회, 입고성 생성/증가, 이동/조정, LOT 변경, 할당/PICK/출고 요청 처리
 */
@Tag(name = "Inventory", description = "창고, location, 현재고와 재고 작업 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/stocks")
public class StockController {

    private final StockService stockService;

    /**
     * - 생성자 주입
     * - Controller는 stock API 진입점만 담당하고, 업무 검증과 수량 처리는 StockService로 위임합니다.
     *
     * @param stockService 현재고 facade 비즈니스 로직
     */
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * - 현재고 목록 조회
     * - productId, warehouseId, locationId 조건 선택 적용
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param includeZero 0수량 현재고 포함 여부
     * @return 현재고 목록 공통 응답
     */
    @Operation(summary = "현재고 목록 조회", description = "상품, 창고, location 조건으로 location 단위 현재고를 조회합니다. 기본값은 0수량 row 제외입니다.")
    @GetMapping
    public ApiResponse<List<StockResponse>> getStocks(
            @Parameter(description = "상품 ID", example = "1") @RequestParam(required = false) Long productId,
            @Parameter(description = "창고 ID", example = "1") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "Location ID", example = "1") @RequestParam(required = false) Long locationId,
            @Parameter(description = "0수량 현재고 포함 여부", example = "false") @RequestParam(defaultValue = "false") boolean includeZero
    ) {
        if (includeZero) {
            return ApiResponse.ok(stockService.getStocks(productId, warehouseId, locationId, true));
        }

        return ApiResponse.ok(stockService.getStocks(productId, warehouseId, locationId));
    }

    /**
     * - 현재고 단건 조회
     *
     * @param stockId 현재고 ID
     * @return 현재고 단건 공통 응답
     */
    @Operation(summary = "현재고 단건 조회", description = "현재고 ID로 총수량, 작업수량, 가용수량을 조회합니다.")
    @GetMapping("/{stockId}")
    public ApiResponse<StockResponse> getStock(
            @Parameter(description = "현재고 ID", example = "1") @PathVariable Long stockId
    ) {
        return ApiResponse.ok(stockService.getStock(stockId));
    }

    /**
     * - 입고성 현재고 생성/증가 API
     *
     * @param request 입고성 현재고 생성 요청
     * @return 현재고 응답
     */
    @Operation(summary = "입고성 현재고 생성 또는 증가", description = "상품, 창고, location, LOT 기준으로 현재고를 생성하거나 기존 현재고 수량을 증가시킵니다.")
    @PostMapping
    public ApiResponse<StockResponse> receiveStock(@Valid @RequestBody ReceiveStockRequest request) {
        return ApiResponse.ok(stockService.receiveStock(request));
    }

    /**
     * - location 간 가용 재고 이동 API
     *
     * @param request 재고 이동 요청
     * @return 도착 location 현재고 응답
     */
    @Operation(summary = "Location 간 재고 이동", description = "출발 현재고의 가용수량을 도착 location으로 이동합니다.")
    @PostMapping("/transfer")
    public ApiResponse<StockResponse> transferStock(@Valid @RequestBody TransferStockRequest request) {
        return ApiResponse.ok(stockService.transferStock(request));
    }

    /**
     * - LOT 속성 변경 API
     *
     * @param request LOT 속성 변경 요청
     * @return 변경 후 LOT 현재고 응답
     */
    @Operation(summary = "LOT 속성 변경", description = "같은 location 안에서 기존 LOT 재고를 변경 후 LOT 재고로 이동합니다. 대상 LOT 현재고가 있으면 병합됩니다.")
    @PostMapping("/change-lot")
    public ApiResponse<StockResponse> changeLot(@Valid @RequestBody ChangeLotRequest request) {
        return ApiResponse.ok(stockService.changeLot(request));
    }

    /**
     * - 수동 재고 조정 API
     *
     * @param request 수동 재고 조정 요청
     * @return 현재고 응답
     */
    @Operation(summary = "수동 재고 조정", description = "현재고 수량을 운영자가 직접 증가 또는 차감합니다.")
    @PostMapping("/adjust")
    public ApiResponse<StockResponse> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        return ApiResponse.ok(stockService.adjustStock(request));
    }

    /**
     * - 재고 할당 API
     *
     * @param request 재고 할당 요청
     * @return 재고 작업 응답
     */
    @Operation(summary = "재고 할당", description = "가용수량을 작업수량으로 전환해 주문 출고 작업에 재고를 할당합니다.")
    @PostMapping("/allocate")
    public ApiResponse<StockJobResponse> allocate(@Valid @RequestBody AllocateStockRequest request) {
        return ApiResponse.ok(stockService.allocate(request));
    }

    /**
     * - 재고 PICK API
     *
     * @param request 재고 PICK 요청
     * @return 재고 작업 응답
     */
    @Operation(summary = "재고 PICK", description = "할당된 재고를 PICKTO location으로 이동합니다.")
    @PostMapping("/pick")
    public ApiResponse<StockJobResponse> pick(@Valid @RequestBody PickStockRequest request) {
        return ApiResponse.ok(stockService.pick(request));
    }

    /**
     * - 재고 출고 API
     *
     * @param request 재고 출고 요청
     * @return 재고 작업 응답
     */
    @Operation(summary = "재고 출고 확정", description = "PICK된 재고를 출고 확정하고 현재고 총수량과 작업수량을 차감합니다.")
    @PostMapping("/outbound")
    public ApiResponse<StockJobResponse> outbound(@Valid @RequestBody OutboundStockRequest request) {
        return ApiResponse.ok(stockService.outbound(request));
    }
}