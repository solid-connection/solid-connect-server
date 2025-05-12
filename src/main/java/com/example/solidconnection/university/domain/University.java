package com.example.solidconnection.university.domain;

import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.region.domain.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String koreanName;

    @Column(nullable = false, length = 100)
    private String englishName;

    @Column(nullable = false, length = 100)
    private String formatName;

    @Column(length = 500)
    private String homepageUrl;

    @Column(length = 500)
    private String englishCourseUrl;

    @Column(length = 500)
    private String accommodationUrl;

    @Column(nullable = false, length = 500)
    private String logoImageUrl;

    @Column(nullable = false, length = 500)
    private String backgroundImageUrl;

    @Column(length = 1000)
    private String detailsForLocal;

    @ManyToOne
    private Country country;

    @ManyToOne
    private Region region;
}
