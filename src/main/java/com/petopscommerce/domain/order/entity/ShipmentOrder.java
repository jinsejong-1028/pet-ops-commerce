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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * - 창고 출고 주문 Entity
 * - 판매 주문 확정 후 창고가 수행할 출고 지시
 */
@Entity
@Table(name = "shipment_orders")
public class ShipmentOrder extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 출고 주문 번호
     */
    @Column(name = "shipment_order_no", nullable = false, unique = true, length = 50)
    private String shipmentOrderNo;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    /**
     * - 출고 주문 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentOrderStatus status;

    @Column(name = "scheduled_ship_date")
    private LocalDate scheduledShipDate;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(length = 500)
    private String reason;

    protected ShipmentOrder() {
        // JPA 기본 생성자
    }

    private ShipmentOrder(String shipmentOrderNo, Long salesOrderId, Long warehouseId, ShipmentOrderStatus status, LocalDate scheduledShipDate, String reason) {
        this.shipmentOrderNo = shipmentOrderNo;
        this.salesOrderId = salesOrderId;
        this.warehouseId = warehouseId;
        this.status = status;
        this.scheduledShipDate = scheduledShipDate;
        this.reason = reason;
    }

    /**
     * - 출고 주문 생성
     * - 재고 할당 전 CREATED 상태로 시작
     *
     * @param shipmentOrderNo 출고 주문 번호
     * @param salesOrderId 판매 주문 ID
     * @param warehouseId 출고 창고 ID
     * @param scheduledShipDate 출고 예정일
     * @param reason 생성 사유
     * @return 출고 주문 Entity
     */
    public static ShipmentOrder create(String shipmentOrderNo, Long salesOrderId, Long warehouseId, LocalDate scheduledShipDate, String reason) {
        return new ShipmentOrder(shipmentOrderNo, salesOrderId, warehouseId, ShipmentOrderStatus.CREATED, scheduledShipDate, reason);
    }

    public Long getId() {
        return id;
    }

    public String getShipmentOrderNo() {
        return shipmentOrderNo;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public ShipmentOrderStatus getStatus() {
        return status;
    }

    public LocalDate getScheduledShipDate() {
        return scheduledShipDate;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public String getReason() {
        return reason;
    }
}