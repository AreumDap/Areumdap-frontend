package com.example.areumdap.data.model

data class ChatMessage(
    val id:String,
    val sender:Sender,
    val text:String,
    val time:Long,
    val status:Status = Status.SENT,
    val chatHistoryId: Long? = null

)

enum class Sender { ME, AI }
enum class Status { SENDING, SENT, FAILED, TYPING }
