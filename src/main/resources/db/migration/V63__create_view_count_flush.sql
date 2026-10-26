CREATE TABLE view_count_flush (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flush_id VARCHAR(100) NOT NULL,
    post_id BIGINT NOT NULL,
    delta BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_view_count_flush_id UNIQUE (flush_id)
);
