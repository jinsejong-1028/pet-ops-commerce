package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * - 창고 생성 요청 DTO
 *
 * @param code 창고 코드
 * @param name 창고명
 */
@Schema(description = "창고 생성 요청")
public record CreateWarehouseRequest(
        @Schema(description = "창고 코드", example = "WH-SEOUL")
        @NotBlank
        @Size(max = 50)
        String code,

        @Schema(description = "창고명", example = "서울 메인 창고")
        @NotBlank
        @Size(max = 100)
        String name
) {
}