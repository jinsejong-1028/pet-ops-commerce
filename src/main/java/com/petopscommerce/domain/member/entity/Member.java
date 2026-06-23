package com.petopscommerce.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * - 회원 Entity
 * - members 테이블 매핑
 */
@Entity
@Table(name = "members")
public class Member {

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

    /**
     * - 생성 일시
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * - 생성자 ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * - 수정 일시
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * - 수정자 ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;

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

    /**
     * - 최초 저장 전 audit 시간 설정
     */
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * - 수정 저장 전 updatedAt 갱신
     */
    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }
}