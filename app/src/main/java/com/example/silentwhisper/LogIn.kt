package com.example.silentwhisper

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Binder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import com.example.silentwhisper.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth


lateinit var binder:ActivityLogInBinding

class LogIn : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    var eyesopen:Boolean=false
    lateinit var sharedpref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binder.root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, back button is disabled
            }
        })
        sharedpref = getSharedPreferences("hasAccepted", Context.MODE_PRIVATE)
        binder.newuserbtn.setOnClickListener {
            startActivity(Intent(this@LogIn, RegisterPage::class.java))
        }
        auth=FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val sharedflag= sharedpref.getBoolean("Accepted",false)
            if(!sharedflag) {
                val intent = Intent(this, PermissionsPage::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent=Intent(this@LogIn,FriendsPage::class.java)
                startActivity(intent)
                finish()
            }
        }

        binder.loginbtn.setOnClickListener {
            if (isInternetAvailable(this)) {
                val email = binder.etemail.text.toString()
                val passreg = binder.etpass.text.toString()
                if (email.isNotEmpty() && passreg.isNotEmpty()) {
                    val loadingDialog = Dialog(this)
                    loadingDialog.setContentView(R.layout.loadingscreen)
                    loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    loadingDialog.setCancelable(false)
                    loadingDialog.show()
                    auth.signInWithEmailAndPassword(email, passreg).addOnCompleteListener {
                        loadingDialog.dismiss()
                        if (it.isSuccessful) {
                            val intent = Intent(this, PermissionsPage::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Empty fields not allowed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Check Your Internet Connection!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binder.eyebtn.setOnClickListener{
            passwordhide(it)
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
    fun passwordhide(view: View) {
        val cursorPosition = binder.etpass.selectionStart

        if (view is ImageButton && !eyesopen) {
            view.setImageResource(R.drawable.openeye)
            eyesopen = true
            binder.etpass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else if (view is ImageButton && eyesopen) {
            view.setImageResource(R.drawable.closeeye)
            eyesopen = false
            binder.etpass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binder.etpass.setSelection(cursorPosition)
    }
}

