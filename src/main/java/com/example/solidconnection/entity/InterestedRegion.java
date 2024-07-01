package com.example.solidconnection.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Getter
@Entity
public class InterestedRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private SiteUser siteUser;

    @ManyToOne
    private Region region;

    public InterestedRegion(SiteUser siteUser, Region region) {
        this.siteUser = siteUser;
        this.region = region;
    }

    protected InterestedRegion() {
    }
}
