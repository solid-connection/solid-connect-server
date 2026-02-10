package com.example.solidconnection.term.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_term_name",
                columnNames = {"name"}
        )
})
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(name = "is_current", unique = true)
    private Boolean isCurrent;

    public Term(String name, boolean isCurrent) {
        this.name = name;
        this.isCurrent = isCurrent ? true : null;
    }

    public void setAsCurrent() {
        this.isCurrent = true;
    }

    public void setAsNotCurrent() {
        this.isCurrent = null;
    }
}
