package com.example.silentwhisper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MyProfile : AppCompatActivity() {
    lateinit var mybind: ActivityMyProfileBinding
    lateinit var auth: FirebaseAuth
    lateinit var sharedpref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mybind= ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(mybind.root)
        auth=FirebaseAuth.getInstance()
        val curruser=auth.currentUser
        if(curruser!=null)
        {
            mybind.tvEmail.setText(curruser.email)
            setnumber(curruser)
            setUsername(curruser)
            setAnonUsername(curruser)
        }
        sharedpref = getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)
        mybind.btnLogout.setOnClickListener {
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
        mybind.backbtn.setOnClickListener{
            startActivity(Intent(this@MyProfile,FriendsPage::class.java))
            finish()
        }
        mybind.editbtn.setOnClickListener{
            startActivity(Intent(this@MyProfile,updateInfo::class.java))
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
                            mybind.usernamesection.setText(username) // Update this to match the UI element
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

    fun setAnonUsername(cUser: FirebaseUser) {
        if (cUser != null) {
            val userId = cUser.uid // Get the current user's UID

            // Reference to the Firestore document
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Fetch the username from the document
                        val username = document.getString("anonusername") // Update this to the correct field name
                        if (username != null) {
                            mybind.anonnametitle.setText(username) // Update this to match the UI element
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
    fun setnumber(cUser:FirebaseUser)
    {
        if (cUser != null) {
            val userId = cUser.uid // Get the current user's UID

            // Reference to the Firestore document
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Fetch the phone number from the document
                        val phoneNumber = document.getString("MobileNum")
                        if (phoneNumber != null) {
                            mybind.phonenumbersection.setText(phoneNumber)
                        } else {
                            Toast.makeText(this, "Phone number not found", Toast.LENGTH_SHORT).show()
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