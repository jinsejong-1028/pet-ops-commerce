package com.petopscommerce.domain.product.repository;

import com.petopscommerce.domain.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * - 상품 카테고리 DB 접근 객체
 * - product_categories 테이블 CRUD 담당
 */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    /**
     * - 표시 순서 기준 전체 카테고리 조회
     *
     * @return 표시 순서 오름차순 카테고리 목록
     */
    List<ProductCategory> findAllByOrderByDisplayOrderAscIdAsc();
}