-- Phase 2: 期限切れ募集の論理削除機能
-- matching_requestsテーブルにdeleted_atカラムを追加
-- deleted_atカラムを追加（論理削除用）
ALTER TABLE matching_requests
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
-- 既存データの整合性確保（削除されていないことを明示）
UPDATE matching_requests
SET deleted_at = NULL
WHERE deleted_at IS NULL;
-- deleted_atカラムにインデックスを作成（クエリパフォーマンス向上）
CREATE INDEX IF NOT EXISTS idx_matching_requests_deleted_at ON matching_requests(deleted_at);
-- 論理削除されていない募集のみを取得するための複合インデックス
CREATE INDEX IF NOT EXISTS idx_matching_requests_active ON matching_requests(status, deleted_at)
WHERE deleted_at IS NULL;
-- コメント追加
COMMENT ON COLUMN matching_requests.deleted_at IS '論理削除日時。NULLの場合は削除されていない、値がある場合は削除済み';