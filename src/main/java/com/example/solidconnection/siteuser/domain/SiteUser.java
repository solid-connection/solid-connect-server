package com.example.solidconnection.siteuser.domain;

import com.example.solidconnection.community.comment.domain.Comment;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostLike;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_site_user_email_auth_type",
                columnNames = {"email", "auth_type"}
        )
})
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "auth_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private AuthType authType;

    @Setter
    @Column(nullable = false, length = 100)
    private String nickname;

    @Setter
    @Column(length = 500)
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

    @Column(nullable = true)
    private String password;

    @OneToMany(mappedBy = "siteUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> postList = new ArrayList<>();

    @OneToMany(mappedBy = "siteUser", cascade = CascadeType.ALL)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "siteUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "siteUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LanguageTestScore> languageTestScoreList = new ArrayList<>();

    @OneToMany(mappedBy = "siteUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GpaScore> gpaScoreList = new ArrayList<>();

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
        this.authType = AuthType.KAKAO;
    }

    public SiteUser(
            String email,
            String nickname,
            String profileImageUrl,
            String birth,
            PreparationStatus preparationStage,
            Role role,
            Gender gender,
            AuthType authType) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.birth = birth;
        this.preparationStage = preparationStage;
        this.role = role;
        this.gender = gender;
        this.authType = authType;
    }

    // todo: 가입 방법에 따라서 정해진 인자만 받고, 그렇지 않을 경우 예외 발생하도록 수정 필요
    public SiteUser(
            String email,
            String nickname,
            String profileImageUrl,
            String birth,
            PreparationStatus preparationStage,
            Role role,
            Gender gender,
            AuthType authType,
            String password) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.birth = birth;
        this.preparationStage = preparationStage;
        this.role = role;
        this.gender = gender;
        this.authType = authType;
        this.password = password;
    }
}
