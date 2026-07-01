package com.petopscommerce.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * - 상품 생성 요청 DTO
 *
 * @param categoryId 상품 카테고리 ID
 * @param name 상품명
 * @param description 상품 설명
 * @param price 판매가
 */
@Schema(description = "상품 생성 요청")
public record CreateProductRequest(
        @Schema(description = "상품 카테고리 ID", example = "1")
        @NotNull(message = "categoryId is required")
        Long categoryId,

        @Schema(description = "상품명", example = "프리미엄 강아지 사료")
        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be 200 characters or less")
        String name,

        @Schema(description = "상품 설명", example = "닭고기 기반의 프리미엄 사료")
        String description,

        @Schema(description = "판매가", example = "32000")
        @NotNull(message = "price is required")
        @Min(value = 0, message = "price must be greater than or equal to 0")
        Integer price
) {
}