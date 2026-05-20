package com.example.zeon.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.zeon.R
import com.example.zeon.ws.Message

class ChatAdapter(
    private val messages: MutableList<Message>,
    private val currentUserId: Int
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvTrainerMessage: CardView = view.findViewById(R.id.cv_trainer_message)
        val cvClientMessage: CardView = view.findViewById(R.id.cv_client_message)
        val tvTrainerMessage: TextView = view.findViewById(R.id.tv_trainer_message)
        val tvClientMessage: TextView = view.findViewById(R.id.tv_client_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        val isFromClient = message.senderClientId != null

        if (isFromClient) {
            holder.cvClientMessage.visibility = View.VISIBLE
            holder.cvTrainerMessage.visibility = View.GONE
            holder.tvClientMessage.text = message.content
        } else {
            holder.cvTrainerMessage.visibility = View.VISIBLE
            holder.cvClientMessage.visibility = View.GONE
            holder.tvTrainerMessage.text = message.content
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}
