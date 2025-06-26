-- テストユーザーデータ
-- パスワード: password123 のBCryptハッシュ値
-- ON CONFLICTを使用して安全に挿入（重複時はスキップ）
-- 認証用のユーザーデータ
INSERT INTO users (
        email,
        password,
        role,
        created_at,
        updated_at,
        is_active
    )
VALUES (
        'admin@example.com',
        '$2a$10$TGiQfIiMCx2OcJi6nM23jupcWlDSGCAvW6vOFwpCwZJU/9jjkTF5u',
        'ADMIN',
        NOW(),
        NOW(),
        true
    ),
    (
        'alice@example.com',
        '$2a$10$TGiQfIiMCx2OcJi6nM23jupcWlDSGCAvW6vOFwpCwZJU/9jjkTF5u',
        'USER',
        NOW(),
        NOW(),
        true
    ),
    (
        'bob@example.com',
        '$2a$10$TGiQfIiMCx2OcJi6nM23jupcWlDSGCAvW6vOFwpCwZJU/9jjkTF5u',
        'USER',
        NOW(),
        NOW(),
        true
    ),
    (
        'charlie@example.com',
        '$2a$10$TGiQfIiMCx2OcJi6nM23jupcWlDSGCAvW6vOFwpCwZJU/9jjkTF5u',
        'USER',
        NOW(),
        NOW(),
        true
    ),
    (
        'diana@example.com',
        '$2a$10$TGiQfIiMCx2OcJi6nM23jupcWlDSGCAvW6vOFwpCwZJU/9jjkTF5u',
        'USER',
        NOW(),
        NOW(),
        true
    ) ON CONFLICT (email) DO NOTHING;
-- 表示・個性情報のプロフィールデータ
INSERT INTO user_profiles (
        user_id,
        display_name,
        profile_image,
        bio,
        age,
        gender,
        has_usj_annual_pass,
        location_prefecture,
        created_at,
        updated_at
    )
SELECT u.id,
    CASE
        WHEN u.email = 'admin@example.com' THEN 'システム管理者'
        WHEN u.email = 'alice@example.com' THEN 'アリス'
        WHEN u.email = 'bob@example.com' THEN 'ボブ'
        WHEN u.email = 'charlie@example.com' THEN 'チャーリー'
        WHEN u.email = 'diana@example.com' THEN 'ダイアナ'
    END as display_name,
    null as profile_image,
    CASE
        WHEN u.email = 'admin@example.com' THEN 'システム管理者です。お困りの際はお気軽にお声かけください。'
        WHEN u.email = 'alice@example.com' THEN 'USJが大好きです！年間パス持ってます♪'
        WHEN u.email = 'bob@example.com' THEN 'プログラミングとUSJのアトラクションが趣味です。'
        WHEN u.email = 'charlie@example.com' THEN '読書と映画鑑賞、そしてUSJが好きです。'
        WHEN u.email = 'diana@example.com' THEN '旅行と写真撮影が趣味です。USJで写真撮るのが好き！'
    END as bio,
    CASE
        WHEN u.email = 'alice@example.com' THEN 25
        WHEN u.email = 'bob@example.com' THEN 28
        WHEN u.email = 'charlie@example.com' THEN 22
        WHEN u.email = 'diana@example.com' THEN 30
    END as age,
    CASE
        WHEN u.email = 'alice@example.com' THEN '女性'
        WHEN u.email = 'bob@example.com' THEN '男性'
        WHEN u.email = 'charlie@example.com' THEN '男性'
        WHEN u.email = 'diana@example.com' THEN '女性'
    END as gender,
    CASE
        WHEN u.email = 'alice@example.com' THEN true
        WHEN u.email = 'bob@example.com' THEN false
        WHEN u.email = 'charlie@example.com' THEN true
        WHEN u.email = 'diana@example.com' THEN true
        ELSE false
    END as has_usj_annual_pass,
    CASE
        WHEN u.email = 'alice@example.com' THEN '大阪府'
        WHEN u.email = 'bob@example.com' THEN '東京都'
        WHEN u.email = 'charlie@example.com' THEN '兵庫県'
        WHEN u.email = 'diana@example.com' THEN '京都府'
    END as location_prefecture,
    NOW() as created_at,
    NOW() as updated_at
FROM users u
WHERE u.email IN (
        'admin@example.com',
        'alice@example.com',
        'bob@example.com',
        'charlie@example.com',
        'diana@example.com'
    ) ON CONFLICT (user_id) DO NOTHING;