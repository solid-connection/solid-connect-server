package com.example.solidconnection.community.board.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseEntity {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 20)
    private String koreanName;

    public Board(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }
}
