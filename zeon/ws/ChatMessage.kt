package com.example.zeon.ws

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id_message") val id: Int,
    @SerializedName("message_content") val content: String,
    @SerializedName("ConversationId") val conversationId: Int,
    @SerializedName("SenderClientId") val senderClientId: Int?,
    @SerializedName("SenderTrainerId") val senderTrainerId: Int?
)

data class Conversation(
    @SerializedName("id_conversation") val id: Int,
    @SerializedName("ClientId") val clientId: Int,
    @SerializedName("TrainerId") val trainerId: Int,
    @SerializedName("trainer_name") val trainerName: String?,
    @SerializedName("trainer_username") val trainerUsername: String?,
    @SerializedName("client_name") val clientName: String?,
    @SerializedName("client_username") val clientUsername: String?,
    val messages: List<Message>?
)

data class ConversationListResponse(
    val conversations: List<Conversation>
)

data class CreateConversationRequest(
    @SerializedName("ClientId") val clientId: Int,
    @SerializedName("TrainerId") val trainerId: Int
)

data class CreateConversationResponse(
    val message: String,
    @SerializedName("id_conversation") val conversationId: Int
)

data class SendMessageRequest(
    @SerializedName("message_content") val messageContent: String
)

data class SendMessageBodyRequest(
    @SerializedName("ConversationId") val conversationId: Int,
    @SerializedName("message_content") val messageContent: String
)

data class SendMessageResponseNew(
    val message: String,
    @SerializedName("id_message") val idMessage: Int,
    @SerializedName("message_content") val messageContent: String,
    @SerializedName("ConversationId") val conversationId: Int,
    @SerializedName("SenderClientId") val senderClientId: Int?,
    @SerializedName("SenderTrainerId") val senderTrainerId: Int?
)
