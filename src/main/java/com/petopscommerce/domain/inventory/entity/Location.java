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
 * - Location Entity
 * - locations 테이블 매핑
 */
@Entity
@Table(name = "locations")
public class Location extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * - 창고 ID
     * - DB FK 제약 대신 Service에서 존재 여부 검증
     */
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    /**
     * - Location 코드
     * - 예: A-01-03, PICKTO-01
     */
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * - Location 유형
     * - 보관 위치와 PICKTO 위치 구분
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 30)
    private LocationType locationType;

    /**
     * - Location 사용 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LocationStatus status;

    protected Location() {
        // JPA 기본 생성자
    }

    private Location(Long warehouseId, String code, String name, LocationType locationType, LocationStatus status) {
        this.warehouseId = warehouseId;
        this.code = code;
        this.name = name;
        this.locationType = locationType;
        this.status = status;
    }

    /**
     * - 신규 location 생성
     * - 기본 상태 ACTIVE
     *
     * @param warehouseId 창고 ID
     * @param code location 코드
     * @param name location명
     * @param locationType location 유형
     * @return 신규 location Entity
     */
    public static Location create(Long warehouseId, String code, String name, LocationType locationType) {
        return new Location(warehouseId, code, name, locationType, LocationStatus.ACTIVE);
    }

    public Long getId() {
        return id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public LocationStatus getStatus() {
        return status;
    }
}
