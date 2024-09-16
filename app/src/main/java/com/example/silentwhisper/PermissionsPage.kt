package com.example.silentwhisper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.example.silentwhisper.databinding.ActivityPermissionsPageBinding
import com.google.firebase.auth.FirebaseAuth

lateinit var pBinder:ActivityPermissionsPageBinding
lateinit var sharedpref:SharedPreferences

class PermissionsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pBinder=ActivityPermissionsPageBinding.inflate(layoutInflater)
        setContentView(pBinder.root)

        sharedpref=getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)
        pBinder.acceptbtn.setOnClickListener{
            if(pBinder.checkbox.isChecked) {
                val sharedflag= sharedpref.edit()
                sharedflag.putBoolean("Accepted",true)
                sharedflag.apply()
                startActivity(Intent(this@PermissionsPage, FriendsPage::class.java))
                finish()
            }
            else{
                Toast.makeText(this@PermissionsPage,"Please Accept The Policies",Toast.LENGTH_SHORT).show()
            }
        }
    }
}