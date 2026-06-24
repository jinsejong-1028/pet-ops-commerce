package com.petopscommerce.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * - 상품 카테고리 Entity
 * - product_categories 테이블 매핑
 */
@Entity
@Table(name = "product_categories")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * - 화면 표시 순서
     * - 값이 작을수록 먼저 노출
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * - 카테고리 사용 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategoryStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected ProductCategory() {
        // JPA 기본 생성자
    }

    private ProductCategory(String name, Integer displayOrder, ProductCategoryStatus status) {
        this.name = name;
        this.displayOrder = displayOrder;
        this.status = status;
    }

    /**
     * - 신규 상품 카테고리 생성
     * - 기본 상태 ACTIVE
     *
     * @param name 카테고리명
     * @param displayOrder 화면 표시 순서
     * @return 신규 상품 카테고리 Entity
     */
    public static ProductCategory create(String name, Integer displayOrder) {
        return new ProductCategory(name, displayOrder, ProductCategoryStatus.ACTIVE);
    }

    /**
     * - 최초 저장 전 audit 시간 설정
     */
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * - 수정 저장 전 updatedAt 갱신
     */
    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public ProductCategoryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }
}