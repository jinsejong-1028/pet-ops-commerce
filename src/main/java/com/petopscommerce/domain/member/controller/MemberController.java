package com.petopscommerce.domain.member.controller;

import com.petopscommerce.domain.member.dto.CreateMemberRequest;
import com.petopscommerce.domain.member.dto.MemberResponse;
import com.petopscommerce.domain.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * - 회원 API 진입점
 * - 회원 생성/단건 조회 요청 처리
 */
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    /**
     * - 생성자 주입
     *
     * @param memberService 회원 비즈니스 로직
     */
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * - 회원 생성
     *
     * @param request 회원 생성 요청
     *                - email: 로그인 이메일
     *                - password: 원본 비밀번호
     *                - name: 회원 이름
     * @return 생성된 회원 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@Valid @RequestBody CreateMemberRequest request) {
        return memberService.createMember(request);
    }

    /**
     * - 회원 단건 조회
     *
     * @param memberId 회원 ID
     * @return 회원 단건 응답
     */
    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable Long memberId) {
        return memberService.getMember(memberId);
    }
}