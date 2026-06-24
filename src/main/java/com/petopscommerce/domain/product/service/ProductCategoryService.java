package com.petopscommerce.domain.product.service;

import com.petopscommerce.domain.product.dto.CreateProductCategoryRequest;
import com.petopscommerce.domain.product.dto.ProductCategoryResponse;
import com.petopscommerce.domain.product.entity.ProductCategory;
import com.petopscommerce.domain.product.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * - 상품 카테고리 비즈니스 로직
 * - 카테고리 생성/목록 조회 담당
 */
@Service
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    /**
     * - 생성자 주입
     *
     * @param productCategoryRepository 상품 카테고리 DB 접근 객체
     */
    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    /**
     * - 상품 카테고리 생성
     *
     * @param request 상품 카테고리 생성 요청
     * @return 생성된 상품 카테고리 응답
     */
    @Transactional
    public ProductCategoryResponse createCategory(CreateProductCategoryRequest request) {
        ProductCategory category = ProductCategory.create(request.name(), request.displayOrder());
        ProductCategory savedCategory = productCategoryRepository.save(category);

        return ProductCategoryResponse.from(savedCategory);
    }

    /**
     * - 상품 카테고리 목록 조회
     * - 표시 순서 기준 정렬
     *
     * @return 상품 카테고리 응답 목록
     */
    public List<ProductCategoryResponse> getCategories() {
        return productCategoryRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(ProductCategoryResponse::from)
                .toList();
    }
}