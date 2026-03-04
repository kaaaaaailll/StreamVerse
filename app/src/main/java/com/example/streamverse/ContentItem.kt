package com.example.streamverse

data class ContentItem(
    val title: String,
    val description: String,
    val episode: String,
    val rating: String,
    val category: String,
    val status: String,
    val isAnime: Boolean,
    val imageUri: String? = null
)