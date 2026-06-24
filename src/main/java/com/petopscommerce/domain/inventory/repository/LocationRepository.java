package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - Location DB 접근 객체
 * - locations 테이블 CRUD 담당
 */
public interface LocationRepository extends JpaRepository<Location, Long> {
}
