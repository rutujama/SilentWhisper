package com.example.silentwhisper

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


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
        fbinder.setting.setOnClickListener{
            startActivity(Intent(this@FriendsPage,MyProfile::class.java))
            finishAffinity()
        }
        fbinder.pullToRefresh.setOnRefreshListener(OnRefreshListener {
            if(cUser!=null)
            {
                setProfilePicture(cUser)
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


}