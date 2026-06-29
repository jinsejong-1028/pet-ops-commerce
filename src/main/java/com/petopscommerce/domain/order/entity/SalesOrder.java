package com.petopscommerce.domain.order.entity;

import com.petopscommerce.global.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * - 관리자 판매 주문 Entity
 * - customer_orders를 운영자가 확인/확정할 내부 판매 기준 주문
 */
@Entity
@Table(name = "sales_orders")
public class SalesOrder extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 판매 주문 번호
     * - 운영 화면과 출고 연결에서 사용하는 업무 번호
     */
    @Column(name = "sales_order_no", nullable = false, unique = true, length = 50)
    private String salesOrderNo;

    /**
     * - 원천 고객 주문 ID
     */
    @Column(name = "customer_order_id", nullable = false)
    private Long customerOrderId;

    /**
     * - 판매 주문 업무일자
     * - row 생성시각이 아니라 주문 업무 기준일
     */
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    /**
     * - 판매 주문 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SalesOrderStatus status;

    /**
     * - 판매 주문 확정 일시
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(length = 500)
    private String reason;

    protected SalesOrder() {
        // JPA 기본 생성자
    }

    private SalesOrder(String salesOrderNo, Long customerOrderId, LocalDate orderDate, SalesOrderStatus status, String reason) {
        this.salesOrderNo = salesOrderNo;
        this.customerOrderId = customerOrderId;
        this.orderDate = orderDate;
        this.status = status;
        this.reason = reason;
    }

    /**
     * - 판매 주문 생성
     * - 고객 주문 생성 시 CREATED 상태로 자동 생성
     *
     * @param salesOrderNo 판매 주문 번호
     * @param customerOrderId 원천 고객 주문 ID
     * @param orderDate 판매 주문 업무일자
     * @param reason 생성 사유
     * @return 판매 주문 Entity
     */
    public static SalesOrder create(String salesOrderNo, Long customerOrderId, LocalDate orderDate, String reason) {
        return new SalesOrder(salesOrderNo, customerOrderId, orderDate, SalesOrderStatus.CREATED, reason);
    }

    /**
     * - 판매 주문 확정
     * - 출고 주문 생성 직전에 CONFIRMED 상태로 변경
     *
     * @param confirmedAt 확정 일시
     * @param reason 확정 사유
     */
    public void confirm(LocalDateTime confirmedAt, String reason) {
        this.status = SalesOrderStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
        this.reason = reason;
    }

    /**
     * - 판매 주문 취소
     * - 출고 주문 생성 전 취소 상태로 변경
     *
     * @param canceledAt 취소 일시
     * @param reason 취소 사유
     */
    public void cancel(LocalDateTime canceledAt, String reason) {
        this.status = SalesOrderStatus.CANCELED;
        this.canceledAt = canceledAt;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public String getSalesOrderNo() {
        return salesOrderNo;
    }

    public Long getCustomerOrderId() {
        return customerOrderId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public String getReason() {
        return reason;
    }
}