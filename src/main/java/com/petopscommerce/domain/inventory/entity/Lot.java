package com.petopscommerce.domain.inventory.entity;

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

/**
 * - LOT Entity
 * - lots 테이블 매핑
 */
@Entity
@Table(name = "lots")
public class Lot extends BaseAuditEntity {

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
     * - LOT 주요 식별값
     */
    @Column(length = 100)
    private String lot1;

    /**
     * - LOT 보조 정보
     */
    @Column(length = 100)
    private String lot2;

    /**
     * - 유효기간
     */
    private LocalDate lot3;

    /**
     * - 입고일자
     */
    private LocalDate lot4;

    /**
     * - 기타 관리값
     */
    @Column(length = 100)
    private String lot5;

    /**
     * - LOT 사용 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LotStatus status;

    protected Lot() {
        // JPA 기본 생성자
    }

    private Lot(Long productId, String lot1, String lot2, LocalDate lot3, LocalDate lot4, String lot5, LotStatus status) {
        this.productId = productId;
        this.lot1 = lot1;
        this.lot2 = lot2;
        this.lot3 = lot3;
        this.lot4 = lot4;
        this.lot5 = lot5;
        this.status = status;
    }

    /**
     * - 신규 LOT 생성
     * - 기본 상태 ACTIVE
     *
     * @param productId 상품 ID
     * @param lot1 LOT 주요 식별값
     * @param lot2 LOT 보조 정보
     * @param lot3 유효기간
     * @param lot4 입고일자
     * @param lot5 기타 관리값
     * @return 신규 LOT Entity
     */
    public static Lot create(Long productId, String lot1, String lot2, LocalDate lot3, LocalDate lot4, String lot5) {
        return new Lot(productId, lot1, lot2, lot3, lot4, lot5, LotStatus.ACTIVE);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getLot1() {
        return lot1;
    }

    public String getLot2() {
        return lot2;
    }

    public LocalDate getLot3() {
        return lot3;
    }

    public LocalDate getLot4() {
        return lot4;
    }

    public String getLot5() {
        return lot5;
    }

    public LotStatus getStatus() {
        return status;
    }
}
