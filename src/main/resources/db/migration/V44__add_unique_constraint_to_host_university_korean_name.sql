ALTER TABLE host_university
    ADD CONSTRAINT uk_host_university_korean_name UNIQUE (korean_name);
