package com.example.solidconnection.entity;

import com.example.solidconnection.type.RegionCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
public class Region {

    @Id
    @Enumerated(EnumType.STRING)
    private RegionCode code;

    @Column(nullable = false, length = 100)
    private String koreanName;

    protected Region() {
    }
}
