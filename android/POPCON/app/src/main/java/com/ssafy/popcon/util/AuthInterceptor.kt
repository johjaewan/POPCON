package com.ssafy.popcon.util

import android.content.Context
import android.util.Log
import com.ssafy.popcon.config.ApplicationClass
import com.ssafy.popcon.repository.auth.AuthRemoteDataSource
import com.ssafy.popcon.repository.auth.AuthRepository
import com.ssafy.popcon.ui.common.MainActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val TAG = "AuthInterceptor_###"
class AuthInterceptor(private val _context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val chainReq = chain.request()

        var req = makeReqHeader(chainReq)
        Log.d("TAG", "auth intercept: ${ApplicationClass.sharedPreferencesUtil.accessToken}")

        var response = chain.proceed(req)
        Log.d(TAG, "interceptttttttttt: ${ApplicationClass.sharedPreferencesUtil.refreshToken!!}")
        when(response.code){
            401 -> {
                val authRepo = AuthRepository(AuthRemoteDataSource(RetrofitUtil.authService))
                runBlocking {
                    val res = authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)

                    ApplicationClass.sharedPreferencesUtil.accessToken = res.acessToken
                    ApplicationClass.sharedPreferencesUtil.refreshToken = res.refreshToken

                    Log.d(TAG, "intercept: ${res.acessToken}\n ***  ${res.refreshToken}")
                    req = makeReqHeader(chainReq)
                    response = chain.proceed(req)
                }
            }
//            200 -> {
//                if (ApplicationClass.sharedPreferencesUtil.refreshToken != null){
//                    val authRepo = AuthRepository(AuthRemoteDataSource(RetrofitUtil.authService))
//                    runBlocking {
//                        val res = authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)
//
//                        ApplicationClass.sharedPreferencesUtil.accessToken = res.acessToken
//                        ApplicationClass.sharedPreferencesUtil.refreshToken = res.refreshToken
//
//                        Log.d(TAG, "intercept: ${res.acessToken}\n *1*1*  ${res.refreshToken}")
//                        req = makeReqHeader(chainReq)
//                        response = chain.proceed(req)
//                    }
//                }
//
//            }
        }

        return response
    }

    private fun makeReqHeader(chainReq: Request): Request{
        return chainReq.newBuilder().addHeader(
            "Authorization",
            "Bearer ${ApplicationClass.sharedPreferencesUtil.accessToken ?: ""}"
        ).build()
    }
}