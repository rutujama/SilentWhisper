package com.example.silentwhisper

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.example.silentwhisper.databinding.ActivityPermissionsPageBinding

lateinit var pBinder:ActivityPermissionsPageBinding
class PermissionsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pBinder=ActivityPermissionsPageBinding.inflate(layoutInflater)
        setContentView(pBinder.root)

        pBinder.acceptbtn.setOnClickListener{
            startActivity(Intent(this@PermissionsPage,FriendsPage::class.java))
            finish()
        }
    }
}