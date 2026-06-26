package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.entity.LocationStatus;
import com.petopscommerce.domain.inventory.entity.LocationType;

import java.time.LocalDateTime;

/**
 * - Location 응답 DTO
 *
 * @param id location ID
 * @param warehouseId 창고 ID
 * @param code location 코드
 * @param name location명
 * @param locationType location 유형
 * @param status location 상태
 * @param createdAt 생성 일시
 */
public record LocationResponse(
        Long id,
        Long warehouseId,
        String code,
        String name,
        LocationType locationType,
        LocationStatus status,
        LocalDateTime createdAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param location location Entity
     * @return location 응답 DTO
     */
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getWarehouseId(),
                location.getCode(),
                location.getName(),
                location.getLocationType(),
                location.getStatus(),
                location.getCreatedAt()
        );
    }
}