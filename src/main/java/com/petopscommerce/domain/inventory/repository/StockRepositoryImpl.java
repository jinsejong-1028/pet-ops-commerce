package com.petopscommerce.domain.inventory.repository;

import com.petopscommerce.domain.inventory.dto.StockSearchCondition;
import com.petopscommerce.domain.inventory.entity.Stock;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

import static com.petopscommerce.domain.inventory.entity.QStock.stock;

/**
 * - 현재고 QueryDSL 조회 구현
 * - 선택 검색 조건을 BooleanExpression으로 분리
 */
public class StockRepositoryImpl implements StockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * - 생성자 주입
     * - EntityManager 기반 QueryDSL query factory 생성
     *
     * @param entityManager JPA EntityManager
     */
    public StockRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    /**
     * - 현재고 목록 조건 조회
     * - where 절의 null 조건은 QueryDSL이 자동 제외
     *
     * @param condition 현재고 검색 조건
     * @return 현재고 목록
     */
    @Override
    public List<Stock> searchStocks(StockSearchCondition condition) {
        return queryFactory
                .selectFrom(stock)
                .where(
                        productIdEq(condition.productId()),
                        warehouseIdEq(condition.warehouseId()),
                        locationIdEq(condition.locationId()),
                        positiveQuantityOnly(condition.includeZero())
                )
                .fetch();
    }

    /**
     * - 상품 ID 조건
     *
     * @param productId 상품 ID
     * @return 상품 ID 조건식
     */
    private BooleanExpression productIdEq(Long productId) {
        return productId == null ? null : stock.productId.eq(productId);
    }

    /**
     * - 창고 ID 조건
     *
     * @param warehouseId 창고 ID
     * @return 창고 ID 조건식
     */
    private BooleanExpression warehouseIdEq(Long warehouseId) {
        return warehouseId == null ? null : stock.warehouseId.eq(warehouseId);
    }

    /**
     * - location ID 조건
     *
     * @param locationId location ID
     * @return location ID 조건식
     */
    private BooleanExpression locationIdEq(Long locationId) {
        return locationId == null ? null : stock.locationId.eq(locationId);
    }

    /**
     * - 0수량 현재고 기본 제외 조건
     * - 원장 추적을 위해 row는 유지하되 운영 목록에서는 기본 숨김 처리
     *
     * @param includeZero 0수량 포함 여부
     * @return 수량이 남아 있는 현재고 조건식
     */
    private BooleanExpression positiveQuantityOnly(Boolean includeZero) {
        if (Boolean.TRUE.equals(includeZero)) {
            return null;
        }

        return stock.totalQuantity.gt(0)
                .or(stock.availableQuantity.gt(0))
                .or(stock.workingQuantity.gt(0));
    }
}