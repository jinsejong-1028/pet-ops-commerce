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

import java.time.LocalDateTime;

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

    /**
     * - 작업 완료 일시
     * - 진행 중 작업 목록은 completedAt null 여부로 구분
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * - 화면/운영 삭제 처리 일시
     * - 원장 삭제가 아니라 작업 헤더 숨김 처리에 사용
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected StockJob() {
        // JPA 기본 생성자
    }

    private StockJob(String jobNo, StockJobType jobType, Long warehouseId, StockReferenceType referenceType, Long referenceId, StockJobStatus status, String reason, LocalDateTime completedAt) {
        this.jobNo = jobNo;
        this.jobType = jobType;
        this.warehouseId = warehouseId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.status = status;
        this.reason = reason;
        this.completedAt = completedAt;
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
        return new StockJob(jobNo, StockJobType.SALES_SHIPMENT, warehouseId, StockReferenceType.SALES_ORDER, orderId, StockJobStatus.ALLOCATED, reason, null);
    }

    /**
     * - 입고성 재고 반영 작업 생성
     * - 현재 입고 API는 프로세스 단계 없이 즉시 완료됨
     *
     * @param jobNo 재고 작업 번호
     * @param warehouseId 창고 ID
     * @param reason 작업 사유
     * @param completedAt 완료 일시
     * @return 신규 재고 작업
     */
    public static StockJob createInbound(String jobNo, Long warehouseId, String reason, LocalDateTime completedAt) {
        return new StockJob(jobNo, StockJobType.INBOUND, warehouseId, null, null, StockJobStatus.RECEIVED, reason, completedAt);
    }

    /**
     * - 수동 재고 조정 작업 생성
     * - 조정은 별도 단계 없이 실행과 동시에 완료됨
     *
     * @param jobNo 재고 작업 번호
     * @param warehouseId 창고 ID
     * @param reason 작업 사유
     * @param completedAt 완료 일시
     * @return 신규 재고 작업
     */
    public static StockJob createAdjustment(String jobNo, Long warehouseId, String reason, LocalDateTime completedAt) {
        return new StockJob(jobNo, StockJobType.ADJUSTMENT, warehouseId, null, null, StockJobStatus.ADJUSTED, reason, completedAt);
    }

    /**
     * - location 간 재고 이동 작업 생성
     * - 일반 이동은 별도 단계 없이 실행과 동시에 완료됨
     *
     * @param jobNo 재고 작업 번호
     * @param warehouseId 창고 ID
     * @param reason 작업 사유
     * @param completedAt 완료 일시
     * @return 신규 재고 작업
     */
    public static StockJob createTransfer(String jobNo, Long warehouseId, String reason, LocalDateTime completedAt) {
        return new StockJob(jobNo, StockJobType.TRANSFER, warehouseId, null, null, StockJobStatus.TRANSFERRED, reason, completedAt);
    }

    /**
     * - PICKTO 이동 완료 상태로 변경
     */
    public void markPicked() {
        this.status = StockJobStatus.PICKED;
    }

    /**
     * - 출고 완료 상태와 완료 시각 기록
     *
     * @param completedAt 완료 일시
     */
    public void markShipped(LocalDateTime completedAt) {
        this.status = StockJobStatus.SHIPPED;
        this.completedAt = completedAt;
    }

    /**
     * - 작업 헤더 삭제 처리
     * - movement 원장은 삭제하지 않고 작업 목록 노출만 제어
     *
     * @param deletedAt 삭제 처리 일시
     */
    public void markDeleted(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}