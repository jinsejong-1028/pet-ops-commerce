package com.petopscommerce.domain.inventory.entity;

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
 * - 재고 작업 헤더 Entity
 * - 출고/입고/조정/이동 같은 작업 묶음의 현재 상태 관리
 */
@Entity
@Table(name = "stock_jobs")
public class StockJob extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 재고 작업 번호
     * - 같은 작업에 속한 movement를 묶는 업무 번호
     */
    @Column(name = "job_no", nullable = false, unique = true, length = 50)
    private String jobNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private StockJobType jobType;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30)
    private StockReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockJobStatus status;

    @Column(length = 500)
    private String reason;

    protected StockJob() {
        // JPA 기본 생성자
    }

    private StockJob(String jobNo, StockJobType jobType, Long warehouseId, StockReferenceType referenceType, Long referenceId, StockJobStatus status, String reason) {
        this.jobNo = jobNo;
        this.jobType = jobType;
        this.warehouseId = warehouseId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.status = status;
        this.reason = reason;
    }

    /**
     * - 판매 주문 출고용 재고 작업 생성
     *
     * @param jobNo 재고 작업 번호
     * @param warehouseId 창고 ID
     * @param orderId 판매 주문 ID
     * @param reason 작업 사유
     * @return 신규 재고 작업
     */
    public static StockJob createSalesShipment(String jobNo, Long warehouseId, Long orderId, String reason) {
        return new StockJob(jobNo, StockJobType.SALES_SHIPMENT, warehouseId, StockReferenceType.SALES_ORDER, orderId, StockJobStatus.ALLOCATED, reason);
    }

    /**
     * - PICKTO 이동 완료 상태로 변경
     */
    public void markPicked() {
        this.status = StockJobStatus.PICKED;
    }

    /**
     * - 출고 완료 상태로 변경
     */
    public void markShipped() {
        this.status = StockJobStatus.SHIPPED;
    }

    public Long getId() {
        return id;
    }

    public String getJobNo() {
        return jobNo;
    }

    public StockJobType getJobType() {
        return jobType;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public StockReferenceType getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public StockJobStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
