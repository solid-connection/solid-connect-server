package com.example.solidconnection.university.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "liked_university_info_for_apply",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_liked_university_site_user_id_university_info_for_apply_id",
                columnNames = {"site_user_id", "university_info_for_apply_id"}
        )
})
public class LikedUnivApplyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="university_info_for_apply_id")
    private long univApplyInfoId;

    @Column(name="site_user_id")
    private long siteUserId;
}
