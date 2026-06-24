package com.petopscommerce.domain.member.entity;

import com.petopscommerce.global.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * - 회원 Entity
 * - members 테이블 매핑
 */
@Entity
@Table(name = "members")
public class Member extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * - 비밀번호 해시
     * - 원본 비밀번호 저장 금지
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * - 회원 권한
     * - 인증/인가 기준값
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberRole role;

    /**
     * - 회원 상태
     * - 운영 제어 기준값
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberStatus status;

    protected Member() {
        // JPA 기본 생성자
    }

    private Member(String email, String passwordHash, String name, MemberRole role, MemberStatus status) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    /**
     * - 신규 회원 생성
     * - 기본 권한 MEMBER
     * - 기본 상태 ACTIVE
     *
     * @param email 로그인 이메일
     * @param passwordHash 비밀번호 해시
     * @param name 회원 이름
     * @return 신규 회원 Entity
     */
    public static Member create(String email, String passwordHash, String name) {
        return new Member(email, passwordHash, name, MemberRole.MEMBER, MemberStatus.ACTIVE);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public MemberRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }
}
