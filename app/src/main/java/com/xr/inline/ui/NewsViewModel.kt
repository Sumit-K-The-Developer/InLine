package com.xr.inline.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xr.inline.data.Article
import com.xr.inline.data.NewsApiServiceImpl
import com.xr.inline.data.NewsCategory
import com.xr.inline.data.NewsLocation
import com.xr.inline.data.NewsRepository
import com.xr.inline.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: NewsCategory = NewsCategory.GENERAL,
    val selectedLocation: NewsLocation = NewsLocation.INDIA
)

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = NewsRepository(NewsApiServiceImpl(Constants.NEWS_API_KEY))
    
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()
    
    init {
        fetchNews()
    }
    
    fun fetchNews() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            repository.getTopHeadlines(
                category = _uiState.value.selectedCategory.apiKey,
                country = _uiState.value.selectedLocation.countryCode
            )
                .onSuccess { articles ->
                    _uiState.value = _uiState.value.copy(
                        articles = articles,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        articles = emptyList(),
                        isLoading = false,
                        error = error.message ?: "Unknown error"
                    )
                }
        }
    }
    
    fun selectCategory(category: NewsCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        fetchNews()
    }
    
    fun selectLocation(location: NewsLocation) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        fetchNews()
    }
}