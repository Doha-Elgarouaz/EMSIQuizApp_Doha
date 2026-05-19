package com.doha.emsiquizapp

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val cardMessage: CardView = view.findViewById(R.id.cardMessage)
        val layoutParams = cardMessage.layoutParams as LinearLayout.LayoutParams
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.text

        if (message.isUser) {
            holder.layoutParams.gravity = Gravity.END
            holder.cardMessage.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary))
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        } else {
            holder.layoutParams.gravity = Gravity.START
            holder.cardMessage.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.black))
        }
        holder.cardMessage.layoutParams = holder.layoutParams
    }

    override fun getItemCount() = messages.size
}
