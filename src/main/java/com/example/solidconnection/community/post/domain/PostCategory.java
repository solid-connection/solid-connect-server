package com.example.solidconnection.community.post.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum PostCategory {
    전체, 자유, 질문;

    private static final Set<String> NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    public static boolean isValid(String name){
        return name != null && NAMES.contains(name);
    }
}
