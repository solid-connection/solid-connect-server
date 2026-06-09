package com.example.solidconnection.application.domain;

import static com.example.solidconnection.common.VerifyStatus.PENDING;

import com.example.solidconnection.common.BaseEntity;
import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@DynamicUpdate
@DynamicInsert
@Entity
@Table(indexes = {
        @Index(name = "idx_app_user_term_delete",
                columnList = "site_user_id, term_id, is_delete")
})
public class Application extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Embedded
    private Gpa gpa;

    @Embedded
    private LanguageTest languageTest;

    @Setter
    @ColumnDefault("'PENDING'")
    @Column(name = "verify_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus = VerifyStatus.PENDING;

    @Column(length = 100, name = "nickname_for_apply")
    private String nicknameForApply;

    @ColumnDefault("1")
    @Column(name = "update_count", nullable = false)
    private Integer updateCount = 1;

    @Column(nullable = false, name = "term_id")
    private long termId;

    @ColumnDefault("false")
    @Column(name = "is_delete", nullable = false)
    private boolean isDelete = false;

    @Column(name = "site_user_id", nullable = false)
    private long siteUserId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "application_choice",
            joinColumns = @JoinColumn(name = "application_id")
    )
    @OrderBy("choiceOrder ASC")
    private List<ApplicationChoice> choices = new ArrayList<>();

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            long termId) {
        this.siteUserId = siteUser.getId();
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.termId = termId;
        this.updateCount = 1;
        this.verifyStatus = PENDING;
    }

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            long termId,
            Integer updateCount,
            List<ApplicationChoice> choices,
            String nicknameForApply) {
        this.siteUserId = siteUser.getId();
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.termId = termId;
        this.updateCount = updateCount;
        this.choices = new ArrayList<>(choices);
        this.nicknameForApply = nicknameForApply;
        this.verifyStatus = PENDING;
    }

    public void setIsDeleteTrue() {
        this.isDelete = true;
    }
}
