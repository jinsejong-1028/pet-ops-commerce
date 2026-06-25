package com.petopscommerce.global.businessnumber.repository;

import com.petopscommerce.global.businessnumber.entity.BusinessNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * - 업무 번호 구간 DB 접근 객체
 * - business_number_sequences 테이블 CRUD/잠금 조회 담당
 */
public interface BusinessNumberSequenceRepository extends JpaRepository<BusinessNumberSequence, Long> {

    /**
     * - 업무 번호 구간 row 생성 보장
     * - 동시 최초 요청 시 unique conflict를 피하기 위해 ON CONFLICT DO NOTHING 사용
     *
     * @param ruleCode 업무 번호 규칙 코드
     * @param scopeKey 채번 범위 key
     * @param sequencePeriod 초기화 주기 key
     */
    @Modifying
    @Query(value = """
            insert into business_number_sequences (rule_code, scope_key, sequence_period, next_value)
            values (:ruleCode, :scopeKey, :sequencePeriod, 1)
            on conflict (rule_code, scope_key, sequence_period) do nothing
            """, nativeQuery = true)
    void ensureSequence(
            @Param("ruleCode") String ruleCode,
            @Param("scopeKey") String scopeKey,
            @Param("sequencePeriod") String sequencePeriod
    );

    /**
     * - 업무 번호 구간 잠금 조회
     * - 같은 번호판 동시 할당 시 한 트랜잭션만 nextValue를 변경
     *
     * @param ruleCode 업무 번호 규칙 코드
     * @param scopeKey 채번 범위 key
     * @param sequencePeriod 초기화 주기 key
     * @return 업무 번호 구간 Optional
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select businessNumberSequence
            from BusinessNumberSequence businessNumberSequence
            where businessNumberSequence.ruleCode = :ruleCode
              and businessNumberSequence.scopeKey = :scopeKey
              and businessNumberSequence.sequencePeriod = :sequencePeriod
            """)
    Optional<BusinessNumberSequence> findForUpdate(
            @Param("ruleCode") String ruleCode,
            @Param("scopeKey") String scopeKey,
            @Param("sequencePeriod") String sequencePeriod
    );
}
