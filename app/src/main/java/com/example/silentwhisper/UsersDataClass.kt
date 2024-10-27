package com.example.silentwhisper

data class User(
    val id: String = "", // Add this line
    val email: String = "",
    val mobileNum: String = "",
    val anonusername: String = "",
    val profilePic: String = "",
    val username: String = "",
    val isAnon:Boolean= true
)



