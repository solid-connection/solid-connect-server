ALTER TABLE language_test_score
    modify language_test_type enum ('CEFR', 'DALF', 'DELF', 'DUOLINGO', 'IELTS', 'JLPT', 'NEW_HSK', 'TCF', 'TEF', 'TOEFL_IBT', 'TOEFL_ITP', 'TOEIC', 'ETC') NOT NULL;