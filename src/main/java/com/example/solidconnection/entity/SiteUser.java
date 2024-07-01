package com.example.solidconnection.entity;

import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    @Setter
    private String nickname;

    @Column(length = 500)
    @Setter
    private String profileImageUrl;

    @Column(nullable = false, length = 20)
    private String birth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PreparationStatus preparationStage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Setter
    private LocalDateTime nicknameModifiedAt;

    @Setter
    private LocalDate quitedAt;

    public SiteUser(
            String email,
            String nickname,
            String profileImageUrl,
            String birth,
            PreparationStatus preparationStage,
            Role role,
            Gender gender) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.birth = birth;
        this.preparationStage = preparationStage;
        this.role = role;
        this.gender = gender;
    }
}
