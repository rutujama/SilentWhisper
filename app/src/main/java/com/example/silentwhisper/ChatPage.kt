package com.example.silentwhisper

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.example.silentwhisper.databinding.ActivityChatPageBinding
import com.example.silentwhisper.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

lateinit var cbind : ActivityChatPageBinding
var firebaseuser:FirebaseUser ?= null
class ChatPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cbind=ActivityChatPageBinding.inflate(layoutInflater)
        setContentView(cbind.root)
        getWindow().setBackgroundDrawable(getDrawable(R.drawable.appwallpaper))
        val userId = intent.getStringExtra("USER_ID") ?: ""
        if(!userId.isEmpty() && isInternetAvailable(this@ChatPage))
        {
            setProfilePicture(userId)
            setUsername(userId)
        }
//        val reference=FirebaseDatabase.getInstance().reference
//            .child("Users").child(userId)

//        reference.addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//              val user: Users? = snapshot.getValue(Users::class.java)
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })


       firebaseuser=FirebaseAuth.getInstance().currentUser
        cbind.backbtn.setOnClickListener {
            startActivity(Intent(this@ChatPage,FriendsPage::class.java))
            finish()
        }


        cbind.sendbtn.setOnClickListener(){
            val message=cbind.msget.text.toString()
            if(message.isEmpty()==false)
            {
                   sendmsg(firebaseuser!!.uid,userId,message)
            }

        }
    }
    fun sendmsg(sId: String,rId:String,message:String)
    {
        val reference=FirebaseDatabase.getInstance().reference
        val messageKey=reference.push().key
        val msgHashMap= HashMap<String,Any?>()

        msgHashMap["sender"]=sId
        msgHashMap["message"]=message
        msgHashMap["receiver"]=rId
        msgHashMap["isseen"]=false
        msgHashMap["url"]=""
        msgHashMap["messageId"]=messageKey
        reference.child("Chats").child(messageKey!!)
            .setValue(msgHashMap ).addOnCompleteListener{
                task ->
                if(task.isSuccessful)
                {
                    val ChatListreference=FirebaseDatabase.getInstance()
                        .reference.child("ChatList")
                        .child(firebaseuser!!.uid)
                        .child(rId)
                    ChatListreference.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists())
                            {
                                ChatListreference.child("id").setValue(rId)
                            }
                            val ChatListReceieverreference=FirebaseDatabase.getInstance()
                                .reference.child("ChatList")
                                .child(rId)
                                .child(firebaseuser!!.uid)
                            ChatListReceieverreference.child("id")
                                .setValue(firebaseuser!!.uid)


                            ChatListreference.child("id")
                                .setValue(firebaseuser!!.uid)

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                    val reference=FirebaseDatabase.getInstance()
                        .reference.child("Users").child(firebaseuser!!.uid)
                }
            }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
    fun setUsername(uId:String) {
            val userId = uId
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val isAnon=document.getBoolean("isAnon")
                        if(isAnon == true){
                            val username = document.getString("anonusername") // Update this to the correct field name
                            if (username != null) {
                                cbind.username.setText(username) // Update this to match the UI element
                            } else {
                                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else {
                            val username =
                                document.getString("username") // Update this to the correct field name
                            if (username != null) {
                                cbind.username.setText(username) // Update this to match the UI element
                            } else {
                                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
    }
    fun setProfilePicture(uId:String) {
        val userId = uId
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {

                    val isAnon=document.getBoolean("isAnon")
                    if(isAnon == true){
                        cbind.userimage.setImageResource(R.drawable.swlogo)
                    }
                    else {
                        val profilePicUrl =
                            document.getString("profilePic") // Fetch profile picture URL from "profilePic" field

                        if (profilePicUrl != null) {
                            // Assuming you are using Glide to load the image into an ImageView (e.g., ubind.profileImageView)
                            Glide.with(this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.swlogo) // Placeholder image
                                .error(R.drawable.swlogo)         // Error image
                                .into(cbind.userimage)                // Update with your ImageView

                        } else {
                            Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error fetching profile picture: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}