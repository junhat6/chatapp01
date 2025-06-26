// User: 認証・アカウント管理
export interface User {
    id: number
    email: string
    createdAt: string
}

// UserProfile: 表示・マッチング用の個人情報
export interface UserProfile {
    userId: number // これをPKとして扱う
    displayName: string
    profileImage?: string
    bio?: string
    age?: number
    gender?: string
    hasUsjAnnualPass: boolean
    favoriteAttractions: string[]
    locationPrefecture?: string
    hobbies: string[]
    createdAt: string
    updatedAt: string
}

export interface CreateUserProfileRequest {
    displayName: string
    profileImage?: string
    bio?: string
    age?: number
    gender?: string
    hasUsjAnnualPass: boolean
    favoriteAttractions: string[]
    locationPrefecture?: string
    hobbies: string[]
}

export interface UpdateUserProfileRequest {
    displayName?: string
    profileImage?: string
    bio?: string
    age?: number
    gender?: string
    hasUsjAnnualPass?: boolean
    favoriteAttractions?: string[]
    locationPrefecture?: string
    hobbies?: string[]
}

export interface UserProfileSearchRequest {
    minAge?: number
    maxAge?: number
    gender?: string
    hasUsjAnnualPass?: boolean
    favoriteAttraction?: string
    locationPrefecture?: string
    displayName?: string
}

export interface SignInRequest {
    email: string
    password: string
}

export interface SignUpRequest {
    email: string
    password: string
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