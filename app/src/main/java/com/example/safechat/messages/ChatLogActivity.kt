package com.example.safechat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class ChatLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatLogBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
     var toUser:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_chat_log)
        binding= ActivityChatLogBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
           database=Firebase.database
        mAuth=Firebase.auth
        setSupportActionBar(binding.toolbar)

       toUser=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        binding.toolbar.setTitle(toUser!!.username)


//      setupDummymessages()
        listenMessages()
        button_send.setOnClickListener {
            Log.d("ChatLog","Trying to send message ...")
            performessage()
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
            if(chatMessage!=null) {
                if (chatMessage.fromid == mAuth.uid) {

                    adapter.add(ChatToitem(chatMessage.text,currentuser!!))
                }
                else{
                    adapter.add(ChatFromitem(chatMessage.text,toUser!!))
                }

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
        binding.recylerviewChatLog.scrollToPosition(adapter.itemCount - 1)

    }

       private fun performessage(){
           //sending message to firebase
           //.push() creates a new node in the messages folder
       val text=  binding.edittextEnterMessage.text.toString()

           //getting the toId using parcelable and putextra method of Intent
           //val user=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromid=mAuth.uid
       if (fromid==null)
           return
        val toid=toUser!!.uid

//        val ref=database.getReference("/messages").push()
           val ref=database.getReference("/user-messages/$fromid/$toid").push()
           val toref=database.getReference("/user-messages/$toid/$fromid").push()



         val chatmessage= ChatMessage(ref.key!!,text,fromid,toid,System.currentTimeMillis()/1000)

            ref.setValue(chatmessage)
                .addOnSuccessListener {
                    Log.d("Chatlog","Saved message successfully ${ref.key}")
                   binding.edittextEnterMessage.text.clear()
                   binding.recylerviewChatLog.scrollToPosition(adapter.itemCount-1)
                }
                .addOnFailureListener {
                    Log.d("Chatlog","Message not saved ${ref.key}")
                }
           toref.setValue(chatmessage)
               .addOnSuccessListener {
                   
               }

          val latestmessageRef=database.getReference("/latest-messages/$fromid/$toid")
               latestmessageRef.setValue(chatmessage)
           val latestmessagetoRef=database.getReference("/latest-messages/$toid/$fromid")
              latestmessagetoRef.setValue(chatmessage)

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