package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CheatCodeWithDetails
import com.example.data.local.GameWithCodeCount
import com.example.ui.components.AsyncDrawableImage
import com.example.ui.components.CheatCodeCard
import com.example.ui.components.DisclaimerBannerCard
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.GameArtworkHeader
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.VerifiedPillBadge
import com.example.ui.components.ViceSearchBar
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalViceNightExtras

private val HeroBannerScrimBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xF0120824),
        Color(0xCC1B0E34),
        Color(0x66120824)
    )
)

@Composable
fun HomeScreen(
    allGames: List<GameWithCodeCount>,
    recentGames: List<GameWithCodeCount>,
    popularCodes: List<CheatCodeWithDetails>,
    favoriteCount: Int,
    totalCodeCount: Int,
    searchQuery: String,
    searchResults: List<CheatCodeWithDetails>,
    onSearchQueryChange: (String) -> Unit,
    onOpenGame: (String) -> Unit,
    onOpenAllGames: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenHowToUse: () -> Unit,
    onCodeClick: (CheatCodeWithDetails) -> Unit,
    onCopyCode: (CheatCodeWithDetails) -> Unit,
    onToggleFavorite: (CheatCodeWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current
    val featuredGames = remember(allGames) { allGames.filter { it.isFeatured } }
    val matchingGames = remember(allGames, searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isNotEmpty()) {
            allGames.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                    it.shortName.contains(trimmed, ignoreCase = true) ||
                    it.eraLabel.contains(trimmed, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Brand Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                AsyncDrawableImage(
                    resId = R.drawable.img_hero_vice_night,
                    contentDescription = strings.appTitle,
                    contentScale = ContentScale.Crop,
                    targetWidthPx = 800,
                    targetHeightPx = 450,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HeroBannerScrimBrush)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncDrawableImage(
                            resId = R.drawable.ic_app_logo_fg,
                            contentDescription = strings.appTitle,
                            contentScale = ContentScale.Crop,
                            targetWidthPx = 192,
                            targetHeightPx = 192,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Column {
                            Text(
                                text = strings.appTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = strings.appTagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD6CEEC)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerifiedPillBadge(
                            text = "$totalCodeCount ${strings.verifiedCodesSuffix}",
                            isVerified = true
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0x991E1636)
                        ) {
                            Text(
                                text = "100% Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFF3E8FF),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Global Instant Search Bar
        item {
            ViceSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = strings.searchHintHome,
                testTag = "home_search_input"
            )
        }

        // Inline Search Results when query is active
        if (searchQuery.isNotBlank()) {
            item {
                SectionHeaderRow(
                    title = "${strings.searchResultsTitle} (${matchingGames.size + searchResults.size})",
                    actionLabel = strings.clearSearch,
                    onActionClick = { onSearchQueryChange("") }
                )
            }

            if (matchingGames.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(matchingGames, key = { it.gameId }) { game ->
                            CompactGamePillCard(
                                game = game,
                                onClick = { onOpenGame(game.gameId) }
                            )
                        }
                    }
                }
            }

            if (searchResults.isEmpty() && matchingGames.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Filled.SearchOff,
                        title = strings.noResultsTitle,
                        subtitle = strings.noResultsSubtitle,
                        actionLabel = strings.clearSearch,
                        onActionClick = { onSearchQueryChange("") }
                    )
                }
            } else {
                items(searchResults, key = { "search_${it.codeId}" }) { cheat ->
                    CheatCodeCard(
                        cheat = cheat,
                        showGameBadge = true,
                        onCardClick = { onCodeClick(cheat) },
                        onCopyClick = { onCopyCode(cheat) },
                        onFavoriteClick = { onToggleFavorite(cheat) }
                    )
                }
            }
        } else {
            // Featured Games Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeaderRow(
                        title = strings.featuredGames,
                        actionLabel = strings.viewAll,
                        onActionClick = onOpenAllGames
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredGames, key = { it.gameId }) { game ->
                            FeaturedGameCard(
                                game = game,
                                onClick = { onOpenGame(game.gameId) }
                            )
                        }
                    }
                }
            }

            // Quick Actions Row: Favorites Shortcut + How to Use Cheats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenFavorites)
                            .testTag("home_favorites_shortcut"),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, extras.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(extras.neonContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = extras.neonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "$favoriteCount ${strings.savedCodesCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extras.neonAccent
                                )
                            }
                            Text(
                                text = strings.favoriteShortcutTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.favoriteShortcutSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenHowToUse)
                            .testTag("home_howto_shortcut"),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, extras.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = strings.howToUseTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.howToUseSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Recently Viewed Games (dynamically populated when games are opened)
            if (recentGames.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.recentlyViewed,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentGames, key = { "recent_${it.gameId}" }) { game ->
                                CompactGamePillCard(
                                    game = game,
                                    onClick = { onOpenGame(game.gameId) }
                                )
                            }
                        }
                    }
                }
            }

            // Latest Verified Updates Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(extras.neonContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = extras.neonAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = strings.latestVerifiedTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.latestVerifiedBody,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Popular Cheat Codes
            item {
                SectionHeaderRow(
                    title = strings.popularCodes,
                    actionLabel = strings.viewAll,
                    onActionClick = onOpenAllGames
                )
            }

            items(popularCodes.take(8), key = { "pop_${it.codeId}" }) { cheat ->
                CheatCodeCard(
                    cheat = cheat,
                    showGameBadge = true,
                    onCardClick = { onCodeClick(cheat) },
                    onCopyClick = { onCopyCode(cheat) },
                    onFavoriteClick = { onToggleFavorite(cheat) }
                )
            }

            // Mandatory Unofficial Fan-Made Disclaimer
            item {
                DisclaimerBannerCard()
            }
        }
    }
}

@Composable
private fun FeaturedGameCard(
    game: GameWithCodeCount,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current

    Card(
        modifier = Modifier
            .width(216.dp)
            .height(152.dp)
            .testTag("featured_game_${game.gameId}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, extras.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GameArtworkHeader(
            artworkKey = game.artworkKey,
            contentDescription = game.name,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xAA161026)
                    ) {
                        Text(
                            text = game.releaseYear,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE9D5FF),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    VerifiedPillBadge(
                        text = if (game.hasVerifiedCodes) "${game.codeCount}" else strings.awaitingReleaseBadge,
                        isVerified = game.hasVerifiedCodes
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = game.eraLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBC3E3),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactGamePillCard(
    game: GameWithCodeCount,
    onClick: () -> Unit
) {
    val extras = LocalViceNightExtras.current
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("recent_game_${game.gameId}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extras.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (game.hasVerifiedCodes) "${game.codeCount} codes • ${game.releaseYear}" else game.releaseYear,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
