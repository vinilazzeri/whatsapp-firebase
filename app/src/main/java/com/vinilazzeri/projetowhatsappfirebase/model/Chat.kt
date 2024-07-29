package com.vinilazzeri.projetowhatsappfirebase.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class Chat(
    val currentUserId: String = "",
    val recipientUserId: String = "",
    val photo: String = "",
    val name: String = "",
    val lastMessage: String = "",
    @ServerTimestamp
    val date: Date? = null
)
