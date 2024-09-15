package com.example.silentwhisper

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth


lateinit var binder:ActivityLogInBinding

class LogIn : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    var eyesopen:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        binder = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binder.root)
        binder.newuserbtn.setOnClickListener {
            startActivity(Intent(this@LogIn, RegisterPage::class.java))
            finish()
        }
        auth=FirebaseAuth.getInstance()

        binder.loginbtn.setOnClickListener{
            val email = binder.etemail.text.toString()
            val passreg = binder.etpass.text.toString()

            if (email.isNotEmpty() && passreg.isNotEmpty() )
            {

                    auth.signInWithEmailAndPassword(email, passreg).addOnCompleteListener {
                        if (it.isSuccessful)
                        {
                            val intent = Intent(this, PermissionsPage::class.java)
                            startActivity(intent)
                        }
                        else
                        {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            else
            {
                Toast.makeText(this, "Empty fields not allowed", Toast.LENGTH_SHORT).show()
            }
        }

        binder.eyebtn.setOnClickListener{
            passwordhide(it)
        }

    }

    fun passwordhide(view: View)
    {
        if(view is ImageButton && eyesopen==false)
        {
            view.setImageResource(R.drawable.openeye)
            eyesopen=true
            binder.etpass.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        else if(view is ImageButton && eyesopen==true){
            view.setImageResource(R.drawable.closeeye)
            eyesopen=false
            binder.etpass.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }
}

