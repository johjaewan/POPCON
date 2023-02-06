package com.ssafy.popcon.util

import android.util.Log
import com.ssafy.popcon.config.ApplicationClass
import com.ssafy.popcon.repository.auth.AuthRemoteDataSource
import com.ssafy.popcon.repository.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "AuthInterceptor_###"
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        var req =
            request.newBuilder().addHeader(
                "Authorization",
                "Bearer ${ApplicationClass.sharedPreferencesUtil.accessToken ?: ""}"
            ).build()
        Log.d("TAG", "auth intercept: ${ApplicationClass.sharedPreferencesUtil.accessToken}")

        var response = chain.proceed(req)
        when(response.code){
            401 -> {
                val authRepo = AuthRepository(AuthRemoteDataSource(RetrofitUtil.authService))
                runBlocking {
                    val res = authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)
                    ApplicationClass.sharedPreferencesUtil.accessToken = res.acessToken
                    ApplicationClass.sharedPreferencesUtil.refreshToken = res.refreshToekn

                    Log.d(TAG, "intercept: ${res.acessToken}")
                    req =
                        request.newBuilder().addHeader(
                            "Authorization",
                            "Bearer ${ApplicationClass.sharedPreferencesUtil.accessToken ?: ""}"
                        ).build()
                    response = chain.proceed(req)
                }
            }
//            500 -> {
//            val authRepo = AuthRepository(AuthRemoteDataSource(RetrofitUtil.authService))
//            runBlocking {
//                val res = authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)
//                ApplicationClass.sharedPreferencesUtil.accessToken = res.acessToken
//                ApplicationClass.sharedPreferencesUtil.refreshToken = res.refreshToekn
//
//                Log.d(TAG, "intercept: ${ApplicationClass.sharedPreferencesUtil.accessToken}")
//                req =
//                    request.newBuilder().addHeader(
//                        "Authorization",
//                        "Bearer ${ApplicationClass.sharedPreferencesUtil.refreshToken ?: ""}"
//                    ).build()
//                response = chain.proceed(req)
//            }
//        }
//            403 -> {
//                val authRepo = AuthRepository(AuthRemoteDataSource(RetrofitUtil.authService))
//                runBlocking {
//                    val res = authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)
//                    ApplicationClass.sharedPreferencesUtil.accessToken = res.acessToken
//                    ApplicationClass.sharedPreferencesUtil.refreshToken = res.refreshToekn
//
//                    Log.d(TAG, "intercept?: ${ApplicationClass.sharedPreferencesUtil.accessToken}")
//                    req =
//                        request.newBuilder().addHeader(
//                            "Authorization",
//                            "Bearer ${ApplicationClass.sharedPreferencesUtil.refreshToken ?: ""}"
//                        ).build()
//                    response = chain.proceed(req)
//                }
//            }
        }

        return response
    }
}