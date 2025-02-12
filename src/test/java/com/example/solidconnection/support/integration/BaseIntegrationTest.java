package com.example.solidconnection.support.integration;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.application.domain.LanguageTest;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.community.board.domain.Board;
import com.example.solidconnection.community.board.repository.BoardRepository;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.community.post.domain.PostImage;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.repositories.CountryRepository;
import com.example.solidconnection.community.post.repository.PostImageRepository;
import com.example.solidconnection.repositories.RegionRepository;
import com.example.solidconnection.score.domain.GpaScore;
import com.example.solidconnection.score.domain.LanguageTestScore;
import com.example.solidconnection.score.repository.GpaScoreRepository;
import com.example.solidconnection.score.repository.LanguageTestScoreRepository;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.support.DatabaseClearExtension;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import com.example.solidconnection.type.Gender;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.PostCategory;
import com.example.solidconnection.type.PreparationStatus;
import com.example.solidconnection.type.Role;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.UniversityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;
import java.util.List;

import static com.example.solidconnection.type.BoardCode.AMERICAS;
import static com.example.solidconnection.type.BoardCode.ASIA;
import static com.example.solidconnection.type.BoardCode.EUROPE;
import static com.example.solidconnection.type.BoardCode.FREE;
import static com.example.solidconnection.type.SemesterAvailableForDispatch.ONE_SEMESTER;
import static com.example.solidconnection.type.TuitionFeeType.HOME_UNIVERSITY_PAYMENT;

@TestContainerSpringBootTest
@ExtendWith(DatabaseClearExtension.class)
public abstract class BaseIntegrationTest {

    public static SiteUser 테스트유저_1;
    public static SiteUser 테스트유저_2;
    public static SiteUser 테스트유저_3;
    public static SiteUser 테스트유저_4;
    public static SiteUser 테스트유저_5;
    public static SiteUser 테스트유저_6;
    public static SiteUser 테스트유저_7;
    public static SiteUser 이전학기_지원자;

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

    public static Application 테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서;
    public static Application 테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서;
    public static Application 테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서;
    public static Application 테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서;
    public static Application 테스트유저_6_X_X_X_지원서;
    public static Application 테스트유저_7_코펜하겐IT대학_X_X_지원서;
    public static Application 이전학기_지원서;

    public static Board 미주권;
    public static Board 아시아권;
    public static Board 유럽권;
    public static Board 자유게시판;

    public static Post 미주권_자유게시글;
    public static Post 아시아권_자유게시글;
    public static Post 유럽권_자유게시글;
    public static Post 자유게시판_자유게시글;
    public static Post 미주권_질문게시글;
    public static Post 아시아권_질문게시글;
    public static Post 유럽권_질문게시글;
    public static Post 자유게시판_질문게시글;

    @Autowired
    private SiteUserRepository siteUserRepository;

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

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GpaScoreRepository gpaScoreRepository;

    @Autowired
    private LanguageTestScoreRepository languageTestScoreRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Value("${university.term}")
    public String term;

    @BeforeEach
    public void setUpBaseData() {
        setUpSiteUsers();
        setUpRegions();
        setUpCountries();
        setUpUniversities();
        setUpUniversityInfos();
        setUpLanguageRequirements();
        setUpApplications();
        setUpBoards();
        setUpPosts();
    }

    private void setUpSiteUsers() {
        테스트유저_1 = siteUserRepository.save(new SiteUser(
                "test1@example.com",
                "nickname1",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE));

        테스트유저_2 = siteUserRepository.save(new SiteUser(
                "test2@example.com",
                "nickname2",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE));

        테스트유저_3 = siteUserRepository.save(new SiteUser(
                "test3@example.com",
                "nickname3",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE));

        테스트유저_4 = siteUserRepository.save(new SiteUser(
                "test4@example.com",
                "nickname4",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE));

        테스트유저_5 = siteUserRepository.save(new SiteUser(
                "test5@example.com",
                "nickname5",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE));

        테스트유저_6 = siteUserRepository.save(new SiteUser(
                "test6@example.com",
                "nickname6",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE));

        테스트유저_7 = siteUserRepository.save(new SiteUser(
                "test7@example.com",
                "nickname7",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.FEMALE));

        이전학기_지원자 = siteUserRepository.save(new SiteUser(
                "old@example.com",
                "oldNickname",
                "profileImageUrl",
                "1999-01-01",
                PreparationStatus.CONSIDERING,
                Role.MENTEE,
                Gender.MALE));
    }

