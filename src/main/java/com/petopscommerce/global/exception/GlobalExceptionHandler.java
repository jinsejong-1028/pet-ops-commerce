package com.petopscommerce.global.exception;

import com.petopscommerce.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.StringJoiner;

/**
 * - 전역 예외 처리기
 * - API 실패 응답 모양 통일
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * - 서비스/컨트롤러에서 지정한 HTTP 예외 처리
     * - 404, 409 같은 의도된 실패 응답 담당
     *
     * @param exception HTTP 상태를 포함한 예외
     * @return 공통 실패 응답
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException exception) {
        String message = exception.getReason() == null ? "request failed" : exception.getReason();

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(ApiResponse.error(message));
    }

    /**
     * - @Valid 검증 실패 처리
     * - 필드별 실패 메시지를 한 줄로 정리
     *
     * @param exception validation 실패 예외
     * @return 400 공통 실패 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        StringJoiner messageJoiner = new StringJoiner(", ");

        exception.getBindingResult().getFieldErrors().forEach(error ->
                messageJoiner.add(error.getField() + ": " + error.getDefaultMessage())
        );

        String message = messageJoiner.length() == 0 ? "validation failed" : messageJoiner.toString();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    /**
     * - 예상하지 못한 서버 오류 처리
     * - 내부 오류 상세는 응답에 노출하지 않음
     *
     * @param exception 처리되지 않은 예외
     * @return 500 공통 실패 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("internal server error"));
    }
}