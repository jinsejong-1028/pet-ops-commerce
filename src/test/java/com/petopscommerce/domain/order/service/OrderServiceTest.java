package com.petopscommerce.domain.order.service;

import com.petopscommerce.domain.order.dto.CreateOrderItemRequest;
import com.petopscommerce.domain.order.dto.CreateOrderRequest;
import com.petopscommerce.domain.order.dto.OrderResponse;
import com.petopscommerce.domain.order.entity.Order;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.entity.OrderStatus;
import com.petopscommerce.domain.order.repository.OrderItemRepository;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import com.petopscommerce.domain.product.repository.ProductRepository;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.service.BusinessNumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * - 주문 Service 테스트
 * - 상품 검증/금액 계산/주문 생성 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BusinessNumberGenerator businessNumberGenerator;

    @InjectMocks
    private OrderService orderService;

    /**
     * - 주문 생성 성공 검증
     */
    @Test
    @DisplayName("로그인 회원 ID와 판매 중 상품으로 주문을 생성한다")
    void createOrder() {
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(1L, 2)
        ));
        Product product = Product.create(10L, "고양이 사료", "실내묘용 건식 사료", 25000);
        ReflectionTestUtils.setField(product, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(businessNumberGenerator.generate(BusinessNumberType.ORDER)).thenReturn("ORD-20260625-000001");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 100L);
            ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2026, 6, 25, 10, 0));
            ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.of(2026, 6, 25, 10, 0));
            return order;
        });
        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<OrderItem> orderItems = invocation.getArgument(0);
            ReflectionTestUtils.setField(orderItems.get(0), "id", 1000L);
            ReflectionTestUtils.setField(orderItems.get(0), "createdAt", LocalDateTime.of(2026, 6, 25, 10, 0));
            return orderItems;
        });

        OrderResponse response = orderService.createOrder(5L, request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.memberId()).isEqualTo(5L);
        assertThat(response.orderNo()).isEqualTo("ORD-20260625-000001");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.totalAmount()).isEqualTo(50000);
        assertThat(response.discountAmount()).isZero();
        assertThat(response.paymentAmount()).isEqualTo(50000);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productId()).isEqualTo(1L);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.items().get(0).unitPrice()).isEqualTo(25000);
        assertThat(response.items().get(0).lineAmount()).isEqualTo(50000);
    }

    /**
     * - 없는 상품 주문 실패 검증
     */
    @Test
    @DisplayName("존재하지 않는 상품으로 주문하면 404 오류를 반환한다")
    void createOrderWithMissingProduct() {
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(999L, 1)
        ));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(5L, request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(NOT_FOUND)
                );
    }

    /**
     * - 판매 불가 상품 주문 실패 검증
     */
    @Test
    @DisplayName("판매 중이 아닌 상품으로 주문하면 409 오류를 반환한다")
    void createOrderWithNotOnSaleProduct() {
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(1L, 1)
        ));
        Product product = Product.create(10L, "고양이 사료", "실내묘용 건식 사료", 25000);
        ReflectionTestUtils.setField(product, "id", 1L);
        ReflectionTestUtils.setField(product, "saleStatus", ProductSaleStatus.STOPPED);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(5L, request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(CONFLICT)
                );
    }
}
