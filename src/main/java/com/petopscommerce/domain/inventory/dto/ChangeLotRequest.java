package com.petopscommerce.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * - LOT 속성 변경 요청 DTO
 * - 같은 location 안에서 기존 LOT 재고를 신규 LOT 재고로 이동시킴
 *
 * @param stockId 변경할 출발 현재고 ID
 * @param lot1 변경 후 LOT 주요 식별값
 * @param lot2 변경 후 LOT 보조 정보
 * @param lot3 변경 후 유효기간
 * @param lot4 변경 후 입고일자, null이면 오늘 날짜
 * @param lot5 변경 후 기타 관리값
 * @param quantity 변경 수량
 * @param reason 변경 사유
 */
@Schema(description = "LOT 속성 변경 요청")
public record ChangeLotRequest(
        @Schema(description = "변경할 출발 현재고 ID", example = "1")
        @NotNull
        Long stockId,

        @Schema(description = "변경 후 LOT 주요 식별값", example = "LOT-20260701-002")
        @Size(max = 100)
        String lot1,

        @Schema(description = "변경 후 LOT 보조 정보", example = "SUPPLIER-B")
        @Size(max = 100)
        String lot2,

        @Schema(description = "변경 후 유효기간", example = "2027-07-01")
        LocalDate lot3,

        @Schema(description = "변경 후 입고일자, null이면 오늘 날짜", example = "2026-07-01")
        LocalDate lot4,

        @Schema(description = "변경 후 기타 관리값", example = "TEMP-NORMAL")
        @Size(max = 100)
        String lot5,

        @Schema(description = "변경 수량", example = "10")
        @NotNull
        @Positive
        Integer quantity,

        @Schema(description = "변경 사유", example = "correct lot attributes")
        @Size(max = 500)
        String reason
) {
}