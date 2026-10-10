package com.xr.inline.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

@Serializable
data class Article(
    val source: Source? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("urlToImage")
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
)

@Serializable
data class Source(
    val id: String? = null,
    val name: String? = null
)

enum class NewsCategory(val displayName: String, val apiKey: String) {
    GENERAL("General", "general"),
    BUSINESS("Business", "business"),
    ENTERTAINMENT("Entertainment", "entertainment"),
    HEALTH("Health", "health"),
    SCIENCE("Science", "science"),
    SPORTS("Sports", "sports"),
    TECHNOLOGY("Technology", "technology")
}

enum class NewsLocation(val displayName: String, val countryCode: String) {
    INDIA("India", "in"),
    INTERNATIONAL("International", "us")
}
