package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.StockMovement;
import com.petopscommerce.domain.inventory.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * - 재고 이동 원장 DB 접근 객체
 * - stock_movements 테이블 CRUD/작업별 조회 담당
 */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /**
     * - 작업의 최신 특정 이동 유형 조회
     *
     * @param jobId 재고 작업 ID
     * @param movementType 재고 이동 유형
     * @return 재고 이동 원장 Optional
     */
    Optional<StockMovement> findFirstByJobIdAndMovementTypeOrderByIdDesc(Long jobId, StockMovementType movementType);
}
