package com.example.silentwhisper


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.silentwhisper.R // Make sure to import your package R file
import com.google.firebase.auth.FirebaseUser

// Adapter Class
class UserAdapter(private val context: Context, private val userList: ArrayList<FirebaseUser>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ViewHolder Class
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName: TextView = itemView.findViewById(R.id.friend_name)
        val friendAvatar: ImageView = itemView.findViewById(R.id.friend_avatar)
    }

    // Create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return UserViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.friendName.text = user.displayName ?: "Unknown" // Use displayName or default to "Unknown"

        // Set avatar or other details here if needed
        // For now, just using a placeholder
    }

    // Number of items in RecyclerView
    override fun getItemCount(): Int {
        return userList.size
    }
}
