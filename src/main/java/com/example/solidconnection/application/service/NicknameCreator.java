package com.example.solidconnection.application.service;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Random;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
class NicknameCreator {

    public static final List<String> ADJECTIVES = List.copyOf(Set.of(
            "기쁜", "행복한", "즐거운", "밝은", "따뜻한", "시원한", "고고한", "예쁜", "신선한", "풍부한", "깨끗한",
            "귀한", "눈부신", "멋진", "고귀한", "화려한", "상큼한", "활기찬", "유쾌한", "똘똘한", "친절한", "좋은",
            "영리한", "용감한", "정직한", "성실한", "강인한", "귀여운", "순수한", "희망찬", "발랄한", "나른한", "후한", "빛나는",
            "따스한", "안락한", "편안한", "성공한", "재미난", "청량한", "찬란한", "소중한", "특별한", "단순한", "반가운", "그리운")
    );
    public static final List<String> NOUNS = List.copyOf(Set.of(
            "청춘", "토끼", "기사", "곰", "사슴", "여우", "팬더", "이슬", "새싹", "햇빛", "나비", "별", "달", "구름",
            "사탕", "젤리", "마법", "풍선", "캔디", "초코", "인형", "쿠키", "요정", "장미", "마녀", "보물", "꽃", "보석",
            "달빛", "오리", "날개", "여행", "편지", "불꽃", "파도", "별빛", "구슬", "노래", "음표", "선율", "미소", "가방",
            "거울", "씨앗", "열매", "바다", "약속", "구두", "공기", "등불", "촛불", "진주", "꿀벌", "예감", "바람",
            "오전", "오후", "아침", "점심", "저녁")
    );

    private static final Random RANDOM = new Random();

    public static String createRandomNickname() {
        String randomAdjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        String randomNoun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        return randomAdjective + " " + randomNoun;
    }
}
