package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.ShipmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 출고 주문 DB 접근 객체
 * - shipment_orders 테이블 CRUD 담당
 */
public interface ShipmentOrderRepository extends JpaRepository<ShipmentOrder, Long> {
}