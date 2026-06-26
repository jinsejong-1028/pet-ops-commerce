package com.petopscommerce.domain.inventory.entity;

import com.petopscommerce.global.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * - 현재고 Entity
 * - stocks 테이블 매핑
 */
@Entity
@Table(name = "stocks")
public class Stock extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 상품 ID
     * - DB FK 제약 대신 Service에서 존재 여부 검증
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * - 창고 ID
     * - 어떤 창고의 재고인지 식별
     */
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    /**
     * - Location ID
     * - 같은 창고 안에서도 Rack/PICKTO 위치별 재고를 분리
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * - LOT ID
     * - 상품의 유효기간/입고 단위별 재고를 분리
     */
    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    /**
     * - 총수량
     * - 가용수량과 작업수량의 합계
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    /**
     * - 가용수량
     * - 할당/피킹에 잡히지 않아 일반 증감/이동에 사용할 수 있는 수량
     */
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    /**
     * - 작업수량
     * - 할당/피킹/출고 작업 중이라 일반 증감/이동에 사용할 수 없는 수량
     */
    @Column(name = "working_quantity", nullable = false)
    private Integer workingQuantity;

    /**
     * - 안전재고 수량
     * - 가용수량이 이 값 이하이면 보충 검토 대상
     */
    @Column(name = "safety_quantity", nullable = false)
    private Integer safetyQuantity;

    /**
     * - 낙관적 잠금 version
     * - 재고 차감 단계에서 동시 수정 충돌 감지에 사용
     */
    @Version
    @Column(nullable = false)
    private Long version;

    protected Stock() {
        // JPA 기본 생성자
    }

    private Stock(Long productId, Long warehouseId, Long locationId, Long lotId, Integer totalQuantity, Integer availableQuantity, Integer workingQuantity, Integer safetyQuantity) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.lotId = lotId;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.workingQuantity = workingQuantity;
        this.safetyQuantity = safetyQuantity;
    }

    /**
     * - 신규 가용 현재고 생성
     * - 입고/초기 재고는 전체 수량이 가용수량으로 시작
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param totalQuantity 총수량
     * @param safetyQuantity 안전재고 수량
     * @return 신규 현재고 Entity
     */
    public static Stock create(Long productId, Long warehouseId, Long locationId, Long lotId, Integer totalQuantity, Integer safetyQuantity) {
        validatePositiveQuantity(totalQuantity);
        validateNonNegativeQuantity(safetyQuantity, "safety quantity must not be negative");
        return new Stock(productId, warehouseId, locationId, lotId, totalQuantity, totalQuantity, 0, safetyQuantity);
    }

    /**
     * - 신규 작업 현재고 생성
     * - PICK으로 새 location에 이동된 재고처럼 이미 작업 중인 수량으로 시작할 때 사용
     *
     * @param productId 상품 ID
     * @param warehouseId 창고 ID
     * @param locationId location ID
     * @param lotId LOT ID
     * @param workingQuantity 작업수량
     * @param safetyQuantity 안전재고 수량
     * @return 신규 현재고 Entity
     */
    public static Stock createWorking(Long productId, Long warehouseId, Long locationId, Long lotId, Integer workingQuantity, Integer safetyQuantity) {
        validatePositiveQuantity(workingQuantity);
        validateNonNegativeQuantity(safetyQuantity, "safety quantity must not be negative");
        return new Stock(productId, warehouseId, locationId, lotId, workingQuantity, 0, workingQuantity, safetyQuantity);
    }

    /**
     * - 입고 증가
     * - 작업수량은 건드리지 않고 총수량/가용수량만 증가
     *
     * @param quantity 입고 수량
     */
    public void receive(Integer quantity) {
        applyAvailableDelta(quantity);
    }

    /**
     * - 재고 할당
     * - 실제 location 이동 없이 가용수량을 작업수량으로 전환
     *
     * @param quantity 할당 수량
     */
    public void allocate(Integer quantity) {
        validatePositiveQuantity(quantity);
        validateAvailableQuantity(quantity);

        this.availableQuantity -= quantity;
        this.workingQuantity += quantity;
    }

    /**
     * - 가용 재고 차감
     * - 조정 차감이나 일반 location 이동 출발에 사용
     *
     * @param quantity 차감 수량
     */
    public void decreaseAvailable(Integer quantity) {
        validatePositiveQuantity(quantity);
        applyAvailableDelta(-quantity);
    }

    /**
     * - 가용 재고 증가
     * - 조정 증가나 일반 location 이동 도착에 사용
     *
     * @param quantity 증가 수량
     */
    public void increaseAvailable(Integer quantity) {
        applyAvailableDelta(quantity);
    }

    /**
     * - PICK 출발 처리
     * - 보관 location에서 총수량과 작업수량을 함께 감소
     *
     * @param quantity PICK 수량
     */
    public void pickOut(Integer quantity) {
        validatePositiveQuantity(quantity);
        applyWorkingDelta(-quantity, "picked stock is not enough");
    }

    /**
     * - PICKTO 입고 처리
     * - PICKTO location에 총수량과 작업수량을 함께 증가
     *
     * @param quantity PICKTO 이동 수량
     */
    public void pickIn(Integer quantity) {
        applyWorkingDelta(quantity, "working stock is not enough");
    }

    /**
     * - 출고 처리
     * - PICKTO location에서 총수량과 작업수량을 함께 감소
     *
     * @param quantity 출고 수량
     */
    public void shipOut(Integer quantity) {
        validatePositiveQuantity(quantity);
        applyWorkingDelta(-quantity, "shipping stock is not enough");
    }

    /**
     * - 수동 재고 조정
     * - 양수는 증가, 음수는 가용수량 기준 차감
     *
     * @param signedQuantity 부호가 있는 조정 수량
     */
    public void adjust(Integer signedQuantity) {
        applyAvailableDelta(signedQuantity);
    }

    /**
     * - 가용수량 delta 반영
     * - 양수는 입고/증가, 음수는 가용수량 기준 차감
     *
     * @param quantityDelta 부호가 있는 변경 수량
     */
    public void applyAvailableDelta(Integer quantityDelta) {
        validateNonZeroQuantity(quantityDelta);

        if (quantityDelta > 0) {
            this.totalQuantity += quantityDelta;
            this.availableQuantity += quantityDelta;
            return;
        }

        int decreaseQuantity = Math.abs(quantityDelta);
        validateAvailableQuantity(decreaseQuantity);
        this.totalQuantity -= decreaseQuantity;
        this.availableQuantity -= decreaseQuantity;
    }

    /**
     * - 작업수량 delta 반영
     * - 양수는 PICKTO 유입, 음수는 PICK 출발/출고 차감
     *
     * @param quantityDelta 부호가 있는 변경 수량
     * @param shortageMessage 부족 시 응답 메시지
     */
    public void applyWorkingDelta(Integer quantityDelta, String shortageMessage) {
        validateNonZeroQuantity(quantityDelta);

        if (quantityDelta > 0) {
            this.totalQuantity += quantityDelta;
            this.workingQuantity += quantityDelta;
            return;
        }

        int decreaseQuantity = Math.abs(quantityDelta);
        validateWorkingQuantity(decreaseQuantity, shortageMessage);
        this.totalQuantity -= decreaseQuantity;
        this.workingQuantity -= decreaseQuantity;
    }

    private void validateAvailableQuantity(Integer quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalArgumentException("available stock is not enough");
        }
    }

    private void validateWorkingQuantity(Integer quantity, String message) {
        if (workingQuantity < quantity || totalQuantity < quantity) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validatePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    private static void validateNonZeroQuantity(Integer quantity) {
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("quantity must not be zero");
        }
    }

    private static void validateNonNegativeQuantity(Integer quantity, String message) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public Long getLotId() {
        return lotId;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public Integer getWorkingQuantity() {
        return workingQuantity;
    }

    public Integer getSafetyQuantity() {
        return safetyQuantity;
    }

    public Long getVersion() {
        return version;
    }
}