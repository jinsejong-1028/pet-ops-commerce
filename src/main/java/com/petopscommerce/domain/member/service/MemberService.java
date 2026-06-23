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
 * MemberService는 회원 도메인의 비즈니스 규칙을 담당합니다.
 * Controller는 HTTP 입출력만 처리하고, 중복 검사와 비밀번호 해시는 이 계층에서 처리합니다.
 */
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원 생성은 DB 변경 작업이므로 readOnly=false 트랜잭션으로 실행합니다.
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
     * 단건 조회는 추후 인증이 붙으면 본인 또는 관리자 권한 검사를 추가할 위치입니다.
     */
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member not found"));

        return MemberResponse.from(member);
    }
}