-- Таблица ролей для хранения типов пользователей
CREATE TABLE roles (
        id_role SERIAL PRIMARY KEY,
        role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица пользователей для хранения основной информации
CREATE TABLE users (
        id_user SERIAL PRIMARY KEY,
        id_role INTEGER NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        user_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        user_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        is_email_verified BOOLEAN DEFAULT FALSE,
        is_online BOOLEAN DEFAULT FALSE,
        FOREIGN KEY (id_role)
            REFERENCES roles(id_role) ON DELETE RESTRICT
);

-- Таблица для хранения токенов верификации email
CREATE TABLE email_verifications (
        id_email_verification SERIAL PRIMARY KEY,
        id_user INTEGER NOT NULL UNIQUE,
        email_verification_token VARCHAR(255) NOT NULL,
        email_verification_token_expires_at TIMESTAMP NOT NULL,
        FOREIGN KEY (id_user)
            REFERENCES users(id_user) ON DELETE CASCADE
);

-- Таблица профилей для хранения данных профиля
CREATE TABLE profiles (
        id_profile SERIAL PRIMARY KEY,
        id_user INTEGER NOT NULL UNIQUE,
        first_name VARCHAR(100) NOT NULL,
        last_name VARCHAR(100) NOT NULL,
        bio TEXT NOT NULL,
        age INTEGER NOT NULL,
        profile_picture_url VARCHAR(255) NOT NULL,
        FOREIGN KEY (id_user)
            REFERENCES users(id_user) ON DELETE CASCADE
);

-- Начальные данные для ролей
INSERT INTO roles (role_name) VALUES ('STUDENT'), ('MENTOR');

-- Индексы для оптимизации
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_online ON users(is_online);
CREATE INDEX idx_email_verifications_user_id ON email_verifications(id_user);
CREATE INDEX idx_email_verifications_token ON email_verifications(email_verification_token);
CREATE INDEX idx_profiles_user_id ON profiles(id_user);