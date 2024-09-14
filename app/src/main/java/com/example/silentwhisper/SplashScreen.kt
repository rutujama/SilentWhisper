package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        GlobalScope.launch {
            delay(3000)
            val intent = Intent(this@SplashScreen, LogIn::class.java)
            startActivity(intent)
            finish()
        }
    }
}