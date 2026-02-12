package com.example.areumdap.data.api

class ApiException(
    val code: String,
    override val message: String
) : RuntimeException(message)
