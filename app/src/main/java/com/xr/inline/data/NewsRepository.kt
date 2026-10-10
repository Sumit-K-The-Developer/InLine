package com.xr.inline.data

import com.xr.inline.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class NewsRepository(private val apiService: NewsApiService) {
    
    suspend fun getTopHeadlines(category: String? = null, country: String = "us"): Result<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                // Return mock data if API key is not configured
                if (Constants.NEWS_API_KEY == "YOUR_API_KEY_HERE" || Constants.NEWS_API_KEY.isEmpty()) {
                    delay(1000) // Simulate network delay
                    return@withContext Result.success(getMockArticles())
                }
                
                val response = apiService.getTopHeadlines(category, country)
                Result.success(response.articles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun searchNews(query: String): Result<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                if (Constants.NEWS_API_KEY == "YOUR_API_KEY_HERE" || Constants.NEWS_API_KEY.isEmpty()) {
                    delay(1000)
                    return@withContext Result.success(getMockArticles())
                }
                
                val response = apiService.searchNews(query)
                Result.success(response.articles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun getMockArticles(): List<Article> {
        return listOf(
            Article(
                title = "Breaking: New Jetpack Compose Features Announced",
                description = "Google announces new features for Jetpack Compose at the latest Android developer conference.",
                source = Source(name = "Tech News"),
                url = "https://example.com/article1",
                urlToImage = "https://picsum.photos/seed/article1/400/300",
                publishedAt = "2026-07-18T10:00:00Z"
            ),
            Article(
                title = "Kotlin 2.2 Released with Performance Improvements",
                description = "The latest Kotlin release brings significant performance improvements and new language features.",
                source = Source(name = "Developer Weekly"),
                url = "https://example.com/article2",
                urlToImage = "https://picsum.photos/seed/article2/400/300",
                publishedAt = "2026-07-17T15:30:00Z"
            ),
            Article(
                title = "Android 17 Beta Now Available for Developers",
                description = "Google releases Android 17 beta with new privacy controls and AI integration.",
                source = Source(name = "Mobile World"),
                url = "https://example.com/article3",
                urlToImage = "https://picsum.photos/seed/article3/400/300",
                publishedAt = "2026-07-16T09:00:00Z"
            )
        )
    }
}
