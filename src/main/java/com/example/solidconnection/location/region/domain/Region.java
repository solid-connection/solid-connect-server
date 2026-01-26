package com.example.solidconnection.location.region.domain;

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
public class Region {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    public Region(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }

    public void updateKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }
}
