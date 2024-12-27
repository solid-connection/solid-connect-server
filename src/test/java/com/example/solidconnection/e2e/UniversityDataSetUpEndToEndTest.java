package com.example.solidconnection.e2e;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.support.DatabaseClearExtension;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.UniversityRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashSet;

import static com.example.solidconnection.type.SemesterAvailableForDispatch.ONE_SEMESTER;
import static com.example.solidconnection.type.TuitionFeeType.HOME_UNIVERSITY_PAYMENT;

@ExtendWith(DatabaseClearExtension.class)
@TestContainerSpringBootTest
abstract class UniversityDataSetUpEndToEndTest {

    public static Region 영미권;
    public static Region 유럽;
    public static Region 아시아;
    public static Country 미국;
    public static Country 캐나다;
    public static Country 덴마크;
    public static Country 오스트리아;
    public static Country 일본;

    public static University 영미권_미국_괌대학;
    public static University 영미권_미국_네바다주립대학_라스베이거스;
    public static University 영미권_캐나다_메모리얼대학_세인트존스;
    public static University 유럽_덴마크_서던덴마크대학교;
    public static University 유럽_덴마크_코펜하겐IT대학;
    public static University 유럽_오스트리아_그라츠대학;
    public static University 유럽_오스트리아_그라츠공과대학;
    public static University 유럽_오스트리아_린츠_카톨릭대학;
    public static University 아시아_일본_메이지대학;

    public static UniversityInfoForApply 괌대학_A_지원_정보;
    public static UniversityInfoForApply 괌대학_B_지원_정보;
    public static UniversityInfoForApply 네바다주립대학_라스베이거스_지원_정보;
    public static UniversityInfoForApply 메모리얼대학_세인트존스_A_지원_정보;
    public static UniversityInfoForApply 서던덴마크대학교_지원_정보;
    public static UniversityInfoForApply 코펜하겐IT대학_지원_정보;
    public static UniversityInfoForApply 그라츠대학_지원_정보;
    public static UniversityInfoForApply 그라츠공과대학_지원_정보;
    public static UniversityInfoForApply 린츠_카톨릭대학_지원_정보;
    public static UniversityInfoForApply 메이지대학_지원_정보;

    @Value("${university.term}")
    public String term;

    @LocalServerPort
    private int port;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UniversityInfoForApplyRepository universityInfoForApplyRepository;

    @Autowired
    private LanguageRequirementRepository languageRequirementRepository;

