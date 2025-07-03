<template>
  <div class="register-page">
    <div class="register-container">
      <div class="card">
        <h1 class="register-title">新規登録</h1>
        <p class="register-subtitle">USJ Chatでつながろう！</p>
        
        <form @submit.prevent="handleRegister" class="register-form">
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
              placeholder="パスワードを入力（6文字以上）"
              required
              :disabled="loading"
            />
          </div>
          
          <div v-if="errors.length > 0" class="error-messages">
            <div v-for="error in errors" :key="error" class="error-message">
              {{ error }}
            </div>
          </div>
          
          <button 
            type="submit" 
            class="btn btn-primary register-btn"
            :disabled="loading"
          >
            {{ loading ? '登録中...' : '新規登録' }}
          </button>
        </form>
        
        <div class="register-footer">
          <p>
            既にアカウントをお持ちの方は
            <router-link to="/login" class="login-link">
              こちらからログイン
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
import type { SignUpRequest } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const form = ref<SignUpRequest>({
  email: '',
  password: ''
})

const loading = ref(false)
const errors = ref<string[]>([])

const validateForm = (): boolean => {
  errors.value = []
  
  if (!form.value.email) {
    errors.value.push('メールアドレスは必須です')
  }
  
  if (!form.value.password) {
    errors.value.push('パスワードは必須です')
  } else if (form.value.password.length < 6) {
    errors.value.push('パスワードは6文字以上で入力してください')
  }
  
  return errors.value.length === 0
}

const handleRegister = async () => {
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  errors.value = []
  
  try {
    const success = await authStore.register(form.value)
    if (success) {
      router.push('/dashboard')
    } else {
      errors.value = [authStore.error || '登録に失敗しました']
    }
  } catch (err) {
    errors.value = ['登録に失敗しました']
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-title {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 0.5rem;
  color: #333;
}

.register-subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 2rem;
}

.register-form {
  margin-bottom: 2rem;
}

.register-btn {
  width: 100%;
  margin-top: 1rem;
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-messages {
  margin-bottom: 1rem;
}

.register-footer {
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.login-link {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

textarea.form-input {
  resize: vertical;
  min-height: 80px;
}
</style> 