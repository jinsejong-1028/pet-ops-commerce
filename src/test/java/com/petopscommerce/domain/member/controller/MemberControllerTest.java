package com.petopscommerce.domain.member.controller;

import com.petopscommerce.domain.member.dto.CreateMemberRequest;
import com.petopscommerce.domain.member.dto.MemberResponse;
import com.petopscommerce.domain.member.entity.MemberRole;
import com.petopscommerce.domain.member.entity.MemberStatus;
import com.petopscommerce.domain.member.service.MemberService;
import com.petopscommerce.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("회원 생성 API는 201과 회원 응답을 반환한다")
    void createMember() throws Exception {
        MemberResponse response = new MemberResponse(
                1L,
                "user@example.com",
                "홍길동",
                MemberRole.MEMBER,
                MemberStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 23, 10, 0)
        );
        when(memberService.createMember(any(CreateMemberRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/members")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123\",\"name\":\"홍길동\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("중복 이메일이면 회원 생성 API는 409를 반환한다")
    void createMemberDuplicatedEmail() throws Exception {
        when(memberService.createMember(any(CreateMemberRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "email already exists"));

        mockMvc.perform(post("/api/v1/members")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123\",\"name\":\"홍길동\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원 단건 조회 API는 회원 응답을 반환한다")
    void getMember() throws Exception {
        MemberResponse response = new MemberResponse(
                1L,
                "user@example.com",
                "홍길동",
                MemberRole.MEMBER,
                MemberStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 23, 10, 0)
        );
        when(memberService.getMember(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    @DisplayName("없는 회원을 조회하면 404를 반환한다")
    void getMemberNotFound() throws Exception {
        when(memberService.getMember(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "member not found"));

        mockMvc.perform(get("/api/v1/members/999"))
                .andExpect(status().isNotFound());
    }
}