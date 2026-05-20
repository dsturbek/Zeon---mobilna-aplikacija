package com.example.zeon.helpers

import android.content.Context
import android.content.SharedPreferences
import com.example.zeon.data.model.User

object UserSession {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_USERNAME = "user_username"
    private const val KEY_USER_HEIGHT = "user_height"
    private const val KEY_USER_WEIGHT = "user_weight"
    private const val KEY_USER_BIRTH_DATE = "user_birth_date"
    private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"
    private const val KEY_USER_TRAINER_ID = "user_trainer_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, user: User) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_USERNAME, user.username)
        editor.putFloat(KEY_USER_HEIGHT, user.height)
        editor.putFloat(KEY_USER_WEIGHT, user.weight)
        editor.putLong(KEY_USER_BIRTH_DATE, user.birthDate.time)
        editor.putString(KEY_USER_PROFILE_IMAGE, user.profileImage)
        editor.putInt(KEY_USER_TRAINER_ID, user.trainerId ?: -1)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getCurrentUser(context: Context): User? {
        val prefs = getPreferences(context)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        if (!isLoggedIn) {
            return null
        }

        val userId = prefs.getInt(KEY_USER_ID, -1)
        if (userId == -1) {
            return null
        }

        val trainerId = prefs.getInt(KEY_USER_TRAINER_ID, -1)
        return User(
            id = userId,
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            username = prefs.getString(KEY_USER_USERNAME, "") ?: "",
            password = "",
            profileImage = prefs.getString(KEY_USER_PROFILE_IMAGE, "") ?: "",
            birthDate = java.util.Date(prefs.getLong(KEY_USER_BIRTH_DATE, 0)),
            height = prefs.getFloat(KEY_USER_HEIGHT, 0f),
            weight = prefs.getFloat(KEY_USER_WEIGHT, 0f),
            trainerId = if (trainerId == -1) null else trainerId
        )
    }

    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, -1)
    }

    fun getTrainerId(context: Context): Int? {
        val trainerId = getPreferences(context).getInt(KEY_USER_TRAINER_ID, -1)
        return if (trainerId == -1) null else trainerId
    }

    fun saveTrainerId(context: Context, trainerId: Int?) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_USER_TRAINER_ID, trainerId ?: -1)
        editor.apply()
    }
}