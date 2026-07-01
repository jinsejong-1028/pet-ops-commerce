package com.petopscommerce.domain.order.dto;

import com.petopscommerce.domain.order.entity.SalesOrder;
import com.petopscommerce.domain.order.entity.ShipmentOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * - 관리자 판매 주문 응답 DTO
 * - 판매 주문 상태와 출고 창고, 확정 후 생성된 출고 주문 핵심 값을 반환
 *
 * @param salesOrderId 판매 주문 ID
 * @param salesOrderNo 판매 주문 번호
 * @param salesOrderStatus 판매 주문 상태
 * @param orderDate 판매 주문 업무일자
 * @param customerOrderId 원천 고객 주문 ID
 * @param warehouseId 판매 주문 출고 창고 ID
 * @param shipmentOrderId 출고 주문 ID
 * @param shipmentOrderNo 출고 주문 번호
 * @param shipmentOrderStatus 출고 주문 상태
 * @param scheduledShipDate 출고 예정일
 * @param itemCount 판매 주문 또는 출고 주문 품목 수
 * @param confirmedAt 판매 주문 확정 일시
 * @param canceledAt 판매 주문 취소 일시
 */
@Schema(description = "관리자 판매 주문 응답")
public record SalesOrderResponse(
        @Schema(description = "판매 주문 ID", example = "1")
        Long salesOrderId,

        @Schema(description = "판매 주문 번호", example = "SO-20260701-000001")
        String salesOrderNo,

        @Schema(description = "판매 주문 상태", example = "CONFIRMED")
        String salesOrderStatus,

        @Schema(description = "판매 주문 업무일자", example = "2026-07-01")
        LocalDate orderDate,

        @Schema(description = "원천 고객 주문 ID", example = "1")
        Long customerOrderId,

        @Schema(description = "판매 주문 출고 창고 ID", example = "1")
        Long warehouseId,

        @Schema(description = "출고 주문 ID", example = "1")
        Long shipmentOrderId,

        @Schema(description = "출고 주문 번호", example = "SHP-20260701-000001")
        String shipmentOrderNo,

        @Schema(description = "출고 주문 상태", example = "CREATED")
        String shipmentOrderStatus,

        @Schema(description = "출고 예정일", example = "2026-07-01")
        LocalDate scheduledShipDate,

        @Schema(description = "판매 주문 또는 출고 주문 품목 수", example = "1")
        Integer itemCount,

        @Schema(description = "판매 주문 확정 일시", example = "2026-07-01T10:00:00")
        LocalDateTime confirmedAt,

        @Schema(description = "판매 주문 취소 일시", example = "2026-07-01T11:00:00")
        LocalDateTime canceledAt
) {

    /**
     * - 판매 주문 단독 응답 생성
     * - 아직 출고 주문이 없거나 취소된 판매 주문에 사용
     *
     * @param salesOrder 판매 주문 Entity
     * @param itemCount 판매 주문 품목 수
     * @return 판매 주문 응답
     */
    public static SalesOrderResponse from(SalesOrder salesOrder, Integer itemCount) {
        return new SalesOrderResponse(
                salesOrder.getId(),
                salesOrder.getSalesOrderNo(),
                salesOrder.getStatus().name(),
                salesOrder.getOrderDate(),
                salesOrder.getCustomerOrderId(),
                salesOrder.getWarehouseId(),
                null,
                null,
                null,
                null,
                itemCount,
                salesOrder.getConfirmedAt(),
                salesOrder.getCanceledAt()
        );
    }

    /**
     * - 출고 주문 포함 응답 생성
     * - 판매 주문 확정 후 출고 주문이 함께 만들어졌을 때 사용
     *
     * @param salesOrder 판매 주문 Entity
     * @param shipmentOrder 출고 주문 Entity
     * @param itemCount 출고 주문 품목 수
     * @return 판매 주문 응답
     */
    public static SalesOrderResponse from(SalesOrder salesOrder, ShipmentOrder shipmentOrder, Integer itemCount) {
        return new SalesOrderResponse(
                salesOrder.getId(),
                salesOrder.getSalesOrderNo(),
                salesOrder.getStatus().name(),
                salesOrder.getOrderDate(),
                salesOrder.getCustomerOrderId(),
                salesOrder.getWarehouseId(),
                shipmentOrder.getId(),
                shipmentOrder.getShipmentOrderNo(),
                shipmentOrder.getStatus().name(),
                shipmentOrder.getScheduledShipDate(),
                itemCount,
                salesOrder.getConfirmedAt(),
                salesOrder.getCanceledAt()
        );
    }
}