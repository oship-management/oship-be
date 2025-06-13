package org.example.oshipserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.entity.BaseTimeEntity;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at is NULL")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", length = 10, nullable = false)
    private UserRole userRole;

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }

    public void setLastLoginAt(){
        this.lastLoginAt = LocalDateTime.now();
    }
}