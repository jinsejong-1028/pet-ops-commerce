package com.petopscommerce.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * - 주문 상품 Entity
 * - order_items 테이블 매핑
 */
@Entity
@Table(name = "order_items")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 주문 ID
     * - DB FK 제약 대신 Service 저장 흐름에서 연결
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * - 상품 ID
     * - 주문 당시 구매 상품
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * - 주문 당시 상품 단가
     * - 상품 가격 변경 이후에도 과거 주문 금액 보존
     */
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    /**
     * - 주문 라인 금액
     * - quantity * unitPrice
     */
    @Column(name = "line_amount", nullable = false)
    private Integer lineAmount;

    /**
     * - 생성 일시
     * - order_items 테이블은 수정 audit 컬럼이 없어 생성 audit만 직접 매핑
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * - 생성자 ID
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    protected OrderItem() {
        // JPA 기본 생성자
    }

    private OrderItem(Long orderId, Long productId, Integer quantity, Integer unitPrice, Integer lineAmount) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineAmount = lineAmount;
    }

    /**
     * - 신규 주문 상품 생성
     * - 주문 당시 단가와 라인 금액을 스냅샷으로 저장
     *
     * @param orderId 주문 ID
     * @param productId 상품 ID
     * @param quantity 주문 수량
     * @param unitPrice 주문 당시 상품 단가
     * @return 신규 주문 상품 Entity
     */
    public static OrderItem create(Long orderId, Long productId, Integer quantity, Integer unitPrice) {
        return new OrderItem(orderId, productId, quantity, unitPrice, quantity * unitPrice);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public Integer getLineAmount() {
        return lineAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
