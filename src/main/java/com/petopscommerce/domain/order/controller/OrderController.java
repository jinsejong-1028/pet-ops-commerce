package com.petopscommerce.domain.order.controller;

import com.petopscommerce.domain.order.dto.CreateOrderRequest;
import com.petopscommerce.domain.order.dto.OrderResponse;
import com.petopscommerce.domain.order.service.OrderService;
import com.petopscommerce.global.response.ApiResponse;
import com.petopscommerce.global.security.LoginMember;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 주문 API 진입점
 * - 주문 생성 요청 처리
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * - 생성자 주입
     *
     * @param orderService 주문 비즈니스 로직
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * - 주문 생성
     *
     * @param loginMember 로그인 회원
     * @param request 주문 생성 요청
     * @return 생성된 주문 공통 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal LoginMember loginMember,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ApiResponse.ok(orderService.createOrder(loginMember.id(), request));
    }
}
