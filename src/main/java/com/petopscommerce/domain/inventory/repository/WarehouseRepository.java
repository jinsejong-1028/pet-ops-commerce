package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 창고 DB 접근 객체
 * - warehouses 테이블 CRUD 담당
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * - 창고 코드 중복 확인
     *
     * @param code 창고 코드
     * @return 존재 여부
     */
    boolean existsByCode(String code);
}