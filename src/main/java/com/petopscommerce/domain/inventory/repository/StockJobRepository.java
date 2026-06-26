package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.StockJob;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 재고 작업 헤더 DB 접근 객체
 * - stock_jobs 테이블 CRUD 담당
 */
public interface StockJobRepository extends JpaRepository<StockJob, Long> {
}
