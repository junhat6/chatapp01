-- PostgreSQL用 テーブル作成スクリプト
-- Users テーブル（認証・アカウント管理用）
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT true
);
-- User Profiles テーブル（表示・マッチング用の個人情報）
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    bio TEXT,
    age INTEGER,
    gender VARCHAR(20),
    has_usj_annual_pass BOOLEAN DEFAULT false,
    favorite_attractions TEXT [],
    -- PostgreSQLの配列型を使用
    location_prefecture VARCHAR(50),
    hobbies TEXT [],
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
-- インデックス作成
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_user_profiles_display_name ON user_profiles(display_name);
CREATE INDEX IF NOT EXISTS idx_user_profiles_age ON user_profiles(age);
CREATE INDEX IF NOT EXISTS idx_user_profiles_gender ON user_profiles(gender);
CREATE INDEX IF NOT EXISTS idx_user_profiles_has_usj_annual_pass ON user_profiles(has_usj_annual_pass);
CREATE INDEX IF NOT EXISTS idx_user_profiles_location_prefecture ON user_profiles(location_prefecture);
-- Matching Requests テーブル（募集情報）
CREATE TABLE IF NOT EXISTS matching_requests (
    id BIGSERIAL PRIMARY KEY,
    host_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attraction VARCHAR(255) NOT NULL,
    preferred_date_time TIMESTAMP NOT NULL,
    max_participants INTEGER NOT NULL DEFAULT 4,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
-- Matching Applications テーブル（応募情報）
CREATE TABLE IF NOT EXISTS matching_applications (
    id BIGSERIAL PRIMARY KEY,
    matching_request_id BIGINT NOT NULL REFERENCES matching_requests(id) ON DELETE CASCADE,
    applicant_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(matching_request_id, applicant_user_id)
);
-- Matching Rooms テーブル（待機室）
CREATE TABLE IF NOT EXISTS matching_rooms (
    id BIGSERIAL PRIMARY KEY,
    matching_request_id BIGINT NOT NULL UNIQUE REFERENCES matching_requests(id) ON DELETE CASCADE,
    participant_user_ids BIGINT [] DEFAULT '{}',
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
-- Chat Rooms テーブル（チャットルーム）
CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    matching_request_id BIGINT NOT NULL UNIQUE REFERENCES matching_requests(id) ON DELETE CASCADE,
    participant_user_ids BIGINT [] DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
-- Chat Messages テーブル（チャットメッセージ）
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    sent_at TIMESTAMP NOT NULL DEFAULT NOW()
);
-- 追加インデックス
CREATE INDEX IF NOT EXISTS idx_matching_requests_host_user_id ON matching_requests(host_user_id);
CREATE INDEX IF NOT EXISTS idx_matching_requests_status ON matching_requests(status);
CREATE INDEX IF NOT EXISTS idx_matching_requests_attraction ON matching_requests(attraction);
CREATE INDEX IF NOT EXISTS idx_matching_requests_preferred_date_time ON matching_requests(preferred_date_time);
CREATE INDEX IF NOT EXISTS idx_matching_applications_matching_request_id ON matching_applications(matching_request_id);
CREATE INDEX IF NOT EXISTS idx_matching_applications_applicant_user_id ON matching_applications(applicant_user_id);
CREATE INDEX IF NOT EXISTS idx_matching_applications_status ON matching_applications(status);
CREATE INDEX IF NOT EXISTS idx_matching_rooms_matching_request_id ON matching_rooms(matching_request_id);
CREATE INDEX IF NOT EXISTS idx_matching_rooms_status ON matching_rooms(status);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_matching_request_id ON chat_rooms(matching_request_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_chat_room_id ON chat_messages(chat_room_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_user_id ON chat_messages(sender_user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sent_at ON chat_messages(sent_at);