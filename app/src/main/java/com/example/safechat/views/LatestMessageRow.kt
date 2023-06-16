package com.example.safechat.views

import android.util.Base64
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
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    val SECRET_KEY = "0123456789abcdef"
    val SECRET_IV = "0123456789abcdef"
     var chatPartnerUser:User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        mAuth= Firebase.auth
        viewHolder.itemView.latest_message.text=chatMessage.text.decryptCBC()
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
    private fun String.decryptCBC(): String {
        val decodedByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}