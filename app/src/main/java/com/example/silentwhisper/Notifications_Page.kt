package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.silentwhisper.R

class Notifications_Page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_page)
        findViewById<ImageButton>(R.id.backbtn).setOnClickListener {
            startActivity(Intent(this@Notifications_Page,FriendsPage::class.java))
            finish()
        }
    }
}