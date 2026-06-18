ALTER TABLE host_university
    ADD CONSTRAINT uk_host_university_english_name UNIQUE (english_name);
