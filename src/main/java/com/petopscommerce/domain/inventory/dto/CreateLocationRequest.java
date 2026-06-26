package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * - Location 생성 요청 DTO
 *
 * @param warehouseId 창고 ID
 * @param code location 코드
 * @param name location명
 * @param locationType location 유형
 */
public record CreateLocationRequest(
        @NotNull
        Long warehouseId,
        @NotBlank
        @Size(max = 50)
        String code,
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        LocationType locationType
) {
}