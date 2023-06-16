package com.example.safechat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.safechat.NewMessageActivity
import com.example.safechat.NewMessageActivity.Companion.USER_KEY
import com.example.safechat.R
import com.example.safechat.databinding.ActivityLatestMessagesBinding
import com.example.safechat.models.ChatMessage
import com.example.safechat.models.User
import com.example.safechat.registerlogin.RegisterActivity
import com.example.safechat.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_login.toolbar
import kotlinx.android.synthetic.main.latest_message_row.view.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class LatestMessagesActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityLatestMessagesBinding
    companion object{
        var currentuser: User?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
   //     setContentView(R.layout.activity_latest_messages)
        binding=ActivityLatestMessagesBinding.inflate(layoutInflater)
        binding.recyclerviewLatestMessages.adapter=adapter
        binding.recyclerviewLatestMessages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        val view =binding.root
        setContentView(view)
       mAuth= Firebase.auth
        database=Firebase.database
         setSupportActionBar(toolbar)
     verifyUserLogin()
        fetchcurrentnuser()

        adapter.setOnItemClickListener { item, view ->

            // safe typecasting row as latest message row
            // as our recycler view has only one type of item
            val row=item as LatestMessageRow

            val intent =Intent(this,ChatLogActivity::class.java)
            intent.putExtra(USER_KEY,row.chatPartnerUser)
         startActivity(intent)
        }

        listenLatestMessage()
    }

    val adapter=GroupAdapter<GroupieViewHolder>()
    val latestmessageMap=HashMap<String,ChatMessage>()
// this function is used so two rows with same username is not created
    //only message is updated
    private fun refreshRecylerview(){
      adapter.clear()
      latestmessageMap.values.forEach {
          adapter.add(LatestMessageRow(it))
      }
    }
// listening to new messages that are added in database
    private fun listenLatestMessage(){
        val fromId=mAuth.uid
        val ref=database.getReference("/latest-messages/$fromId")
    ref.addChildEventListener(object :ChildEventListener{
            // we can add notification here for newmessage from new user
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                 val chatmessage=snapshot.getValue(ChatMessage::class.java) ?:return

                latestmessageMap[snapshot.key!!]=chatmessage
                refreshRecylerview()

            }
            // we can add notification here for newmessage from existing user
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatmessage=snapshot.getValue(ChatMessage::class.java) ?:return
                latestmessageMap[snapshot.key!!]=chatmessage
                refreshRecylerview()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    private fun fetchcurrentnuser(){
        val uid = mAuth.uid
        val ref=database.getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentuser=snapshot.getValue(User::class.java)
                binding.latestMessageProgressbar.visibility=View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun verifyUserLogin(){
        val uid = mAuth.uid
        if(uid==null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.new_message_menu ->{
                 val intent=Intent(this, NewMessageActivity::class.java)
                startActivity(intent)

            }
            R.id.sign_out_menu ->{
                mAuth.signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}