package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.silentwhisper.databinding.ActivityUpdateInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class updateInfo : AppCompatActivity() {

    lateinit var ubind : ActivityUpdateInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ubind= ActivityUpdateInfoBinding.inflate(layoutInflater)
        setContentView(ubind.root)
        val curruser=FirebaseAuth.getInstance().currentUser
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        if (curruser != null) {
            setUsername(curruser)
            setAnonUsername(curruser)
        }
        ubind.savebtn.setOnClickListener{
            ubind.savebtn.isClickable=false
            if(!ubind.newUsername.text.toString().isEmpty()) {
                if (curruser != null) {
                    val userId = curruser.uid
                    val db = FirebaseFirestore.getInstance()
                    val userUpdate = hashMapOf(
                        "username" to ubind.newUsername.text.toString(),
                        "anonusername" to ubind.anonymousnamebtn.text.toString()
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
        ubind.anonymousnamereloadbtn.setOnClickListener{
            getAnonName()
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
                            ubind.anonymousnamebtn.setText(username) // Update this to match the UI element
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

    fun getAnonName()
    {
        val client=OkHttpClient()
        val url="https://usernameapiv1.vercel.app/api/random-usernames"
        val request= Request.Builder().url(url).build()
        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@updateInfo,"Error Fetching Username",Toast.LENGTH_SHORT)
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    response.body?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody.string())
                        val usernamesArray = jsonObject.getJSONArray("usernames")
                        val randomUsername = usernamesArray.getString(0)
                        runOnUiThread {
                            ubind.anonymousnamebtn.setText(randomUsername)
                        }
                    }
                }
            }

        })
    }
}