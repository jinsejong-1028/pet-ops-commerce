package com.petopscommerce.domain.inventory.service;

import com.petopscommerce.domain.inventory.dto.CreateLocationRequest;
import com.petopscommerce.domain.inventory.dto.LocationResponse;
import com.petopscommerce.domain.inventory.entity.Location;
import com.petopscommerce.domain.inventory.repository.LocationRepository;
import com.petopscommerce.domain.inventory.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - Location 비즈니스 로직
 * - 관리자 location 생성 담당
 */
@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * - 생성자 주입
     *
     * @param locationRepository location DB 접근 객체
     * @param warehouseRepository 창고 DB 접근 객체
     */
    public LocationService(LocationRepository locationRepository, WarehouseRepository warehouseRepository) {
        this.locationRepository = locationRepository;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * - Location 생성
     * - 같은 창고 안에서는 location code 중복을 허용하지 않음
     *
     * @param request location 생성 요청
     * @return location 응답
     */
    public LocationResponse createLocation(CreateLocationRequest request) {
        if (!warehouseRepository.existsById(request.warehouseId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "warehouse not found");
        }

        if (locationRepository.existsByWarehouseIdAndCode(request.warehouseId(), request.code())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "location code already exists in warehouse");
        }

        Location location = locationRepository.save(Location.create(
                request.warehouseId(),
                request.code(),
                request.name(),
                request.locationType()
        ));
        return LocationResponse.from(location);
    }
}