package com.example.solidconnection.community.board.domain;

import com.example.solidconnection.community.post.domain.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Board {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 20)
    private String koreanName;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> postList = new ArrayList<>();

    public Board(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }
}
