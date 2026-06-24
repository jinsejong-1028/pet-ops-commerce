package com.petopscommerce.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * - JPA Auditing 설정
 * - Entity 저장/수정 시 audit 필드 자동 입력
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "loginMemberAuditorAware")
public class JpaAuditingConfig {
}
