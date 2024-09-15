package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.example.silentwhisper.databinding.ActivityLogInBinding
import com.example.silentwhisper.databinding.ActivityPermissionsPageBinding
import com.google.firebase.auth.FirebaseAuth

class FriendsPage : AppCompatActivity() {
    lateinit var fbinder: ActivityFriendsPageBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        fbinder= ActivityFriendsPageBinding.inflate(layoutInflater)
        setContentView(fbinder.root)

        auth= FirebaseAuth.getInstance()

        fbinder.logoutbtn.setOnClickListener {

            intent= Intent(this,LogIn::class.java)
            auth.signOut()
            startActivity(intent)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Toast.makeText(this,"You have been Logged Out ", Toast.LENGTH_SHORT).show()
        }
    }
}