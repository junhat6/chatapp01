import axios from 'axios'
import type {
    SignInRequest,
    SignUpRequest,
    ApiResponse,
    AuthResponse,
    User
} from '@/types'

const api = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// リクエストインターセプター（JWTトークンを自動追加）
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// レスポンスインターセプター（認証エラー時の処理）
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export const authApi = {
    signIn: (credentials: SignInRequest) =>
        api.post<ApiResponse<AuthResponse>>('/auth/signin', credentials),

    signUp: (userData: SignUpRequest) =>
        api.post<ApiResponse<AuthResponse>>('/auth/signup', userData),

    getCurrentUser: () =>
        api.get<ApiResponse<User>>('/auth/me')
}

export const healthApi = {
    check: () => api.get<{ status: string; service: string; timestamp: string }>('/health')
}

export default api 