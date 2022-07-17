package com.example.safechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.safechat.databinding.ActivityNewMessageBinding
import com.example.safechat.messages.ChatLogActivity
import com.example.safechat.models.User


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_newmessage.view.*

class NewMessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewMessageBinding
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        database=Firebase.database
        super.onCreate(savedInstanceState)
        binding=ActivityNewMessageBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)
               binding.toolbar.setTitle("Select User")
        setSupportActionBar(binding.toolbar)
 //for back button on toolbar
//        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
//        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

//        val adapter=GroupAdapter<GroupieViewHolder>()
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        binding.recyclerviewNewmessage.adapter=adapter

        fetchuser()
    }
companion object{
    val USER_KEY="USER_KEY"
}
    private fun fetchuser(){
        val ref= database.getReference("/users")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
           //iterating through each user
                val adapter=GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    Log.d("New Message",it.toString())
                    val user=it.getValue(User::class.java)

                  if(user!=null )
                    adapter.add(UserItem(user))

                }
                adapter.setOnItemClickListener { item, view ->
                  //item is casted as object of Useritem class
                    //to access the username
                    val userItem=item as UserItem
                     //sending user name as the user is clicked to show on toolbar in next
                    //activity
                    val intent=Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                    finish()
                }
                recyclerview_newmessage.adapter=adapter
            }
           override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
class UserItem(val user: User):Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username

            Picasso.get().load(user.profileimageurl).into(viewHolder.itemView.userimage)

    }
    override fun getLayout(): Int {
           return R.layout.user_row_newmessage
    }

}
