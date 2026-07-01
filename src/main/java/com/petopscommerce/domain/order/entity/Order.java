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

import java.time.LocalDateTime;

/**
 * - 주문 Entity
 * - customer_orders 테이블 매핑
 */
@Entity
@Table(name = "customer_orders")
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 주문 회원 ID
     * - 로그인된 회원 ID를 저장
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * - 주문 번호
     * - 외부 노출/조회용 unique 값
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    /**
     * - 고객 주문 유형
     * - 공통 코드 테이블 전까지 enum 문자열로 저장
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private OrderType orderType;

    /**
     * - 주문 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    /**
     * - 상품 합계 금액
     */
    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    /**
     * - 할인 금액
     * - 쿠폰 도메인 전까지 0으로 저장
     */
    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    /**
     * - 결제 대상 금액
     */
    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount;

    /**
     * - 주문 일시
     */
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    /**
     * - 고객 주문 확정 일시
     * - 판매 주문 확정 시 같은 시각으로 저장
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    protected Order() {
        // JPA 기본 생성자
    }

    private Order(Long memberId, String orderNo, OrderType orderType, OrderStatus status, Integer totalAmount, Integer discountAmount, Integer paymentAmount, LocalDateTime orderedAt, LocalDateTime confirmedAt) {
        this.memberId = memberId;
        this.orderNo = orderNo;
        this.orderType = orderType;
        this.status = status;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paymentAmount = paymentAmount;
        this.orderedAt = orderedAt;
        this.confirmedAt = confirmedAt;
    }

    /**
     * - 신규 주문 생성
     * - 고객 주문 유형 CUSTOMER_ORDER, 최초 상태 CREATED, 할인 금액 0
     *
     * @param memberId 로그인 회원 ID
     * @param orderNo 주문 번호
     * @param totalAmount 상품 합계 금액
     * @param orderedAt 주문 일시
     * @return 신규 주문 Entity
     */
    public static Order create(Long memberId, String orderNo, Integer totalAmount, LocalDateTime orderedAt) {
        return new Order(memberId, orderNo, OrderType.CUSTOMER_ORDER, OrderStatus.CREATED, totalAmount, 0, totalAmount, orderedAt, null);
    }

    /**
     * - 고객 주문 확정
     * - 판매 주문 확정 시 고객 주문 상태와 확정 시각을 함께 저장
     *
     * @param confirmedAt 확정 일시
     */
    public void confirm(LocalDateTime confirmedAt) {
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    /**
     * - 고객 주문 취소
     * - 판매 주문 취소 시 고객 주문 상태도 함께 취소
     */
    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public Integer getPaymentAmount() {
        return paymentAmount;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
}