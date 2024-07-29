package com.vinilazzeri.projetowhatsappfirebase.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.vinilazzeri.projetowhatsappfirebase.databinding.CurrentUserItemMessagesBinding
import com.vinilazzeri.projetowhatsappfirebase.databinding.RecipientUserItemMessagesBinding
import com.vinilazzeri.projetowhatsappfirebase.model.Messages
import com.vinilazzeri.projetowhatsappfirebase.utils.Constants

class MessagesAdapter : Adapter<ViewHolder>(){

    private var chatMessagesList = emptyList<Messages>()
    fun addList(list: List<Messages>){
        chatMessagesList = list
        notifyDataSetChanged()
    }

    class CurrentUserChatViewHolder( //viewholder
        private val binding: CurrentUserItemMessagesBinding
    ): ViewHolder(binding.root){

        fun bind(message: Messages){
            binding.textCurrentUserChat.text = message.messageContent
        }

        companion object{
            fun layoutInflater(parent: ViewGroup): CurrentUserChatViewHolder{
                val inflater = LayoutInflater.from(parent.context)
                val itemView = CurrentUserItemMessagesBinding.inflate(
                    inflater, parent, false
                )

                return CurrentUserChatViewHolder(itemView)
            }
        }

    }

    class RecipientUserChatViewHolder( //viewholder
        private val binding: RecipientUserItemMessagesBinding
    ): ViewHolder(binding.root){

        fun bind(message: Messages){
            binding.textRecipientUserChat.text = message.messageContent
        }

        companion object{
            fun layoutInflater(parent: ViewGroup): RecipientUserChatViewHolder{
                val inflater = LayoutInflater.from(parent.context)
                val itemView = RecipientUserItemMessagesBinding.inflate(
                    inflater, parent, false
                )

                return RecipientUserChatViewHolder(itemView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        val message = chatMessagesList[position]
        val loggedUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        return if (loggedUserId == message.userId){
            Constants.CURRENT_USER
        }else{
            Constants.RECIPIENT_USER
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == Constants.CURRENT_USER)
            return CurrentUserChatViewHolder.layoutInflater(parent)
        return RecipientUserChatViewHolder.layoutInflater(parent)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = chatMessagesList[position]

        when (holder){
            is CurrentUserChatViewHolder -> holder.bind(message)
            is RecipientUserChatViewHolder -> holder.bind(message)
        }

        /*val CurrentUserViewHolderMessages = holder as CurrentUserChatViewHolder
        CurrentUserViewHolderMessages.bind()*/
    }

    override fun getItemCount(): Int {
        return chatMessagesList.size
    }


}