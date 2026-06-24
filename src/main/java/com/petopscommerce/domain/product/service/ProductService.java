package com.petopscommerce.domain.product.service;

import com.petopscommerce.domain.product.dto.CreateProductRequest;
import com.petopscommerce.domain.product.dto.ProductResponse;
import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import com.petopscommerce.domain.product.repository.ProductCategoryRepository;
import com.petopscommerce.domain.product.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * - 상품 비즈니스 로직
 * - 카테고리 검증/상품 생성/조회 담당
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    /**
     * - 생성자 주입
     *
     * @param productRepository 상품 DB 접근 객체
     * @param productCategoryRepository 상품 카테고리 DB 접근 객체
     */
    public ProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    /**
     * - 상품 생성
     * - DB FK 대신 categoryId 존재 여부를 Service에서 검증
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품 응답
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (!productCategoryRepository.existsById(request.categoryId())) { // 상품 카테고리가 존재하지 않으면 404 오류 반환
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found");
        }

        Product product = Product.create(request.categoryId(), request.name(), request.description(), request.price());
        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }

    /**
     * - 상품 단건 조회
     * - 없으면 404 응답
     *
     * @param productId 상품 ID
     * @return 상품 단건 응답
     */
    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        return ProductResponse.from(product);
    }

    /**
     * - 상품 목록 조회
     * - categoryId, saleStatus 조건 선택 적용
     *
     * @param categoryId 상품 카테고리 ID
     * @param saleStatus 상품 판매 상태
     * @return 상품 응답 목록
     */
    public List<ProductResponse> getProducts(Long categoryId, ProductSaleStatus saleStatus) {
        List<Product> products;

        // 조건: 카테고리 + 판매상태
        // 결과: 카테고리/판매상태 필터 조회
        if (categoryId != null && saleStatus != null) {
            products = productRepository.findByCategoryIdAndSaleStatus(categoryId, saleStatus);

        // 조건: 카테고리
        // 결과: 카테고리 필터 조회
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);

        // 조건: 판매상태
        // 결과: 판매상태 필터 조회
        } else if (saleStatus != null) {
            products = productRepository.findBySaleStatus(saleStatus);

        // 조건: 없음
        // 결과: 전체 조회
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }
}

