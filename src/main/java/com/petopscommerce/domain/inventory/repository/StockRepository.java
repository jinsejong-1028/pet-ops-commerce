package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * - 현재고 DB 접근 객체
 * - 기본 CRUD와 QueryDSL 조건 조회 조합
 */
public interface StockRepository extends JpaRepository<Stock, Long>, StockRepositoryCustom {

    /**
     * - 현재고 잠금 조회
     * - 수량 변경 중 같은 stock row 동시 수정 방지
     *
     * @param stockId 현재고 ID
     * @return 현재고 Optional
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stock> findWithLockById(Long stockId);

    /**
     * - location 단위 현재고 조회
     * - PICKTO 입고 시 기존 현재고 재사용 여부 확인
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @return 현재고 Optional
     */
    Optional<Stock> findByProductIdAndWarehouseIdAndLocationIdAndLotId(Long productId, Long warehouseId, Long locationId, Long lotId);
}
