
    create table application (
        gpa float(53) not null,
        gpa_criteria float(53) not null,
        update_count int not null default 0,
        first_choice_university_id bigint,
        id bigint not null auto_increment,
        second_choice_university_id bigint,
        site_user_id bigint,
        third_choice_university_id bigint,
        language_test_type enum ('CEFR','DALF','DELF','DUOLINGO','IELTS','JLPT','NEW_HSK','TCF','TEF','TOEFL_IBT','TOEFL_ITP','TOEIC') not null,
        nickname_for_apply varchar(100),
        gpa_report_url varchar(500) not null,
        language_test_report_url varchar(500) not null,
        language_test_score varchar(255) not null,
        verify_status varchar(50) not null default 'PENDING',
        primary key (id)
    ) engine=InnoDB;

    create table board (
        code varchar(20) not null,
        korean_name varchar(20) not null,
        primary key (code)
    ) engine=InnoDB;

    create table comment (
        created_at datetime(6),
        id bigint not null auto_increment,
        parent_id bigint,
        post_id bigint,
        site_user_id bigint,
        updated_at datetime(6),
        content varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table country (
        code varchar(2) not null,
        region_code varchar(10),
        korean_name varchar(100) not null,
        primary key (code)
    ) engine=InnoDB;

    create table interested_country (
        country_code varchar(2),
        id bigint not null auto_increment,
        site_user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table interested_region (
        id bigint not null auto_increment,
        site_user_id bigint,
        region_code varchar(10),
        primary key (id)
    ) engine=InnoDB;

    create table language_requirement (
        id bigint not null auto_increment,
        university_info_for_apply_id bigint,
        language_test_type enum ('CEFR','DALF','DELF','DUOLINGO','IELTS','JLPT','NEW_HSK','TCF','TEF','TOEFL_IBT','TOEFL_ITP','TOEIC') not null,
        min_score varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table liked_university (
        id bigint not null auto_increment,
        site_user_id bigint,
        university_info_for_apply_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table post (
        is_question bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        like_count bigint,
        site_user_id bigint,
        updated_at datetime(6),
        view_count bigint,
        board_code varchar(20),
        content varchar(1000),
        category enum ('자유','전체','질문'),
        title varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table post_image (
        id bigint not null auto_increment,
        post_id bigint,
        url varchar(500),
        primary key (id)
    ) engine=InnoDB;

    create table post_like (
        id bigint not null auto_increment,
        post_id bigint,
        site_user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table region (
        code varchar(10) not null,
        korean_name varchar(100) not null,
        primary key (code)
    ) engine=InnoDB;

    create table site_user (
        quited_at date,
        id bigint not null auto_increment,
        nickname_modified_at datetime(6),
        birth varchar(20) not null,
        email varchar(100) not null,
        nickname varchar(100) not null,
        profile_image_url varchar(500),
        gender enum ('FEMALE','MALE','PREFER_NOT_TO_SAY') not null,
        preparation_stage enum ('AFTER_EXCHANGE','CONSIDERING','PREPARING_FOR_DEPARTURE','STUDYING_ABROAD') not null,
        role enum ('MENTEE','MENTOR') not null,
        primary key (id)
    ) engine=InnoDB;

    create table university (
        country_code varchar(2),
        id bigint not null auto_increment,
        region_code varchar(10),
        english_name varchar(100) not null,
        format_name varchar(100) not null,
        korean_name varchar(100) not null,
        accommodation_url varchar(500),
        background_image_url varchar(500) not null,
        english_course_url varchar(500),
        homepage_url varchar(500),
        logo_image_url varchar(500) not null,
        details_for_local varchar(1000),
        primary key (id)
    ) engine=InnoDB;

    create table university_info_for_apply (
        student_capacity integer,
        id bigint not null auto_increment,
        university_id bigint,
        term varchar(50) not null,
        gpa_requirement varchar(100),
        gpa_requirement_criteria varchar(100),
        korean_name varchar(100) not null,
        semester_requirement varchar(100),
        details varchar(500),
        details_for_accommodation varchar(1000),
        details_for_apply varchar(1000),
        details_for_english_course varchar(1000),
        details_for_language varchar(1000),
        details_for_major varchar(1000),
        semester_available_for_dispatch enum ('FOUR_SEMESTER','IRRELEVANT','NO_DATA','ONE_OR_TWO_SEMESTER','ONE_SEMESTER','ONE_YEAR'),
        tuition_fee_type enum ('HOME_UNIVERSITY_PAYMENT','MIXED_PAYMENT','OVERSEAS_UNIVERSITY_PAYMENT'),
        primary key (id)
    ) engine=InnoDB;

    alter table application 
       add constraint FKi822ljuirbu9o0lnd9jt7l7qg 
       foreign key (first_choice_university_id) 
       references university_info_for_apply (id);

    alter table application 
       add constraint FKepp2by7frnkt1o1w3v4t4lgtu 
       foreign key (second_choice_university_id) 
       references university_info_for_apply (id);

    alter table application 
       add constraint FKs4s3hebtn7vwd0b4xt8msxsis 
       foreign key (site_user_id) 
       references site_user (id);

    alter table application 
       add constraint FKeajojvwgn069mfxhbq5ja1sws 
       foreign key (third_choice_university_id) 
       references university_info_for_apply (id);

    alter table comment 
       add constraint FKde3rfu96lep00br5ov0mdieyt 
       foreign key (parent_id) 
       references comment (id);

    alter table comment 
       add constraint FKs1slvnkuemjsq2kj4h3vhx7i1 
       foreign key (post_id) 
       references post (id);

    alter table comment 
       add constraint FK11tfff2an5hdv747cktxbdi6t 
       foreign key (site_user_id) 
       references site_user (id);

    alter table country 
       add constraint FKife035f2scmgcutdtv6bfd6g8 
       foreign key (region_code) 
       references region (code);

    alter table interested_country 
       add constraint FK7x4ad24lblkq2ss0920uqfd6s 
       foreign key (country_code) 
       references country (code);

    alter table interested_country 
       add constraint FK26u5am55jefclcd7r5smk8ai7 
       foreign key (site_user_id) 
       references site_user (id);

    alter table interested_region 
       add constraint FK7h2182pqkavi9d8o2pku6gidi 
       foreign key (region_code) 
       references region (code);

    alter table interested_region 
       add constraint FKia6h0pbisqhgm3lkeya6vqo4w 
       foreign key (site_user_id) 
       references site_user (id);

    alter table language_requirement 
       add constraint FKr75pgslwfbrvjkfau6dwtlg8l 
       foreign key (university_info_for_apply_id) 
       references university_info_for_apply (id);

    alter table liked_university 
       add constraint FKkuqxb64dnfrl7har8t5ionw83 
       foreign key (site_user_id) 
       references site_user (id);

    alter table liked_university 
       add constraint FKo317gq6apc3a091w32qhidtjt 
       foreign key (university_info_for_apply_id) 
       references university_info_for_apply (id);

    alter table post 
       add constraint FKlpnkhhbfb3gg3tfreh2a7qh8b 
       foreign key (board_code) 
       references board (code);

    alter table post 
       add constraint FKfu9q9o3mlqkd58wg45ykgu8ni 
       foreign key (site_user_id) 
       references site_user (id);

    alter table post_image 
       add constraint FKsip7qv57jw2fw50g97t16nrjr 
       foreign key (post_id) 
       references post (id);

    alter table post_like 
       add constraint FKj7iy0k7n3d0vkh8o7ibjna884 
       foreign key (post_id) 
       references post (id);

    alter table post_like 
       add constraint FKgx1v0whinnoqveopoh6tb4ykb 
       foreign key (site_user_id) 
       references site_user (id);

    alter table university 
       add constraint FKksoyt17h0te1ra588y4a3208r 
       foreign key (country_code) 
       references country (code);

    alter table university 
       add constraint FKpwr8ocev54r8d22wdyj4a37bc 
       foreign key (region_code) 
       references region (code);

    alter table university_info_for_apply 
       add constraint FKd0257hco6uy2utd1xccjh3fal 
       foreign key (university_id) 
       references university (id);
