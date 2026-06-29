package com.petopscommerce.domain.order.service;

import com.petopscommerce.domain.inventory.entity.Warehouse;
import com.petopscommerce.domain.inventory.entity.WarehouseStatus;
import com.petopscommerce.domain.inventory.repository.WarehouseRepository;
import com.petopscommerce.domain.order.dto.CancelSalesOrderRequest;
import com.petopscommerce.domain.order.dto.ConfirmSalesOrderRequest;
import com.petopscommerce.domain.order.dto.SalesOrderResponse;
import com.petopscommerce.domain.order.entity.Order;
import com.petopscommerce.domain.order.entity.OrderStatus;
import com.petopscommerce.domain.order.entity.SalesOrder;
import com.petopscommerce.domain.order.entity.SalesOrderItem;
import com.petopscommerce.domain.order.entity.SalesOrderStatus;
import com.petopscommerce.domain.order.entity.ShipmentOrder;
import com.petopscommerce.domain.order.entity.ShipmentOrderItem;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.domain.order.repository.SalesOrderItemRepository;
import com.petopscommerce.domain.order.repository.SalesOrderRepository;
import com.petopscommerce.domain.order.repository.ShipmentOrderItemRepository;
import com.petopscommerce.domain.order.repository.ShipmentOrderRepository;
import com.petopscommerce.global.businessnumber.entity.BusinessNumberType;
import com.petopscommerce.global.businessnumber.service.BusinessNumberGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * - 관리자 판매 주문 비즈니스 로직
 * - 판매 주문 확정/취소와 출고 주문 생성 담당
 */
