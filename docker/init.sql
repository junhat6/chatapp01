-- USJ Chat App データベース初期化スクリプト

-- データベースの作成は既にDocker Composeで設定済み

-- 必要に応じて初期データやテーブル設定をここに追加
-- 例: 初期管理ユーザーの作成など

-- UTF-8エンコーディング確認
SELECT current_setting('server_encoding');

-- タイムゾーン設定
SET timezone = 'Asia/Tokyo';

-- ログ出力
SELECT 'USJ Chat App データベースが正常に初期化されました' AS status; 