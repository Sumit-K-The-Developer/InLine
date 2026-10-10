package com.xr.inline.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xr.inline.data.Article
import com.xr.inline.data.NewsCategory
import com.xr.inline.data.NewsLocation
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Simple refresh icon using a vector path
private val RefreshIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Refresh",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = androidx.compose.ui.graphics.SolidColor(Color.Black)
    ) {
        moveTo(17.65f, 7.65f)
        lineTo(18.35f, 6.95f)
        curveTo(16.12f, 4.72f, 13.28f, 3.5f, 10.5f, 3.5f)
        curveTo(6.36f, 3.5f, 3f, 6.86f, 3f, 11f)
        curveTo(3f, 16.14f, 6.36f, 19.5f, 10.5f, 19.5f)
        curveTo(14.64f, 19.5f, 17f, 16.14f, 17f, 11f)
        horizontalLineTo(18.5f)
        curveTo(18.5f, 6.86f, 15.64f, 4f, 10.5f, 4f)
        curveTo(7.53f, 4f, 5.05f, 5.72f, 4.25f, 8.25f)
    }.build()

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onPrivateSpaceRequested: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pagerState = rememberPagerState { uiState.articles.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InLine News") },
                actions = {
                    // Location toggle (India/International)
                    TextButton(
                        onClick = {
                            val newLocation = if (uiState.selectedLocation == NewsLocation.INDIA) {
                                NewsLocation.INTERNATIONAL
                            } else {
                                NewsLocation.INDIA
                            }
                            viewModel.selectLocation(newLocation)
                        }
                    ) {
                        Text(
                            text = uiState.selectedLocation.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.fetchNews()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = RefreshIcon,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NewsCategoryChips(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                onPrivateSpaceRequested = onPrivateSpaceRequested
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            onRefresh = {
                viewModel.fetchNews()
            },
            isRefreshing = uiState.isLoading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (uiState.articles.isEmpty()) {
                    Text(
                        text = "No news available",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) { page ->
                        ArticlePage(
                            article = uiState.articles[page],
                            onExploreClick = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticlePage(
    article: Article,
    onExploreClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Headpart: Image background with headline overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Image background
                if (!article.urlToImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = article.urlToImage,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Gray)
                    )
                }

                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x80000000)
                                ),
                                startY = 0.5f
                            )
                        )
                )

                // Headline text at the bottom of the image
                Text(
                    text = article.title ?: "Untitled",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Date in tiny text below headline
            Text(
                text = formatDate(article.publishedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Details section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Description
                Text(
                    text = article.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Content (if available)
                if (!article.content.isNullOrEmpty()) {
                    Text(
                        text = article.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Source
                Text(
                    text = article.source?.name ?: "Unknown source",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Explore button at bottom end (side bottom button)
        val url = article.url
        FilledTonalButton(
            onClick = {
                if (!url.isNullOrEmpty()) {
                    onExploreClick(url)
                }
            },
            enabled = !url.isNullOrEmpty(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Explore")
        }
    }
}

@Composable
fun NewsCategoryChips(
    selectedCategory: NewsCategory,
    onCategorySelected: (NewsCategory) -> Unit,
    onPrivateSpaceRequested: () -> Unit = {}
) {
    val categories = NewsCategory.entries

    LazyRow(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isLastChip = index == categories.lastIndex
            var lastTapTime by remember(category) { mutableLongStateOf(0L) }

            AssistChip(
                onClick = {
                    val now = android.os.SystemClock.uptimeMillis()
                    val isDoubleTap = isLastChip &&
                        now - lastTapTime <= android.view.ViewConfiguration.getDoubleTapTimeout()

                    lastTapTime = if (isDoubleTap) 0L else now
                    onCategorySelected(category)

                    if (isDoubleTap) {
                        onPrivateSpaceRequested()
                    }
                },
                label = { Text(category.displayName) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (category == selectedCategory) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.height(32.dp)
            )
        }
    }
}
