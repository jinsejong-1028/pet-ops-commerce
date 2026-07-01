package com.petopscommerce.domain.product.dto;

import com.petopscommerce.domain.product.entity.ProductCategory;
import com.petopscommerce.domain.product.entity.ProductCategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * - 상품 카테고리 응답 DTO
 *
 * @param id 카테고리 ID
 * @param name 카테고리명
 * @param displayOrder 화면 표시 순서
 * @param status 카테고리 상태
 * @param createdAt 생성 일시
 */
@Schema(description = "상품 카테고리 응답")
public record ProductCategoryResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long id,

        @Schema(description = "카테고리명", example = "사료")
        String name,

        @Schema(description = "화면 표시 순서", example = "1")
        Integer displayOrder,

        @Schema(description = "카테고리 상태", example = "ACTIVE")
        ProductCategoryStatus status,

        @Schema(description = "생성 일시", example = "2026-07-01T10:00:00")
        LocalDateTime createdAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param category 상품 카테고리 Entity
     * @return 상품 카테고리 응답 DTO
     */
    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDisplayOrder(),
                category.getStatus(),
                category.getCreatedAt()
        );
    }
}