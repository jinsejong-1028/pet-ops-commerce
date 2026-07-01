package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.Warehouse;
import com.petopscommerce.domain.inventory.entity.WarehouseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * - 창고 응답 DTO
 *
 * @param id 창고 ID
 * @param code 창고 코드
 * @param name 창고명
 * @param status 창고 상태
 * @param createdAt 생성 일시
 */
@Schema(description = "창고 응답")
public record WarehouseResponse(
        @Schema(description = "창고 ID", example = "1")
        Long id,

        @Schema(description = "창고 코드", example = "WH-SEOUL")
        String code,

        @Schema(description = "창고명", example = "서울 메인 창고")
        String name,

        @Schema(description = "창고 상태", example = "ACTIVE")
        WarehouseStatus status,

        @Schema(description = "생성 일시", example = "2026-07-01T10:00:00")
        LocalDateTime createdAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param warehouse 창고 Entity
     * @return 창고 응답 DTO
     */
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getStatus(),
                warehouse.getCreatedAt()
        );
    }
}