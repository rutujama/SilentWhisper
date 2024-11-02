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
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


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
            finishAffinity()
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
                .setMessage("This feature allows you to hide your identity. Turn on the switch to use your anonymous username and a hidden profile picture.")
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

        usersRef.get().addOnSuccessListener { querySnapshot ->
            val usersList = mutableListOf<User>() // Temporary list to hold users

            for (document in querySnapshot) {
                // Retrieve the 'isAnon' field from the document
                val isAnon = document.getBoolean("isAnon") ?: false // Default to false if not present

                // Create the user object
                val user = document.toObject(User::class.java).copy(
                    id = document.id,
                    isAnon = isAnon // Set isAnon based on Firestore value
                )

                // Check if the user ID is not the current user's ID
                if (document.id != currentUserId) {
                    usersList.add(user) // Add the user to the list
                    fadapter.add(UItem(user,this@FriendsPage)) // Add the UserItem to the adapter
                }
            }
            allUsers = usersList // Store all users for filtering
        }.addOnFailureListener { exception ->
            Log.e("AddUsers", "Error getting users: ", exception) // Log errors if any
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
        fadapter.clear() // Clear the current displayed items
        val filteredUsers = allUsers.filter { user ->
            user.username.contains(query, ignoreCase = true) // Filter based on username
        }
        for (user in filteredUsers) {
            fadapter.add(UItem(user,this@FriendsPage)) // Add filtered users to the adapter
        }
    }
}
class UItem(private val user: User,private val context: Context) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // Set the user data to the view
        if(user.isAnon)
        {
            viewHolder.itemView.findViewById<TextView>(R.id.username).text = user.anonusername
            viewHolder.itemView.findViewById<ImageView>(R.id.userimage).setImageResource(R.drawable.swlogo)
        }
        else {
            viewHolder.itemView.findViewById<TextView>(R.id.username).text = user.username
            Glide.with(viewHolder.itemView)
                .load(user.profilePic)
                .into(viewHolder.itemView.findViewById<ImageView>(R.id.userimage))
        }
        viewHolder.itemView.findViewById<LinearLayout>(R.id.userbar).setOnClickListener {
            val intent = Intent(context, ChatPage::class.java)
            intent.putExtra("USER_ID", user.id)
            context.startActivity(intent)
        }
        viewHolder.itemView.findViewById<ImageView>(R.id.userimage).setOnClickListener{
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
        return R.layout.item_container_users
    }
}