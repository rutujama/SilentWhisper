package com.example.silentwhisper

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.silentwhisper.databinding.ActivityLogInBinding


lateinit var binder:ActivityLogInBinding

class LogIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        binder = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binder.root)

        binder.submitbtn.setOnClickListener {


        }


        binder.newuserbtn.setOnClickListener {
            startActivity(Intent(this@LogIn, RegisterPage::class.java))
            finish()
        }
    }
}
