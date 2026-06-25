package com.petopscommerce.domain.product.controller;

import com.petopscommerce.domain.product.dto.CreateProductCategoryRequest;
import com.petopscommerce.domain.product.dto.CreateProductRequest;
import com.petopscommerce.domain.product.dto.ProductCategoryResponse;
import com.petopscommerce.domain.product.dto.ProductResponse;
import com.petopscommerce.domain.product.entity.ProductCategoryStatus;
import com.petopscommerce.domain.product.entity.ProductSaleStatus;
import com.petopscommerce.domain.product.service.ProductCategoryService;
import com.petopscommerce.domain.product.service.ProductService;
import com.petopscommerce.global.config.SecurityConfig;
import com.petopscommerce.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * - 상품 Controller 테스트
 * - 공통 응답 구조 검증
 */
@WebMvcTest({ProductCategoryController.class, ProductController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCategoryService productCategoryService;

    @MockitoBean
    private ProductService productService;

    /**
     * - 상품 카테고리 생성 성공 응답 검증
     */
    @Test
    @DisplayName("상품 카테고리 생성 API는 201과 공통 응답을 반환한다")
    void createCategory() throws Exception {
        ProductCategoryResponse response = new ProductCategoryResponse(
                1L,
                "사료",
                1,
                ProductCategoryStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(productCategoryService.createCategory(any(CreateProductCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/product-categories")
                        .with(adminJwt())
                        .contentType("application/json")
                        .content("{\"name\":\"사료\",\"displayOrder\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("사료"));
    }

    /**
     * - 상품 카테고리 목록 응답 검증
     */
    @Test
    @DisplayName("상품 카테고리 목록 API는 공통 목록 응답을 반환한다")
    void getCategories() throws Exception {
        ProductCategoryResponse response = new ProductCategoryResponse(
                1L,
                "사료",
                1,
                ProductCategoryStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(productCategoryService.getCategories()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/product-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    /**
     * - 상품 생성 성공 응답 검증
     */
    @Test
    @DisplayName("상품 생성 API는 201과 공통 상품 응답을 반환한다")
    void createProduct() throws Exception {
        ProductResponse response = new ProductResponse(
                10L,
                1L,
                "고양이 사료",
                "실내묘용 건식 사료",
                25000,
                ProductSaleStatus.ON_SALE,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .with(adminJwt())
                        .contentType("application/json")
                        .content("{\"categoryId\":1,\"name\":\"고양이 사료\",\"description\":\"실내묘용 건식 사료\",\"price\":25000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.categoryId").value(1L))
                .andExpect(jsonPath("$.data.saleStatus").value("ON_SALE"));
    }

    /**
     * - 없는 카테고리 실패 응답 검증
     */
    @Test
    @DisplayName("없는 카테고리로 상품 생성 시 404 공통 실패 응답을 반환한다")
    void createProductWithMissingCategory() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "category not found"));

        mockMvc.perform(post("/api/v1/products")
                        .with(adminJwt())
                        .contentType("application/json")
                        .content("{\"categoryId\":999,\"name\":\"고양이 사료\",\"description\":\"실내묘용 건식 사료\",\"price\":25000}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("category not found"));
    }

    /**
     * - 상품 단건 조회 응답 검증
     */
    @Test
    @DisplayName("상품 단건 조회 API는 공통 상품 응답을 반환한다")
    void getProduct() throws Exception {
        ProductResponse response = new ProductResponse(
                10L,
                1L,
                "고양이 사료",
                "실내묘용 건식 사료",
                25000,
                ProductSaleStatus.ON_SALE,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(productService.getProduct(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    /**
     * - 상품 목록 조회 응답 검증
     */
    @Test
    @DisplayName("상품 목록 API는 공통 목록 응답을 반환한다")
    void getProducts() throws Exception {
        ProductResponse response = new ProductResponse(
                10L,
                1L,
                "고양이 사료",
                "실내묘용 건식 사료",
                25000,
                ProductSaleStatus.ON_SALE,
                LocalDateTime.of(2026, 6, 24, 10, 0)
        );
        when(productService.getProducts(1L, ProductSaleStatus.ON_SALE)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/products?categoryId=1&saleStatus=ON_SALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10L));
    }

    /**
     * - 일반 회원 상품 생성 차단 검증
     */
    @Test
    @DisplayName("일반 회원은 상품 생성 API 호출 시 403을 반환한다")
    void createProductWithMemberRole() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(memberJwt())
                        .contentType("application/json")
                        .content("{\"categoryId\":1,\"name\":\"고양이 사료\",\"description\":\"실내묘용 건식 사료\",\"price\":25000}"))
                .andExpect(status().isForbidden());
    }
    /**
     * - 테스트용 관리자 JWT
     * - 상품/카테고리 생성 인증 통과용
     *
     * @return mock JWT 요청 설정
     */
    private RequestPostProcessor adminJwt() {
        return jwt().jwt(jwt -> jwt
                .subject("1")
                .claim("email", "admin@example.com")
                .claim("role", "ADMIN")
        ).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    /**
     * - 테스트용 일반 회원 JWT
     * - 관리자 API 권한 차단 확인용
     *
     * @return mock JWT 요청 설정
     */
    private RequestPostProcessor memberJwt() {
        return jwt().jwt(jwt -> jwt
                .subject("2")
                .claim("email", "user@example.com")
                .claim("role", "MEMBER")
        ).authorities(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }
}
