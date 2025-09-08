CREATE TABLE reviews (
        id_review SERIAL PRIMARY KEY,
        id_student INTEGER NOT NULL,
        id_mentor INTEGER NOT NULL,
        review_comment TEXT NOT NULL,
        review_rating INTEGER NOT NULL CHECK (review_rating BETWEEN 1 AND 5),
        review_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
        FOREIGN KEY (id_mentor) REFERENCES mentors(id_mentor) ON DELETE CASCADE,
        UNIQUE (id_student, id_mentor)
);

-- Индексы для оптимизации
CREATE INDEX idx_reviews_student_id ON reviews(id_student);
CREATE INDEX idx_reviews_mentor_id ON reviews(id_mentor);