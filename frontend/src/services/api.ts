import axios from 'axios'
import type {
    SignInRequest,
    SignUpRequest,
    ApiResponse,
    AuthResponse,
    User
} from '@/types'
import { API_BASE, AUTH_PATHS, HEALTH_PATHS } from '@/constants/apiPaths'

const api = axios.create({
    baseURL: '', // baseURLを空にしてフルパスを使用
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
        // 認証エラーの場合の処理
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            // SPAなので履歴を残さずリダイレクト
            window.location.replace('/login')
        }
        
        // ネットワークエラーの場合
        if (!error.response) {
            error.isNetworkError = true
        }
        
        // サーバーエラーの場合
        if (error.response?.status >= 500) {
            error.isServerError = true
        }
        
        return Promise.reject(error)
    }
)

export const authApi = {
    signin: (credentials: SignInRequest) =>
        api.post<ApiResponse<AuthResponse>>(AUTH_PATHS.SIGNIN, credentials),

    signup: (userData: SignUpRequest) =>
        api.post<ApiResponse<AuthResponse>>(AUTH_PATHS.SIGNUP, userData),

    me: () => api.get<ApiResponse<User>>(AUTH_PATHS.ME)
}

export const healthApi = {
    check: () => api.get<{ status: string; service: string; timestamp: string }>(HEALTH_PATHS.CHECK)
}

export default api 