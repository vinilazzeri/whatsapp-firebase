package com.vinilazzeri.projetowhatsappfirebase.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.vinilazzeri.projetowhatsappfirebase.R
import com.vinilazzeri.projetowhatsappfirebase.adapters.MessagesAdapter
import com.vinilazzeri.projetowhatsappfirebase.databinding.ActivityMessagesBinding
import com.vinilazzeri.projetowhatsappfirebase.model.Chat
import com.vinilazzeri.projetowhatsappfirebase.model.Messages
import com.vinilazzeri.projetowhatsappfirebase.model.User
import com.vinilazzeri.projetowhatsappfirebase.utils.Constants
import com.vinilazzeri.projetowhatsappfirebase.utils.showMessage

class MessagesActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMessagesBinding.inflate(layoutInflater)
    }

    private lateinit var listenerRegistration: ListenerRegistration

    private lateinit var chatAdapter: MessagesAdapter

    // Firebase
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var recipientData: User? = null
    private var currentUserData: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContentView(binding.root)
        fetchUsersData()
        toolBarInitialize()
        initializeClickListeners()
        initializeListeners()
        initializeRecyclerView()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeRecyclerView() {
        with(binding){
            chatAdapter = MessagesAdapter()
            rvMessages.adapter = chatAdapter
            rvMessages.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun initializeListeners() {
        val currentUser = firebaseAuth.currentUser?.uid
        val recipientUser = recipientData?.id
        if (currentUser != null && recipientUser != null){

            listenerRegistration = firestore
                .collection(Constants.DB_MESSAGES)
                .document(currentUser)
                .collection(recipientUser)
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        showMessage("error retrieving messages")
                    }

                    val chatList = mutableListOf<Messages>()
                    val documents = querySnapshot?.documents
                    documents?.forEach {

                        val message = it.toObject(Messages::class.java)
                        if ( message != null){
                            chatList.add(message)
                            Log.i("message_display", message.messageContent)
                        }
                    }
                    if ( chatList.isNotEmpty() ){
                        chatAdapter.addList(chatList)
                    }

                }
        }
    }

    private fun initializeClickListeners() {
        binding.btnSendMessage.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            saveMessage(messageText)
        }
    }

    private fun saveMessage(messageContent: String) {
        if (messageContent.isNotEmpty()) {
            val currentUser = firebaseAuth.currentUser?.uid
            val recipientUser = recipientData?.id
            if (currentUser != null && recipientUser != null) {
                // Inclua o conteúdo da mensagem ao criar o objeto Messages
                val message = Messages(
                    userId = currentUser,
                    messageContent = messageContent // Passa o texto da mensagem
                )

                // Salvando para o usuário que enviar mensagem
                savingFirestoreMessage(
                    currentUser, recipientUser, message
                )

                val currentUserChat = Chat(
                    currentUser, recipientUser, recipientData!!.photo,
                    recipientData!!.name, messageContent
                )

                firestoreChatSaver(currentUserChat)

                // Salvando a mesma mensagem para quem está recebendo
                savingFirestoreMessage(
                    recipientUser, currentUser, message
                )

                binding.editTextMessage.setText("")

                val recipientUserChat = Chat(
                     recipientUser, currentUser, currentUserData!!.photo,
                     currentUserData!!.name, messageContent
                )
            }
        }
    }

    private fun firestoreChatSaver(chat: Chat) {

        firestore.collection(Constants.CHATS)
            .document(chat.currentUserId)
            .collection(Constants.LAST_MESSAGES)
            .document(chat.recipientUserId)
            .set(chat)
            . addOnFailureListener {
                showMessage("Saving chat has failed")
            }
    }

    private fun savingFirestoreMessage(
        currentUser: String,
        recipientUser: String,
        message: Messages
    ) {
        firestore.collection(Constants.DB_MESSAGES)
            .document(currentUser)
            .collection(recipientUser)
            .add(message)
            .addOnFailureListener {
                showMessage("Message failed to send")
            }
    }

    private fun toolBarInitialize() {
        val toolbar = binding.chatTb
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            if (recipientData != null) {
                binding.textProfileUsername.text = recipientData!!.name
                Picasso.get()
                    .load(recipientData!!.photo)
                    .into(binding.imgProfilePhoto)
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun fetchUsersData() {

        //recuperando o usuario que esta logado
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null){
            firestore
                .collection(Constants.USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener {

                    val user = it.toObject(User::class.java)
                    if (user != null){
                        currentUserData = user
                    }
                }
        }


        //recuperando dados do destinatario
        val extras = intent.extras
        if (extras != null) {
            recipientData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable("recipientData", User::class.java)
            } else {
                extras.getParcelable("recipientData")
            }
        }
    }
}
