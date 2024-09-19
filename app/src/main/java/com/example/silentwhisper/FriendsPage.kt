package com.example.silentwhisper

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import android.view.ViewParent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import com.example.silentwhisper.databinding.ActivityFriendsPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.auth.User


class FriendsPage(val context: Context,val  userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter,UserViewHolder>{
override fun onCreateViewholder(parent: ViewGroup, viewType: Int):UserViewHolder{

}
    override fun onBindViewHolder(holder: UserViewHolder,position:Int)
    {

    }
    override fun getItemCount():Int{

    }
    class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }
}





class FriendsPage : AppCompatActivity() {
    lateinit var fbinder: ActivityFriendsPageBinding
    lateinit var auth: FirebaseAuth
    lateinit var sharedpref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        fbinder= ActivityFriendsPageBinding.inflate(layoutInflater)
        setContentView(fbinder.root)
        sharedpref = getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)

        val cUser = FirebaseAuth.getInstance().currentUser
        auth=FirebaseAuth.getInstance()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, back button is disabled
            }
        })
        if (cUser != null) {
            setUsername(cUser)
        }

        fbinder.myprofilebtn.setOnClickListener{
            startActivity(Intent(this@FriendsPage,MyProfile::class.java))
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
                            fbinder.hibox.setText("Hey $username") // Update this to match the UI element
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



}