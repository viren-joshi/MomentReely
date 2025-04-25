package com.example.momentreely.model

data class User (
    val userId: String,
    val email: String,
    val friends: List<Friend>,
    val name: String
)