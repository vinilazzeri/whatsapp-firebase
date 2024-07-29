package com.vinilazzeri.projetowhatsappfirebase.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso
import com.vinilazzeri.projetowhatsappfirebase.databinding.ContactsItemBinding
import com.vinilazzeri.projetowhatsappfirebase.model.User

class ContactsAdapter(
    private val onClick : (User) -> Unit
) : Adapter <ContactsAdapter.ContactsViewHolder>(){

    private var contactsList = emptyList<User>()
    fun addList(list: List<User>){
        contactsList = list
        notifyDataSetChanged()
    }

    inner class ContactsViewHolder(
        private val binding: ContactsItemBinding
    ): ViewHolder (binding.root){
        fun bind(user: User){

            binding.textContactName.text = user.name
            Picasso.get()
                .load( user.photo )
                .into( binding.profileContactImg )

            //eventos de clique
            binding.clContactItem.setOnClickListener {
                onClick(user)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = ContactsItemBinding.inflate(
            inflater, parent, false
        )
        return ContactsViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val user = contactsList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }
}