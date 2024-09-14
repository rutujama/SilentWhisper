package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityRegisterPageBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class RegisterPage : AppCompatActivity() {
    lateinit var bind:ActivityRegisterPageBinding
    lateinit var auth:FirebaseAuth
//    lateinit var emailinputedittext:TextInputEditText
//    lateinit var passinputedittext:TextInputEditText

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
            val email = bind.emailbox.text.toString()
            val passreg = bind.passbox.text.toString()
            val confirmpass = bind.confirmpassbox.text.toString()
            if (email.isNotEmpty() && passreg.isNotEmpty() && confirmpass.isNotEmpty())
            {
                if (passreg == confirmpass)
                {
                    auth.createUserWithEmailAndPassword(email, passreg).addOnCompleteListener {
                        if (it.isSuccessful)
                        {
                            val intent = Intent(this, RegisterPage::class.java)
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
                    Toast.makeText(this, "Password not matching", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Empty fields not allowed", Toast.LENGTH_SHORT).show()
            }
        }

    }
}