INSERT INTO region (code, korean_name)
VALUES ('ASIA', '아시아권'),
       ('AMERICAS', '미주권'),
       ('CHINA', '중국권'),
       ('EUROPE', '유럽권');

INSERT INTO country (code, korean_name, region_code)
VALUES ('BN', '브루나이', 'ASIA'),
       ('SG', '싱가포르', 'ASIA'),
       ('AZ', '아제르바이잔', 'ASIA'),
       ('ID', '인도네시아', 'ASIA'),
       ('JP', '일본', 'ASIA'),
       ('TR', '튀르키예', 'ASIA'),
       ('HK', '홍콩', 'ASIA'),
       ('US', '미국', 'AMERICAS'),
       ('CA', '캐나다', 'AMERICAS'),
       ('AU', '호주', 'ASIA'),
       ('BR', '브라질', 'AMERICAS'),
       ('NL', '네덜란드', 'EUROPE'),
       ('NO', '노르웨이', 'EUROPE'),
       ('DK', '덴마크', 'EUROPE'),
       ('DE', '독일', 'EUROPE'),
       ('SE', '스웨덴', 'EUROPE'),
       ('CH', '스위스', 'EUROPE'),
       ('ES', '스페인', 'EUROPE'),
       ('GB', '영국', 'EUROPE'),
       ('AT', '오스트리아', 'EUROPE'),
       ('IT', '이탈리아', 'EUROPE'),
       ('CZ', '체코', 'EUROPE'),
       ('PT', '포르투갈', 'EUROPE'),
       ('FR', '프랑스', 'EUROPE'),
       ('FI', '핀란드', 'EUROPE'),
       ('CN', '중국', 'CHINA'),
       ('TW', '대만', 'CHINA'),
       ('HU', '헝가리', 'EUROPE'),
       ('LT', '리투아니아', 'EUROPE'),
       ('TH', '태국', 'ASIA'),
       ('UZ', '우즈베키스탄', 'ASIA'),
       ('KZ', '카자흐스탄', 'ASIA'),
       ('IL', '이스라엘', 'ASIA'),
       ('MY', '말레이시아', 'ASIA'),
       ('RU', '러시아', 'EUROPE');

INSERT INTO site_user (email, nickname, profile_image_url, preparation_stage, role, password, auth_type)
VALUES ('test@test.email', 'yonso', 'https://github.com/nayonsoso.png',
        'CONSIDERING', 'MENTEE',
        '$2a$10$psmwlxPfqWnIlq9JrlQJkuXr1XtjRNsyVOgcTWYZub5jFfn0TML76', 'EMAIL'); -- 12341234

INSERT INTO university(id, country_code, region_code, english_name, format_name, korean_name,
                       accommodation_url, english_course_url, homepage_url,
                       details_for_local, logo_image_url, background_image_url)
