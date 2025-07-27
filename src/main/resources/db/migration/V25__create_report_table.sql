CREATE TABLE report
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    target_type ENUM ('POST') NOT NULL,
    target_id   BIGINT NOT NULL,
    report_type ENUM ('ADVERTISEMENT', 'SPAM', 'PERSONAL_INFO_EXPOSURE', 'PORNOGRAPHY', 'COPYRIGHT_INFRINGEMENT', 'ILLEGAL_ACTIVITY', 'IMPERSONATION', 'INSULT') NOT NULL,
    primary key (id),
    constraint fk_report_reporter_id foreign key (reporter_id) references site_user (id),
    unique uk_report_reporter_id_target_type_target_id (reporter_id, target_type, target_id)
);
