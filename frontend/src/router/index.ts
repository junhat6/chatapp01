import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import DashboardView from '@/views/DashboardView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ProfileSearchView from '@/views/ProfileSearchView.vue'
import MatchingListView from '@/views/MatchingListView.vue'
import MatchingCreateView from '@/views/MatchingCreateView.vue'
import MatchingDetailView from '@/views/MatchingDetailView.vue'
import MatchingRoomView from '@/views/MatchingRoomView.vue'
import ChatRoomView from '@/views/ChatRoomView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView,
            meta: { requiresGuest: true }
        },
        {
            path: '/register',
            name: 'register',
            component: RegisterView,
            meta: { requiresGuest: true }
        },
        {
            path: '/dashboard',
            name: 'dashboard',
            component: DashboardView,
            meta: { requiresAuth: true }
        },
        {
            path: '/profile',
            name: 'profile',
            component: ProfileView,
            meta: { requiresAuth: true }
        },
        {
            path: '/profile/search',
            name: 'profile-search',
            component: ProfileSearchView,
            meta: { 
                requiresAuth: true,
                requiresCompleteProfile: true 
            }
        },
        {
            path: '/matching',
            name: 'matching',
            component: MatchingListView,
            meta: { 
                requiresAuth: true,
                requiresCompleteProfile: true 
            }
        },
        {
            path: '/matching/create',
            name: 'matching-create',
            component: MatchingCreateView,
            meta: { 
                requiresAuth: true,
                requiresCompleteProfile: true 
            }
        },
        {
            path: '/matching/:id',
            name: 'matching-detail',
            component: MatchingDetailView,
            meta: { 
                requiresAuth: true,
                requiresCompleteProfile: true 
            },
            props: route => ({ 
                requestId: parseInt(route.params.id as string) 
            })
        },
        {
            path: '/matching/:id/room',
            name: 'matching-room',
            component: MatchingRoomView,
            meta: { 
                requiresAuth: true,
                requiresCompleteProfile: true 
            },
            props: route => ({ 
                requestId: parseInt(route.params.id as string) 
            })
        },
        {
            path: '/chat/:requestId',
            name: 'chat-room',
            component: ChatRoomView,
            meta: { 
                requiresAuth: true,
                requiresCompleteProfile: true 
            },
            props: route => ({ 
                requestId: parseInt(route.params.requestId as string) 
            })
        }
    ]
})

router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    const profileStore = useProfileStore()

    // 認証チェック
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')
        return
    } 
    
    if (to.meta.requiresGuest && authStore.isAuthenticated) {
        next('/dashboard')
        return
    }

    // プロフィール完了チェック
    if (to.meta.requiresCompleteProfile && authStore.isAuthenticated) {
        try {
            // プロフィール完了ステータスを取得
            await profileStore.getCompletionStatus()
            
            if (!profileStore.isProfileComplete) {
                // プロフィールが未完了の場合、プロフィール設定ページにリダイレクト
                next({ 
                    name: 'profile', 
                    query: { 
                        incomplete: 'true',
                        missing: profileStore.missingFields.join(','),
                        from: to.name as string
                    }
                })
                return
            }
        } catch (error) {
            console.error('プロフィールステータス取得エラー:', error)
            // エラーの場合もプロフィールページにリダイレクト
            next({ name: 'profile', query: { incomplete: 'true' } })
            return
        }
    }

    next()
})

export default router 