VALUES (1, 'US', 'AMERICAS', 'University of Guam', 'university_of_guam', '괌대학',
        'https://www.uog.edu/life-at-uog/residence-halls/', 'https://www.uog.edu/admissions/course-schedule',
        'https://www.uog.edu/admissions/international-students', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_guam/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_guam/1.png'),
       (2, 'US', 'AMERICAS', 'University of Nevada, Las Vegas', 'university_of_nevada_las_vegas', '네바다주립대학 라스베이거스',
        'https://www.unlv.edu/housing', 'https://www.unlv.edu/engineering/academic-programs',
        'https://www.unlv.edu/engineering/eip', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_nevada_las_vegas/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_nevada_las_vegas/1.png'),
       (3, 'CA', 'AMERICAS', 'Memorial University of Newfoundland St. John''s',
        'memorial_university_of_newfoundland_st_johns', '메모리얼 대학 세인트존스', 'https://www.mun.ca/residences/',
        'https://www.mun.ca/regoff/registration-and-final-exams/course-offerings/',
        'https://mun.ca/goabroad/visiting-students-inbound/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/memorial_university_of_newfoundland_st_johns/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/memorial_university_of_newfoundland_st_johns/1.png'),
       (4, 'AU', 'AMERICAS', 'University of Southern Queensland', 'university_of_southern_queensland', '서던퀸스랜드대학',
        'https://www.unisq.edu.au/current-students/support/accommodation',
        'https://www.unisq.edu.au/course/specification/current/',
        'https://www.unisq.edu.au/international/partnerships/study-abroad-exchange', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_southern_queensland/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_southern_queensland/1.png'),
       (5, 'AU', 'AMERICAS', 'University of Sydney', 'university_of_sydney', '시드니대학',
        'https://www.sydney.edu.au/study/accommodation.html', 'www.sydney.edu.au/sydney-abroad-units',
        'https://www.sydney.edu.au/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_sydney/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_sydney/1.png'),
       (6, 'AU', 'AMERICAS', 'Curtin University', 'curtin_university', '커틴대학',
        'https://www.curtin.edu.au/study/campus-life/accommodation/#perth', 'https://handbook.curtin.edu.au/',
        'https://www.curtin.edu.au/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/curtin_university/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/curtin_university/1.png'),
       (7, 'DK', 'EUROPE', 'University of Southern Denmark', 'university_of_southern_denmark', '서던덴마크대학교',
        'https://www.sdu.dk/en/uddannelse/information_for_international_students/studenthousing',
        'https://www.sdu.dk/en/uddannelse/exchange_programmes', 'https://www.sdu.dk/en', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_southern_denmark/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_southern_denmark/1.png'),
       (8, 'DK', 'EUROPE', 'IT University of Copenhagen', 'it_university_of_copenhagen', '코펜하겐 IT대학',
        'https://en.itu.dk/Programmes/Student-Life/Practical-information-for-international-students',
        'https://en.itu.dk/Programmes/Exchange-students/Become-an-exchange-student-at-ITU',
        'https://en.itu.dk/programmes/exchange-students/become-an-exchange-student-at-itu', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/it_university_of_copenhagen/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/it_university_of_copenhagen/1.png'),
       (9, 'DE', 'EUROPE', 'Neu-Ulm University of Applied Sciences', 'neu-ulm_university_of_applied_sciences',
        '노이울름 대학',
        'https://www.hnu.de/fileadmin/user_upload/5_Internationales/International_Incomings/Bewerbung/Housing_Broschure.pdf',
        'https://www.hnu.de/en/international/international-exchange-students/courses-taught-in-english',
        'https://www.hnu.de/en/international', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/neu-ulm_university_of_applied_sciences/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/neu-ulm_university_of_applied_sciences/1.png'),
       (10, 'GB', 'EUROPE', 'University of Hull', 'university_of_hull', '헐대학',
        'https://www.hull.ac.uk/Choose-Hull/Student-life/Accommodation/accommodation.aspx',
        'https://universityofhull.app.box.com/s/mpvulz3yz0uijdt68rybce19nek0d8eh',
        'https://www.hull.ac.uk/choose-hull/study-at-hull/need-to-know/key-dates', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_hull/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_hull/1.png'),
       (11, 'AT', 'EUROPE', 'University of Graz', 'university_of_graz', '그라츠 대학',
        'https://orientation.uni-graz.at/de/planning-the-arrival/accommodation/',
        'https://static.uni-graz.at/fileadmin/veranstaltungen/orientation/documents/incstud_application-courses.pdf',
        'https://www.uni-graz.at/en/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_graz/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_graz/1.png'),
       (12, 'AT', 'EUROPE', 'Graz University of Technology', 'graz_university_of_technology', '그라츠공과대학',
        'https://www.tugraz.at/en/studying-and-teaching/studying-internationally/incoming-students-exchange-at-tu-graz/your-stay-at-tu-graz/preparation#c75033',
        'https://tugraz.at/go/search-courses', 'https://www.tugraz.at/en/home', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/graz_university_of_technology/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/graz_university_of_technology/1.png'),
       (13, 'AT', 'EUROPE', 'Catholic Private University Linz', 'catholic_private_university_linz', '린츠 카톨릭 대학교', NULL,
        'https://ku-linz.at/en/ku_international/incomings/kulis', 'https://ku-linz.at/en', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/catholic_private_university_linz/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/catholic_private_university_linz/1.png'),
       (14, 'AT', 'EUROPE', 'University of Applied Sciences Technikum Wien',
        'university_of_applied_sciences_technikum_wien', '빈 공과대학교', NULL,
        'https://www.technikum-wien.at/en/international/student-mobility/',
        'https://www.technikum-wien.at/international/studierendenmobilitaet-2/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_applied_sciences_technikum_wien/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/university_of_applied_sciences_technikum_wien/1.png'),
       (15, 'FR', 'EUROPE', 'IPSA', 'ipsa', 'IPSA', 'https://www.ipsa.fr/en/student-life/pratical-information/', NULL,
        'https://www.ipsa.fr/en/engineering-school/aeronautical-space', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/ipsa/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/ipsa/1.png'),
       (16, 'JP', 'ASIA', 'Meiji University', 'meiji_university', '메이지대학',
        'https://www.meiji.ac.jp/cip/english/admissions/co7mm90000000461-att/co7mm900000004fa.pdf', NULL,
        'https://www.meiji.ac.jp/cip/english/admissions/co7mm90000000461-att/co7mm900000004fa.pdf', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/meiji_university/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/meiji_university/1.png'),
       (17, 'JP', 'ASIA', 'BAIKA Women''s University', 'baika_womens_university', '바이카여자대학',
        'https://dormy-ac.com/page/baika/', NULL, 'https://www.baika.ac.jp/english/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/baika_womens_university/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/baika_womens_university/1.png'),
       (18, 'JP', 'ASIA', 'Bunkyo Gakuin University', 'bunkyo_gakuin_university', '분쿄가쿠인대학', NULL, NULL,
        'https://www.bgu.ac.jp/', NULL,
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/bunkyo_gakuin_university/logo.png',
        'https://solid-connection.s3.ap-northeast-2.amazonaws.com/original/bunkyo_gakuin_university/1.png');

INSERT INTO university_info_for_apply(term, university_id, korean_name, semester_requirement, student_capacity,
                                      semester_available_for_dispatch, tuition_fee_type, details_for_major,
                                      details_for_apply, details_for_language, details_for_english_course,
                                      details_for_accommodation, details)
VALUES ('2024-1', 1, '괌대학(A형)', 2, 1, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT', '파견대학에 지원하는 전공과 본교 전공이 일치해야함', NULL,
        '외국어 성적 유효기간이 파견대학의 지원시까지 유효해야함', NULL, NULL, NULL),
       ('2024-1', 1, '괌대학(B형)', 2, 2, 'IRRELEVANT', 'OVERSEAS_UNIVERSITY_PAYMENT', '파견대학에 지원하는 전공과 본교 전공이 일치해야함', NULL,
        '외국어 성적 유효기간이 파견대학의 지원시까지 유효해야함', NULL, NULL, '등록금 관련 정보: https://www.uog.edu/financial-aid/cost-to-attend'),
       ('2024-1', 2, '네바다주립대학 라스베이거스(B형)', 2, 5, 'IRRELEVANT', 'OVERSEAS_UNIVERSITY_PAYMENT',
        '- 지원가능전공: 공학계열 관련 전공자<br>- 파견대학에 지원하는 전공과 본교 전공이 일치해야함',
        NULL, '영어 점수는 다음의 세부영역 점수를 각각 만족해야 함<br>    - IELTS : 모든 영역에서 5.5 이상', NULL, NULL,
        ' - The Engineering International Programs (EIP) Programs 안의 글로벌 하이브리드 프로그램으로 선발됨 <br>※ 하이브리드 프로그램: 정규 과목 + 비정규 General Education Courses 과목 수강으로 구성, 정규(약 6학점) / 비정규 (약 135시간 이상) 수업 수강 (세부사항 변동 가능)<br>- 기숙사가 있지만 기숙사 확정이 늦게 발표되고 전원보장이 어려워, 외부숙소로 진행될 수도 있음, 한 학기 기숙사 비용: 약 $4,500~$6,000<br>- 한 학기 등록금: 약 $7,500<br>- International Program and Service Fees $2,500'),
       ('2024-1', 3, '메모리얼 대학 세인트존스(A형)', 2, 4, 'ONE_SEMESTER', 'HOME_UNIVERSITY_PAYMENT',
        '타전공 지원 및 수강 가능 <br>- 지원불가능전공: Medicine, Pharmacy, Social work, Nursing<br>- Computer Science, Music 지원 제한적',
        NULL,
        '영어 점수는 다음의 세부영역 점수를 각각 만족해야함<br>  - TOEFL iBT : 읽기/쓰기 20점, 듣기/말하기 17점 이상<br>  - IELTS : 모든 영역에서 6.0 이상<br> - 외국어 성적 유효기간이 파견대학의 학기 시작하는 날까지 유효해야함 ',
        NULL, NULL, NULL),
       ('2024-1', 3, '메모리얼 대학 세인트존스(B형)', 2, 5, 'IRRELEVANT', 'OVERSEAS_UNIVERSITY_PAYMENT',
        '타전공 지원 및 수강 가능 <br>- 지원불가능전공: Medicine, Pharmacy, Social work, Nursing<br>- Computer Science, Music 지원 제한적',
        NULL,
        '영어 점수는 다음의 세부영역 점수를 각각 만족해야함<br>  - TOEFL iBT : 읽기/쓰기 20점, 듣기/말하기 17점 이상<br>  - IELTS : 모든 영역에서 6.0 이상<br> - 외국어 성적 유효기간이 파견대학의 학기 시작하는 날까지 유효해야함 ',
        NULL, NULL, '국제학생 등록금 적용 (학점당 $2,080)'),
       ('2024-1', 4, '서던퀸스랜드대학(B형)', 2, 5, 'ONE_SEMESTER', 'OVERSEAS_UNIVERSITY_PAYMENT',
        '- 타전공 지원 및 수강 가능  <br>- 미술 계열, 간호학, 약학, 교육학 등 제한 있음<br>- 학과별 지원 자격요건이 있는 경우 모두 충족해야 하며, 사전 승인 필요', NULL,
        '영어 점수는 다음의 세부영역 점수를 각각 만족해야 함<br>  - IELTS: 각 영역 최소 5.5 이상<br>- 외국어 성적 유효기간이 파견대학의 지원시까지 유효해야함 ', NULL, NULL,
        '서던퀸스랜드대학은 Trimester로 운영되므로 학사일정을 반드시 참고하길 바람<br>- In-state 등록금 납부 <br>(등록금 관련 정보 : https://www.unisq.edu.au/international/partnerships/study-abroad-exchange/fees-scholarships)'),
       ('2024-1', 5, '시드니대학', 2, 5, 'IRRELEVANT', 'OVERSEAS_UNIVERSITY_PAYMENT',
        '타전공 지원 및 수강 가능<br>- MECO, CAEL, LAWS unit 수강 여석 제한 있음',
        NULL,
        '영어 점수는 다음의 세부영역 점수를 각각 만족해야함<br>   - IELTS: 모든 영역에서 6.0 이상<br>   - TOEFL IBT: 읽기/듣기/말하기 17점, 쓰기 19점 이상<br>- 어학성적은 파견학기 시작시까지 유효하여야함',
        NULL, NULL, 'OSHC(Overseas Student Health Cover) 국제학생 보험가입 의무 (2023년 기준 AUD 348/학기, 학기마다 비용 상이)'),
       ('2024-1', 6, '커틴대학(A형)', 2, 3, 'ONE_SEMESTER', 'HOME_UNIVERSITY_PAYMENT',
        '타전공 지원 및 수강 가능<br>지원 불가능 전공: Physiotherapy, Medicine, Nursing, Occupational Therapy ', NULL,
        '영어 점수는 다음의 세부영역 점수를 각각 만족해야함<br>   - IELTS: 모든 영역에서 6.0 이상<br>   - TOEFL IBT: 읽기 13점, 쓰기 21점, 듣기 13점, 말하기 18점 이상<br>- 어학성적은 파견학기 시작시까지 유효하여야함',
        NULL, NULL,
        '※ 24-1학기에 한하여 ''Destination Australia Cheung Kong Exchange Program Scholarship'' 지급 예정 (신청자 중 가장 총점이 우수한 학생 1명에게 AUD$6000 지급, 상세 내용은 국제처 홈페이지 해외대학정보 공지글 참고)'),
       ('2024-1', 7, '서던덴마크대학교', 4, 2, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT',
        '- 주전공과 지원전공이 반드시 일치할 필요는 없으나 본교에서 기초과목을 이수하여야 함<br>- 교환학생에게 제공되는 수업만 수강 가능<br>- Faculty of Engineering 내에서 2/3이상의 수업을 수강하여야 함<br>- 30 ECTS 수강',
        '- 어학성적표가 해당 대학 신청서 제출 시 유효하여야 함(~10월 1일)', NULL, NULL, '- 교외 숙소', NULL),
       ('2024-1', 8, '코펜하겐 IT대학', 2, 2, 'ONE_SEMESTER', 'HOME_UNIVERSITY_PAYMENT',
        '- 본교 기초과목 이수사항에 따라 지원이 제한될 수 있으나 소속전공과 정확하게 일치 하지 않아도 지원은 가능(연관 전공이어야 함)<br>- 최소 7.5 ECTS, 최대 30ECTS 수강 가능<br>- 교차 수강 가능(선수과목이 지정되어있는 과목은 사전에 이수하여야 수강이 가능함)',
        '- 어학성적표가 해당 대학 신청서 제출 시 유효하여야 함(~11월 1일)', NULL, NULL, '- 제공(학교 운영 기숙사 아님) <br>- 선착순 배정', NULL),
       ('2024-1', 9, '노이울름 대학', 2, 3, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT', '타전공 지원 및 수강 가능', NULL,
        '영어 점수는 다음의 세부영역 점수를 각각 만족해야 함<br>   - TOEFL IBT: 읽기 18점; 듣기 17점, 말하기 20점, 쓰기 17점<br>   - TOEIC: 읽기 385점, 듣기 400점, 말하기 160점, 쓰기 150점<br>외국어 성적 유효기간이 파견대학의 학기 시작하는 시점까지 유효해야 함',
        NULL, NULL, NULL),
       ('2024-1', 10, '헐대학', 4, 3, 'ONE_SEMESTER', 'HOME_UNIVERSITY_PAYMENT',
        '제한학과 많음. (Factsheet참조및Factsheet언급된 제한학과  외에도 학기마다 제한학과 발생가능성있음). 지원 전 권역 담당자랑 사전상담 요망. 학기당 30ECTS수강해야 LA승인남. 성적처리 늦은 편이라 8차 학기 수학자는 성적처리 늦은 거 감안하고 추가 이에 따른 불편함이 있음을 인지후 지원요망. ',
        '지원 전 권역 담당자와 사전상담 요망',
        '- 영어 점수는 다음의 세부영역 점수를 각각 만족해야 함<br>  - TOEFL iBT : 듣기 및 쓰기 18점, 읽기 18점, 말하기 20점, 쓰기 18점 이상<br>  - IELTS : 모든 영역에서 6.0이상',
        NULL, NULL, '영국 생활비 및 숙소비용 유럽권 지역 중 상대적으로 매우 높은편. 지원전 반드시 사전고려 요망'),
       ('2024-1', 11, '그라츠 대학', 3, 2, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT', '-주전공 혹은 제2전공(혹은 연계전공과) 유관학과여아 함',
        '선발인원 중 차순위 합격자는 학기제한(1개 학기)이 있을 수 있음', NULL, NULL, '학교인근 외부 숙소는 있지만, 외부업체운영숙소라 대학관할아님', NULL),
       ('2024-1', 12, '그라츠공과대학', 2, 2, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT', '-주전공 혹은 제2전공(혹은 연계전공과) 유관학과여아 함',
        '선발인원 중 차순위 합격자는 학기제한(1개 학기)이 있을 수 있음',
        '- 영어 점수는 다음의 세부영역 점수를 각각 만족해야 함<br>  - TOEFL IBT: 읽기 18점 이상, 쓰기 17점 이상, 말하기 20점 이상, 듣기 17점 이상<br>  - IELTS: 쓰기 5.5점 이상, 말하기 6점 이상<br> ''- TOEIC의 경우 S/W 점수 합산 310점 이상 ',
        NULL,
        '자체기숙사는 없음. 교환학생이 많이 지원한 학기에는 예약이 어려울 수도 있음(선착순 경우많음). 더블룸 기준약 한달에 € 340 per month (기숙사 종류게 따라 가격 차이 유) 예산잡으면됨.',
        NULL),
       ('2024-1', 13, '린츠 카톨릭 대학교', 3, 2, 'ONE_SEMESTER', 'HOME_UNIVERSITY_PAYMENT',
        '- 지원가능전공: History, Philosophy, Art History, theology<br>(영어과목 수가 그리 많지는 않으므로, 사전 확인필요)<br>''- 학기당 최소 15ECTS 수강신청해야 함',
        '봄학기에는 영어과목이 극히 제한적으로 열린다고 함. 지원 전 권역 담당자와 사전상담 요망', NULL, NULL, '학교에서 몇가지 기숙사 옵션 합격시 연결예정.', NULL),
       ('2024-1', 14, '빈 공과대학교', 3, 2, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT',
        '지원전공과 일치하지 않아도 지원가능하나 유사전공자만 지원가능하며, 본전공과 일치하지않으면 입학 및 수강에 불리할 수 있음<br>''-학기당 최소 15.ECTS 수강신청해야함',
        '선발인원 중 차순위 합격자는 학기제한(1개 학기)이 있을 수 있음', NULL, NULL, '기숙사없음', NULL),
       ('2024-1', 15, 'IPSA', 4, 3, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT',
        '- 소속전공과 지원전공이 일치 또는 유사하여야 함 : 전공이 제한적이므로 반드시 홈페이지에서 지원 가능 전공을 확인할 것<br>- 최대 30ECTS 수강',
        '- 어학성적표가 해당 대학 신청서 제출 시 유효하여야 함(~11월 15일)', NULL, NULL, '- 미제공', NULL),
       ('2024-1', 16, '메이지대학', 2, 3, 'IRRELEVANT', 'HOME_UNIVERSITY_PAYMENT',
        'https://www.meiji.ac.jp/cip/english/admissions/co7mm90000000461-att/co7mm900000004d1.pdf',
        '*해당 학교 일정 상 10월초까지 서류제출 필요', '학부별로 기준 상이, 관련페이지 참조', NULL, NULL, NULL),
       ('2024-1', 17, '바이카여자대학', 2, 1, 'IRRELEVANT', NULL,
        '교환학생 지원가능 : Department of Global English, Department of Japanese culture, Department of Media and Information, Department of Psychology.',
        '여학생만 신청가능', NULL, NULL,
        '기숙사 없음, 계약된 외부 기숙사 사용-“Maison de Claire Ibaraki” 62,300엔/월, 2식 포함, 계약시 66,000엔 청구 (2023년 6월기준)', NULL),
       ('2024-1', 18, '분쿄가쿠인대학', 2, 3, 'ONE_YEAR', 'HOME_UNIVERSITY_PAYMENT', NULL, NULL, NULL, NULL,
        '기숙사 보유, off campus, 식사 미제공, 45,000~50,000엔/월', NULL);

INSERT INTO language_requirement(language_test_type, min_score, university_info_for_apply_id)
VALUES ('TOEFL_IBT', '80', 1),
       ('IELTS', '6.5', 1),
       ('TOEFL_IBT', '80', 2),
       ('IELTS', '6.5', 2),
       ('TOEFL_IBT', '79', 3),
       ('IELTS', '6.0', 3),
       ('TOEFL_IBT', '88', 4),
       ('IELTS', '6.5', 4),
       ('TOEFL_IBT', '88', 5),
       ('IELTS', '6.5', 5),
       ('TOEFL_IBT', '85', 6),
       ('IELTS', '6.5', 6),
       ('TOEFL_IBT', '85', 7),
       ('IELTS', '6.5', 7),
       ('TOEFL_IBT', '80', 8),
       ('IELTS', '6.0', 8),
       ('TOEFL_IBT', '83', 9),
       ('IELTS', '6.5', 9),
       ('TOEFL_IBT', '87', 10),
       ('IELTS', '6.5', 10),
       ('TOEFL_IBT', '90', 11),
       ('IELTS', '6.5', 11),
       ('TOEFL_IBT', '85', 12),
       ('IELTS', '6.5', 12),
       ('TOEFL_IBT', '82', 13),
       ('IELTS', '6.0', 13),
       ('TOEFL_IBT', '85', 14),
       ('IELTS', '6.5', 14),
       ('TOEFL_IBT', '90', 15),
       ('IELTS', '7.0', 15),
       ('TOEFL_IBT', '85', 16),
       ('IELTS', '6.5', 16),
       ('DELF', 'B2', 17),
       ('DALF', 'C1', 17),
       ('JLPT', 'N2', 18),
       ('JLPT', 'N1', 19),
       ('TOEFL_IBT', '85', 20),
       ('IELTS', '6.5', 20);

INSERT INTO board (code, korean_name)
VALUES ('EUROPE', '유럽권'),
       ('AMERICAS', '미주권'),
       ('ASIA', '아시아권'),
       ('FREE', '자유게시판');
