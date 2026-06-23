package com.petopscommerce.domain.member.service;

import com.petopscommerce.domain.member.dto.CreateMemberRequest;
import com.petopscommerce.domain.member.dto.MemberResponse;
import com.petopscommerce.domain.member.entity.Member;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.domain.member.entity.MemberStatus;
import com.petopscommerce.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 생성 시 비밀번호를 해시로 변환하고 기본 상태를 반환한다")
    void createMember() {
        CreateMemberRequest request = new CreateMemberRequest("user@example.com", "password123", "홍길동");

        when(memberRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            // DB 저장 결과를 흉내 내기 위해 테스트에서만 id와 생성 시간을 주입합니다.
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "createdAt", LocalDateTime.of(2026, 6, 23, 10, 0));
            return member;
        });

        MemberResponse response = memberService.createMember(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.role()).isEqualTo(MemberRole.MEMBER);
        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 409 오류를 반환한다")
    void createMemberWithDuplicatedEmail() {
        CreateMemberRequest request = new CreateMemberRequest("user@example.com", "password123", "홍길동");
        when(memberRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(CONFLICT)
                );
    }

    @Test
    @DisplayName("회원 id로 단건 조회한다")
    void getMember() {
        Member member = Member.create("user@example.com", "hashed-password", "홍길동");
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "createdAt", LocalDateTime.of(2026, 6, 23, 10, 0));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberResponse response = memberService.getMember(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("없는 회원 id를 조회하면 404 오류를 반환한다")
    void getMemberNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMember(999L))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(NOT_FOUND)
                );
    }
}