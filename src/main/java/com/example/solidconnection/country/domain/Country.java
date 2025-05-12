package com.example.solidconnection.country.domain;

import com.example.solidconnection.region.domain.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(of = {"code", "koreanName"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Country {

    @Id
    @Column(length = 2)
    private String code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    @ManyToOne
    private Region region;

    public Country(String code, String koreanName, Region region) {
        this.code = code;
        this.koreanName = koreanName;
        this.region = region;
    }
}
