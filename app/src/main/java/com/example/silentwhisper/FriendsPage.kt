package com.example.silentwhisper

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import android.view.ViewParent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.bumptech.glide.Glide
import com.google.firebase.firestore.auth.User

class FriendsPage : AppCompatActivity() {
    lateinit var fbinder: ActivityFriendsPageBinding
    lateinit var auth: FirebaseAuth
    lateinit var sharedpref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        fbinder= ActivityFriendsPageBinding.inflate(layoutInflater)
        setContentView(fbinder.root)
        sharedpref = getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)
        val userList = ArrayList<FirebaseUser>()
        val cUser = FirebaseAuth.getInstance().currentUser
        auth=FirebaseAuth.getInstance()
        if(cUser!=null)
        {
            setProfilePicture(cUser)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, back button is disabled
            }
        })
        fbinder.setting.setOnClickListener{
            startActivity(Intent(this@FriendsPage,MyProfile::class.java))
        }
    }

    fun setProfilePicture(cUser: FirebaseUser) {
        if (cUser != null) {
            val userId = cUser.uid // Get the current user's UID

            // Reference to the Firestore document
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Fetch the profile picture URL from the document
                        val profilePicUrl = document.getString("profilePic") // Fetch profile picture URL from "profilePic" field

                        if (profilePicUrl != null) {
                            // Assuming you are using Glide to load the image into an ImageView (e.g., ubind.profileImageView)
                            Glide.with(this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.swlogo) // Placeholder image
                                .error(R.drawable.swlogo)         // Error image
                                .into(fbinder.myprofileicon)                // Update with your ImageView

                        } else {
                            Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

}