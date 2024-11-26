package com.example.silentwhisper
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FriendsPage : AppCompatActivity() {
    private lateinit var fadapter: GroupAdapter<GroupieViewHolder>
    lateinit var fbinder: ActivityFriendsPageBinding
    lateinit var auth: FirebaseAuth
    lateinit var sharedpref: SharedPreferences
    private lateinit var allUsers: List<User>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        fbinder= ActivityFriendsPageBinding.inflate(layoutInflater)
        setContentView(fbinder.root)
        sharedpref = getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)
        val userList = ArrayList<FirebaseUser>()
        val cUser = FirebaseAuth.getInstance().currentUser
        auth=FirebaseAuth.getInstance()
        val loadingDialog = Dialog(this)
        if(isInternetAvailable(this) && cUser!=null)
        {
            setProfilePicture(cUser)
            setAnonstatus(cUser)
        }
        else
        {
            Toast.makeText(this, "Please Check You Internet Connection", Toast.LENGTH_SHORT).show()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, back button is disabled
            }
        })
        fadapter = GroupAdapter<GroupieViewHolder>()
        fbinder.usersRecyclerView.adapter=fadapter
        fUsers()
        fbinder.setting.setOnClickListener{
            startActivity(Intent(this@FriendsPage,MyProfile::class.java))
        }
        fbinder.pullToRefresh.setOnRefreshListener(OnRefreshListener {
            if(cUser!=null)
            {
                setProfilePicture(cUser)
                fadapter.clear()
                fUsers()
            }
        })

        fbinder.addbtn.setOnClickListener{
            startActivity(Intent(this@FriendsPage,AddUsers::class.java))
        }

        fbinder.anonIcon.setOnClickListener {
            val dialog = AlertDialog.Builder(this@FriendsPage)
                .setIcon(getDrawable(R.drawable.anonymous))
                .setTitle("Go Anonymous!!")
                .setMessage("This feature lets you stay anonymous. Enable it to use your anonymous username and a hidden profile picture. To get a random display picture and name, go to Settings.")
                .setCancelable(true)
                .setNegativeButton("Close") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }

        fbinder.anonSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(isInternetAvailable(this)){
                    val userUpdate = hashMapOf(
                        "isAnon" to true
                    )
                    if (cUser != null) {
                        val db=FirebaseFirestore.getInstance()
                        db.collection("users").document(cUser.uid)
                            .update(userUpdate as Map<String, Any>)
                    }
                }
                else{
                    fbinder.anonSwitch.isChecked=false
                    Toast.makeText(this@FriendsPage, "To update Anonymity You require Network", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                if(isInternetAvailable(this)){
                    val userUpdate = hashMapOf(
                        "isAnon" to false
                    )
                    if (cUser != null) {
                        val db=FirebaseFirestore.getInstance()
                        db.collection("users").document(cUser.uid)
                            .update(userUpdate as Map<String, Any>)
                    }
                }
                else{
                    fbinder.anonSwitch.isChecked=true
                    Toast.makeText(this@FriendsPage, "To update Anonymity You require Network", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fbinder.searchfriend.addTextChangedListener{text ->
            if(isInternetAvailable(this@FriendsPage)) {
                filterUsers(text.toString())
            }
        }
        fbinder.notificationBtn.setOnClickListener {
            startActivity(Intent(this@FriendsPage,Notifications_Page::class.java))
        }
    }


    private fun fUsers() {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users") // Reference to the "users" collection
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID
        val chatListRef = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUserId!!) // Reference to the current user's ChatList

        chatListRef.get().addOnSuccessListener { chatListSnapshot ->
            val usersList = mutableListOf<User>() // Temporary list to hold users

            for (chatSnapshot in chatListSnapshot.children) {
                val userId = chatSnapshot.key // The user ID from the ChatList

                if (userId != null && userId != currentUserId) {
                    // Get user details from Firestore
                    usersRef.document(userId).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val isAnon = document.getBoolean("isAnon") ?: false // Default to false if not present
                            val user = document.toObject(User::class.java)?.copy(
                                id = document.id,
                                isAnon = isAnon // Set isAnon based on Firestore value
                            )

                            if (user != null) {
                                // Retrieve last message and time from the ChatList
                                val lastText = chatSnapshot.child("lastText").getValue(String::class.java) ?: "No messages"
                                val lastTextTime = chatSnapshot.child("lastTextTime").getValue(Long::class.java)
                                val newmsg = chatSnapshot.child("newmsg").getValue(Long::class.java) ?: 0L
                                val formattedLastTextTime = if (lastTextTime != null) formatTimestamp(lastTextTime) else ""

                                // Add the user to the adapter with the last message data
                                fadapter.add(UItem(newmsg,user, lastText, formattedLastTextTime, this@FriendsPage))
                                usersList.add(user) // Add the user to the list
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("fUsers", "Error fetching user data for $userId: ", exception)
                    }
                }
            }

            allUsers = usersList // Store all users for filtering (if needed)
        }.addOnFailureListener { exception ->
            Log.e("fUsers", "Error fetching chat list: ", exception)
        }
    }




    fun setAnonstatus(cUser: FirebaseUser){
        if (cUser != null) {
            val userId = cUser.uid // Get the current user's UID

            // Reference to the Firestore document
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Fetch the phone number from the document
                        val isAnon = document.getBoolean("isAnon")
                        if (isAnon != null) {
                            if(isAnon) {
                                fbinder.anonSwitch.isChecked = true
                            }
                            else{
                                fbinder.anonSwitch.isChecked = false
                            }
                        } else {
                            Toast.makeText(this, "Anonymous status not found", Toast.LENGTH_SHORT).show()
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

    fun setProfilePicture(cUser: FirebaseUser) {
        if (cUser != null) {
            val userId = cUser.uid // Get the current user's UID

            // Reference to the Firestore document
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Fetch the profile picture URL from the document
                        val profilePicUrl = document.getString("profilePic")

                        if (profilePicUrl != null) {
                            fbinder.myprofileicon.setImageDrawable(null) // Clear the previous image
                            Glide.with(this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.swlogo)
                                .error(R.drawable.swlogo)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(fbinder.myprofileicon)

                        } else {
                            Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                    // Stop the refresh action
                    fbinder.pullToRefresh.isRefreshing = false
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
                    // Stop the refresh action even if there is an error
                    fbinder.pullToRefresh.isRefreshing = false
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            // Stop the refresh action if the user is not logged in
            fbinder.pullToRefresh.isRefreshing = false
        }
    }
    private fun filterUsers(query: String) {
        val filteredList = mutableListOf<User>() // List to hold the filtered users

        // Loop through all the users and apply the filtering criteria
        for (user in allUsers) {
            // Check if the user matches the filter criteria based on username
            val usernameMatches = user.username?.contains(query, ignoreCase = true) == true

            // Check if the last message matches the filter criteria (optional)
            val lastMessageMatches = user.lastText?.contains(query, ignoreCase = true) == true

            // Combine the conditions (username or last message)
            if (usernameMatches || lastMessageMatches) {
                filteredList.add(user) // Add user to the filtered list if it matches
            }
        }

        // Update the adapter with the filtered list of users
        updateAdapter(filteredList)
    }

    private fun updateAdapter(filteredUsers: List<User>) {
        fadapter.clear() // Clear the current adapter
        // Add the filtered users to the adapter
        for (user in filteredUsers) {
            fadapter.add(UItem(0L,user, user.lastText, user.lastTextTime, this@FriendsPage)) // Directly pass lastTextTime
        }
        fadapter.notifyDataSetChanged() // Notify the adapter to refresh the data
    }

    private fun formatTimestamp(timestamp: Long?): String {
        if (timestamp != null) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()) // Format as HH:mm (24-hour format)
            return sdf.format(Date(timestamp)) // Format the timestamp and return as a string
        }
        return "No time" // Return default string if timestamp is null
    }
}
class UItem(
    private val newmsg: Long,
    private val user: User,
    private val lastText: String? = null, // Optional: last message content
    private val lastTextTime: String? = null, // Optional: formatted last message time
    private val context: Context

) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        if ((newmsg ?: 0L) != 0L) {
            viewHolder.itemView.findViewById<TextView>(R.id.newmsgcount).visibility=View.VISIBLE
            viewHolder.itemView.findViewById<TextView>(R.id.newmsgcount).setText(newmsg.toString())
        }
        // Set the user data to the view
        if (user.isAnon) {
            viewHolder.itemView.findViewById<TextView>(R.id.username).text = user.anonusername
            viewHolder.itemView.findViewById<ImageView>(R.id.userimage).setImageResource(R.drawable.swlogo)
        } else {
            viewHolder.itemView.findViewById<TextView>(R.id.username).text = user.username
            Glide.with(viewHolder.itemView)
                .load(user.profilePic)
                .into(viewHolder.itemView.findViewById<ImageView>(R.id.userimage))
        }

        // Display the last message and time if available
        viewHolder.itemView.findViewById<TextView>(R.id.recentText).text = lastText ?: "No messages"
        viewHolder.itemView.findViewById<TextView>(R.id.last_time).text = lastTextTime ?: ""

        viewHolder.itemView.findViewById<LinearLayout>(R.id.userbar).setOnClickListener {
            val intent = Intent(context, ChatPage::class.java)
            intent.putExtra("USER_ID", user.id)
            context.startActivity(intent)
        }

        viewHolder.itemView.findViewById<ImageView>(R.id.userimage).setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dp_view_dialog)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(true)
            Glide.with(viewHolder.itemView)
                .load(user.profilePic)
                .circleCrop()
                .into(dialog.findViewById<ImageView>(R.id.dp_view))
            dialog.show()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_container_users // Your existing layout
    }
}
