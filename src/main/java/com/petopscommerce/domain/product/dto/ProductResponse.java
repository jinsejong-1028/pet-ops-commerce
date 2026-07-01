package com.petopscommerce.domain.product.dto;

import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "상품 응답")
public record ProductResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,

        @Schema(description = "상품 카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "상품명", example = "프리미엄 강아지 사료")
        String name,

        @Schema(description = "상품 설명", example = "닭고기 기반의 프리미엄 사료")
        String description,

        @Schema(description = "판매가", example = "32000")
        Integer price,

        @Schema(description = "상품 판매 상태", example = "ON_SALE")
        ProductSaleStatus saleStatus,

        @Schema(description = "생성 일시", example = "2026-07-01T10:00:00")
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