package com.example.safechat.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts


import com.example.safechat.databinding.ActivityRegisterBinding
import com.example.safechat.messages.LatestMessagesActivity
import com.example.safechat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*


class RegisterActivity : AppCompatActivity() {
private lateinit var binding: ActivityRegisterBinding
private lateinit var mAuth: FirebaseAuth
private lateinit var imagestorage:FirebaseStorage
private lateinit var database:FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_register)
        binding= ActivityRegisterBinding.inflate(layoutInflater)
        mAuth= Firebase.auth
        val view=binding.root
        setContentView(view)
        binding.registerButton.setOnClickListener {
            register()
        }
        binding.textviewAlreadyAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val start= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode==Activity.RESULT_OK)
                dosomething(0,it.data)
        }

        binding.profileImage.setOnClickListener {

          val intent= Intent(Intent.ACTION_PICK)
          intent.type="image/*"
          start.launch(intent)
      }

    }
    var selectedphotourl:Uri? = null
   private fun dosomething(requestcode:Int,data:Intent?){

       if(requestcode==0 && data!=null){
        Log.d("Register","Image is selected")

         selectedphotourl = data.data
           val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,selectedphotourl)

            binding.circleProfileImage.setImageBitmap(bitmap)
           binding.profileImage.alpha=0f
       //           val bitmapDrawable=BitmapDrawable(bitmap)
//           profile_image.setBackgroundDrawable(bitmapDrawable)
       }
   }
    private fun register(){
        val email = binding.emailEdittextReg.text.toString()
        val pass= binding.passwordEdittextReg.text.toString()
        Log.d("User","Email is $email")
        Log.d("Pass","Password is $pass")
        if(email.isEmpty()|| pass.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_LONG).show()
            return
        }
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
            if (!it.isSuccessful)
                return@addOnCompleteListener
              uploadimage()
            //else if successful
            Log.d("Register","Done with user uid : ${ it.result.user!!.uid}")
        }
            .addOnFailureListener {
                Log.d("Register","Failed to register user : ${it.message}")
            }

    }
    private fun uploadimage(){
        if(selectedphotourl==null)
            return
        val filename=UUID.randomUUID().toString()
      imagestorage=Firebase.storage
        val ref= imagestorage.getReference("/images/$filename")
        ref.putFile(selectedphotourl!!)
            .addOnSuccessListener {
                Log.d("Register","Upload image success : ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("Register","File:Location $it")
                    saveToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                //add some code
            }
    }
    private fun saveToFirebaseDatabase(profileimageurl: String){
     database=Firebase.database
        val uid = mAuth.uid?:""
        val ref= database.getReference("/users/$uid")

         val user= User(uid,binding.usernameEdittextReg.text.toString(),profileimageurl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register","Success in registering user")
               val intent = Intent(this, LatestMessagesActivity::class.java)
                //this line clears all the activities before
                //for ex:- register activity
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                //add some log or code
            }
    }
}

