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
 * - 창고 출고 주문 품목 Entity
 * - 품목별 지시/할당/피킹/출고 수량을 누적 관리
 */
@Entity
@Table(name = "shipment_order_items")
public class ShipmentOrderItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_order_id", nullable = false)
    private Long shipmentOrderId;

    @Column(name = "sales_order_item_id", nullable = false)
    private Long salesOrderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity;

    @Column(name = "picked_quantity", nullable = false)
    private Integer pickedQuantity;

    @Column(name = "shipped_quantity", nullable = false)
    private Integer shippedQuantity;

    /**
     * - 출고 주문 품목 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentOrderItemStatus status;

    protected ShipmentOrderItem() {
        // JPA 기본 생성자
    }

    private ShipmentOrderItem(Long shipmentOrderId, Long salesOrderItemId, Long productId, Integer orderQuantity, Integer allocatedQuantity, Integer pickedQuantity, Integer shippedQuantity, ShipmentOrderItemStatus status) {
        this.shipmentOrderId = shipmentOrderId;
        this.salesOrderItemId = salesOrderItemId;
        this.productId = productId;
        this.orderQuantity = orderQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.pickedQuantity = pickedQuantity;
        this.shippedQuantity = shippedQuantity;
        this.status = status;
    }

    /**
     * - 출고 주문 품목 생성
     * - 할당/피킹/출고 수량은 0으로 시작
     *
     * @param shipmentOrderId 출고 주문 ID
     * @param salesOrderItem 판매 주문 품목
     * @return 출고 주문 품목 Entity
     */
    public static ShipmentOrderItem create(Long shipmentOrderId, SalesOrderItem salesOrderItem) {
        return new ShipmentOrderItem(
                shipmentOrderId,
                salesOrderItem.getId(),
                salesOrderItem.getProductId(),
                salesOrderItem.getOrderQuantity(),
                0,
                0,
                0,
                ShipmentOrderItemStatus.CREATED
        );
    }

    public Long getId() {
        return id;
    }

    public Long getShipmentOrderId() {
        return shipmentOrderId;
    }

    public Long getSalesOrderItemId() {
        return salesOrderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getOrderQuantity() {
        return orderQuantity;
    }

    public Integer getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public Integer getPickedQuantity() {
        return pickedQuantity;
    }

    public Integer getShippedQuantity() {
        return shippedQuantity;
    }

    public ShipmentOrderItemStatus getStatus() {
        return status;
    }
}