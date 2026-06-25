package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.dto.StockSearchCondition;
import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * - 현재고 비즈니스 로직
 * - location 단위 재고 조회 담당
 */
@Service
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    /**
     * - 생성자 주입
     *
     * @param stockRepository 현재고 DB 접근 객체
     */
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * - 현재고 목록 조회
     * - 상품/창고/location 조건 선택 적용
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @return 현재고 응답 목록
     */
    public List<StockResponse> getStocks(Long productId, Long warehouseId, Long locationId) {
        StockSearchCondition condition = new StockSearchCondition(productId, warehouseId, locationId);

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
    public StockResponse getStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));

        return StockResponse.from(stock);
    }
}
