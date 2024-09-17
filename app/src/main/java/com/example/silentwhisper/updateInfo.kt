package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityUpdateInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class updateInfo : AppCompatActivity() {

    lateinit var ubind : ActivityUpdateInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ubind= ActivityUpdateInfoBinding.inflate(layoutInflater)
        setContentView(ubind.root)
        val curruser=FirebaseAuth.getInstance().currentUser
        if (curruser != null) {
            setnumber(curruser)
            setUsername(curruser)
        }
        ubind.savebtn.setOnClickListener{
            ubind.savebtn.isClickable=false
            if(!ubind.newUsername.text.toString().isEmpty()) {
                if (curruser != null) {
                    val userId = curruser.uid
                    val db = FirebaseFirestore.getInstance()
                    val userUpdate = hashMapOf(
                        "username" to ubind.newUsername.text.toString(),
                    )
                    db.collection("users").document(userId).update(userUpdate as Map<String, Any>)
                }
                ubind.savebtn.isClickable=true
                startActivity(Intent(this@updateInfo, FriendsPage::class.java))
                finish()
            }
            else{
                ubind.savebtn.isClickable=false
                Toast.makeText(this@updateInfo,"Name Can't Be Empty",Toast.LENGTH_SHORT).show()
            }
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
                            ubind.newUsername.setText(username) // Update this to match the UI element
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
                            ubind.phoneNumber.setText(phoneNumber)
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