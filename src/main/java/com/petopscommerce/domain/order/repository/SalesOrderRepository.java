package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 판매 주문 DB 접근 객체
 * - sales_orders 테이블 CRUD 담당
 */
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
}