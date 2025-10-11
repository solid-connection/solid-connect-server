create table mentor_application
(
    created_at                datetime(6),
    id                        bigint       not null auto_increment,
    site_user_id              bigint,
    university_id             bigint,
    updated_at                datetime(6),
    mentor_proof_url          varchar(500) not null,
    country_code              varchar(255),
    exchange_phase            enum ('AFTER_EXCHANGE','STUDYING_ABROAD') not null,
    mentor_application_status enum ('APPROVED','PENDING','REJECTED') not null,
    region_code               varchar(255),
    rejected_reason           varchar(255),
    primary key (id)
) engine=InnoDB