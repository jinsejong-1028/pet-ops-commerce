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

/**
 * - 창고 Entity
 * - warehouses 테이블 매핑
 */
@Entity
@Table(name = "warehouses")
public class Warehouse extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 창고 코드
     * - 운영 화면과 재고 조회에서 창고를 식별
     */
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * - 창고 사용 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WarehouseStatus status;

    protected Warehouse() {
        // JPA 기본 생성자
    }

    private Warehouse(String code, String name, WarehouseStatus status) {
        this.code = code;
        this.name = name;
        this.status = status;
    }

    /**
     * - 신규 창고 생성
     * - 기본 상태 ACTIVE
     *
     * @param code 창고 코드
     * @param name 창고명
     * @return 신규 창고 Entity
     */
    public static Warehouse create(String code, String name) {
        return new Warehouse(code, name, WarehouseStatus.ACTIVE);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public WarehouseStatus getStatus() {
        return status;
    }
}
