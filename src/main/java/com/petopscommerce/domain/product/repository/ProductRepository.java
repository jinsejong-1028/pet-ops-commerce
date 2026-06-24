package com.petopscommerce.domain.product.repository;

import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * - 상품 DB 접근 객체
 * - products 테이블 CRUD 담당
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * - 카테고리별 상품 목록 조회
     *
     * @param categoryId 상품 카테고리 ID
     * @return 상품 목록
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * - 판매 상태별 상품 목록 조회
     *
     * @param saleStatus 상품 판매 상태
     * @return 상품 목록
     */
    List<Product> findBySaleStatus(ProductSaleStatus saleStatus);

    /**
     * - 카테고리와 판매 상태 기준 상품 목록 조회
     *
     * @param categoryId 상품 카테고리 ID
     * @param saleStatus 상품 판매 상태
     * @return 상품 목록
     */
    List<Product> findByCategoryIdAndSaleStatus(Long categoryId, ProductSaleStatus saleStatus);
}