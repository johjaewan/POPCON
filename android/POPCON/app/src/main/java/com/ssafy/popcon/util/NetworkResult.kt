package com.ssafy.popcon.util

import android.util.Log
import com.ssafy.popcon.config.ApplicationClass
import com.ssafy.popcon.repository.auth.AuthDataSource
import com.ssafy.popcon.repository.auth.AuthRemoteDataSource
import com.ssafy.popcon.repository.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Response

//sealed class NetworkResult<T>(val data: T? = null, val message: String? = null) {
//    class Success<T>(data: T) : NetworkResult<T>(data)
//    class Error<T>(message: String?, data: T? = null) : NetworkResult<T>(data, message)
//    class Loading<T> : NetworkResult<T>()
//
//} // End of NetworkResult sealed class

private const val TAG = "NetworkResult_###"
sealed class NetworkResult<T>() {
    class Success<T>(val data: T) : NetworkResult<T>()
    class Error<T>(val exception: String) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()

} // End of NetworkResult sealed class


suspend fun <T: Any> safeApiCall(call: suspend() -> Response<T>): NetworkResult<T>{
    return try {
        val response = call.invoke()

        if (response.isSuccessful){
            NetworkResult.Success(response.body()!!)
        } else{
            NetworkResult.Error(response.message() ?: "Something goes wrong")
        }
    } catch (e: Exception){
        NetworkResult.Error(e.message ?: "Internet error")
    }
}

fun main() = runBlocking {
    GlobalScope.launch(Dispatchers.IO) {
        val authRepo = AuthRepository(AuthRemoteDataSource(RetrofitUtil.authService))
//        runBlocking {
//            val res = authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)
//
//            ApplicationClass.sharedPreferencesUtil.accessToken = res.acessToken
//            ApplicationClass.sharedPreferencesUtil.refreshToken = res.refreshToken
//
//            Log.d(TAG, "intercept: ${res.acessToken}\n ***  ${res.refreshToken}")
//            req = makeReqHeader(chainReq)
//            response = chain.proceed(req)
//        }

        Log.d(TAG, "main: ")
        when(val result = safeApiCall {
            authRepo.refreshToken(ApplicationClass.sharedPreferencesUtil.refreshToken!!)
        }){
            is NetworkResult.Success -> {
                Log.d(TAG, "main: SUCCESSS")
            }
            is NetworkResult.Error -> {
                Log.d(TAG, "main: ERRORRRR")
            }
            else -> {}
        }
    }.join()
}