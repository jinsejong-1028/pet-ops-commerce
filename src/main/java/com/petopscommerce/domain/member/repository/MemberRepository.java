package com.petopscommerce.domain.member.repository;

import com.petopscommerce.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MemberRepository는 members 테이블에 대한 기본 CRUD와 회원 전용 조회를 담당합니다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일은 로그인 식별자로 사용할 예정이므로 가입 전에 중복 여부를 확인합니다.
     */
    boolean existsByEmail(String email);
}