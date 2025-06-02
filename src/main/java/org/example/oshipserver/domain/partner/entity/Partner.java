package org.example.oshipserver.domain.partner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "partners")
@Entity
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(length = 50, nullable = false)
    private String companyName;

    @Column(length = 30)
    private String companyTelNo;

    @Column(length = 50, nullable = false, unique = true)
    private String companyRegisterNo;
}
