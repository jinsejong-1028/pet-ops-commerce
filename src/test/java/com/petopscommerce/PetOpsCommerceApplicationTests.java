package com.petopscommerce;

import com.petopscommerce.domain.inventory.repository.LocationRepository;
import com.petopscommerce.domain.inventory.repository.LotRepository;
import com.petopscommerce.domain.inventory.repository.WarehouseRepository;
import com.petopscommerce.domain.inventory.repository.StockJobRepository;
import com.petopscommerce.domain.inventory.repository.StockMovementRepository;
import com.petopscommerce.domain.inventory.repository.StockRepository;
import com.petopscommerce.domain.member.repository.MemberRepository;
import com.petopscommerce.domain.order.repository.OrderItemRepository;
import com.petopscommerce.domain.order.repository.OrderRepository;
import com.petopscommerce.domain.product.repository.ProductCategoryRepository;
import com.petopscommerce.domain.product.repository.ProductRepository;
import com.petopscommerce.global.businessnumber.repository.BusinessNumberRuleRepository;
import com.petopscommerce.global.businessnumber.repository.BusinessNumberSequenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class PetOpsCommerceApplicationTests {

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private ProductCategoryRepository productCategoryRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private StockRepository stockRepository;

    @MockitoBean
    private LocationRepository locationRepository;

    @MockitoBean
    private WarehouseRepository warehouseRepository;

    @MockitoBean
    private LotRepository lotRepository;

    @MockitoBean
    private StockJobRepository stockJobRepository;

    @MockitoBean
    private StockMovementRepository stockMovementRepository;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private BusinessNumberRuleRepository businessNumberRuleRepository;

    @MockitoBean
    private BusinessNumberSequenceRepository businessNumberSequenceRepository;

    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void contextLoads() {
        // DB 없이 Spring Bean 구성이 가능한지 확인하는 가장 작은 context smoke test입니다.
    }
}
