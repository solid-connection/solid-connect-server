package com.example.solidconnection.board.domain;

import com.example.solidconnection.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;

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
