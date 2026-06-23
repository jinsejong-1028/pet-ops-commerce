package com.petopscommerce.domain.member.repository;

import com.petopscommerce.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - 회원 DB 접근 객체
 * - members 테이블 CRUD 담당
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * - 이메일 중복 여부 확인
     *
     * @param email 로그인 이메일
     * @return 중복이면 true
     */
    boolean existsByEmail(String email);
}