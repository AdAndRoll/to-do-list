package com.example.domain.model

data class Task(
    val id:Int,
    val title:String,
    val status:Boolean,
    val imageUrl: String? = null,

    )

