package com.petopscommerce;

import com.petopscommerce.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    void contextLoads() {
        // DB 없이 Spring Bean 구성이 가능한지 확인하는 가장 작은 context smoke test입니다.
    }
}