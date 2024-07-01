package com.example.solidconnection.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Getter
@Entity
public class InterestedCountry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SiteUser siteUser;

    @ManyToOne
    private Country country;

    protected InterestedCountry() {
    }

    public InterestedCountry(SiteUser siteUser, Country country) {
        this.siteUser = siteUser;
        this.country = country;
    }
}
