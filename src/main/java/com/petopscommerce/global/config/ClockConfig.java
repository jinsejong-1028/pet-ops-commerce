package com.petopscommerce.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * - 시간 설정
 * - 애플리케이션 기준 시각 제공
 */
@Configuration
public class ClockConfig {

    /**
     * - 시스템 기본 시간대 Clock
     * - 테스트에서는 고정 Clock으로 대체 가능
     *
     * @return 시스템 기본 시간대 Clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}