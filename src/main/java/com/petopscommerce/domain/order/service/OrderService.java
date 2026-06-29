package com.petopscommerce.domain.order.service;

import com.petopscommerce.domain.order.dto.CreateOrderItemRequest;
import com.petopscommerce.domain.order.dto.CreateOrderRequest;
import com.petopscommerce.domain.order.dto.OrderResponse;
import com.petopscommerce.domain.order.entity.Order;
import com.petopscommerce.domain.order.entity.OrderItem;
import com.petopscommerce.domain.order.entity.SalesOrder;
import com.petopscommerce.domain.order.entity.SalesOrderItem;
import com.petopscommerce.domain.order.repository.OrderItemRepository;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.domain.order.repository.SalesOrderItemRepository;
import com.petopscommerce.domain.order.repository.SalesOrderRepository;
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
 * - 상품 검증/금액 계산/고객 주문 생성/판매 주문 자동 생성 담당
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final ProductRepository productRepository;
    private final BusinessNumberGenerator businessNumberGenerator;

    /**
     * - 생성자 주입
     *
     * @param orderRepository 주문 DB 접근 객체
     * @param orderItemRepository 주문 상품 DB 접근 객체
     * @param salesOrderRepository 판매 주문 DB 접근 객체
     * @param salesOrderItemRepository 판매 주문 품목 DB 접근 객체
     * @param productRepository 상품 DB 접근 객체
     * @param businessNumberGenerator 업무 번호 생성기
     */
    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            ProductRepository productRepository,
            BusinessNumberGenerator businessNumberGenerator
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.productRepository = productRepository;
        this.businessNumberGenerator = businessNumberGenerator;
    }

    /**
     * - 주문 생성
     * - 고객 주문 저장 후 운영자가 확인할 판매 주문을 CREATED 상태로 자동 생성
     *
     * @param memberId 로그인 회원 ID
     * @param request 주문 생성 요청
     * @return 생성된 주문 응답
     */
    @Transactional
    public OrderResponse createOrder(Long memberId, CreateOrderRequest request) {
        // 단계 1: 주문 요청 상품 검증
        // 결과: 상품 현재 가격 기준 주문 라인 snapshot 생성
        List<OrderLine> orderLines = request.items().stream()
                .map(this::createOrderLine)
                .toList();

        // 단계 2: 고객 주문 금액 계산
        // 결과: customer_orders에 저장할 총 주문 금액 계산
        Integer totalAmount = orderLines.stream()
                .map(OrderLine::lineAmount)
                .reduce(0, Integer::sum);

        // 단계 3: 고객 주문 저장
        // 결과: 고객이 만든 원천 주문은 CREATED 상태로 저장
        LocalDateTime orderedAt = LocalDateTime.now();
        String orderNo = businessNumberGenerator.generate(BusinessNumberType.ORDER);
        Order order = Order.create(memberId, orderNo, totalAmount, orderedAt);
        Order savedOrder = orderRepository.save(order);

        // 단계 4: 고객 주문 품목 저장
        // 결과: 주문 당시 상품/수량/단가/라인금액 snapshot 저장
        List<OrderItem> orderItems = orderLines.stream()
                .map(orderLine -> OrderItem.create(
                        savedOrder.getId(),
                        orderLine.productId(),
                        orderLine.quantity(),
                        orderLine.unitPrice()
                ))
                .toList();
        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

        // 단계 5: 판매 주문 자동 생성
        // 결과: 운영자가 확인/확정할 sales_orders와 sales_order_items를 CREATED 상태로 생성
        createSalesOrderForCustomerOrder(savedOrder, savedOrderItems);

        return OrderResponse.from(savedOrder, savedOrderItems);
    }

    /**
     * - 고객 주문 기반 판매 주문 자동 생성
     * - 출고 주문은 아직 만들지 않고 관리자 확정 단계에서 생성
     *
     * @param order 고객 주문 Entity
     * @param orderItems 고객 주문 품목 목록
     */
    private void createSalesOrderForCustomerOrder(Order order, List<OrderItem> orderItems) {
        String salesOrderNo = businessNumberGenerator.generate(BusinessNumberType.SALES_ORDER);
        SalesOrder salesOrder = SalesOrder.create(
                salesOrderNo,
                order.getId(),
                order.getOrderedAt().toLocalDate(),
                "created from customer order"
        );
        SalesOrder savedSalesOrder = salesOrderRepository.save(salesOrder);

        List<SalesOrderItem> salesOrderItems = orderItems.stream()
                .map(orderItem -> SalesOrderItem.create(savedSalesOrder.getId(), orderItem))
                .toList();
        salesOrderItemRepository.saveAll(salesOrderItems);
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