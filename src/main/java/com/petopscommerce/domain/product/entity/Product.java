package com.petopscommerce.domain.product.entity;

import com.petopscommerce.global.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * - 상품 Entity
 * - products 테이블 매핑
 */
@Entity
@Table(name = "products")
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 상품 카테고리 ID
     * - DB FK 제약 대신 Service에서 존재 여부 검증
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    /**
     * - 판매가
     * - DB check constraint로 0 이상 보장
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * - 상품 판매 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 30)
    private ProductSaleStatus saleStatus;

    protected Product() {
        // JPA 기본 생성자
    }

    private Product(Long categoryId, String name, String description, Integer price, ProductSaleStatus saleStatus) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.saleStatus = saleStatus;
    }

    /**
     * - 신규 상품 생성
     * - 기본 판매 상태 ON_SALE
     *
     * @param categoryId 상품 카테고리 ID
     * @param name 상품명
     * @param description 상품 설명
     * @param price 판매가
     * @return 신규 상품 Entity
     */
    public static Product create(Long categoryId, String name, String description, Integer price) {
        return new Product(categoryId, name, description, price, ProductSaleStatus.ON_SALE);
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPrice() {
        return price;
    }

    public ProductSaleStatus getSaleStatus() {
        return saleStatus;
    }
}
