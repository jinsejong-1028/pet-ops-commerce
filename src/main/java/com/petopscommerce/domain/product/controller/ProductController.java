package com.petopscommerce.domain.product.controller;

import com.petopscommerce.domain.product.dto.CreateProductRequest;
import com.petopscommerce.domain.product.dto.ProductResponse;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import com.petopscommerce.domain.product.service.ProductService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * - 상품 API 진입점
 * - 상품 생성/단건 조회/목록 조회 요청 처리
 */
@Tag(name = "Product", description = "상품 카테고리와 상품 카탈로그 API")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    /**
     * - 생성자 주입
     *
     * @param productService 상품 비즈니스 로직
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * - 상품 생성
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품 공통 응답
     */
    @Operation(summary = "상품 생성", description = "관리자 또는 운영자가 판매 상품을 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.ok(productService.createProduct(request));
    }

    /**
     * - 상품 단건 조회
     *
     * @param productId 상품 ID
     * @return 상품 단건 공통 응답
     */
    @Operation(summary = "상품 단건 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId
    ) {
        return ApiResponse.ok(productService.getProduct(productId));
    }

    /**
     * - 상품 목록 조회
     * - categoryId, saleStatus 조건 선택 적용
     *
     * @param categoryId 상품 카테고리 ID
     * @param saleStatus 상품 판매 상태
     * @return 상품 목록 공통 응답
     */
    @Operation(summary = "상품 목록 조회", description = "카테고리와 판매 상태 조건으로 상품 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<ProductResponse>> getProducts(
            @Parameter(description = "상품 카테고리 ID", example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "상품 판매 상태", example = "ON_SALE") @RequestParam(required = false) ProductSaleStatus saleStatus
    ) {
        return ApiResponse.ok(productService.getProducts(categoryId, saleStatus));
    }
}