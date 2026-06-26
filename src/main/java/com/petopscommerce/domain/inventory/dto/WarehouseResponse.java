package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.Warehouse;
import com.petopscommerce.domain.inventory.entity.WarehouseStatus;

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
public record WarehouseResponse(
        Long id,
        String code,
        String name,
        WarehouseStatus status,
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