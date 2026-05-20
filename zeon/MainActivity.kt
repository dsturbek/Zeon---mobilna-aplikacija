package com.example.zeon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.zeon.presentation.ui.fragments.LoginFragment
import com.example.zeon.ws.NetworkModule

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NetworkModule.initialize(this)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}