package com.petopscommerce.domain.product.dto;

import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;

import java.time.LocalDateTime;

/**
 * - 상품 응답 DTO
 *
 * @param id 상품 ID
 * @param categoryId 상품 카테고리 ID
 * @param name 상품명
 * @param description 상품 설명
 * @param price 판매가
 * @param saleStatus 상품 판매 상태
 * @param createdAt 생성 일시
 */
public record ProductResponse(
        Long id,
        Long categoryId,
        String name,
        String description,
        Integer price,
        ProductSaleStatus saleStatus,
        LocalDateTime createdAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param product 상품 Entity
     * @return 상품 응답 DTO
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategoryId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSaleStatus(),
                product.getCreatedAt()
        );
    }
}