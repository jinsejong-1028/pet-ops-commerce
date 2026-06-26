package com.petopscommerce.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * - 수동 재고 조정 요청 DTO
 * - quantity 부호로 증가/차감을 구분
 *
 * @param stockId 현재고 ID
 * @param quantity 조정 수량, 양수는 증가/음수는 차감
 * @param reason 조정 사유
 */
public record AdjustStockRequest(
        @NotNull
        Long stockId,
        @NotNull
        Integer quantity,
        @Size(max = 500)
        String reason
) {
}