package com.petopscommerce.domain.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * - 창고 생성 요청 DTO
 *
 * @param code 창고 코드
 * @param name 창고명
 */
public record CreateWarehouseRequest(
        @NotBlank
        @Size(max = 50)
        String code,
        @NotBlank
        @Size(max = 100)
        String name
) {
}