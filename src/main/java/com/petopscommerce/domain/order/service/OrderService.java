package com.petopscommerce.domain.order.service;

import com.petopscommerce.domain.order.dto.CreateOrderItemRequest;
import com.petopscommerce.domain.order.dto.CreateOrderRequest;
import com.petopscommerce.domain.order.dto.OrderResponse;
import com.petopscommerce.domain.order.entity.Order;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.repository.OrderItemRepository;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import com.petopscommerce.domain.product.repository.ProductRepository;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.service.BusinessNumberGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * - 주문 비즈니스 로직
 * - 상품 검증/금액 계산/주문 생성 담당
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final BusinessNumberGenerator businessNumberGenerator;

    /**
     * - 생성자 주입
     *
     * @param orderRepository 주문 DB 접근 객체
     * @param orderItemRepository 주문 상품 DB 접근 객체
     * @param productRepository 상품 DB 접근 객체
     * @param businessNumberGenerator 업무 번호 생성기
     */
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, BusinessNumberGenerator businessNumberGenerator) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.businessNumberGenerator = businessNumberGenerator;
    }

    /**
     * - 주문 생성
     * - 로그인 회원 ID 기준으로 상품 검증 후 주문/주문상품 저장
     *
     * @param memberId 로그인 회원 ID
     * @param request 주문 생성 요청
     * @return 생성된 주문 응답
     */
    @Transactional
    public OrderResponse createOrder(Long memberId, CreateOrderRequest request) {
        List<OrderLine> orderLines = request.items().stream()
                .map(this::createOrderLine)
                .toList();

        Integer totalAmount = orderLines.stream()
                .map(OrderLine::lineAmount)
                .reduce(0, Integer::sum);

        LocalDateTime orderedAt = LocalDateTime.now();
        String orderNo = businessNumberGenerator.generate(BusinessNumberType.ORDER);
        Order order = Order.create(memberId, orderNo, totalAmount, orderedAt);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderLines.stream()
                .map(orderLine -> OrderItem.create(
                        savedOrder.getId(),
                        orderLine.productId(),
                        orderLine.quantity(),
                        orderLine.unitPrice()
                ))
                .toList();
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        return OrderResponse.from(savedOrder, savedOrderItems);
    }

    private OrderLine createOrderLine(CreateOrderItemRequest itemRequest) {
        Product product = productRepository.findById(itemRequest.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        if (product.getSaleStatus() != ProductSaleStatus.ON_SALE) { // 판매 중 상품만 주문 가능
            throw new ResponseStatusException(HttpStatus.CONFLICT, "product is not on sale");
        }

        return new OrderLine(
                product.getId(),
                itemRequest.quantity(),
                product.getPrice(),
                itemRequest.quantity() * product.getPrice()
        );
    }

    private record OrderLine(
            Long productId,
            Integer quantity,
            Integer unitPrice,
            Integer lineAmount
    ) {
    }
}
