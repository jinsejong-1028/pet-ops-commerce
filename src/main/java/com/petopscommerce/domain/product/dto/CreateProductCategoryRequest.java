package com.petopscommerce.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * - 상품 카테고리 생성 요청 DTO
 *
 * @param name 카테고리명
 * @param displayOrder 화면 표시 순서
 */
@Schema(description = "상품 카테고리 생성 요청")
public record CreateProductCategoryRequest(
        @Schema(description = "카테고리명", example = "사료")
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be 100 characters or less")
        String name,

        @Schema(description = "화면 표시 순서", example = "1")
        @NotNull(message = "displayOrder is required")
        @Min(value = 0, message = "displayOrder must be greater than or equal to 0")
        Integer displayOrder
) {
}