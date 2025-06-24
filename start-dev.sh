#!/bin/bash

echo "🎢 USJ Chat App 開発環境起動スクリプト"
echo "=================================="

# PostgreSQL（Docker）起動オプション
echo ""
echo "データベース選択："
echo "1. PostgreSQL（Docker）- 本番環境と同じ"
echo "2. H2インメモリ - セットアップ不要"
echo ""
read -p "選択してください (1 または 2): " choice

case $choice in
    1)
        echo "PostgreSQL（Docker）を起動しています..."
        docker-compose up -d postgres
        
        echo "PostgreSQLの起動を待機中..."
        until docker exec usjchatapp-postgres pg_isready -U usjchatapp -d usjchatapp; do
            sleep 1
        done
        
        echo "バックエンドを起動しています..."
        cd backend && ./gradlew bootRun --args='--spring.profiles.active=default'
        ;;
    2)
        echo "H2インメモリデータベースでバックエンドを起動しています..."
        cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev'
        ;;
    *)
        echo "無効な選択です。1 または 2 を選択してください。"
        exit 1
        ;;
esac 