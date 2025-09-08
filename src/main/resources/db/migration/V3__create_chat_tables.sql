CREATE TABLE chats (
            id_chat SERIAL PRIMARY KEY,
            id_student INTEGER NOT NULL,
            id_mentor INTEGER NOT NULL,
            chat_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            chat_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (id_student) REFERENCES students(id_student) ON DELETE CASCADE,
            FOREIGN KEY (id_mentor) REFERENCES mentors(id_mentor) ON DELETE CASCADE,
            UNIQUE (id_student, id_mentor)
);

CREATE TABLE chat_messages (
            id_chat_message SERIAL PRIMARY KEY,
            id_chat INTEGER NOT NULL,
            id_sender INTEGER NOT NULL,
            message_content TEXT NOT NULL,
            message_sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            is_read BOOLEAN DEFAULT FALSE,
            FOREIGN KEY (id_chat) REFERENCES chats(id_chat) ON DELETE CASCADE,
            FOREIGN KEY (id_sender) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Индексы для оптимизации
CREATE INDEX idx_chats_student_id ON chats(id_student);
CREATE INDEX idx_chats_mentor_id ON chats(id_mentor);
CREATE INDEX idx_chat_messages_chat_id ON chat_messages(id_chat);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(id_sender);
CREATE INDEX idx_chat_messages_sent_at ON chat_messages(message_sent_at);