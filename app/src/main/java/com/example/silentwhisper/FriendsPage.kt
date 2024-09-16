package com.example.silentwhisper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class FriendsPage : AppCompatActivity() {
    lateinit var fbinder: ActivityFriendsPageBinding
    lateinit var auth: FirebaseAuth
    lateinit var sharedpref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        fbinder= ActivityFriendsPageBinding.inflate(layoutInflater)
        setContentView(fbinder.root)

        auth= FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.getCurrentUser()
        sharedpref=getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)

        fbinder.logoutbtn.setOnClickListener {
            val sharedflag= sharedpref.edit()
            sharedflag.putBoolean("Accepted",false)
            sharedflag.apply()
            intent= Intent(this,LogIn::class.java)
            auth.signOut()
            startActivity(intent)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Toast.makeText(this,"You have been Logged Out ", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}