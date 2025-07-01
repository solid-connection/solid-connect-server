package com.example.solidconnection.university.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_liked_university_site_user_id_university_info_for_apply_id",
                columnNames = {"site_user_id", "university_info_for_apply_id"}
        )
})
public class LikedUniversity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="university_info_for_apply_id")
    private long univApplyInfoId;

    @Column(name="site_user_id")
    private long siteUserId;
}
