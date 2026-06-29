package com.petopscommerce.domain.order.repository;

import com.petopscommerce.domain.order.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * - 판매 주문 품목 DB 접근 객체
 * - sales_order_items 테이블 CRUD 담당
 */
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    /**
     * - 판매 주문 ID 기준 품목 목록 조회
     * - 판매 주문 확정 시 출고 주문 품목 생성에 사용
     *
     * @param salesOrderId 판매 주문 ID
     * @return 판매 주문 품목 목록
     */
    List<SalesOrderItem> findBySalesOrderId(Long salesOrderId);
}