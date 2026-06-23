package com.petopscommerce.domain.member.service;

import com.petopscommerce.domain.member.dto.CreateMemberRequest;
import com.petopscommerce.domain.member.dto.MemberResponse;
import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * - 회원 도메인 비즈니스 로직
 * - 중복 검사/비밀번호 해시 처리
 */
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * - 생성자 주입
     *
     * @param memberRepository 회원 DB 접근 객체
     * @param passwordEncoder 비밀번호 해시 도구
     */
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * - 회원 생성
     * - 이메일 중복 검사
     * - 비밀번호 해시 저장
     *
     * @param request 회원 생성 요청
     * @return 생성된 회원 응답
     */
    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        Member member = Member.create(request.email(), passwordHash, request.name());
        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    /**
     * - 회원 단건 조회
     * - 없으면 404 응답
     *
     * @param memberId 회원 ID
     * @return 회원 단건 응답
     */
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member not found"));

        return MemberResponse.from(member);
    }
}