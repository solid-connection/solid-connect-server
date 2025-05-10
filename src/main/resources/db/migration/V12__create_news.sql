CREATE TABLE news (
                      created_at datetime(6),
                      id bigint not null auto_increment,
                      updated_at datetime(6),
                      thumbnail_url varchar(500),
                      url varchar(500),
                      description varchar(255),
                      title varchar(255),
                      primary key (id)
)