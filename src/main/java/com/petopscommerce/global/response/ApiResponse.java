package com.petopscommerce.global.response;

/**
 * - API 공통 응답 DTO
 * - 성공/실패 응답 모양 통일
 *
 * @param success 요청 성공 여부
 * @param data 응답 데이터
 * @param message 응답 메시지
 * @param <T> 응답 데이터 타입
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {

    /**
     * - 성공 응답 생성
     * - 기본 메시지 OK 사용
     *
     * @param data 응답 데이터
     * @param <T> 응답 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "OK");
    }

    /**
     * - 실패 응답 생성
     * - data는 null로 고정
     *
     * @param message 실패 메시지
     * @return 실패 응답
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}