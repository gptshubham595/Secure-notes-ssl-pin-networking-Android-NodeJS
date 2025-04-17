package com.shubham.securenotesandroid.core.data.models

data class Note(
    val _id: String,
    val title: String,
    val content: String,
    val user: String,
    val createdAt: String,
    val updatedAt: String
)