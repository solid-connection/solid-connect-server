package com.example.solidconnection.university.domain;

import com.example.solidconnection.common.BaseEntity;
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
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HostUniversity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
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

    public void update(
            String koreanName,
            String englishName,
            String formatName,
            String homepageUrl,
            String englishCourseUrl,
            String accommodationUrl,
            String logoImageUrl,
            String backgroundImageUrl,
            String detailsForLocal,
            Country country,
            Region region
    ) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.formatName = formatName;
        this.homepageUrl = homepageUrl;
        this.englishCourseUrl = englishCourseUrl;
        this.accommodationUrl = accommodationUrl;
        this.logoImageUrl = logoImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.detailsForLocal = detailsForLocal;
        this.country = country;
        this.region = region;
    }
}
