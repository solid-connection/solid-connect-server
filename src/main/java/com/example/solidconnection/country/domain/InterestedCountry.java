package com.example.solidconnection.country.domain;

import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
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
