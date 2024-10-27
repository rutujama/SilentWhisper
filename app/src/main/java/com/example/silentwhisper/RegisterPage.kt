package com.example.silentwhisper

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityRegisterPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class RegisterPage : AppCompatActivity() {
    lateinit var bind:ActivityRegisterPageBinding
    lateinit var auth:FirebaseAuth
    var eyesopen1:Boolean=false
    var eyesopen2:Boolean=false
    var allotted_username:String="Random_Guy"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind=ActivityRegisterPageBinding.inflate(layoutInflater)
        setContentView(bind.root)
        bind.existbtn.setOnClickListener{
            startActivity(Intent(this@RegisterPage,LogIn::class.java))
            finish()
        }
        auth=FirebaseAuth.getInstance()
        bind.signinbtn.setOnClickListener{
            if(isInternetAvailable(this)) {
                bind.signinbtn.isClickable = false
                val email = bind.emailbox.text.toString()
                val passreg = bind.passbox.text.toString()
                val confirmpass = bind.confirmpassbox.text.toString()
                val phonenumber = bind.mobilenumbox.text.toString()
                if (email.isNotEmpty() && passreg.isNotEmpty() && confirmpass.isNotEmpty()&& phonenumber.isNotEmpty()) {
                    if (passreg == confirmpass) {
                        val loadingDialog = Dialog(this)
                        loadingDialog.setContentView(R.layout.loadingscreen)
                        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        loadingDialog.setCancelable(false)
                        loadingDialog.show()
                        auth.createUserWithEmailAndPassword(email, passreg).addOnCompleteListener {
                            loadingDialog.dismiss()
                            if (it.isSuccessful) {
                                getAnonName()
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null) {
                                    val userId = currentUser.uid
                                    val db = FirebaseFirestore.getInstance()
                                    val user = hashMapOf(
                                        "Email" to bind.emailbox.text.toString(),
                                        "MobileNum" to bind.mobilenumbox.text.toString(),
                                        "anonusername" to allotted_username,
                                        "isAnon" to false,
                                        "profilePic" to "gs://silentwhisperdb.appspot.com/profilePic/swlogo.png"
                                    )
                                    db.collection("users").document(userId).set(user)
                                }
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT)
                                    .show()
                                bind.signinbtn.isClickable = true
                                val intent = Intent(this, LogIn::class.java)
                                startActivity(intent)
                            } else {
                                Log.e("TAG", it.exception.toString())
                                Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
                                bind.signinbtn.isClickable = true
                            }
                        }

                    } else {
                        Toast.makeText(this, "Password didn't match", Toast.LENGTH_SHORT).show()
                        bind.signinbtn.isClickable = true
                    }
                } else {
                    bind.signinbtn.isClickable = true
                    Toast.makeText(this, "Empty fields not allowed", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT)
                    .show()
            }

        }
        
        //PASSWORD HIDE OR SHOW FUNCTIONALITY
        bind.openeyebtn1.setOnClickListener{
            passwordhide(it,1)
        }
        bind.openeyebtn2.setOnClickListener{
            passwordhide(it,2)
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


    fun getAnonName()
    {
        val client= OkHttpClient()
        val url="https://usernameapiv1.vercel.app/api/random-usernames"
        val request= Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@RegisterPage,"Error Fetching Username",Toast.LENGTH_SHORT)
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    response.body?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody.string())
                        val usernamesArray = jsonObject.getJSONArray("usernames")
                        val randomUsername = usernamesArray.getString(0)
                        allotted_username=randomUsername
                    }
                }
            }

        })
    }
    fun passwordhide(view: View, num: Int) {
        if (num == 1) {
            val cursorPosition = bind.passbox.selectionStart

            if (view is ImageButton && !eyesopen1) {
                view.setImageResource(R.drawable.openeye)
                eyesopen1 = true
                bind.passbox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else if (view is ImageButton && eyesopen1) {
                view.setImageResource(R.drawable.closeeye)
                eyesopen1 = false
                bind.passbox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            bind.passbox.setSelection(cursorPosition)

        } else {
            val cursorPosition = bind.confirmpassbox.selectionStart

            if (view is ImageButton && !eyesopen2) {
                view.setImageResource(R.drawable.openeye)
                eyesopen2 = true
                bind.confirmpassbox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else if (view is ImageButton && eyesopen2) {
                view.setImageResource(R.drawable.closeeye)
                eyesopen2 = false
                bind.confirmpassbox.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            bind.confirmpassbox.setSelection(cursorPosition)
        }
    }

}