package com.example.solidconnection.type;

import java.util.Comparator;

public enum LanguageTestType {

    CEFR((s1, s2) -> s1.compareTo(s2)),
    JLPT((s1, s2) -> s2.compareTo(s1)),
    DALF(LanguageTestType::compareIntegerScores),
    DELF(LanguageTestType::compareIntegerScores),
    DUOLINGO(LanguageTestType::compareIntegerScores),
    IELTS(LanguageTestType::compareDoubleScores),
    NEW_HSK(LanguageTestType::compareIntegerScores),
    TCF(LanguageTestType::compareIntegerScores),
    TEF(LanguageTestType::compareIntegerScores),
    TOEFL_IBT(LanguageTestType::compareIntegerScores),
    TOEFL_ITP(LanguageTestType::compareIntegerScores),
    TOEIC(LanguageTestType::compareIntegerScores),
    ETC((s1, s2) -> 1), // 기타 언어시험은 점수를 비교할 수 없으므로 항상 크다고 비교한다.
    ;

    private final Comparator<String> comparator;

    LanguageTestType(Comparator<String> comparator) {
        this.comparator = comparator;
    }

    private static int compareIntegerScores(String s1, String s2) {
        return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
    }

    private static int compareDoubleScores(String s1, String s2) {
        return Double.compare(Double.parseDouble(s1), Double.parseDouble(s2));
    }

    public int compare(String s1, String s2) {
        return comparator.compare(s1, s2);
    }
}
