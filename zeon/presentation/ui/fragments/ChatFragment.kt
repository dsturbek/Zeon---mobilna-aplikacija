package com.example.zeon.presentation.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.zeon.R
import com.example.zeon.presentation.ui.adapters.ChatAdapter
import com.example.zeon.helpers.UserSession
import com.example.zeon.ws.Conversation
import com.example.zeon.ws.CreateConversationRequest
import com.example.zeon.ws.CreateConversationResponse
import com.example.zeon.ws.Message
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.SendMessageBodyRequest
import com.example.zeon.ws.SendMessageResponseNew
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var etMessageInput: EditText
    private lateinit var fabSendMessage: FloatingActionButton
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvNoTrainer: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var conversationId: Int? = null
    private var clientId: Int = -1
    private var trainerId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_chat_messages)
        etMessageInput = view.findViewById(R.id.et_message_input)
        fabSendMessage = view.findViewById(R.id.fab_send_message)
        pbLoading = view.findViewById(R.id.pb_loading)
        tvNoTrainer = view.findViewById(R.id.tv_no_trainer)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)

        clientId = UserSession.getUserId(requireContext())
        trainerId = UserSession.getTrainerId(requireContext())

        setupRecyclerView()
        setupSwipeRefresh()

        if (trainerId == null) {
            showNoTrainerMessage()
        } else {
            loadOrCreateConversation()
        }

        fabSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(mutableListOf(), clientId)
        recyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.crvena)
        swipeRefresh.setOnRefreshListener {
            if (conversationId != null) {
                loadMessages()
            } else {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showNoTrainerMessage() {
        tvNoTrainer.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        etMessageInput.isEnabled = false
        fabSendMessage.isEnabled = false
    }

    private fun loadOrCreateConversation() {
        pbLoading.visibility = View.VISIBLE

        android.util.Log.d("ChatFragment", "Loading conversations, clientId=$clientId, trainerId=$trainerId")

        NetworkModule.zeonApiService.getConversations(clientId)
            .enqueue(object : Callback<List<Conversation>> {
                override fun onResponse(
                    call: Call<List<Conversation>>,
                    response: Response<List<Conversation>>
                ) {
                    android.util.Log.d("ChatFragment", "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val conversations = response.body()
                        android.util.Log.d("ChatFragment", "Conversations: $conversations")
                        val existingConversation = conversations?.find { it.trainerId == trainerId }

                        if (existingConversation != null) {
                            conversationId = existingConversation.id
                            loadMessages()
                        } else {
                            createConversation()
                        }
                    } else {
                        pbLoading.visibility = View.GONE
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("ChatFragment", "Error: $errorBody")
                        showToast("Greška ${response.code()}: $errorBody")
                    }
                }

                override fun onFailure(call: Call<List<Conversation>>, t: Throwable) {
                    pbLoading.visibility = View.GONE
                    android.util.Log.e("ChatFragment", "Failure", t)
                    showToast("Greška: ${t.message}")
                }
            })
    }

    private fun createConversation() {
        val request = CreateConversationRequest(
            clientId = clientId,
            trainerId = trainerId!!
        )

        NetworkModule.zeonApiService.createConversation(request)
            .enqueue(object : Callback<CreateConversationResponse> {
                override fun onResponse(
                    call: Call<CreateConversationResponse>,
                    response: Response<CreateConversationResponse>
                ) {
                    if (response.isSuccessful) {
                        conversationId = response.body()?.conversationId
                        loadMessages()
                    } else {
                        pbLoading.visibility = View.GONE
                        showToast("Greška pri kreiranju konverzacije")
                    }
                }

                override fun onFailure(call: Call<CreateConversationResponse>, t: Throwable) {
                    pbLoading.visibility = View.GONE
                    showToast("Greška: ${t.message}")
                }
            })
    }

    private fun loadMessages() {
        val convId = conversationId ?: return

        NetworkModule.zeonApiService.getMessages(convId)
            .enqueue(object : Callback<List<Message>> {
                override fun onResponse(
                    call: Call<List<Message>>,
                    response: Response<List<Message>>
                ) {
                    pbLoading.visibility = View.GONE
                    swipeRefresh.isRefreshing = false

                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        chatAdapter.updateMessages(messages)
                        if (messages.isNotEmpty()) {
                            recyclerView.scrollToPosition(messages.size - 1)
                        }
                    } else {
                        showToast("Greška pri učitavanju poruka")
                    }
                }

                override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                    pbLoading.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    showToast("Greška: ${t.message}")
                }
            })
    }

    private fun sendMessage() {
        val messageText = etMessageInput.text.toString().trim()
        val convId = conversationId

        if (messageText.isEmpty()) {
            showToast("Upiši poruku")
            return
        }

        if (convId == null) {
            showToast("Konverzacija nije spremna")
            return
        }

        fabSendMessage.isEnabled = false

        val request = SendMessageBodyRequest(convId, messageText)

        NetworkModule.zeonApiService.sendMessage(request)
            .enqueue(object : Callback<SendMessageResponseNew> {
                override fun onResponse(
                    call: Call<SendMessageResponseNew>,
                    response: Response<SendMessageResponseNew>
                ) {
                    fabSendMessage.isEnabled = true

                    if (response.isSuccessful) {
                        response.body()?.let { resp ->
                            val message = Message(
                                id = resp.idMessage,
                                content = resp.messageContent,
                                conversationId = resp.conversationId,
                                senderClientId = resp.senderClientId,
                                senderTrainerId = resp.senderTrainerId
                            )
                            chatAdapter.addMessage(message)
                            recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                            etMessageInput.text.clear()
                        }
                    } else {
                        showToast("Greška pri slanju poruke")
                    }
                }

                override fun onFailure(call: Call<SendMessageResponseNew>, t: Throwable) {
                    fabSendMessage.isEnabled = true
                    showToast("Greška: ${t.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
