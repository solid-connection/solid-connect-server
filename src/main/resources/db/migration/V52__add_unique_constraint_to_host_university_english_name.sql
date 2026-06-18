ALTER TABLE host_university
    ADD CONSTRAINT uk_host_university_english_name_korean_name UNIQUE (english_name, korean_name);
