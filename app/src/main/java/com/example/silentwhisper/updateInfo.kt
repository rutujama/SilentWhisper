package com.example.silentwhisper

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.silentwhisper.databinding.ActivityUpdateInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.ArrayList

class updateInfo : AppCompatActivity() {

    lateinit var ubind : ActivityUpdateInfoBinding
    private lateinit var imageUri : Uri

    private val contract=registerForActivityResult(ActivityResultContracts.GetContent())
    {
        findViewById<ImageView>(R.id.newdp).setImageURI(it)
        if (it != null) {
            imageUri=it
        }
    }
    private val camContract=registerForActivityResult(ActivityResultContracts.TakePicture())
    {
        findViewById<ImageView>(R.id.newdp).setImageURI(null)
        findViewById<ImageView>(R.id.newdp).setImageURI(imageUri)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ubind= ActivityUpdateInfoBinding.inflate(layoutInflater)
        setContentView(ubind.root)
        imageUri=createImageUri()
        val curruser=FirebaseAuth.getInstance().currentUser
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        if (curruser != null) {
            setUsername(curruser)
            setAnonUsername(curruser)
            setProfilePicture(curruser)
        }
        ubind.savebtn.setOnClickListener{
            ubind.savebtn.isClickable=false
            if(!ubind.newUsername.text.toString().isEmpty()) {
                    uploadtoFirestore(imageUri)
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

        ubind.dpbtn.setOnClickListener {
            if(requestpermission()) {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.activity_dp_dialog)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.findViewById<LinearLayout>(R.id.camerabox).setOnClickListener {
                    camContract.launch(imageUri)
                    dialog.dismiss()
                }
                dialog.findViewById<LinearLayout>(R.id.gallerybtn).setOnClickListener {
                    contract.launch("image/*")
                    dialog.dismiss()
                }
                dialog.show()
            }
            else{
                Toast.makeText(this, "Oops Your Need To allow all permissions to access this feature!!", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun uploadtoFirestore(photoUri: Uri){
        val curruser=FirebaseAuth.getInstance().currentUser
        val currUserId= curruser?.uid
        val photoRef=FirebaseStorage.getInstance()
            .reference
            .child("profilePic/"+currUserId)
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener {
                    postToFirestore(it.toString())
                }
            }
    }

    fun postToFirestore(url: String)
    {
        val curruser=FirebaseAuth.getInstance().currentUser
        if (curruser != null) {
            val userId = curruser.uid
            val db = FirebaseFirestore.getInstance()
            val userUpdate = hashMapOf(
                "profilePic" to url
            )
            db.collection("users").document(userId).update(userUpdate as Map<String, Any>)
        }
    }
    private fun createImageUri():Uri{
        val image = File(filesDir,"SilentWhisper"+System.currentTimeMillis()/1000+".png")
        return FileProvider.getUriForFile(this,
            "com.example.silentwhisper.FileProvider",
            image)
    }

    fun permissionlist(): ArrayList<String>
    {
        var listofpermissions= ArrayList<String>()
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            listofpermissions.add(Manifest.permission.CAMERA)
        }
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED)
        {
            listofpermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        return listofpermissions
    }

    fun requestpermission():Boolean{
        var listofpermissions =permissionlist()
        if(listofpermissions.isEmpty()) {
            return true
        }
        else{
            ActivityCompat.requestPermissions(this,listofpermissions.toTypedArray(),1)
            listofpermissions=permissionlist()
            if(permissionlist().isEmpty()){
                return true
            }
            else{
                return false
            }
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
                        val profilePicUrl = document.getString("profilePic") // Fetch profile picture URL from "profilePic" field

                        if (profilePicUrl != null) {
                            // Assuming you are using Glide to load the image into an ImageView (e.g., ubind.profileImageView)
                            Glide.with(this)
                                .load(profilePicUrl)
                                .placeholder(R.drawable.swlogo) // Placeholder image
                                .error(R.drawable.swlogo)         // Error image
                                .into(ubind.newdp)                // Update with your ImageView

                        } else {
                            Toast.makeText(this, "Profile picture not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
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