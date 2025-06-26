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