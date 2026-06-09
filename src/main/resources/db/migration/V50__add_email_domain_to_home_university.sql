ALTER TABLE home_university
    ADD COLUMN email_domain VARCHAR(100) NULL UNIQUE;

UPDATE home_university SET email_domain = 'inha.edu' WHERE name = '인하대학교';
UPDATE home_university SET email_domain = 'khu.ac.kr' WHERE name = '경희대학교';
UPDATE home_university SET email_domain = 'cau.ac.kr' WHERE name = '중앙대학교';
UPDATE home_university SET email_domain = 'sungshin.ac.kr' WHERE name = '성신여자대학교';
UPDATE home_university SET email_domain = 'inu.ac.kr' WHERE name = '인천대학교';
