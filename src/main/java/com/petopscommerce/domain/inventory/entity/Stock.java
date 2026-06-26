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
     * - 해당 location에 실제 존재하는 재고 수량
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    /**
     * - 작업수량
     * - 할당/피킹/출고 작업 중이라 판매 가능하지 않은 수량
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

    private Stock(Long productId, Long warehouseId, Long locationId, Long lotId, Integer totalQuantity, Integer workingQuantity, Integer safetyQuantity) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.locationId = locationId;
        this.lotId = lotId;
        this.totalQuantity = totalQuantity;
        this.workingQuantity = workingQuantity;
        this.safetyQuantity = safetyQuantity;
    }

    /**
     * - 신규 현재고 생성
     * - 최초 작업수량은 0으로 시작
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
        return new Stock(productId, warehouseId, locationId, lotId, totalQuantity, 0, safetyQuantity);
    }

    /**
     * - 가용수량 계산
     * - 총수량에서 할당/피킹/출고 작업 중인 수량을 제외
     *
     * @return 판매/할당 가능한 수량
     */
    public Integer getAvailableQuantity() {
        return totalQuantity - workingQuantity;
    }


    /**
     * - 재고 할당
     * - 물리 총수량은 유지하고 작업수량만 증가
     *
     * @param quantity 할당 수량
     */
    public void allocate(Integer quantity) {
        validatePositiveQuantity(quantity);

        if (getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("available stock is not enough");
        }

        this.workingQuantity += quantity;
    }

    /**
     * - PICK 출발 처리
     * - 보관 location에서 총수량과 작업수량을 함께 감소
     *
     * @param quantity PICK 수량
     */
    public void pickOut(Integer quantity) {
        validatePositiveQuantity(quantity);

        if (workingQuantity < quantity || totalQuantity < quantity) {
            throw new IllegalArgumentException("picked stock is not enough");
        }

        this.totalQuantity -= quantity;
        this.workingQuantity -= quantity;
    }

    /**
     * - PICKTO 입고 처리
     * - PICKTO location에 총수량과 작업수량을 함께 증가
     *
     * @param quantity PICKTO 이동 수량
     */
    public void pickIn(Integer quantity) {
        validatePositiveQuantity(quantity);

        this.totalQuantity += quantity;
        this.workingQuantity += quantity;
    }

    /**
     * - 출고 처리
     * - PICKTO location에서 총수량과 작업수량을 함께 감소
     *
     * @param quantity 출고 수량
     */
    public void shipOut(Integer quantity) {
        validatePositiveQuantity(quantity);

        if (workingQuantity < quantity || totalQuantity < quantity) {
            throw new IllegalArgumentException("shipping stock is not enough");
        }

        this.totalQuantity -= quantity;
        this.workingQuantity -= quantity;
    }

    private void validatePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
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
