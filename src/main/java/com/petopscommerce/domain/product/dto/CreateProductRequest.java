package com.petopscommerce.domain.product.dto;

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
public record CreateProductRequest(
        @NotNull(message = "categoryId is required")
        Long categoryId,

        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be 200 characters or less")
        String name,

        String description,

        @NotNull(message = "price is required")
        @Min(value = 0, message = "price must be greater than or equal to 0")
        Integer price
) {
}