package com.petopscommerce.domain.product.dto;

import com.petopscommerce.domain.product.entity.ProductCategory;
import com.petopscommerce.domain.product.entity.ProductCategoryStatus;

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
public record ProductCategoryResponse(
        Long id,
        String name,
        Integer displayOrder,
        ProductCategoryStatus status,
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