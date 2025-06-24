export interface User {
    id: number
    email: string
    displayName: string
    profileImage?: string
    bio?: string
    createdAt: string
}

export interface SignInRequest {
    email: string
    password: string
}

export interface SignUpRequest {
    email: string
    password: string
    displayName: string
    profileImage?: string
    bio?: string
}

export interface AuthResponse {
    token: string
    user: User
}

export interface ApiResponse<T> {
    success: boolean
    data?: T
    message: string
    errors: string[]
} 