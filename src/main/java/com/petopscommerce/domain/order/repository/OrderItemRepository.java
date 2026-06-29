package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * - 주문 상품 DB 접근 객체
 * - customer_order_items 테이블 CRUD 담당
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * - 고객 주문 ID 기준 주문 품목 목록 조회
     * - 판매 주문 생성 시 원천 주문 품목 snapshot을 만들 때 사용
     *
     * @param orderId 고객 주문 ID
     * @return 주문 품목 목록
     */
    List<OrderItem> findByOrderId(Long orderId);
}