<template>
  <n-config-provider :theme="null">
    <n-message-provider>
      <n-loading-bar-provider>
        <n-dialog-provider>
          <n-notification-provider>
            <div id="app" class="min-h-screen bg-gray-50">
              <!-- シンプルなナビゲーション -->
              <nav class="bg-white shadow-sm border-b border-gray-200">
                <div class="container-app py-4">
                  <div class="flex-between">
                    <!-- ブランドロゴ -->
                    <router-link to="/" class="flex items-center space-x-3 group">
                      <div class="w-8 h-8 bg-primary-500 rounded-lg flex items-center justify-center">
                        <div class="text-white text-xl font-bold">M</div>
                      </div>
                      <div class="text-gray-900 font-bold text-xl">
                        マッチング
                      </div>
                    </router-link>

                    <!-- ナビゲーションリンク -->
                    <div class="flex items-center space-x-3">
                      <!-- ゲストリンク -->
                      <template v-if="!isAuthenticated">
                        <n-button 
                          text 
                          @click="$router.push('/login')"
                          class="text-gray-600 hover:text-primary-600"
                        >
                          ログイン
                        </n-button>
                        <n-button 
                          type="primary"
                          @click="$router.push('/register')"
                          class="btn-primary"
                        >
                          新規登録
                        </n-button>
                      </template>

                      <!-- 認証済みリンク -->
                      <template v-if="isAuthenticated">
                        <n-button 
                          text
                          @click="navigateTo('/dashboard')"
                          class="text-gray-600 hover:text-primary-600"
                        >
                          ダッシュボード
                        </n-button>
                        
                        <n-button 
                          text
                          @click="navigateTo('/profile')"
                          class="text-gray-600 hover:text-primary-600"
                        >
                          プロフィール
                        </n-button>

                        <!-- マッチングボタン -->
                        <n-button 
                          type="primary"
                          @click="navigateToMatching"
                          :disabled="!isProfileComplete"
                          class="btn-primary relative"
                        >
                          マッチング
                          <div v-if="!isProfileComplete" class="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"></div>
                        </n-button>

                        <!-- 検索ボタン -->
                        <n-button 
                          @click="navigateToSearch"
                          :disabled="!isProfileComplete"
                          class="btn-secondary relative"
                        >
                          仲間探し
                          <div v-if="!isProfileComplete" class="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"></div>
                        </n-button>

                        <!-- ログアウトボタン -->
                        <n-button 
                          text
                          @click="logout"
                          class="text-gray-600 hover:text-red-600"
                        >
                          ログアウト
                        </n-button>
                      </template>
                    </div>
                  </div>
                </div>
              </nav>
              
              <!-- メインコンテンツエリア -->
              <main class="min-h-screen">
                <div class="container-app py-6">
                  <router-view />
                </div>
              </main>
              
              <!-- シンプルなフッター -->
              <footer class="bg-white border-t border-gray-200 py-8 mt-auto">
                <div class="container-app">
                  <div class="text-center">
                    <h3 class="text-lg font-semibold text-gray-900 mb-2">マッチングアプリ</h3>
                    <p class="text-gray-600">新しい出会いを見つけよう</p>
                  </div>
                </div>
              </footer>
            </div>
          </n-notification-provider>
        </n-dialog-provider>
      </n-loading-bar-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useProfileStore } from './stores/profile'

const router = useRouter()
const authStore = useAuthStore()
const profileStore = useProfileStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const isProfileComplete = computed(() => profileStore.isProfileComplete)

const logout = () => {
  try {
    console.log('Logout clicked')
    authStore.logout()
    router.push('/')
  } catch (error) {
    console.error('Logout error:', error)
  }
}

const navigateToMatching = () => {
  try {
    console.log('Navigate to matching clicked', {
      isProfileComplete: isProfileComplete.value,
      missingFields: profileStore.missingFields
    })
    
    if (!isProfileComplete.value) {
      router.push({ 
        name: 'profile', 
        query: { 
          incomplete: 'true',
          missing: profileStore.missingFields.join(','),
          from: 'matching'
        }
      })
    } else {
      router.push('/matching')
    }
  } catch (error) {
    console.error('Navigate to matching error:', error)
  }
}

const navigateToSearch = () => {
  try {
    console.log('Navigate to search clicked', {
      isProfileComplete: isProfileComplete.value,
      missingFields: profileStore.missingFields
    })
    
    if (!isProfileComplete.value) {
      router.push({ 
        name: 'profile', 
        query: { 
          incomplete: 'true',
          missing: profileStore.missingFields.join(','),
          from: 'profile-search'
        }
      })
    } else {
      router.push('/profile/search')
    }
  } catch (error) {
    console.error('Navigate to search error:', error)
  }
}

const navigateTo = (path: string) => {
  try {
    console.log('Navigate to:', path)
    router.push(path)
  } catch (error) {
    console.error('Navigation error:', error)
  }
}
</script>

<style scoped>
/* レスポンシブ対応 */
@media (max-width: 768px) {
  .flex-between {
    flex-direction: column;
    gap: 1rem;
  }
  
  .space-x-3 > * + * {
    margin-left: 0;
    margin-top: 0.5rem;
  }
}
</style> 