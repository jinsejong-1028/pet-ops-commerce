package com.petopscommerce.domain.inventory.dto;

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
public record ReceiveStockRequest(
        @NotNull
        Long productId,
        @NotNull
        Long warehouseId,
        @NotNull
        Long locationId,
        @Size(max = 100)
        String lot1,
        @Size(max = 100)
        String lot2,
        LocalDate lot3,
        LocalDate lot4,
        @Size(max = 100)
        String lot5,
        @NotNull
        @Positive
        Integer quantity,
        @Size(max = 500)
        String reason
) {
}