package com.example.areumdap.data.api

data class ApiResponse<T>(
    val isSuccess : Boolean,
    val code : String,
    val message : String,
    val data : T?
)