package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.StockResponse;
import com.petopscommerce.domain.inventory.entity.Stock;
import com.petopscommerce.domain.inventory.repository.StockRepository;
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
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * - 현재고 Service 테스트
 * - location 단위 재고 조회와 가용수량 계산 검증
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    /**
     * - 현재고 목록 조회와 가용수량 계산 검증
     */
    @Test
    @DisplayName("조건에 맞는 현재고 목록을 조회하고 가용수량을 계산한다")
    void getStocks() {
        Stock stock = Stock.create(1L, 2L, 3L, 4L, 100, 10);
        ReflectionTestUtils.setField(stock, "id", 5L);
        ReflectionTestUtils.setField(stock, "workingQuantity", 3);
        ReflectionTestUtils.setField(stock, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
        when(stockRepository.findStocks(1L, 2L, 3L)).thenReturn(List.of(stock));

        List<StockResponse> responses = stockService.getStocks(1L, 2L, 3L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).totalQuantity()).isEqualTo(100);
        assertThat(responses.get(0).workingQuantity()).isEqualTo(3);
        assertThat(responses.get(0).availableQuantity()).isEqualTo(97);
    }

    /**
     * - 현재고 단건 조회 검증
     */
    @Test
    @DisplayName("현재고 id로 단건 조회한다")
    void getStock() {
        Stock stock = Stock.create(1L, 2L, 3L, 4L, 100, 10);
        ReflectionTestUtils.setField(stock, "id", 5L);
        ReflectionTestUtils.setField(stock, "createdAt", LocalDateTime.of(2026, 6, 24, 10, 0));
        when(stockRepository.findById(5L)).thenReturn(Optional.of(stock));

        StockResponse response = stockService.getStock(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.availableQuantity()).isEqualTo(100);
    }

    /**
     * - 없는 현재고 단건 조회 실패 검증
     */
    @Test
    @DisplayName("없는 현재고를 조회하면 404 오류를 반환한다")
    void getMissingStock() {
        when(stockRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.getStock(999L))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(NOT_FOUND)
                );
    }
}
