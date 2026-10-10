package com.xr.inline.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

interface NewsApiService {
    suspend fun getTopHeadlines(category: String? = null, country: String = "us"): NewsResponse
    suspend fun searchNews(query: String): NewsResponse
}

class NewsApiServiceImpl(
    private val apiKey: String
) : NewsApiService {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    
    private val newsApi = retrofit.create(NewsApi::class.java)
    
    override suspend fun getTopHeadlines(category: String?, country: String): NewsResponse {
        return if (category != null) {
            newsApi.getTopHeadlines(country = country, category = category, apiKey = apiKey)
        } else {
            newsApi.getTopHeadlines(country = country, apiKey = apiKey)
        }
    }
    
    override suspend fun searchNews(query: String): NewsResponse {
        return newsApi.searchNews(query = query, apiKey = apiKey)
    }
}

interface NewsApi {
    @retrofit2.http.GET("top-headlines")
    suspend fun getTopHeadlines(
        @retrofit2.http.Query("country") country: String = "us",
        @retrofit2.http.Query("category") category: String? = null,
        @retrofit2.http.Query("apiKey") apiKey: String
    ): NewsResponse
    
    @retrofit2.http.GET("everything")
    suspend fun searchNews(
        @retrofit2.http.Query("q") query: String,
        @retrofit2.http.Query("apiKey") apiKey: String
    ): NewsResponse
}