@Service
@Transactional(readOnly = true)
public class SalesOrderService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final ShipmentOrderRepository shipmentOrderRepository;
    private final ShipmentOrderItemRepository shipmentOrderItemRepository;
    private final BusinessNumberGenerator businessNumberGenerator;

    /**
     * - 생성자 주입
     *
     * @param orderRepository 고객 주문 DB 접근 객체
     * @param warehouseRepository 창고 DB 접근 객체
     * @param salesOrderRepository 판매 주문 DB 접근 객체
     * @param salesOrderItemRepository 판매 주문 품목 DB 접근 객체
     * @param shipmentOrderRepository 출고 주문 DB 접근 객체
     * @param shipmentOrderItemRepository 출고 주문 품목 DB 접근 객체
     * @param businessNumberGenerator 업무 번호 생성기
     */
    public SalesOrderService(
            OrderRepository orderRepository,
            WarehouseRepository warehouseRepository,
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            ShipmentOrderRepository shipmentOrderRepository,
            ShipmentOrderItemRepository shipmentOrderItemRepository,
            BusinessNumberGenerator businessNumberGenerator
    ) {
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.shipmentOrderRepository = shipmentOrderRepository;
        this.shipmentOrderItemRepository = shipmentOrderItemRepository;
        this.businessNumberGenerator = businessNumberGenerator;
    }

    /**
     * - 판매 주문 확정
     * - 판매 주문과 고객 주문을 확정하고 창고 출고 주문을 생성
     *
     * @param salesOrderId 판매 주문 ID
     * @param request 판매 주문 확정 요청
     * @return 확정된 판매 주문과 생성된 출고 주문 응답
     */
    @Transactional
    public SalesOrderResponse confirmSalesOrder(Long salesOrderId, ConfirmSalesOrderRequest request) {
        // 단계 1: 판매 주문 확인
        // 결과: CREATED 상태 판매 주문만 확정 가능
        SalesOrder salesOrder = getSalesOrder(salesOrderId);
        validateSalesOrderCreated(salesOrder);

        // 단계 2: 원천 고객 주문 확인
        // 결과: 고객 주문도 CREATED 상태일 때만 함께 확정
        Order customerOrder = getCustomerOrder(salesOrder.getCustomerOrderId());
        validateCustomerOrderCreated(customerOrder);

        // 단계 3: 판매 주문 품목 확인
        // 결과: 출고 주문 품목으로 복사할 판매 주문 품목 확보
        List<SalesOrderItem> salesOrderItems = getSalesOrderItems(salesOrder.getId());

        // 단계 4: 출고 창고 확인
        // 결과: ACTIVE 창고만 출고 주문 생성에 사용
        Warehouse warehouse = getActiveWarehouse(request.warehouseId());

        // 단계 5: 판매 주문/고객 주문 확정
        // 결과: sales_orders와 customer_orders가 같은 업무 확정 상태로 변경
        LocalDateTime confirmedAt = LocalDateTime.now();
        salesOrder.confirm(confirmedAt, request.reason());
        customerOrder.confirm();
        salesOrderItems.forEach(SalesOrderItem::confirm);

        // 단계 6: 출고 주문 저장
        // 결과: 재고 할당 workflow가 참조할 shipment_orders row 생성
        String shipmentOrderNo = businessNumberGenerator.generate(BusinessNumberType.SHIPMENT_ORDER);
        ShipmentOrder shipmentOrder = ShipmentOrder.create(
                shipmentOrderNo,
                salesOrder.getId(),
                warehouse.getId(),
                request.scheduledShipDate(),
                request.reason()
        );
        ShipmentOrder savedShipmentOrder = shipmentOrderRepository.save(shipmentOrder);

        // 단계 7: 출고 주문 품목 저장
        // 결과: 할당/피킹/출고 수량을 0부터 추적할 shipment_order_items row 생성
        List<ShipmentOrderItem> shipmentOrderItems = salesOrderItems.stream()
                .map(salesOrderItem -> ShipmentOrderItem.create(savedShipmentOrder.getId(), salesOrderItem))
                .toList();
        shipmentOrderItemRepository.saveAll(shipmentOrderItems);

        return SalesOrderResponse.from(salesOrder, savedShipmentOrder, shipmentOrderItems.size());
    }

    /**
     * - 판매 주문 취소
     * - 출고 주문 생성 전 판매 주문과 고객 주문을 함께 취소
     *
     * @param salesOrderId 판매 주문 ID
     * @param request 판매 주문 취소 요청
     * @return 취소된 판매 주문 응답
     */
    @Transactional
    public SalesOrderResponse cancelSalesOrder(Long salesOrderId, CancelSalesOrderRequest request) {
        // 단계 1: 판매 주문 확인
        // 결과: 출고 전 CREATED 상태 판매 주문만 취소 가능
        SalesOrder salesOrder = getSalesOrder(salesOrderId);
        validateSalesOrderCreated(salesOrder);

        // 단계 2: 원천 고객 주문 확인
        // 결과: 판매 주문 취소와 같은 트랜잭션에서 고객 주문도 취소
        Order customerOrder = getCustomerOrder(salesOrder.getCustomerOrderId());
        validateCustomerOrderCreated(customerOrder);

        // 단계 3: 판매 주문 품목 확인
        // 결과: 품목 상태도 CANCELED로 함께 변경
        List<SalesOrderItem> salesOrderItems = getSalesOrderItems(salesOrder.getId());

        // 단계 4: 판매 주문/고객 주문 취소
        // 결과: 출고 주문 없이 주문 흐름 종료
        salesOrder.cancel(LocalDateTime.now(), request.reason());
        customerOrder.cancel();
        salesOrderItems.forEach(SalesOrderItem::cancel);

        return SalesOrderResponse.from(salesOrder, salesOrderItems.size());
    }

    /**
     * - 판매 주문 단건 조회
     *
     * @param salesOrderId 판매 주문 ID
     * @return 판매 주문 Entity
     */
    private SalesOrder getSalesOrder(Long salesOrderId) {
        return salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "sales order not found"));
    }

    /**
     * - 고객 주문 단건 조회
     *
     * @param customerOrderId 고객 주문 ID
     * @return 고객 주문 Entity
     */
    private Order getCustomerOrder(Long customerOrderId) {
        return orderRepository.findById(customerOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer order not found"));
    }

    /**
     * - 판매 주문 품목 목록 조회
     *
     * @param salesOrderId 판매 주문 ID
     * @return 판매 주문 품목 목록
     */
    private List<SalesOrderItem> getSalesOrderItems(Long salesOrderId) {
        List<SalesOrderItem> salesOrderItems = salesOrderItemRepository.findBySalesOrderId(salesOrderId);
        if (salesOrderItems.isEmpty()) { // 품목 없는 판매 주문은 출고 지시를 만들 수 없음
            throw new ResponseStatusException(HttpStatus.CONFLICT, "sales order items are empty");
        }
        return salesOrderItems;
    }

    /**
     * - 활성 창고 조회
     *
     * @param warehouseId 창고 ID
     * @return 활성 창고 Entity
     */
    private Warehouse getActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "warehouse not found"));

        if (warehouse.getStatus() != WarehouseStatus.ACTIVE) { // 출고 작업은 사용 가능한 창고에서만 시작
            throw new ResponseStatusException(HttpStatus.CONFLICT, "warehouse is not active");
        }
        return warehouse;
    }

    /**
     * - 판매 주문 CREATED 상태 검증
     *
     * @param salesOrder 판매 주문 Entity
     */
    private void validateSalesOrderCreated(SalesOrder salesOrder) {
        if (salesOrder.getStatus() != SalesOrderStatus.CREATED) { // 확정/취소된 판매 주문은 다시 처리할 수 없음
            throw new ResponseStatusException(HttpStatus.CONFLICT, "sales order is not created status");
        }
    }

    /**
     * - 고객 주문 CREATED 상태 검증
     *
     * @param customerOrder 고객 주문 Entity
     */
    private void validateCustomerOrderCreated(Order customerOrder) {
        if (customerOrder.getStatus() != OrderStatus.CREATED) { // 판매 주문 처리와 고객 주문 상태가 어긋난 경우 중단
            throw new ResponseStatusException(HttpStatus.CONFLICT, "customer order is not created status");
        }
    }
}