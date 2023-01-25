package com.ssafy.popcon.repository.user

import com.ssafy.popcon.dto.User
import com.ssafy.popcon.network.api.UserApi

class UserRemoteDataSource(private val apiClient: UserApi) : UserDataSource {
    override suspend fun signInNaver(user: User): User {
        return apiClient.signInNaver(user)
    }

    override suspend fun signInKakao(user: User): User {
        return apiClient.signInKakao(user)
    }

    override suspend fun withdraw(user: User): User {
        return apiClient.withdraw(user)
    }
}