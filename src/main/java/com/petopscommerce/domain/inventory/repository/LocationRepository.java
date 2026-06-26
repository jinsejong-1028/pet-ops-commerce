package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.entity.LocationStatus;
import com.petopscommerce.domain.inventory.entity.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * - Location DB 접근 객체
 * - locations 테이블 CRUD 담당
 */
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * - 활성 location 유형 조회
     *
     * @param id location ID
     * @param locationType location 유형
     * @param status location 상태
     * @return location Optional
     */
    Optional<Location> findByIdAndLocationTypeAndStatus(Long id, LocationType locationType, LocationStatus status);
}
