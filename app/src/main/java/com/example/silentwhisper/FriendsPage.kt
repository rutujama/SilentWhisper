package com.example.silentwhisper

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class FriendsPage : AppCompatActivity() {
    lateinit var fbinder: ActivityFriendsPageBinding
    lateinit var auth: FirebaseAuth
    lateinit var sharedpref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        fbinder= ActivityFriendsPageBinding.inflate(layoutInflater)
        setContentView(fbinder.root)
        sharedpref = getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)
        val cUser = FirebaseAuth.getInstance().currentUser
        auth=FirebaseAuth.getInstance()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, back button is disabled
            }
        })
        fbinder.myprofilebtn.setOnClickListener{
            startActivity(Intent(this@FriendsPage,MyProfile::class.java))
        }
    }



}