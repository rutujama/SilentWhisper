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
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, back button is disabled
            }
        })
        val cUser = FirebaseAuth.getInstance().currentUser
        if (cUser != null) {
            setUsername(cUser)
        }
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
    fun setUsername(cUser: FirebaseUser) {
        if (cUser != null) {
            val userId = cUser.uid // Get the current user's UID

            // Reference to the Firestore document
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Fetch the username from the document
                        val username = document.getString("username") // Update this to the correct field name
                        if (username != null) {
                            fbinder.hibox.setText("Hey $username") // Update this to match the UI element
                        } else {
                            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

}