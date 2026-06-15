package com.example.solidconnection.university.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HomeUniversity extends BaseEntity {

    public static final int DEFAULT_MAX_CHOICE_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "max_choice_count", nullable = false)
    private int maxChoiceCount;

    @Column(name = "email_domain", unique = true, length = 100)
    private String emailDomain;

    public void update(String name, int maxChoiceCount, String emailDomain) {
        this.name = name;
        this.maxChoiceCount = maxChoiceCount;
        this.emailDomain = emailDomain;
    }
}