    @BeforeEach
    public void setUpBasicData() {
        RestAssured.port = port;

        영미권 = regionRepository.save(new Region("AMERICAS", "영미권"));
        유럽 = regionRepository.save(new Region("EUROPE", "유럽"));
        아시아 = regionRepository.save(new Region("ASIA", "아시아"));

        미국 = countryRepository.save(new Country("US", "미국", 영미권));
        캐나다 = countryRepository.save(new Country("CA", "캐나다", 영미권));
        덴마크 = countryRepository.save(new Country("DK", "덴마크", 유럽));
        오스트리아 = countryRepository.save(new Country("AT", "오스트리아", 유럽));
        일본 = countryRepository.save(new Country("JP", "일본", 아시아));

        영미권_미국_괌대학 = universityRepository.save(new University(
                null, "괌대학", "University of Guam", "university_of_guam",
                "https://www.uog.edu/admissions/international-students",
                "https://www.uog.edu/admissions/course-schedule",
                "https://www.uog.edu/life-at-uog/residence-halls/",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_guam/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_guam/1.png",
                null, 미국, 영미권
        ));

        영미권_미국_네바다주립대학_라스베이거스 = universityRepository.save(new University(
                null, "네바다주립대학 라스베이거스", "University of Nevada, Las Vegas", "university_of_nevada_las_vegas",
                "https://www.unlv.edu/engineering/eip",
                "https://www.unlv.edu/engineering/academic-programs",
                "https://www.unlv.edu/housing",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_nevada_las_vegas/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_nevada_las_vegas/1.png",
                null, 미국, 영미권
        ));

        영미권_캐나다_메모리얼대학_세인트존스 = universityRepository.save(new University(
                null, "메모리얼 대학 세인트존스", "Memorial University of Newfoundland St. John's", "memorial_university_of_newfoundland_st_johns",
                "https://mun.ca/goabroad/visiting-students-inbound/",
                "https://www.unlv.edu/engineering/academic-programs",
                "https://www.mun.ca/residences/",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/memorial_university_of_newfoundland_st_johns/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/memorial_university_of_newfoundland_st_johns/1.png",
                null, 캐나다, 영미권
        ));

        유럽_덴마크_서던덴마크대학교 = universityRepository.save(new University(
                null, "서던덴마크대학교", "University of Southern Denmark", "university_of_southern_denmark",
                "https://www.sdu.dk/en",
                "https://www.sdu.dk/en",
                "https://www.sdu.dk/en/uddannelse/information_for_international_students/studenthousing",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_southern_denmark/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_southern_denmark/1.png",
                null, 덴마크, 유럽
        ));

        유럽_덴마크_코펜하겐IT대학 = universityRepository.save(new University(
                null, "코펜하겐 IT대학", "IT University of Copenhagen", "it_university_of_copenhagen",
                "https://en.itu.dk/", null,
                "https://en.itu.dk/Programmes/Student-Life/Practical-information-for-international-students",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/it_university_of_copenhagen/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/it_university_of_copenhagen/1.png",
                null, 덴마크, 유럽
        ));

        유럽_오스트리아_그라츠대학 = universityRepository.save(new University(
                null, "그라츠 대학", "University of Graz", "university_of_graz",
                "https://www.uni-graz.at/en/",
                "https://static.uni-graz.at/fileadmin/veranstaltungen/orientation/documents/incstud_application-courses.pdf",
                "https://orientation.uni-graz.at/de/planning-the-arrival/accommodation/",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_graz/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_graz/1.png",
                null, 오스트리아, 유럽
        ));

        유럽_오스트리아_그라츠공과대학 = universityRepository.save(new University(
                null, "그라츠공과대학", "Graz University of Technology", "graz_university_of_technology",
                "https://www.tugraz.at/en/home", null,
                "https://www.tugraz.at/en/studying-and-teaching/studying-internationally/incoming-students-exchange-at-tu-graz/your-stay-at-tu-graz/preparation#c75033",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/graz_university_of_technology/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/graz_university_of_technology/1.png",
                null, 오스트리아, 유럽
        ));

        유럽_오스트리아_린츠_카톨릭대학 = universityRepository.save(new University(
                null, "린츠 카톨릭 대학교", "Catholic Private University Linz", "catholic_private_university_linz",
                "https://ku-linz.at/en", null,
                "https://ku-linz.at/en/ku_international/incomings/kulis",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/catholic_private_university_linz/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/catholic_private_university_linz/1.png",
                null, 오스트리아, 유럽
        ));

        아시아_일본_메이지대학 = universityRepository.save(new University(
                null, "메이지대학", "Meiji University", "meiji_university",
                "https://www.meiji.ac.jp/cip/english/admissions/co7mm90000000461-att/co7mm900000004fa.pdf", null,
                "https://www.meiji.ac.jp/cip/english/admissions/co7mm90000000461-att/co7mm900000004fa.pdf",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/meiji_university/logo.png",
                "https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/meiji_university/1.png",
                null, 일본, 아시아
        ));

        괌대학_A_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "괌대학(A형)", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 영미권_미국_괌대학
        ));

        괌대학_B_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "괌대학(B형)", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 영미권_미국_괌대학
        ));

        네바다주립대학_라스베이거스_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "네바다주립대학 라스베이거스(B형)", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 영미권_미국_네바다주립대학_라스베이거스
        ));

        메모리얼대학_세인트존스_A_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "메모리얼 대학 세인트존스(A형)", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 영미권_캐나다_메모리얼대학_세인트존스
        ));

        서던덴마크대학교_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "서던덴마크대학교", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 유럽_덴마크_서던덴마크대학교
        ));

        코펜하겐IT대학_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "코펜하겐 IT대학", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 유럽_덴마크_코펜하겐IT대학
        ));

        그라츠대학_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "그라츠 대학", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 유럽_오스트리아_그라츠대학
        ));

        그라츠공과대학_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "그라츠공과대학", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 유럽_오스트리아_그라츠공과대학
        ));

        린츠_카톨릭대학_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "린츠 카톨릭 대학교", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 유럽_오스트리아_린츠_카톨릭대학
        ));

        메이지대학_지원_정보 = universityInfoForApplyRepository.save(new UniversityInfoForApply(
                null, term, "메이지대학", 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), 아시아_일본_메이지대학
        ));

        saveLanguageTestRequirement(괌대학_A_지원_정보, LanguageTestType.TOEFL_IBT, "80");
        saveLanguageTestRequirement(괌대학_A_지원_정보, LanguageTestType.TOEIC, "800");
        saveLanguageTestRequirement(괌대학_B_지원_정보, LanguageTestType.TOEFL_IBT, "70");
        saveLanguageTestRequirement(괌대학_B_지원_정보, LanguageTestType.TOEIC, "900");
        saveLanguageTestRequirement(네바다주립대학_라스베이거스_지원_정보, LanguageTestType.TOEIC, "800");
        saveLanguageTestRequirement(메모리얼대학_세인트존스_A_지원_정보, LanguageTestType.TOEIC, "800");
        saveLanguageTestRequirement(서던덴마크대학교_지원_정보, LanguageTestType.TOEFL_IBT, "70");
        saveLanguageTestRequirement(코펜하겐IT대학_지원_정보, LanguageTestType.TOEFL_IBT, "80");
        saveLanguageTestRequirement(그라츠대학_지원_정보, LanguageTestType.TOEFL_IBT, "80");
        saveLanguageTestRequirement(그라츠공과대학_지원_정보, LanguageTestType.TOEIC, "800");
        saveLanguageTestRequirement(린츠_카톨릭대학_지원_정보, LanguageTestType.TOEIC, "800");
        saveLanguageTestRequirement(메이지대학_지원_정보, LanguageTestType.JLPT, "N2");
    }

    private void saveLanguageTestRequirement(
            UniversityInfoForApply universityInfoForApply, LanguageTestType testType, String minScore) {
        LanguageRequirement languageRequirement = new LanguageRequirement(
                null,
                testType,
                minScore,
                universityInfoForApply);
        universityInfoForApply.addLanguageRequirements(languageRequirement);
        universityInfoForApplyRepository.save(universityInfoForApply);
        languageRequirementRepository.save(languageRequirement);
    }
}
