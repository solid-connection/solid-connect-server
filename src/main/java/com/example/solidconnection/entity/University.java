package com.example.solidconnection.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
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
