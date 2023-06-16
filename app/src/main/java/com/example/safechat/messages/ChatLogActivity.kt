package com.example.safechat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.safechat.NewMessageActivity
import com.example.safechat.R
import com.example.safechat.databinding.ActivityChatLogBinding
import com.example.safechat.models.ChatMessage
import com.example.safechat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from.view.*
import kotlinx.android.synthetic.main.chat_to.view.*
import javax.crypto.Cipher
import javax.crypto.Cipher.SECRET_KEY
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class ChatLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatLogBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    val SECRET_KEY = "0123456789abcdef"
    val SECRET_IV = "0123456789abcdef"

     var toUser:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChatLogBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
           database=Firebase.database
        mAuth=Firebase.auth
        setSupportActionBar(binding.toolbar)
       toUser=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        binding.toolbar.setTitle(toUser!!.username)

        binding.edittextEnterMessage.setOnClickListener {
            binding.recylerviewChatLog.scrollToPosition(adapter.itemCount-1)
            Log.d("Scrolled", "Edittext Scrolled")
        }

        listenMessages()

        button_send.setOnClickListener {
            Log.d("ChatLog", "Trying to send message ...")
            if (binding.edittextEnterMessage.text.toString() == "")
                Toast.makeText(this, "Please enter message", Toast.LENGTH_LONG).show()
            else {
                performessage()

                //for testing encryption and decryption
//                val user= "Hello how are youu"
//                val user2=  user.encryptCBC()
//                val user3=user2.decryptCBC()
//
//                Log.d("Encryption","Encyrpted $user2 , ${user2.length}")
//
//                Log.d("Decryption","Decyrpted $user3 , ${user3.length}")
//                val user11= "Who are you "
//                val user12=  "ehT1WVI/guzQy7TeZ97jVQ=="
//                val user13=user12.decryptCBC()
//
//                Log.d("Encryption","Encyrpted $user12, ${user12.length}")
//
//                Log.d("Decryption","Decyrpted $user13, ${user13.length}")
            }
        }
    }
    val adapter=GroupAdapter<GroupieViewHolder>()
    private fun listenMessages(){
        val fromid=mAuth.uid
        val toid=toUser!!.uid

        val ref=database.getReference("/user-messages/$fromid/$toid")
        binding.recylerviewChatLog.adapter=adapter
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)

                val currentuser=LatestMessagesActivity.currentuser
            if(chatMessage!=null ) {

                if (chatMessage.fromid == mAuth.uid) {
                    val decrypt=chatMessage.text.substring(0,chatMessage.text.length-1).decryptCBC()
                    adapter.add(ChatToitem(decrypt,currentuser!!))
                    Log.d("Added","Chat added")
                }
                else{
                    adapter.add(ChatFromitem(chatMessage.text,toUser!!))
                   }
                binding.recylerviewChatLog.scrollToPosition(adapter.itemCount - 1)
                Log.d("Scrolled", "Scrolled")
              }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

             }

        })

    }

       private fun performessage(){
           //sending message to firebase
           //.push() creates a new node in the messages folder
//       val text=  binding.edittextEnterMessage.text.toString().encryptCBC()

           val chats=binding.edittextEnterMessage.text.toString().encryptCBC()

           Log.d("Encrypt","Chat $chats")
           //getting the toId using parcelable and putextra method of Intent
           //val user=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromid=mAuth.uid
       if (fromid==null)
           return
        val toid=toUser!!.uid

//        val ref=database.getReference("/messages").push()
           val ref=database.getReference("/user-messages/$fromid/$toid").push()
           val toref=database.getReference("/user-messages/$toid/$fromid").push()

         val chatmessage= ChatMessage(ref.key!!,chats,fromid,toid,System.currentTimeMillis()/1000)

              ref.setValue(chatmessage)
                   .addOnSuccessListener {
                       Log.d("Chatlog", "Saved message successfully ${ref.key}")
                       binding.edittextEnterMessage.text.clear()
                       binding.recylerviewChatLog.scrollToPosition(adapter.itemCount - 1)
                   }
                   .addOnFailureListener {
                       Log.d("Chatlog", "Message not saved ${ref.key}")
                   }
               toref.setValue(chatmessage)
                   .addOnSuccessListener {

                   }

               val latestmessageRef = database.getReference("/latest-messages/$fromid/$toid")
               latestmessageRef.setValue(chatmessage)
               val latestmessagetoRef = database.getReference("/latest-messages/$toid/$fromid")
               latestmessagetoRef.setValue(chatmessage)

       }
    private fun String.encryptCBC(): String {
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val crypted = cipher.doFinal(this.toByteArray())
        val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
        return String(encodedByte)
    }
    private fun String.decryptCBC(): String {
        val decodedByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }
}

class ChatFromitem(val text:String,val user: User):Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
              viewHolder.itemView.frommessage.text=text
        val uri=user.profileimageurl
        val targetview=viewHolder.itemView.fromchatimage
        Picasso.get().load(uri).into(targetview)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from
    }
}
class ChatToitem(val text:String,val user: User):Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
              viewHolder.itemView.tomessage.text=text
         val uri=user.profileimageurl
        val targetview=viewHolder.itemView.tochatimage
        Picasso.get().load(uri).into(targetview)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to
    }
}