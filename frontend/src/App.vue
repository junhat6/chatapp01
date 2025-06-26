<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-container">
        <router-link to="/" class="nav-brand">
          🎢 USJ Chat
        </router-link>
        <div class="nav-links">
          <router-link to="/login" class="nav-link" v-if="!isAuthenticated">
            ログイン
          </router-link>
          <router-link to="/register" class="nav-link" v-if="!isAuthenticated">
            新規登録
          </router-link>
          <router-link to="/dashboard" class="nav-link" v-if="isAuthenticated">
            ダッシュボード
          </router-link>
          <router-link to="/profile" class="nav-link" v-if="isAuthenticated">
            プロフィール
          </router-link>
          <router-link to="/profile/search" class="nav-link" v-if="isAuthenticated">
            仲間探し
          </router-link>
          <button @click="logout" class="nav-link logout-btn" v-if="isAuthenticated">
            ログアウト
          </button>
        </div>
      </div>
    </nav>
    
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)

const logout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  background-color: #1976d2;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
  text-decoration: none;
  color: white;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
  font-weight: 600;
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: inherit;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

@media (max-width: 768px) {
  .nav-container {
    flex-direction: column;
    gap: 1rem;
  }
  
  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style> 