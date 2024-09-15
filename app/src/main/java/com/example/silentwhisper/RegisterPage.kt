package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.example.silentwhisper.databinding.ActivityRegisterPageBinding
import com.google.firebase.auth.FirebaseAuth


class RegisterPage : AppCompatActivity() {
    lateinit var bind:ActivityRegisterPageBinding
    lateinit var auth:FirebaseAuth
    var eyesopen1:Boolean=false
    var eyesopen2:Boolean=false
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
                            Toast.makeText(this,"Registration Successful",Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LogIn::class.java)
                            startActivity(intent)
                        }
                        else
                        {
//                            Log.e(it.exception.toString())
                            Toast.makeText(this,"Error Occurred" , Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                else
                {
                    Toast.makeText(this, "Password didn't match", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Empty fields not allowed", Toast.LENGTH_SHORT).show()
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

    fun passwordhide(view: View,num: Int)
    {
        if(num==1) {
            if (view is ImageButton && eyesopen1 == false) {
                view.setImageResource(R.drawable.openeye)
                eyesopen1 = true
                bind.passbox.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else if (view is ImageButton && eyesopen1 == true) {
                view.setImageResource(R.drawable.closeeye)
                eyesopen1 = false
                bind.passbox.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        else{
            if (view is ImageButton && eyesopen2 == false) {
                view.setImageResource(R.drawable.openeye)
                eyesopen2 = true
                bind.confirmpassbox.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else if (view is ImageButton && eyesopen2 == true) {
                view.setImageResource(R.drawable.closeeye)
                eyesopen2 = false
                bind.confirmpassbox.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }
}