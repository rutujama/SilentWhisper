package com.example.silentwhisper

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.example.silentwhisper.databinding.ActivityChatPageBinding
import com.example.silentwhisper.models.Chat
import com.example.silentwhisper.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar

class ChatPage : AppCompatActivity() {
    lateinit var cbind : ActivityChatPageBinding
    var firebaseuser:FirebaseUser ?= null
    lateinit var cadapter:GroupAdapter<GroupieViewHolder>
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
        firebaseuser=FirebaseAuth.getInstance().currentUser

        cadapter = GroupAdapter<GroupieViewHolder>()
        cbind.chatRecyclerView.adapter=cadapter
        fetchChats(firebaseuser!!.uid, userId)


        cbind.msget.setOnClickListener{
            cbind.chatRecyclerView.scrollToPosition(cadapter.itemCount-1)
        }
        cbind.backbtn.setOnClickListener {
            startActivity(Intent(this@ChatPage,FriendsPage::class.java))
            finish()
        }
        cbind.sendbtn.setOnClickListener(){
            val message=cbind.msget.text.toString()
            if(message.isEmpty()==false)
            {
                   if(isInternetAvailable(this@ChatPage)) {
                       sendmsg(firebaseuser!!.uid, userId, message)
                       cbind.msget.setText("")
                   }
                   else{
                       Toast.makeText(this, "Please check your Network!", Toast.LENGTH_SHORT).show()
                   }
            }
        }
    }

    private fun fetchChats(sId: String, rId: String) {
        if (!isInternetAvailable(this@ChatPage)) {
            Toast.makeText(this, "Please check your connection", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("Chats")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cadapter.clear() // Clear adapter to avoid duplicate entries

                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    if (chat != null) {
                        // Filter messages between the sender and receiver
                        if ((chat.getsender() == sId && chat.getreceiver() == rId) ||
                            (chat.getsender() == rId && chat.getreceiver() == sId)) {

                            // Determine if message is sent by the current user
                            val isCurrentUserSender = chat.getsender() == currentUserId
                            val messageText = chat.getmessage()
                            val time= chat.gettime()

                            // Add chat item to the adapter with flag for message alignment
                            cadapter.add(chatitem(isCurrentUserSender, messageText,time))
                        }
                    }
                }
                cbind.chatRecyclerView.scrollToPosition(cadapter.itemCount-1)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Error fetching chats: ${error.message}")
            }
        })
    }


    fun sendmsg(sId: String, rId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key ?: return
        val msgHashMap = HashMap<String, Any?>()

        msgHashMap["sender"] = sId
        msgHashMap["message"] = message
        msgHashMap["receiver"] = rId
        msgHashMap["isseen"] = false
        msgHashMap["url"] = ""
        msgHashMap["messageId"] = messageKey
        msgHashMap["time"] = SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)

        // Save message to Chats node
        reference.child("Chats").child(messageKey).setValue(msgHashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Update sender's ChatList
                val senderChatListRef = FirebaseDatabase.getInstance().getReference("ChatList").child(sId).child(rId)
                senderChatListRef.child("lastText").setValue(message)
                senderChatListRef.child("lastTextTime").setValue(System.currentTimeMillis())

                // Update receiver's ChatList
                val receiverChatListRef = FirebaseDatabase.getInstance().getReference("ChatList").child(rId).child(sId)
                receiverChatListRef.child("lastText").setValue(message)
                receiverChatListRef.child("lastTextTime").setValue(System.currentTimeMillis())

                // Ensure the sender's chat list also has the "id" field if not already set
                senderChatListRef.child("id").setValue(rId)

                // Ensure the receiver's chat list also has the "id" field if not already set
                receiverChatListRef.child("id").setValue(sId)
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
class chatitem(val flag:Boolean,val currtext:String,val time:String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
           viewHolder.itemView.findViewById<TextView>(R.id.texthere).setText(currtext)
           viewHolder.itemView.findViewById<TextView>(R.id.currtime).setText(time)
    }
    override fun getLayout(): Int {
        if(flag==false) {
            return R.layout.message_left
        }
        else{
            return R.layout.message_right
        }
    }

}