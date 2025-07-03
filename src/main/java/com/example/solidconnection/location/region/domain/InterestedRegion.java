package com.example.solidconnection.location.region.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_interested_region_site_user_id_region_code",
                columnNames = {"site_user_id", "region_code"}
        )
})
public class InterestedRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="site_user_id")
    private long siteUserId;

    @Column(name="region_code")
    private String regionCode;

    public InterestedRegion(SiteUser siteUser, Region region) {
        this.siteUserId = siteUser.getId();
        this.regionCode = region.getCode();
    }
}
