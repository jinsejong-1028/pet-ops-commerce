package com.petopscommerce.domain.order.entity;

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
 * - 주문 상품 Entity
 * - customer_order_items 테이블 매핑
 */
@Entity
@Table(name = "customer_order_items")
public class OrderItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 고객 주문 ID
     * - DB FK 제약 대신 Service 저장 흐름에서 연결
     */
    @Column(name = "customer_order_id", nullable = false)
    private Long orderId;

    /**
     * - 고객 주문 품목 상태
     * - 판매 주문 확정/취소 시 고객 주문 header와 함께 변경
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderItemStatus status;

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

    protected OrderItem() {
        // JPA 기본 생성자
    }

    private OrderItem(Long orderId, OrderItemStatus status, Long productId, Integer quantity, Integer unitPrice, Integer lineAmount) {
        this.orderId = orderId;
        this.status = status;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineAmount = lineAmount;
    }

    /**
     * - 신규 주문 상품 생성
     * - 주문 당시 단가와 라인 금액을 CREATED 상태 snapshot으로 저장
     *
     * @param orderId 고객 주문 ID
     * @param productId 상품 ID
     * @param quantity 주문 수량
     * @param unitPrice 주문 당시 상품 단가
     * @return 신규 주문 상품 Entity
     */
    public static OrderItem create(Long orderId, Long productId, Integer quantity, Integer unitPrice) {
        return new OrderItem(orderId, OrderItemStatus.CREATED, productId, quantity, unitPrice, quantity * unitPrice);
    }

    /**
     * - 고객 주문 품목 확정
     * - 판매 주문 확정 시 품목도 함께 확정
     */
    public void confirm() {
        this.status = OrderItemStatus.CONFIRMED;
    }

    /**
     * - 고객 주문 품목 취소
     * - 판매 주문 취소 시 품목도 함께 취소
     */
    public void cancel() {
        this.status = OrderItemStatus.CANCELED;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public OrderItemStatus getStatus() {
        return status;
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
}