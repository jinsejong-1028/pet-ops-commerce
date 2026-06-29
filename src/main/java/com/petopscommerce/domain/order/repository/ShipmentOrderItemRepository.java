package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.ShipmentOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 출고 주문 품목 DB 접근 객체
 * - shipment_order_items 테이블 CRUD 담당
 */
public interface ShipmentOrderItemRepository extends JpaRepository<ShipmentOrderItem, Long> {
}