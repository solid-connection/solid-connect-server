package com.example.solidconnection.location.country.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_interested_country_site_user_id_country_code",
                columnNames = {"site_user_id", "country_code"}
        )
})
public class InterestedCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="site_user_id")
    private long siteUserId;

    @Column(name="country_code")
    private String countryCode;

    public InterestedCountry(SiteUser siteUser, Country country) {
        this.siteUserId = siteUser.getId();
        this.countryCode = country.getCode();
    }
}
