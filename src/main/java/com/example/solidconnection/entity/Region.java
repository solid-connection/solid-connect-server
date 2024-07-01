package com.example.solidconnection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"code", "koreanName"})
@Entity
public class Region {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    protected Region() {
    }

    public Region(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }
}
