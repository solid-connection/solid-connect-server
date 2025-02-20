ALTER TABLE university_info_for_apply
MODIFY COLUMN semester_available_for_dispatch
ENUM('ONE_SEMESTER',
    'TWO_SEMESTER',
    'FOUR_SEMESTER',
    'ONE_OR_TWO_SEMESTER',
    'ONE_YEAR',
    'IRRELEVANT',
    'NO_DATA') NULL;