package com.example.solidconnection.type;

import com.example.solidconnection.custom.exception.CustomException;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.solidconnection.custom.exception.ErrorCode.INVALID_COUNTRY_NAME;

@Getter
public enum CountryCode {
    BN("브루나이"),
    SG("싱가포르"),
    AZ("아제르바이잔"),
    ID("인도네시아"),
    JP("일본"),
    TR("튀르키예"),
    HK("홍콩"),
    US("미국"),
    CA("캐나다"),
    AU("호주"),
    BR("브라질"),
    NL("네덜란드"),
    NO("노르웨이"),
    DK("덴마크"),
    DE("독일"),
    SE("스웨덴"),
    CH("스위스"),
    ES("스페인"),
    GB("영국"),
    AT("오스트리아"),
    IT("이탈리아"),
    CZ("체코"),
    PT("포르투갈"),
    FR("프랑스"),
    FI("핀란드"),
    CN("중국"),
    TW("대만"),
    HU("헝가리"),
    LT("리투아니아"),
    TH("태국"),
    UZ("우즈베키스탄");

    private static final Map<String, CountryCode> CACHE = new HashMap<>();

    private final String koreanName;

    static {
        for (CountryCode countryCode : CountryCode.values()) {
            CACHE.put(countryCode.getKoreanName(), countryCode);
        }
    }

    CountryCode(String koreanName) {
        this.koreanName = koreanName;
    }

    public static CountryCode getCountryCodeByKoreanName(String koreanName) {
        return Optional.ofNullable(CACHE.get(koreanName))
                .orElseThrow(() -> new CustomException(INVALID_COUNTRY_NAME));
    }

    public static List<CountryCode> getCountryCodeMatchesToKeyword(List<String> keywords) {
        List<CountryCode> matchedCountryCodes = new LinkedList<>();
        keywords.forEach(keyword -> {
            List<CountryCode> countryCodes = Arrays.stream(CountryCode.values())
                    .filter(country -> country.koreanName.contains(keyword))
                    .toList();
            matchedCountryCodes.addAll(countryCodes);
        });
        return matchedCountryCodes;
    }
}
