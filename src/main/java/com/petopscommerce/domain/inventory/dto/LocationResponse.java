package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.entity.LocationStatus;
import com.petopscommerce.domain.inventory.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Location 응답")
public record LocationResponse(
        @Schema(description = "Location ID", example = "1")
        Long id,

        @Schema(description = "창고 ID", example = "1")
        Long warehouseId,

        @Schema(description = "Location 코드", example = "A-01-01")
        String code,

        @Schema(description = "Location명", example = "A구역 1열 1칸")
        String name,

        @Schema(description = "Location 유형", example = "NORMAL")
        LocationType locationType,

        @Schema(description = "Location 상태", example = "ACTIVE")
        LocationStatus status,

        @Schema(description = "생성 일시", example = "2026-07-01T10:00:00")
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