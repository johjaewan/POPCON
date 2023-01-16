package com.ssafy.popcon.util

import android.content.Context
import android.content.SharedPreferences
import com.ssafy.popcon.dto.User

class SharedPreferencesUtil(context: Context) {
    val SHARED_PREFERENCES_NAME = "popcon_preference"
    var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    //로그인 유저 추가
    fun addUser(user: User) {
        val editor = preferences.edit()
        editor.putString("id", user.email)
        editor.putInt("type", user.type)
        editor.apply()
    }

    //로그인 유저 받기
    fun getUser(): User {
        val id = preferences.getString("id", "")
        return if (id != "") {
            val type = preferences.getInt("type", 0)
            User(id!!, type)
        } else {
            User()
        }
    }

    //preference 지우기
    fun deleteUser() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}