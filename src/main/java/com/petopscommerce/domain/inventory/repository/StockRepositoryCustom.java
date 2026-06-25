package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.dto.StockSearchCondition;
import com.petopscommerce.domain.inventory.entity.Stock;

import java.util.List;

/**
 * - 현재고 QueryDSL 조회 계약
 * - 관리자 검색 조건이 늘어나는 흐름에 대비
 */
public interface StockRepositoryCustom {

    /**
     * - 현재고 목록 조건 조회
     * - null 조건은 QueryDSL where에서 제외
     *
     * @param condition 현재고 검색 조건
     * @return 현재고 목록
     */
    List<Stock> searchStocks(StockSearchCondition condition);
}
