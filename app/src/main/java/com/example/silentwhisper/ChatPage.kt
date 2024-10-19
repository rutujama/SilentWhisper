package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.silentwhisper.databinding.ActivityChatPageBinding

lateinit var cbind : ActivityChatPageBinding
class ChatPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cbind=ActivityChatPageBinding.inflate(layoutInflater)
        setContentView(cbind.root)
        cbind.backbtn.setOnClickListener {
            startActivity(Intent(this@ChatPage,FriendsPage::class.java))
            finish()
        }
    }
}