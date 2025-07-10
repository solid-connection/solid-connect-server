package com.example.solidconnection.application.domain;

import com.example.solidconnection.common.VerifyStatus;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import static com.example.solidconnection.common.VerifyStatus.PENDING;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@DynamicUpdate
@DynamicInsert
@Entity
@Table(indexes = {
        @Index(name = "idx_app_user_term_delete",
                columnList = "site_user_id, term, is_delete"),
        @Index(name = "idx_app_first_choice_search",
                columnList = "verify_status, term, is_delete, first_choice_university_info_for_apply_id"),
        @Index(name = "idx_app_second_choice_search",
                columnList = "verify_status, term, is_delete, second_choice_university_info_for_apply_id"),
        @Index(name = "idx_app_third_choice_search",
                columnList = "verify_status, term, is_delete, third_choice_university_info_for_apply_id")
})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Gpa gpa;

    @Embedded
    private LanguageTest languageTest;

    @Setter
    @Column(columnDefinition = "varchar(50) not null default 'PENDING'", name="verify_status")
    @Enumerated(EnumType.STRING)
    private VerifyStatus verifyStatus;

    @Column(length = 100, name="nickname_for_apply")
    private String nicknameForApply;

    @Column(columnDefinition = "int not null default 1", name="update_count")
    private Integer updateCount;

    @Column(length = 50, nullable = false, name="term")
    private String term;

    @Column(name="is_delete")
    private boolean isDelete = false;

    @Column(nullable = false , name = "first_choice_university_info_for_apply_id")
    private long firstChoiceUnivApplyInfoId;

    @Column(name = "second_choice_university_info_for_apply_id")
    private Long secondChoiceUnivApplyInfoId;

    @Column(name = "third_choice_university_info_for_apply_id")
    private Long thirdChoiceUnivApplyInfoId;

    @Column(name = "site_user_id")
    private long siteUserId;

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String term) {
        this.siteUserId = siteUser.getId();
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.term = term;
        this.updateCount = 1;
        this.verifyStatus = PENDING;
    }

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String term,
            Integer updateCount,
            long firstChoiceUnivApplyInfoId,
            Long secondChoiceUnivApplyInfoId,
            Long thirdChoiceUnivApplyInfoId,
            String nicknameForApply) {
        this.siteUserId = siteUser.getId();
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.term = term;
        this.updateCount = updateCount;
        this.firstChoiceUnivApplyInfoId = firstChoiceUnivApplyInfoId;
        this.secondChoiceUnivApplyInfoId = secondChoiceUnivApplyInfoId;
        this.thirdChoiceUnivApplyInfoId = thirdChoiceUnivApplyInfoId;
        this.nicknameForApply = nicknameForApply;
        this.verifyStatus = PENDING;
    }

    public Application(
            SiteUser siteUser,
            Gpa gpa,
            LanguageTest languageTest,
            String term,
            long firstChoiceUnivApplyInfoId,
            Long secondChoiceUnivApplyInfoId,
            Long thirdChoiceUnivApplyInfoId,
            String nicknameForApply) {
        this.siteUserId = siteUser.getId();
        this.gpa = gpa;
        this.languageTest = languageTest;
        this.term = term;
        this.updateCount = 1;
        this.firstChoiceUnivApplyInfoId = firstChoiceUnivApplyInfoId;
        this.secondChoiceUnivApplyInfoId = secondChoiceUnivApplyInfoId;
        this.thirdChoiceUnivApplyInfoId = thirdChoiceUnivApplyInfoId;
        this.nicknameForApply = nicknameForApply;
        this.verifyStatus = PENDING;
    }

    public void setIsDeleteTrue() {
        this.isDelete = true;
    }
}
