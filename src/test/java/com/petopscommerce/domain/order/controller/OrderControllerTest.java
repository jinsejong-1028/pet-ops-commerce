package com.petopscommerce.domain.order.controller;

import com.petopscommerce.domain.order.dto.OrderItemResponse;
import com.petopscommerce.domain.order.dto.OrderResponse;
import com.petopscommerce.domain.order.entity.OrderStatus;
import com.petopscommerce.domain.order.service.OrderService;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.global.config.SecurityConfig;
import com.petopscommerce.global.exception.GlobalExceptionHandler;
import com.petopscommerce.global.security.LoginMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - 주문 Controller 테스트
 * - 인증 회원 기반 주문 생성 응답 검증
 */
@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    /**
     * - 주문 생성 성공 응답 검증
     */
    @Test
    @DisplayName("주문 생성 API는 201과 공통 주문 응답을 반환한다")
    void createOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                100L,
                "ORD-20260625100000-ABCDEF12",
                5L,
                OrderStatus.CREATED,
                50000,
                0,
                50000,
                LocalDateTime.of(2026, 6, 25, 10, 0),
                List.of(new OrderItemResponse(1000L, 1L, 2, 25000, 50000))
        );
        when(orderService.createOrder(eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .with(memberAuthentication())
                        .contentType("application/json")
                        .content("{\"items\":[{\"productId\":1,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.memberId").value(5L))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.totalAmount").value(50000))
                .andExpect(jsonPath("$.data.items[0].productId").value(1L))
                .andExpect(jsonPath("$.data.items[0].lineAmount").value(50000));
    }

    /**
     * - 없는 상품 실패 응답 검증
     */
    @Test
    @DisplayName("없는 상품으로 주문 생성 시 404 공통 실패 응답을 반환한다")
    void createOrderWithMissingProduct() throws Exception {
        when(orderService.createOrder(eq(5L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        mockMvc.perform(post("/api/v1/orders")
                        .with(memberAuthentication())
                        .contentType("application/json")
                        .content("{\"items\":[{\"productId\":999,\"quantity\":1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("product not found"));
    }

    /**
     * - 주문 생성 요청 validation 실패 응답 검증
     */
    @Test
    @DisplayName("주문 상품 목록이 비어 있으면 400 공통 실패 응답을 반환한다")
    void createOrderValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .with(memberAuthentication())
                        .contentType("application/json")
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * - 테스트용 회원 인증 객체
     * - 주문 생성 인증 통과용
     *
     * @return mock 인증 요청 설정
     */
    private RequestPostProcessor memberAuthentication() {
        LoginMember loginMember = new LoginMember(5L, "user@example.com", MemberRole.MEMBER);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginMember,
                "token",
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        return authentication(authentication);
    }
}
