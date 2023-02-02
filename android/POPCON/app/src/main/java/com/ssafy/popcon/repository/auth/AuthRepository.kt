package com.ssafy.popcon.repository.auth

import com.ssafy.popcon.dto.TokenResponse
import com.ssafy.popcon.dto.User

class AuthRepository(private val remoteDataSource: AuthRemoteDataSource) {
    suspend fun signIn(user : User) : TokenResponse{
        return remoteDataSource.signIn(user)
    }

    suspend fun refreshToken(refreshToken: String) : TokenResponse {
        return remoteDataSource.refreshToken(refreshToken)
    }
}