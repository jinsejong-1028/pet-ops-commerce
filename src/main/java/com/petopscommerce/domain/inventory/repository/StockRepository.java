package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 현재고 DB 접근 객체
 * - 기본 CRUD와 QueryDSL 조건 조회 조합
 */
public interface StockRepository extends JpaRepository<Stock, Long>, StockRepositoryCustom {
}
