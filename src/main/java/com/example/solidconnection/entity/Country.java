package com.example.solidconnection.entity;

import com.example.solidconnection.type.CountryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Getter
@Entity
public class Country {

    @Id
    @Enumerated(EnumType.STRING)
    private CountryCode code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    @ManyToOne
    private Region region;

    protected Country() {
    }
}
