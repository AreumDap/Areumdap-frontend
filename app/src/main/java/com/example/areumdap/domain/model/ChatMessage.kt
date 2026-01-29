package com.example.areumdap.domain.model

data class ChatMessage(
    val id:String,
    val sender:Sender,
    val text:String,
    val time:Long,
    val status:Status = Status.SENT

)

enum class Sender { ME, AI }
enum class Status { SENDING, SENT, FAILED, TYPING }
