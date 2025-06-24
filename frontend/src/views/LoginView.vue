<template>
  <div class="login-page">
    <div class="login-container">
      <div class="card">
        <h1 class="login-title">ログイン</h1>
        <p class="login-subtitle">USJ Chatへようこそ！</p>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email" class="form-label">メールアドレス</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              class="form-input"
              placeholder="メールアドレスを入力"
              required
              :disabled="loading"
            />
          </div>
          
          <div class="form-group">
            <label for="password" class="form-label">パスワード</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              class="form-input"
              placeholder="パスワードを入力"
              required
              :disabled="loading"
            />
          </div>
          
          <div v-if="error" class="error-message">
            {{ error }}
          </div>
          
          <button 
            type="submit" 
            class="btn btn-primary login-btn"
            :disabled="loading"
          >
            {{ loading ? 'ログイン中...' : 'ログイン' }}
          </button>
        </form>
        
        <div class="login-footer">
          <p>
            アカウントをお持ちでない方は
            <router-link to="/register" class="register-link">
              こちらから新規登録
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { SignInRequest } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const form = ref<SignInRequest>({
  email: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!form.value.email || !form.value.password) {
    error.value = 'メールアドレスとパスワードを入力してください'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const success = await authStore.login(form.value)
    if (success) {
      router.push('/dashboard')
    } else {
      error.value = authStore.error || 'ログインに失敗しました'
    }
  } catch (err) {
    error.value = 'ログインに失敗しました'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-title {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 0.5rem;
  color: #333;
}

.login-subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 2rem;
}

.login-form {
  margin-bottom: 2rem;
}

.login-btn {
  width: 100%;
  margin-top: 1rem;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.register-link {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}
</style> 