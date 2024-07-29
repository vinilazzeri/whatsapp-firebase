package com.vinilazzeri.projetowhatsappfirebase.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.vinilazzeri.projetowhatsappfirebase.activities.MessagesActivity
import com.vinilazzeri.projetowhatsappfirebase.adapters.ChatsAdapter
import com.vinilazzeri.projetowhatsappfirebase.databinding.FragmentChatBinding
import com.vinilazzeri.projetowhatsappfirebase.model.Chat
import com.vinilazzeri.projetowhatsappfirebase.model.User
import com.vinilazzeri.projetowhatsappfirebase.utils.Constants
import com.vinilazzeri.projetowhatsappfirebase.utils.showMessage


class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var snapshotEvent: ListenerRegistration
    private lateinit var chatsAdapter: ChatsAdapter

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(
            inflater, container, false
        )

        chatsAdapter = ChatsAdapter {
            val intent = Intent(context, MessagesActivity::class.java)

            val user = User(
                id = it.recipientUserId,
                name = it.name,
                photo = it.photo
            )

            intent.putExtra("recipientData", user)
            //intent.putExtra("source", Constants.CHAT_SOURCE)
            startActivity(intent)
        }
        binding.rvChats.adapter = chatsAdapter
        binding.rvChats.layoutManager = LinearLayoutManager(context)
        binding.rvChats.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayoutManager.VERTICAL
            )
        )

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        addChatListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotEvent.remove()
    }

    private fun addChatListener() {

        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            snapshotEvent = firestore
                .collection(Constants.CHATS)
                .document(currentUserId)
                .collection(Constants.LAST_MESSAGES)
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener { querySnapshot, error ->

                    if (error != null) {
                        activity?.showMessage("error retrieving messages")
                    }

                    val chatList = mutableListOf<Chat>()
                    val documents = querySnapshot?.documents

                    documents?.forEach {
                        val chat = it.toObject(Chat::class.java)
                        if ( chat != null ){
                            chatList.add(chat)
                            Log.i("chat_exhibition", "${chat.name} - ${chat.lastMessage}")
                        }
                    }

                    //atualizar o adapter
                    if (chatList.isNotEmpty()){
                        chatsAdapter.addList(chatList)
                    }
                }
        }
    }
}