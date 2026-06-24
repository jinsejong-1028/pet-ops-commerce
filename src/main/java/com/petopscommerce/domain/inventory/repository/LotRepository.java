package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - LOT DB 접근 객체
 * - lots 테이블 CRUD 담당
 */
public interface LotRepository extends JpaRepository<Lot, Long> {
}
