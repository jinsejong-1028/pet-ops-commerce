package com.petopscommerce.domain.member.controller;

import com.petopscommerce.domain.member.dto.CreateMemberRequest;
import com.petopscommerce.domain.member.dto.MemberResponse;
import com.petopscommerce.domain.member.service.MemberService;
import com.petopscommerce.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Member", description = "회원 가입과 회원 조회 API")
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
     * - 성공 응답을 공통 응답으로 감싸서 반환
     *
     * @param request 회원 생성 요청
     *                - email: 로그인 이메일
     *                - password: 원본 비밀번호
     *                - name: 회원 이름
     * @return 생성된 회원 공통 응답
     */
    @Operation(summary = "회원 생성", description = "로그인에 사용할 회원 계정을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        return ApiResponse.ok(memberService.createMember(request));
    }

    /**
     * - 회원 단건 조회
     * - 성공 응답을 공통 응답으로 감싸서 반환
     *
     * @param memberId 회원 ID
     * @return 회원 단건 공통 응답
     */
    @Operation(summary = "회원 단건 조회", description = "회원 ID로 회원 기본 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getMember(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId
    ) {
        return ApiResponse.ok(memberService.getMember(memberId));
    }
}