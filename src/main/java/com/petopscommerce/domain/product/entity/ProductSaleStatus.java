package com.petopscommerce.domain.product.entity;

/**
 * - 상품 판매 상태
 * - 상품 노출/판매 가능 여부 기준
 */
public enum ProductSaleStatus {
    /** - 판매 중 */
    ON_SALE,

    /** - 품절 */
    SOLD_OUT,

    /** - 판매 중지 */
    STOPPED
}