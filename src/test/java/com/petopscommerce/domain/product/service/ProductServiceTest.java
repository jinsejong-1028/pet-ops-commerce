package com.petopscommerce.domain.product.service;

import com.petopscommerce.domain.product.dto.CreateProductRequest;
import com.petopscommerce.domain.product.dto.ProductResponse;
import com.petopscommerce.domain.product.entity.Product;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import com.petopscommerce.domain.product.repository.ProductCategoryRepository;
import com.petopscommerce.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * - 상품 Service 테스트
 * - 카테고리 검증/생성/조회 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private ProductService productService;

    /**
     * - 상품 생성 성공 검증
     */
    @Test
    @DisplayName("카테고리가 존재하면 상품을 생성한다")
    void createProduct() {
        CreateProductRequest request = new CreateProductRequest(1L, "고양이 사료", "실내묘용 건식 사료", 25000);

        when(productCategoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ReflectionTestUtils.setField(product, "id", 10L);
            ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
            return product;
        });

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.categoryId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("고양이 사료");
        assertThat(response.price()).isEqualTo(25000);
        assertThat(response.saleStatus()).isEqualTo(ProductSaleStatus.ON_SALE);
    }

    /**
     * - 없는 카테고리 상품 생성 실패 검증
     */
    @Test
    @DisplayName("없는 카테고리로 상품을 생성하면 404 오류를 반환한다")
    void createProductWithMissingCategory() {
        CreateProductRequest request = new CreateProductRequest(999L, "고양이 사료", "실내묘용 건식 사료", 25000);
        when(productCategoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(NOT_FOUND)
                );
    }

    /**
     * - 상품 단건 조회 검증
     */
    @Test
    @DisplayName("상품 id로 단건 조회한다")
    void getProduct() {
        Product product = Product.create(1L, "고양이 사료", "실내묘용 건식 사료", 25000);
        ReflectionTestUtils.setField(product, "id", 10L);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("고양이 사료");
    }

    /**
     * - 상품 목록 조회 검증
     */
    @Test
    @DisplayName("카테고리와 판매 상태 조건으로 상품 목록을 조회한다")
    void getProductsByCategoryAndSaleStatus() {
        Product product = Product.create(1L, "고양이 사료", "실내묘용 건식 사료", 25000);
        ReflectionTestUtils.setField(product, "id", 10L);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
        when(productRepository.findByCategoryIdAndSaleStatus(1L, ProductSaleStatus.ON_SALE)).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getProducts(1L, ProductSaleStatus.ON_SALE);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).categoryId()).isEqualTo(1L);
        assertThat(responses.get(0).saleStatus()).isEqualTo(ProductSaleStatus.ON_SALE);
    }
}