package com.vinilazzeri.projetowhatsappfirebase.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vinilazzeri.projetowhatsappfirebase.activities.MessagesActivity
import com.vinilazzeri.projetowhatsappfirebase.adapters.ContactsAdapter
import com.vinilazzeri.projetowhatsappfirebase.databinding.FragmentContactsBinding
import com.vinilazzeri.projetowhatsappfirebase.model.User
import com.vinilazzeri.projetowhatsappfirebase.utils.Constants

class ContactsFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var snapshotEvent: ListenerRegistration
    private lateinit var contactsAdapter: ContactsAdapter

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /*private val storage by lazy {
        FirebaseStorage.getInstance()
    }*/



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContactsBinding.inflate(
            inflater, container, false

        )
        contactsAdapter = ContactsAdapter {
            val intent = Intent(context, MessagesActivity::class.java)
            intent.putExtra("recipientData", it)
            //intent.putExtra("source", Constants.CONTACT_SOURCE)
            startActivity(intent)
        }
        binding.contactsRv.adapter = contactsAdapter
        binding.contactsRv.layoutManager = LinearLayoutManager(context)
        binding.contactsRv.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayoutManager.VERTICAL
            )
        )

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        addContactsListener()
    }

    private fun addContactsListener() {
        snapshotEvent = firestore
            .collection(Constants.USERS)
            .addSnapshotListener { querySnapshot, error ->

                val contactsList = mutableListOf<User>()
                val documents = querySnapshot?.documents
                documents?.forEach {

                    val loggedUserId = firebaseAuth.currentUser?.uid
                    val user = it.toObject(User::class.java)

                    if ( user != null && loggedUserId != null){
                        if (loggedUserId != user.id){
                            contactsList.add(user)
                        }
                    }
                }

                // lista de contatos - atualizar o recyclerview

                if( contactsList.isNotEmpty() ){
                    contactsAdapter.addList(contactsList)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotEvent.remove()
    }

}