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
 * - 관리자 판매 주문 품목 Entity
 * - 고객 주문 품목을 내부 판매 주문 품목으로 생성한 snapshot
 */
@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "customer_order_item_id", nullable = false)
    private Long customerOrderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "line_amount", nullable = false)
    private Integer lineAmount;

    /**
     * - 판매 주문 품목 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SalesOrderItemStatus status;

    protected SalesOrderItem() {
        // JPA 기본 생성자
    }

    private SalesOrderItem(Long salesOrderId, Long customerOrderItemId, Long productId, Integer orderQuantity, Integer unitPrice, Integer lineAmount, SalesOrderItemStatus status) {
        this.salesOrderId = salesOrderId;
        this.customerOrderItemId = customerOrderItemId;
        this.productId = productId;
        this.orderQuantity = orderQuantity;
        this.unitPrice = unitPrice;
        this.lineAmount = lineAmount;
        this.status = status;
    }

    /**
     * - 판매 주문 품목 생성
     * - 고객 주문 품목 수량/금액을 CREATED 상태 snapshot으로 복사
     *
     * @param salesOrderId 판매 주문 ID
     * @param orderItem 고객 주문 품목
     * @return 판매 주문 품목 Entity
     */
    public static SalesOrderItem create(Long salesOrderId, OrderItem orderItem) {
        return new SalesOrderItem(
                salesOrderId,
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getLineAmount(),
                SalesOrderItemStatus.CREATED
        );
    }

    /**
     * - 판매 주문 품목 확정
     * - 판매 주문 확정 시 출고 주문 품목 생성 대상으로 전환
     */
    public void confirm() {
        this.status = SalesOrderItemStatus.CONFIRMED;
    }

    /**
     * - 판매 주문 품목 취소
     * - 판매 주문 취소 시 품목도 함께 취소
     */
    public void cancel() {
        this.status = SalesOrderItemStatus.CANCELED;
    }

    public Long getId() {
        return id;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public Long getCustomerOrderItemId() {
        return customerOrderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getOrderQuantity() {
        return orderQuantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public Integer getLineAmount() {
        return lineAmount;
    }

    public SalesOrderItemStatus getStatus() {
        return status;
    }
}