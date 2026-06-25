package com.petopscommerce.global.businessnumber.entity;

/**
 * - 업무 번호 할당 구간
 *
 * @param start 시작 번호
 * @param end 종료 번호
 */
public record BusinessNumberRange(
        Long start,
        Long end
) {
}
