package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * - 현재고 DB 접근 객체
 * - stocks 테이블 CRUD/조건 조회 담당
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * - 현재고 목록 조건 조회
     * - null 조건은 필터에서 제외
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @return 현재고 목록
     */
    @Query("""
            select stock
            from Stock stock
            where (:productId is null or stock.productId = :productId)
              and (:warehouseId is null or stock.warehouseId = :warehouseId)
              and (:locationId is null or stock.locationId = :locationId)
            """)
    List<Stock> findStocks(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("locationId") Long locationId
    );
}
