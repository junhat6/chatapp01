FROM node:18-alpine

# 作業ディレクトリを設定
WORKDIR /app

# package.jsonとpackage-lock.jsonをコピー
COPY package*.json ./

# 依存関係をインストール
RUN npm install

# アプリケーションのソースをコピー
COPY . .

# ポート8100を公開
EXPOSE 8100

# アプリケーションを起動
CMD ["npm", "start"] 