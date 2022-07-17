package com.example.safechat.views

import com.example.safechat.R
import com.example.safechat.models.ChatMessage
import com.example.safechat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
     var chatPartnerUser:User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        mAuth= Firebase.auth
        viewHolder.itemView.latest_message.text=chatMessage.text
        val chatPartner:String
        if(chatMessage.fromid==mAuth.uid){
            chatPartner=chatMessage.toid
        }
        else{
            chatPartner=chatMessage.fromid
        }
        database= Firebase.database
        val ref=database.getReference("/users/$chatPartner")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser =snapshot.getValue(User::class.java)
                viewHolder.itemView.username_textView.text=chatPartnerUser!!.username
                val targetImageview=viewHolder.itemView.latest_profile_image
                Picasso.get().load(chatPartnerUser!!.profileimageurl).into(targetImageview)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        viewHolder.itemView.username_textView.text
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}