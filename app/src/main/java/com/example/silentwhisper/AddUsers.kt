package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.silentwhisper.R
import com.example.silentwhisper.databinding.ActivityAddUsersBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

lateinit var abind:ActivityAddUsersBinding
class AddUsers : AppCompatActivity() {
    private lateinit var adapter: GroupAdapter<GroupieViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        abind = ActivityAddUsersBinding.inflate(layoutInflater)
        setContentView(abind.root)

        abind.backbtn.setOnClickListener {
            startActivity(Intent(this@AddUsers, FriendsPage::class.java))
            finish()
        }

        // Initialize the adapter
        adapter = GroupAdapter<GroupieViewHolder>()
        fetchUsers()
        abind.addRecyclerView.adapter = adapter // Set the adapter to RecyclerView // Fetch users
    }

    private fun fetchUsers() {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users") // Reference to the "users" collection
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        usersRef.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val user = document.toObject(User::class.java) // Convert document to User object

                // Check if the user ID is not the current user's ID
                if (document.id != currentUserId) {
                    adapter.add(UserItem(user)) // Add the UserItem to the adapter
                }
            }
            abind.addRecyclerView.adapter = adapter // Set the adapter to RecyclerView
        }.addOnFailureListener { exception ->
            Log.e("AddUsers", "Error getting users: ", exception) // Log errors if any
        }
    }
}


class UserItem(private val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // Set the user data to the view
        viewHolder.itemView.findViewById<TextView>(R.id.container_name).text = user.username
        // Optionally load the profile picture using Glide or another image loading library
        Glide.with(viewHolder.itemView)
            .load(user.profilePic)
            .into(viewHolder.itemView.findViewById<ImageView>(R.id.container_image))
    }

    override fun getLayout(): Int {
        return R.layout.newuser_container
    }
}
