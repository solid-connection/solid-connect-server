package com.example.solidconnection.location.country.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
                name = "uk_interested_country_site_user_id_country_code",
                columnNames = {"site_user_id", "country_code"}
        )
})
public class InterestedCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SiteUser siteUser;

    @ManyToOne
    private Country country;

    public InterestedCountry(SiteUser siteUser, Country country) {
        this.siteUser = siteUser;
        this.country = country;
    }
}
