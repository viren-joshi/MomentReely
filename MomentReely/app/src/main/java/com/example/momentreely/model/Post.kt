package com.example.momentreely.model

data class Post (
    val senderId: String,
    val receiverId: String,
    val postId: String,
    val dateTime: String,
    val image: String
)