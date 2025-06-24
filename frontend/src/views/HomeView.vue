<template>
  <div class="home">
    <div class="hero">
      <h1 class="hero-title">🎢 USJ Chat</h1>
      <p class="hero-subtitle">
        ユニバーサル・スタジオ・ジャパンで新しい仲間と出会おう！
      </p>
      <div class="hero-actions">
        <router-link to="/register" class="btn btn-primary">
          今すぐ始める
        </router-link>
        <router-link to="/login" class="btn btn-secondary">
          ログイン
        </router-link>
      </div>
    </div>

    <div class="features">
      <h2 class="features-title">主な機能</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">👥</div>
          <h3>リアルタイムマッチング</h3>
          <p>パーク内で同じエリアにいる人と瞬時につながれます</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">💬</div>
          <h3>チャット機能</h3>
          <p>リアルタイムでメッセージをやり取りできます</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🎪</div>
          <h3>グループ作成</h3>
          <p>アトラクションを一緒に楽しむグループを作成できます</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// ロジックが必要になったらここに追加
</script>

<style scoped>
.home {
  min-height: 80vh;
}

.hero {
  text-align: center;
  padding: 4rem 0;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  color: white;
  margin: -2rem -1rem 3rem -1rem;
}

.hero-title {
  font-size: 3rem;
  margin-bottom: 1rem;
  font-weight: bold;
}

.hero-subtitle {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.features {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 1rem;
}

.features-title {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 3rem;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style> 