package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * - 입고성 현재고 생성/증가 요청 DTO
 * - 같은 LOT/current stock이 있으면 기존 현재고를 증가시킴
 *
 * @param productId 상품 ID
 * @param warehouseId 창고 ID
 * @param locationId 입고 location ID
 * @param lot1 LOT 주요 식별값
 * @param lot2 LOT 보조 정보
 * @param lot3 유효기간
 * @param lot4 입고일자, null이면 오늘 날짜
 * @param lot5 기타 관리값
 * @param quantity 입고 수량
 * @param reason 입고 사유
 */
@Schema(description = "입고성 현재고 생성 또는 증가 요청")
public record ReceiveStockRequest(
        @Schema(description = "상품 ID", example = "1")
        @NotNull
        Long productId,

        @Schema(description = "창고 ID", example = "1")
        @NotNull
        Long warehouseId,

        @Schema(description = "입고 Location ID", example = "1")
        @NotNull
        Long locationId,

        @Schema(description = "LOT 주요 식별값", example = "LOT-20260701-001")
        @Size(max = 100)
        String lot1,

        @Schema(description = "LOT 보조 정보", example = "SUPPLIER-A")
        @Size(max = 100)
        String lot2,

        @Schema(description = "유효기간", example = "2027-07-01")
        LocalDate lot3,

        @Schema(description = "입고일자, null이면 오늘 날짜", example = "2026-07-01")
        LocalDate lot4,

        @Schema(description = "기타 관리값", example = "TEMP-NORMAL")
        @Size(max = 100)
        String lot5,

        @Schema(description = "입고 수량", example = "100")
        @NotNull
        @Positive
        Integer quantity,

        @Schema(description = "입고 사유", example = "initial stock")
        @Size(max = 500)
        String reason
) {
}