    private void setUpRegions() {
        영미권 = regionRepository.save(new Region("AMERICAS", "영미권"));
        유럽 = regionRepository.save(new Region("EUROPE", "유럽"));
        아시아 = regionRepository.save(new Region("ASIA", "아시아"));
    }

    private void setUpCountries() {
        미국 = countryRepository.save(new Country("US", "미국", 영미권));
        캐나다 = countryRepository.save(new Country("CA", "캐나다", 영미권));
        덴마크 = countryRepository.save(new Country("DK", "덴마크", 유럽));
        오스트리아 = countryRepository.save(new Country("AT", "오스트리아", 유럽));
        일본 = countryRepository.save(new Country("JP", "일본", 아시아));
    }

    private void setUpUniversities() {
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
    }

    private void setUpUniversityInfos() {
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
    }

    private void setUpLanguageRequirements() {
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

    private void setUpApplications() {
        테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서 = new Application(테스트유저_2, createApprovedGpaScore(테스트유저_2).getGpa(), createApprovedLanguageTestScore(테스트유저_2).getLanguageTest(),
                term, 괌대학_B_지원_정보, 괌대학_A_지원_정보, 린츠_카톨릭대학_지원_정보, "user2_nickname");

        테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서 = new Application(테스트유저_3, createApprovedGpaScore(테스트유저_3).getGpa(), createApprovedLanguageTestScore(테스트유저_3).getLanguageTest(),
                term, 괌대학_A_지원_정보, 괌대학_B_지원_정보, 그라츠공과대학_지원_정보, "user3_nickname");

        테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서 = new Application(테스트유저_4, createApprovedGpaScore(테스트유저_4).getGpa(), createApprovedLanguageTestScore(테스트유저_4).getLanguageTest(),
                term, 메이지대학_지원_정보, 그라츠대학_지원_정보, 서던덴마크대학교_지원_정보, "user4_nickname");

        테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서 = new Application(테스트유저_5, createApprovedGpaScore(테스트유저_5).getGpa(), createApprovedLanguageTestScore(테스트유저_5).getLanguageTest(),
                term, 네바다주립대학_라스베이거스_지원_정보, 그라츠공과대학_지원_정보, 메이지대학_지원_정보, "user5_nickname");

        테스트유저_6_X_X_X_지원서 = new Application(테스트유저_6, createApprovedGpaScore(테스트유저_6).getGpa(), createApprovedLanguageTestScore(테스트유저_6).getLanguageTest(),
                term, null, null, null, "user6_nickname");

        테스트유저_7_코펜하겐IT대학_X_X_지원서 = new Application(테스트유저_7, createApprovedGpaScore(테스트유저_7).getGpa(), createApprovedLanguageTestScore(테스트유저_7).getLanguageTest(),
                term, 코펜하겐IT대학_지원_정보, null, null, "user7_nickname");

        이전학기_지원서 = new Application(이전학기_지원자, createApprovedGpaScore(이전학기_지원자).getGpa(), createApprovedLanguageTestScore(이전학기_지원자).getLanguageTest(),
                "1988-1", 네바다주립대학_라스베이거스_지원_정보, 그라츠공과대학_지원_정보, 메이지대학_지원_정보, "old_nickname");

        테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서.setVerifyStatus(VerifyStatus.APPROVED);
        테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서.setVerifyStatus(VerifyStatus.APPROVED);
        테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서.setVerifyStatus(VerifyStatus.APPROVED);
        테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서.setVerifyStatus(VerifyStatus.APPROVED);
        테스트유저_6_X_X_X_지원서.setVerifyStatus(VerifyStatus.APPROVED);
        테스트유저_7_코펜하겐IT대학_X_X_지원서.setVerifyStatus(VerifyStatus.APPROVED);
        이전학기_지원서.setVerifyStatus(VerifyStatus.APPROVED);

        applicationRepository.saveAll(List.of(
                테스트유저_2_괌대학_B_괌대학_A_린츠_카톨릭대학_지원서, 테스트유저_3_괌대학_A_괌대학_B_그라츠공과대학_지원서, 테스트유저_4_메이지대학_그라츠대학_서던덴마크대학_지원서, 테스트유저_5_네바다주립대학_그라츠공과대학_메이지대학_지원서,
                테스트유저_6_X_X_X_지원서, 테스트유저_7_코펜하겐IT대학_X_X_지원서, 이전학기_지원서));
    }

    private void setUpBoards() {
        미주권 = boardRepository.save(new Board(AMERICAS.name(), "미주권"));
        아시아권 = boardRepository.save(new Board(ASIA.name(), "아시아권"));
        유럽권 = boardRepository.save(new Board(EUROPE.name(), "유럽권"));
        자유게시판 = boardRepository.save(new Board(FREE.name(), "자유게시판"));
    }

    private void setUpPosts() {
        미주권_자유게시글 = createPost(미주권, 테스트유저_1, "미주권 자유게시글", "미주권 자유게시글 내용", PostCategory.자유);
        아시아권_자유게시글 = createPost(아시아권, 테스트유저_2, "아시아권 자유게시글", "아시아권 자유게시글 내용", PostCategory.자유);
        유럽권_자유게시글 = createPost(유럽권, 테스트유저_1, "유럽권 자유게시글", "유럽권 자유게시글 내용", PostCategory.자유);
        자유게시판_자유게시글 = createPost(자유게시판, 테스트유저_2, "자유게시판 자유게시글", "자유게시판 자유게시글 내용", PostCategory.자유);
        미주권_질문게시글 = createPost(미주권, 테스트유저_1, "미주권 질문게시글", "미주권 질문게시글 내용", PostCategory.질문);
        아시아권_질문게시글 = createPost(아시아권, 테스트유저_2, "아시아권 질문게시글", "아시아권 질문게시글 내용", PostCategory.질문);
        유럽권_질문게시글 = createPost(유럽권, 테스트유저_1, "유럽권 질문게시글", "유럽권 질문게시글 내용", PostCategory.질문);
        자유게시판_질문게시글 = createPost(자유게시판, 테스트유저_2, "자유게시판 질문게시글", "자유게시판 질문게시글 내용", PostCategory.질문);
    }

    private void saveLanguageTestRequirement(
            UniversityInfoForApply universityInfoForApply,
            LanguageTestType testType,
            String minScore
    ) {
        LanguageRequirement languageRequirement = new LanguageRequirement(
                null,
                testType,
                minScore,
                universityInfoForApply);
        universityInfoForApply.addLanguageRequirements(languageRequirement);
        universityInfoForApplyRepository.save(universityInfoForApply);
        languageRequirementRepository.save(languageRequirement);
    }

    private GpaScore createApprovedGpaScore(SiteUser siteUser) {
        GpaScore gpaScore = new GpaScore(
                new Gpa(4.0, 4.5, "/gpa-report.pdf"),
                siteUser
        );
        gpaScore.setVerifyStatus(VerifyStatus.APPROVED);
        return gpaScoreRepository.save(gpaScore);
    }

    private LanguageTestScore createApprovedLanguageTestScore(SiteUser siteUser) {
        LanguageTestScore languageTestScore = new LanguageTestScore(
                new LanguageTest(LanguageTestType.TOEIC, "100", "/gpa-report.pdf"),
                siteUser
        );
        languageTestScore.setVerifyStatus(VerifyStatus.APPROVED);
        return languageTestScoreRepository.save(languageTestScore);
    }

    private Post createPost (Board board, SiteUser siteUser, String title, String content, PostCategory category){
        Post post = new Post(
                title,
                content,
                false,
                0L,
                0L,
                category
        );
        post.setBoardAndSiteUser(board, siteUser);
        Post savedPost = postRepository.save(post);
        PostImage postImage = new PostImage("imageUrl");
        postImage.setPost(savedPost);
        postImageRepository.save(postImage);
        return savedPost;
    }
}
