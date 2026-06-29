package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 주문 DB 접근 객체
 * - customer_orders 테이블 CRUD 담당
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
