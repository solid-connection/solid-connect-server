package com.example.solidconnection.location.country.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(of = {"code", "koreanName"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Country extends BaseEntity {

    @Id
    @Column(length = 2)
    private String code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    @Column(name = "region_code")
    private String regionCode;

    public Country(String code, String koreanName, String regionCode) {
        this.code = code;
        this.koreanName = koreanName;
        this.regionCode = regionCode;
    }
}
