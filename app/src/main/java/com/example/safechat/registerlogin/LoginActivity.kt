package com.example.safechat.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.safechat.databinding.ActivityLoginBinding
import com.example.safechat.messages.LatestMessagesActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        mAuth= Firebase.auth
        binding=ActivityLoginBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

          binding.loginButton.setOnClickListener {
              login()
          }

        binding.textviewBackRegister.setOnClickListener {
            finish()
        }
    }
    private fun login(){
        val email=binding.emailEdittextLog.text.toString()
        val pass=binding.passwordEdittextLog.text.toString()

        if(email.isEmpty() || pass.isEmpty()){
            Toast.makeText(this,"Enter email and password",Toast.LENGTH_LONG).show()
           return
        }
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
            if(!it.isSuccessful)
                return@addOnCompleteListener
               val intent= Intent(this,LatestMessagesActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Log.d("Login","User is logged in with uid ${it.result.user!!.uid}")

        }
            .addOnFailureListener {
                Log.d("Login","Not able to login ${it.message}")
            }
    }
}