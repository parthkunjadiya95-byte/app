package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.CheatCodeWithDetails
import com.example.ui.components.CheatCodeCard
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.ViceSearchBar
import com.example.ui.i18n.LocalAppStrings

@Composable
fun FavoritesScreen(
    allFavorites: List<CheatCodeWithDetails>,
    filteredFavorites: List<CheatCodeWithDetails>,
    selectedGameFilter: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectGameFilter: (String?) -> Unit,
    onBrowseCodes: () -> Unit,
    onCodeClick: (CheatCodeWithDetails) -> Unit,
    onCopyCode: (CheatCodeWithDetails) -> Unit,
    onToggleFavorite: (CheatCodeWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val gamesPresent = remember(allFavorites) {
        allFavorites
            .map { it.gameId to it.gameShortName }
            .distinctBy { it.first }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${strings.favoritesTitle} (${allFavorites.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.favoritesSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (allFavorites.isEmpty()) {
            item {
                EmptyStatePanel(
                    icon = Icons.Filled.FavoriteBorder,
                    title = strings.favoritesEmptyTitle,
                    subtitle = strings.favoritesEmptyBody,
                    actionLabel = strings.browseCodesCta,
                    onActionClick = onBrowseCodes,
                    modifier = Modifier.testTag("favorites_empty_state")
                )
            }
        } else {
            item {
                ViceSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = strings.searchHintFavorites,
                    testTag = "favorites_search_input"
                )
            }

            if (gamesPresent.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "fav_game_all") {
                            FilterChip(
                                selected = selectedGameFilter == null,
                                onClick = { onSelectGameFilter(null) },
                                label = { Text(strings.allGamesFilter) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        items(gamesPresent, key = { it.first }) { (gameId, shortName) ->
                            FilterChip(
                                selected = selectedGameFilter == gameId,
                                onClick = { onSelectGameFilter(gameId) },
                                label = { Text(shortName) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            if (filteredFavorites.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Filled.SearchOff,
                        title = strings.noResultsTitle,
                        subtitle = strings.noResultsSubtitle,
                        actionLabel = strings.clearSearch,
                        onActionClick = {
                            onSearchQueryChange("")
                            onSelectGameFilter(null)
                        }
                    )
                }
            } else {
                items(filteredFavorites, key = { "fav_${it.codeId}" }) { cheat ->
                    CheatCodeCard(
                        cheat = cheat,
                        showGameBadge = true,
                        onCardClick = { onCodeClick(cheat) },
                        onCopyClick = { onCopyCode(cheat) },
                        onFavoriteClick = { onToggleFavorite(cheat) }
                    )
                }
            }
        }
    }
}
