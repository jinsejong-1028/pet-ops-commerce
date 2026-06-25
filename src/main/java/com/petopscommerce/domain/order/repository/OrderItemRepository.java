package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 주문 상품 DB 접근 객체
 * - order_items 테이블 CRUD 담당
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
