package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 창고 DB 접근 객체
 * - warehouses 테이블 CRUD 담당
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
