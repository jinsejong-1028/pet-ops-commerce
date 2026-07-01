package com.petopscommerce.domain.inventory.service.operation;

import com.petopscommerce.domain.inventory.entity.Stock;

/**
 * - 재고 수량 변경 결과
 * - from/to가 모두 있는 작업과 단일 stock 작업을 같은 반환 모델로 표현
 *
 * @param sourceStock 출발 또는 단일 변경 현재고
 * @param targetStock 도착 현재고
 */
public record StockOperationResult(
        Stock sourceStock,
        Stock targetStock
) {

    /**
     * - 응답에 우선 사용할 현재고
     * - 도착 재고가 있으면 도착 재고를, 없으면 출발/단일 재고를 반환
     *
     * @return 대표 변경 현재고
     */
    public Stock changedStock() {
        return targetStock != null ? targetStock : sourceStock;
    }
}