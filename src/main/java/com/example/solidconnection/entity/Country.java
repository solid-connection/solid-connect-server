package com.example.solidconnection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"code", "koreanName"})
@Entity
public class Country {

    @Id
    @Column(length = 2)
    private String code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    @ManyToOne
    private Region region;

    protected Country() {
    }

    public Country(String code, String koreanName, Region region) {
        this.code = code;
        this.koreanName = koreanName;
        this.region = region;
    }
}
