package com.petopscommerce.domain.product.service;

import com.petopscommerce.domain.product.dto.CreateProductCategoryRequest;
import com.petopscommerce.domain.product.dto.ProductCategoryResponse;
import com.petopscommerce.domain.product.entity.ProductCategory;
import com.petopscommerce.domain.product.entity.ProductCategoryStatus;
import com.petopscommerce.domain.product.repository.ProductCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * - 상품 카테고리 Service 테스트
 * - 생성/목록 조회 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private ProductCategoryService productCategoryService;

    /**
     * - 상품 카테고리 생성 검증
     */
    @Test
    @DisplayName("상품 카테고리를 생성하고 기본 상태를 반환한다")
    void createCategory() {
        CreateProductCategoryRequest request = new CreateProductCategoryRequest("사료", 1);

        when(productCategoryRepository.save(any(ProductCategory.class))).thenAnswer(invocation -> {
            ProductCategory category = invocation.getArgument(0);
            ReflectionTestUtils.setField(category, "id", 1L);
            ReflectionTestUtils.setField(category, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
            return category;
        });

        ProductCategoryResponse response = productCategoryService.createCategory(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("사료");
        assertThat(response.displayOrder()).isEqualTo(1);
        assertThat(response.status()).isEqualTo(ProductCategoryStatus.ACTIVE);
    }

    /**
     * - 표시 순서 기준 목록 조회 검증
     */
    @Test
    @DisplayName("상품 카테고리 목록을 표시 순서 기준으로 조회한다")
    void getCategories() {
        ProductCategory category = ProductCategory.create("사료", 1);
        ReflectionTestUtils.setField(category, "id", 1L);
        ReflectionTestUtils.setField(category, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
        when(productCategoryRepository.findAllByOrderByDisplayOrderAscIdAsc()).thenReturn(List.of(category));

        List<ProductCategoryResponse> responses = productCategoryService.getCategories();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("사료");
    }
}