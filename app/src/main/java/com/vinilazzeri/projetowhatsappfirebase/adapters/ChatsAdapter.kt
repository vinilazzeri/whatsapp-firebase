package com.vinilazzeri.projetowhatsappfirebase.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.squareup.picasso.Picasso
import com.vinilazzeri.projetowhatsappfirebase.databinding.ChatsItemBinding
import com.vinilazzeri.projetowhatsappfirebase.model.Chat

class ChatsAdapter (
    private val onClick : (Chat) -> Unit
) : Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var chatList = emptyList<Chat>()
    fun addList(list: List<Chat>){
        chatList = list
        notifyDataSetChanged()
    }

    inner class ChatsViewHolder(
        private val binding: ChatsItemBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(chat: Chat){

            binding.textChatUsername.text = chat.name
            binding.textLastMessage.text = chat.lastMessage

            Picasso.get()
                .load( chat.photo )
                .into( binding.profileImgsChats )

            //eventos de clique
            binding.clChatsItem.setOnClickListener {
                onClick(chat)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val itemView = ChatsItemBinding.inflate(
            inflater, parent, false
        )
        return ChatsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {

        val chat = chatList[position]
        holder.bind(chat)

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

}