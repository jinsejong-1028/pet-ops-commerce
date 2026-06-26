package com.petopscommerce.domain.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * - 재고 이동 원장 Entity
 * - stock row의 수량 증감 이력을 append-only 형태로 저장
 */
@Entity
@Table(name = "stock_movements")
@EntityListeners(AuditingEntityListener.class)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_no", nullable = false, length = 50)
    private String jobNo;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private StockMovementType movementType;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "from_location_id")
    private Long fromLocationId;

    @Column(name = "to_location_id")
    private Long toLocationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    /**
     * - 해당 location 기준 증감 수량
     * - 증가면 양수, 감소면 음수
     */
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_quantity_delta", nullable = false)
    private Integer totalQuantityDelta;

    @Column(name = "available_quantity_delta", nullable = false)
    private Integer availableQuantityDelta;

    @Column(name = "working_quantity_delta", nullable = false)
    private Integer workingQuantityDelta;

    @Column(length = 500)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    protected StockMovement() {
        // JPA 기본 생성자
    }

    private StockMovement(Long jobId, String jobNo, Long stockId, StockMovementType movementType, Long warehouseId, Long locationId, Long fromLocationId, Long toLocationId, Long productId, Long lotId, Integer quantity, Integer totalQuantityDelta, Integer availableQuantityDelta, Integer workingQuantityDelta, String reason) {
        this.jobId = jobId;
        this.jobNo = jobNo;
        this.stockId = stockId;
        this.movementType = movementType;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.productId = productId;
        this.lotId = lotId;
        this.quantity = quantity;
        this.totalQuantityDelta = totalQuantityDelta;
        this.availableQuantityDelta = availableQuantityDelta;
        this.workingQuantityDelta = workingQuantityDelta;
        this.reason = reason;
    }

    /**
     * - 신규 재고 이동 원장 생성
     *
     * @param job 재고 작업 헤더
     * @param stock 수량이 변경된 현재고
     * @param movementType 재고 이동 유형
     * @param fromLocationId 출발 location ID
     * @param toLocationId 도착 location ID
     * @param quantity location 기준 증감 수량
     * @param totalQuantityDelta 총수량 변화량
     * @param availableQuantityDelta 가용수량 변화량
     * @param workingQuantityDelta 작업수량 변화량
     * @param reason 작업 사유
     * @return 신규 재고 이동 원장
     */
    public static StockMovement create(StockJob job, Stock stock, StockMovementType movementType, Long fromLocationId, Long toLocationId, Integer quantity, Integer totalQuantityDelta, Integer availableQuantityDelta, Integer workingQuantityDelta, String reason) {
        return new StockMovement(
                job.getId(),
                job.getJobNo(),
                stock.getId(),
                movementType,
                stock.getWarehouseId(),
                stock.getLocationId(),
                fromLocationId,
                toLocationId,
                stock.getProductId(),
                stock.getLotId(),
                quantity,
                totalQuantityDelta,
                availableQuantityDelta,
                workingQuantityDelta,
                reason
        );
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobNo() {
        return jobNo;
    }

    public Long getStockId() {
        return stockId;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Long getFromLocationId() {
        return fromLocationId;
    }

    public Long getToLocationId() {
        return toLocationId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getLotId() {
        return lotId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getTotalQuantityDelta() {
        return totalQuantityDelta;
    }

    public Integer getAvailableQuantityDelta() {
        return availableQuantityDelta;
    }

    public Integer getWorkingQuantityDelta() {
        return workingQuantityDelta;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}