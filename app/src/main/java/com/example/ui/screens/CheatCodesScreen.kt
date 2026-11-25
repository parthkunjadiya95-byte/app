package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CheatCodeWithDetails
import com.example.data.local.GameWithCodeCount
import com.example.data.model.CheatCategory
import com.example.data.model.GamePlatform
import com.example.ui.components.CheatCodeCard
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.GameArtworkHeader
import com.example.ui.components.VerifiedPillBadge
import com.example.ui.components.ViceSearchBar
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalViceNightExtras
import com.example.ui.viewmodel.CodesTabUiState

@Composable
fun CheatCodesScreen(
    allGames: List<GameWithCodeCount>,
    state: CodesTabUiState,
    onSelectGame: (String) -> Unit,
    onSelectPlatform: (GamePlatform) -> Unit,
    onSelectCategory: (CheatCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCodeClick: (CheatCodeWithDetails) -> Unit,
    onCopyCode: (CheatCodeWithDetails) -> Unit,
    onToggleFavorite: (CheatCodeWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current
    val selectedGame = state.selectedGame

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cheat_codes_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Text(
                text = strings.codesScreenTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Horizontal Game Selector Strip
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("game_selector_row")
            ) {
                items(allGames, key = { it.gameId }) { game ->
                    val isSelected = selectedGame?.gameId == game.gameId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectGame(game.gameId) },
                        label = {
                            Text(
                                text = game.shortName,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = extras.cardBorder,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("game_chip_${game.gameId}")
                    )
                }
            }
        }

        // Selected Game Banner Card
        if (selectedGame != null) {
            item {
                GameArtworkHeader(
                    artworkKey = selectedGame.artworkKey,
                    contentDescription = selectedGame.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedGame.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            VerifiedPillBadge(
                                text = if (selectedGame.hasVerifiedCodes) {
                                    "${selectedGame.codeCount} ${strings.verifiedBadge}"
                                } else {
                                    strings.awaitingReleaseBadge
                                },
                                isVerified = selectedGame.hasVerifiedCodes
                            )
                        }
                        Text(
                            text = selectedGame.statusNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2DCF2),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // GTA VI Unverified / Upcoming State
        if (selectedGame != null && !selectedGame.hasVerifiedCodes) {
            item {
                EmptyStatePanel(
                    icon = Icons.Filled.HourglassTop,
                    title = strings.gtaViNoticeTitle,
                    subtitle = strings.gtaViNoticeBody,
                    modifier = Modifier.testTag("gta_vi_unverified_notice")
                )
            }
        } else {
            // Platform Selector (strictly verified platforms only)
            if (state.availablePlatforms.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = strings.selectPlatform,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("platform_selector_row")
                        ) {
                            items(state.availablePlatforms, key = { it.id }) { platform ->
                                val isSelected = state.selectedPlatform == platform
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectPlatform(platform) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = iconForPlatform(platform),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(platform.displayName) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = extras.neonContainer,
                                        selectedLabelColor = extras.neonAccent,
                                        selectedLeadingIconColor = extras.neonAccent,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = extras.cardBorder,
                                        selectedBorderColor = extras.neonAccent.copy(alpha = 0.65f)
                                    ),
                                    modifier = Modifier.testTag("platform_chip_${platform.id.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar inside Codes tab
            item {
                ViceSearchBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = strings.searchHintCodes,
                    testTag = "codes_search_input"
                )
            }

            // Category Filter Chips (strictly applicable categories only)
            if (state.availableCategories.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("category_filter_row")
                    ) {
                        item(key = "cat_all") {
                            val isSelected = state.selectedCategory == null
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(null) },
                                label = { Text(strings.allCategories) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("category_chip_all")
                            )
                        }
                        items(state.availableCategories, key = { it.id }) { category ->
                            val isSelected = state.selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(category) },
                                label = { Text(category.label(strings)) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("category_chip_${category.id.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Results Count Strip
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, extras.cardBorder)
                ) {
                    Text(
                        text = "${state.filteredCodes.size} ${strings.verifiedCodesSuffix}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Cheat Code Cards or Empty Search State
            if (state.filteredCodes.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Filled.SearchOff,
                        title = strings.noResultsTitle,
                        subtitle = strings.noResultsSubtitle,
                        actionLabel = if (state.searchQuery.isNotBlank() || state.selectedCategory != null) {
                            strings.clearSearch
                        } else null,
                        onActionClick = {
                            onSearchQueryChange("")
                            onSelectCategory(null)
                        }
                    )
                }
            } else {
                items(state.filteredCodes, key = { it.codeId }) { cheat ->
                    CheatCodeCard(
                        cheat = cheat,
                        showGameBadge = false,
                        onCardClick = { onCodeClick(cheat) },
                        onCopyClick = { onCopyCode(cheat) },
                        onFavoriteClick = { onToggleFavorite(cheat) }
                    )
                }
            }
        }
    }
}

private fun iconForPlatform(platform: GamePlatform): ImageVector = when (platform) {
    GamePlatform.PC -> Icons.Filled.Computer
    GamePlatform.PLAYSTATION -> Icons.Filled.SportsEsports
    GamePlatform.PSP -> Icons.Filled.Gamepad
    GamePlatform.XBOX -> Icons.Filled.Gamepad
    GamePlatform.IN_GAME_PHONE -> Icons.Filled.Phone
    GamePlatform.ANDROID -> Icons.Filled.PhoneAndroid
    GamePlatform.IOS -> Icons.Filled.PhoneIphone
}
