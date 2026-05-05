create table gpa_score (
                           gpa float(53) not null,
                           gpa_criteria float(53) not null,
                           issue_date date,
                           created_at datetime(6),
                           id bigint not null auto_increment,
                           site_user_id bigint,
                           updated_at datetime(6),
                           gpa_report_url varchar(500) not null,
                           rejected_reason varchar(255),
                           verify_status varchar(50) not null default 'PENDING',
                           primary key (id)
) engine=InnoDB;

alter table gpa_score
    add constraint FK2k65qncfxvol5j4l4hb7d6iv1
        foreign key (site_user_id)
            references site_user (id);

create table language_test_score (
                                     issue_date date,
                                     created_at datetime(6),
                                     id bigint not null auto_increment,
                                     site_user_id bigint,
                                     updated_at datetime(6),
                                     language_test_type enum ('CEFR', 'DALF', 'DELF', 'DUOLINGO', 'IELTS', 'JLPT', 'NEW_HSK', 'TCF', 'TEF', 'TOEFL_IBT', 'TOEFL_ITP', 'TOEIC') not null,
                                     language_test_report_url varchar(500) not null,
                                     language_test_score varchar(255) not null,
                                     rejected_reason varchar(255),
                                     verify_status varchar(50) not null default 'PENDING',
                                     primary key (id)
) engine=InnoDB;

alter table language_test_score
    add constraint FKt2uevj2r4iuxumblj5ofbgmqn
        foreign key (site_user_id)
            references site_user (id);

alter table application add column is_delete bit;

alter table application drop foreign key fk_university_info_for_apply_id_1;
alter table application drop foreign key fk_university_info_for_apply_id_2;

alter table application
    add constraint FKi822ljuirbu9o0lnd9jt7l7qg
        foreign key (first_choice_university_id)
            references university_info_for_apply (id);

alter table application
    add constraint FKepp2by7frnkt1o1w3v4t4lgtu
        foreign key (second_choice_university_id)
            references university_info_for_apply (id);

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

alter table liked_university drop foreign key FKhj3gn3mqmfeiiw9jt83g7t3rk;
alter table liked_university drop foreign key FKrrhud921brslcukx6fyuh0th3;

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

-- alter table post_image
--     add constraint FKsip7qv57jw2fw50g97t16nrjr
--         foreign key (post_id)
--             references post (id);

alter table post_like
    add constraint FKj7iy0k7n3d0vkh8o7ibjna884
        foreign key (post_id)
            references post (id);
