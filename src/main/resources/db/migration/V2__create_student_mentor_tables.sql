-- Таблица студентов
CREATE TABLE students (
            id_student SERIAL PRIMARY KEY,
            id_user INTEGER NOT NULL UNIQUE,
            education_level VARCHAR(100),
            learning_goals TEXT,
            FOREIGN KEY (id_user)
                REFERENCES users(id_user) ON DELETE CASCADE
);

-- Таблица менторов
CREATE TABLE mentors (
            id_mentor SERIAL PRIMARY KEY,
            id_user INTEGER NOT NULL UNIQUE,
            hourly_rate DECIMAL(10, 2),
            specialization VARCHAR(255),
            experience_years INTEGER,
            average_rating DECIMAL(3, 2) DEFAULT 0.0,
            mentor_target_students TEXT,
            FOREIGN KEY (id_user)
                REFERENCES users(id_user) ON DELETE CASCADE
);

-- Индексы для оптимизации
CREATE INDEX idx_students_user_id ON students(id_user);
CREATE INDEX idx_mentors_user_id ON mentors(id_user);