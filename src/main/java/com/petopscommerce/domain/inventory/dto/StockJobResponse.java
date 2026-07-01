package com.petopscommerce.domain.inventory.dto;

import com.petopscommerce.domain.inventory.entity.StockJob;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * - 재고 작업 응답 DTO
 *
 * @param jobId 재고 작업 ID
 * @param jobNo 재고 작업 번호
 * @param jobType 재고 작업 유형
 * @param status 재고 작업 상태
 * @param warehouseId 창고 ID
 * @param referenceType 참조 업무 유형
 * @param referenceId 참조 업무 ID
 * @param completedAt 작업 완료 일시
 */
@Schema(description = "재고 작업 응답")
public record StockJobResponse(
        @Schema(description = "재고 작업 ID", example = "1")
        Long jobId,

        @Schema(description = "재고 작업 번호", example = "STJ-20260701-000001")
        String jobNo,

        @Schema(description = "재고 작업 유형", example = "SALES_SHIPMENT")
        String jobType,

        @Schema(description = "재고 작업 상태", example = "ALLOCATED")
        String status,

        @Schema(description = "창고 ID", example = "1")
        Long warehouseId,

        @Schema(description = "참조 업무 유형", example = "SALES_ORDER")
        String referenceType,

        @Schema(description = "참조 업무 ID", example = "1")
        Long referenceId,

        @Schema(description = "작업 완료 일시", example = "2026-07-01T10:00:00")
        LocalDateTime completedAt
) {

    /**
     * - Entity를 응답 DTO로 변환
     *
     * @param stockJob 재고 작업 Entity
     * @return 재고 작업 응답
     */
    public static StockJobResponse from(StockJob stockJob) {
        return new StockJobResponse(
                stockJob.getId(),
                stockJob.getJobNo(),
                stockJob.getJobType().name(),
                stockJob.getStatus().name(),
                stockJob.getWarehouseId(),
                stockJob.getReferenceType() == null ? null : stockJob.getReferenceType().name(),
                stockJob.getReferenceId(),
                stockJob.getCompletedAt()
        );
    }
}