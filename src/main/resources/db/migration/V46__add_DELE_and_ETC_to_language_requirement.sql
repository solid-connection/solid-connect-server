ALTER TABLE language_requirement
    MODIFY COLUMN language_test_type ENUM(
    'CEFR',
    'DALF',
    'DELF',
    'DELE',
    'DUOLINGO',
    'IELTS',
    'JLPT',
    'NEW_HSK',
    'TCF',
    'TEF',
    'TOEFL_IBT',
    'TOEFL_ITP',
    'TOEIC',
    'ETC'
    ) NOT NULL;
