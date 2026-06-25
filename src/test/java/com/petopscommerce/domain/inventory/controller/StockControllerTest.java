package com.petopscommerce.domain.inventory.controller;

import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.service.StockService;
import com.petopscommerce.global.config.SecurityConfig;
import com.petopscommerce.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - 현재고 Controller 테스트
 * - 공통 응답 구조와 인증 필요 흐름 검증
 */
@WebMvcTest(StockController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    /**
     * - 현재고 목록 응답 검증
     */
    @Test
    @DisplayName("현재고 목록 API는 공통 목록 응답을 반환한다")
    void getStocks() throws Exception {
        StockResponse response = new StockResponse(
                5L,
                1L,
                2L,
                3L,
                4L,
                100,
                3,
                97,
                10,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(stockService.getStocks(1L, 2L, 3L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/stocks?productId=1&warehouseId=2&locationId=3")
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(5L))
                .andExpect(jsonPath("$.data[0].totalQuantity").value(100))
                .andExpect(jsonPath("$.data[0].workingQuantity").value(3))
                .andExpect(jsonPath("$.data[0].availableQuantity").value(97));
    }

    /**
     * - 현재고 단건 응답 검증
     */
    @Test
    @DisplayName("현재고 단건 API는 공통 현재고 응답을 반환한다")
    void getStock() throws Exception {
        StockResponse response = new StockResponse(
                5L,
                1L,
                2L,
                3L,
                4L,
                100,
                3,
                97,
                10,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(stockService.getStock(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/stocks/5")
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5L));
    }

    /**
     * - 없는 현재고 실패 응답 검증
     */
    @Test
    @DisplayName("없는 현재고 조회 시 404 공통 실패 응답을 반환한다")
    void getMissingStock() throws Exception {
        when(stockService.getStock(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "stock not found"));

        mockMvc.perform(get("/api/v1/admin/stocks/999")
                        .with(adminJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("stock not found"));
    }

    /**
     * - 인증 없는 현재고 조회 차단 검증
     */
    @Test
    @DisplayName("현재고 목록 API는 인증 없이 호출하면 401을 반환한다")
    void getStocksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stocks"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * - 일반 회원 현재고 조회 차단 검증
     */
    @Test
    @DisplayName("일반 회원은 현재고 목록 API 호출 시 403을 반환한다")
    void getStocksWithMemberRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stocks")
                        .with(memberJwt()))
                .andExpect(status().isForbidden());
    }
    /**
     * - 테스트용 관리자 JWT
     * - 현재고 조회 인증 통과용
     *
     * @return mock JWT 요청 설정
     */
    private RequestPostProcessor adminJwt() {
        return jwt().jwt(jwt -> jwt
                .subject("1")
                .claim("email", "admin@example.com")
                .claim("role", "ADMIN")
        ).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    /**
     * - 테스트용 일반 회원 JWT
     * - 관리자 API 권한 차단 확인용
     *
     * @return mock JWT 요청 설정
     */
    private RequestPostProcessor memberJwt() {
        return jwt().jwt(jwt -> jwt
                .subject("2")
                .claim("email", "user@example.com")
                .claim("role", "MEMBER")
        ).authorities(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }
}
