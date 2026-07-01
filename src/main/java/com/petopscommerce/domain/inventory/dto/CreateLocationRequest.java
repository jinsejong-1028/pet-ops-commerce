package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.LocationType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Location 생성 요청")
public record CreateLocationRequest(
        @Schema(description = "창고 ID", example = "1")
        @NotNull
        Long warehouseId,

        @Schema(description = "Location 코드", example = "A-01-01")
        @NotBlank
        @Size(max = 50)
        String code,

        @Schema(description = "Location명", example = "A구역 1열 1칸")
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(description = "Location 유형", example = "NORMAL")
        @NotNull
        LocationType locationType
) {
}