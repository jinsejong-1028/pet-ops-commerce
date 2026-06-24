package com.petopscommerce.domain.product.controller;

import com.petopscommerce.domain.product.dto.CreateProductCategoryRequest;
import com.petopscommerce.domain.product.dto.ProductCategoryResponse;
import com.petopscommerce.domain.product.service.ProductCategoryService;
import com.petopscommerce.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * - 상품 카테고리 API 진입점
 * - 카테고리 생성/목록 조회 요청 처리
 */
@RestController
@RequestMapping("/api/v1/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    /**
     * - 생성자 주입
     *
     * @param productCategoryService 상품 카테고리 비즈니스 로직
     */
    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    /**
     * - 상품 카테고리 생성
     *
     * @param request 상품 카테고리 생성 요청
     * @return 생성된 상품 카테고리 공통 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductCategoryResponse> createCategory(@Valid @RequestBody CreateProductCategoryRequest request) {
        return ApiResponse.ok(productCategoryService.createCategory(request));
    }

    /**
     * - 상품 카테고리 목록 조회
     *
     * @return 상품 카테고리 목록 공통 응답
     */
    @GetMapping
    public ApiResponse<List<ProductCategoryResponse>> getCategories() {
        return ApiResponse.ok(productCategoryService.getCategories());
    }
}