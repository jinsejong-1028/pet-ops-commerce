package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.CreateWarehouseRequest;
import com.petopscommerce.domain.inventory.dto.WarehouseResponse;
import com.petopscommerce.domain.inventory.entity.Warehouse;
import com.petopscommerce.domain.inventory.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 창고 비즈니스 로직
 * - 관리자 창고 생성 담당
 */
@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    /**
     * - 생성자 주입
     *
     * @param warehouseRepository 창고 DB 접근 객체
     */
    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * - 창고 생성
     * - 창고 코드는 전체 창고에서 중복될 수 없음
     *
     * @param request 창고 생성 요청
     * @return 창고 응답
     */
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "warehouse code already exists");
        }

        Warehouse warehouse = warehouseRepository.save(Warehouse.create(request.code(), request.name()));
        return WarehouseResponse.from(warehouse);
    }
}