package com.example.zeon.ws

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieJar(private val context: Context) : CookieJar {

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val key = url.host
        cookieStore[key] = cookies.toMutableList()

        val prefs = context.getSharedPreferences("api_cookies", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        cookies.forEach { cookie ->
            if (cookie.name == "connect.sid") {
                editor.putString("session_cookie", cookie.value)
                editor.putLong("session_expires", cookie.expiresAt)
            }
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val key = url.host
        val cookies = cookieStore[key] ?: mutableListOf()

        val prefs = context.getSharedPreferences("api_cookies", Context.MODE_PRIVATE)
        val sessionValue = prefs.getString("session_cookie", null)
        val sessionExpires = prefs.getLong("session_expires", 0)

        if (sessionValue != null && System.currentTimeMillis() < sessionExpires) {
            val sessionCookie = Cookie.Builder()
                .name("connect.sid")
                .value(sessionValue)
                .domain(url.host)
                .path("/")
                .expiresAt(sessionExpires)
                .build()

            if (!cookies.any { it.name == "connect.sid" }) {
                cookies.add(sessionCookie)
            }
        }

        return cookies.filter { it.expiresAt > System.currentTimeMillis() }
    }

    fun clearSession() {
        cookieStore.clear()
        val prefs = context.getSharedPreferences("api_cookies", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}