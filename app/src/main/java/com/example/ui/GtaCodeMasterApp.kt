package com.example.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ads.AdConsentManager
import com.example.ads.AdaptiveBannerAd
import com.example.ads.InterstitialAdController
import com.example.data.local.CheatCodeWithDetails
import com.example.ui.components.CodeDetailModalSheet
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.screens.CheatCodesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToUseCheatsDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalViceNightExtras
import com.example.ui.theme.viceNightBackdrop
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

object NavRoutes {
    const val HOME = "home"
    const val GAMES = "games"
    const val CODES = "codes"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
}

private data class NavTabItem(
    val route: String,
    val label: (AppStrings) -> String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val TopLevelTabs = listOf(
    NavTabItem(NavRoutes.HOME, { it.navHome }, Icons.Filled.Home, Icons.Outlined.Home),
    NavTabItem(NavRoutes.GAMES, { it.navGames }, Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    NavTabItem(NavRoutes.CODES, { it.navCodes }, Icons.Filled.Code, Icons.Outlined.Code),
    NavTabItem(NavRoutes.FAVORITES, { it.navFavorites }, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    NavTabItem(NavRoutes.SETTINGS, { it.navSettings }, Icons.Filled.Settings, Icons.Outlined.Settings)
)

private val BannerEligibleRoutes = setOf(
    NavRoutes.HOME,
    NavRoutes.GAMES,
    NavRoutes.CODES,
    NavRoutes.FAVORITES
)

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

@Composable
fun GtaCodeMasterApp(
    viewModel: MainViewModel,
    userDisplayName: String,
    userEmail: String?,
    onSignOut: () -> Unit
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current
    val context = LocalContext.current
    val hostActivity = remember(context) { context.findHostActivity() }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val adConsentState by AdConsentManager.state.collectAsStateWithLifecycle()
    val canRequestBannerAds = adConsentState.canRequestAds && adConsentState.isMobileAdsInitialized

    LaunchedEffect(hostActivity) {
        InterstitialAdController.initialize(context)
        hostActivity?.let { AdConsentManager.gatherConsentAndInitialize(it) }
    }

    LaunchedEffect(adConsentState.canRequestAds, adConsentState.isMobileAdsInitialized) {
        if (adConsentState.canRequestAds && adConsentState.isMobileAdsInitialized) {
            InterstitialAdController.preloadIfEligible(
                canRequestAds = true,
                forceRetry = true
            )
        }
    }

    var showHowToUseDialog by remember { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavRoutes.HOME
    val showBannerOnCurrentScreen = currentRoute in BannerEligibleRoutes

    fun performNavigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToTab(route: String) {
        if (route == currentRoute) return
        val isNaturalPostGameTransition =
            currentRoute == NavRoutes.CODES && (route == NavRoutes.GAMES || route == NavRoutes.HOME)
        if (isNaturalPostGameTransition) {
            InterstitialAdController.maybeShowOnNaturalTransition(
                activity = hostActivity,
                canRequestAds = adConsentState.canRequestAds,
                transitionLabel = "exit_codes_to:$route",
                onContinue = { performNavigate(route) }
            )
        } else {
            performNavigate(route)
        }
    }

    // Shared centralized ad navigation function used by ALL GTA game cards, buttons, and chips.
    fun openGameWithAd(gameId: String) {
        val alreadyViewingSameGame =
            currentRoute == NavRoutes.CODES && viewModel.selectedGameId.value == gameId
        if (alreadyViewingSameGame) return

        InterstitialAdController.maybeShowOnNaturalTransition(
            activity = hostActivity,
            canRequestAds = adConsentState.canRequestAds,
            transitionLabel = "open_game:$gameId",
            onContinue = {
                viewModel.selectGame(gameId)
                performNavigate(NavRoutes.CODES)
            }
        )
    }

    fun copyCheatToClipboard(cheat: CheatCodeWithDetails) {
        InterstitialAdController.recordCodeCopied()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(cheat.title, cheat.code))
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = strings.copiedSnackbar,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun handleToggleFavorite(cheat: CheatCodeWithDetails, allowUndoOnRemove: Boolean = false) {
        viewModel.toggleFavorite(cheat) { isNowFavorite ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                if (isNowFavorite) {
                    snackbarHostState.showSnackbar(
                        message = strings.addedToFavorites,
                        duration = SnackbarDuration.Short
                    )
                } else if (allowUndoOnRemove) {
                    val result = snackbarHostState.showSnackbar(
                        message = strings.removedFromFavorites,
                        actionLabel = strings.undoAction,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreFavorite(cheat)
                    }
                } else {
                    snackbarHostState.showSnackbar(
                        message = strings.removedFromFavorites,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .viceNightBackdrop(extras)
    ) {
        val useNavRail = maxWidth >= 640.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        actionColor = extras.neonAccent
                    )
                }
            },
            bottomBar = {
                if (!useNavRail) {
                    Column {
                        if (showBannerOnCurrentScreen) {
                            AdaptiveBannerAd(canRequestAds = canRequestBannerAds)
                        }
                        HorizontalDivider(color = extras.cardBorder, thickness = 1.dp)
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            TopLevelTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label(strings)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label(strings),
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = extras.neonAccent,
                                        selectedTextColor = extras.neonAccent,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.route}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        TopLevelTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label(strings)
                                    )
                                },
                                label = { Text(tab.label(strings), maxLines = 1) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = extras.neonAccent,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_rail_${tab.route}")
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = NavRoutes.HOME,
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 980.dp)
                        ) {
                            composable(NavRoutes.HOME) {
                                val allGames by viewModel.allGames.collectAsStateWithLifecycle()
                                val recentGames by viewModel.recentGames.collectAsStateWithLifecycle()
                                val popularCodes by viewModel.popularCodes.collectAsStateWithLifecycle()
                                val favoriteCodes by viewModel.favoriteCodes.collectAsStateWithLifecycle()
                                val totalCodeCount by viewModel.totalCodeCount.collectAsStateWithLifecycle()
                                val homeSearchQuery by viewModel.homeSearchQuery.collectAsStateWithLifecycle()
                                val homeSearchResults by viewModel.homeSearchResults.collectAsStateWithLifecycle()

                                HomeScreen(
                                    allGames = allGames,
                                    recentGames = recentGames,
                                    popularCodes = popularCodes,
                                    favoriteCount = favoriteCodes.size,
                                    totalCodeCount = totalCodeCount,
                                    searchQuery = homeSearchQuery,
                                    searchResults = homeSearchResults,
                                    onSearchQueryChange = viewModel::updateHomeSearchQuery,
                                    onOpenGame = ::openGameWithAd,
                                    onOpenAllGames = { navigateToTab(NavRoutes.GAMES) },
                                    onOpenFavorites = { navigateToTab(NavRoutes.FAVORITES) },
                                    onOpenHowToUse = { showHowToUseDialog = true },
                                    onCodeClick = viewModel::openCodeDetail,
                                    onCopyCode = ::copyCheatToClipboard,
                                    onToggleFavorite = { handleToggleFavorite(it) }
                                )
                            }
                            composable(NavRoutes.GAMES) {
                                val allGames by viewModel.allGames.collectAsStateWithLifecycle()

                                GamesScreen(
                                    games = allGames,
                                    onViewCodes = ::openGameWithAd
                                )
                            }
                            composable(NavRoutes.CODES) {
                                val allGames by viewModel.allGames.collectAsStateWithLifecycle()
                                val codesTabState by viewModel.codesTabState.collectAsStateWithLifecycle()

                                CheatCodesScreen(
                                    allGames = allGames,
                                    state = codesTabState,
                                    onSelectGame = ::openGameWithAd,
                                    onSelectPlatform = viewModel::selectPlatform,
                                    onSelectCategory = viewModel::selectCategory,
                                    onSearchQueryChange = viewModel::updateCodesSearchQuery,
                                    onCodeClick = viewModel::openCodeDetail,
                                    onCopyCode = ::copyCheatToClipboard,
                                    onToggleFavorite = { handleToggleFavorite(it) }
                                )
                            }
                            composable(NavRoutes.FAVORITES) {
                                val favoriteCodes by viewModel.favoriteCodes.collectAsStateWithLifecycle()
                                val filteredFavorites by viewModel.filteredFavorites.collectAsStateWithLifecycle()
                                val favoritesGameFilter by viewModel.favoritesGameFilter.collectAsStateWithLifecycle()
                                val favoritesSearchQuery by viewModel.favoritesSearchQuery.collectAsStateWithLifecycle()

                                FavoritesScreen(
                                    allFavorites = favoriteCodes,
                                    filteredFavorites = filteredFavorites,
                                    selectedGameFilter = favoritesGameFilter,
                                    searchQuery = favoritesSearchQuery,
                                    onSearchQueryChange = viewModel::updateFavoritesSearchQuery,
                                    onSelectGameFilter = viewModel::selectFavoritesGameFilter,
                                    onBrowseCodes = { navigateToTab(NavRoutes.CODES) },
                                    onCodeClick = viewModel::openCodeDetail,
                                    onCopyCode = ::copyCheatToClipboard,
                                    onToggleFavorite = { handleToggleFavorite(it, allowUndoOnRemove = true) }
                                )
                            }
                            composable(NavRoutes.SETTINGS) {
                                val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
                                val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
                                val allGames by viewModel.allGames.collectAsStateWithLifecycle()
                                val totalCodeCount by viewModel.totalCodeCount.collectAsStateWithLifecycle()
                                val favoriteCodes by viewModel.favoriteCodes.collectAsStateWithLifecycle()
                                val submittedReports by viewModel.submittedReports.collectAsStateWithLifecycle()

                                SettingsScreen(
                                    preferences = preferences,
                                    userDisplayName = userDisplayName,
                                    userEmail = userEmail,
                                    cloudSyncState = cloudSyncState,
                                    totalGamesCount = allGames.size,
                                    totalCodesCount = totalCodeCount,
                                    favoriteCount = favoriteCodes.size,
                                    submittedReports = submittedReports,
                                    onToggleDarkTheme = viewModel::setDarkTheme,
                                    onSelectLanguage = viewModel::setLanguage,
                                    onOpenHowToUse = { showHowToUseDialog = true },
                                    onSignOut = onSignOut,
                                    isPrivacyOptionsRequired = adConsentState.isPrivacyOptionsRequired,
                                    onOpenPrivacyOptions = {
                                        hostActivity?.let { activity ->
                                            AdConsentManager.showPrivacyOptionsForm(activity)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (useNavRail && showBannerOnCurrentScreen) {
                        AdaptiveBannerAd(canRequestAds = canRequestBannerAds)
                    }
                }
            }
        }
    }

    CodeDetailSheetHost(
        viewModel = viewModel,
        onCopy = ::copyCheatToClipboard,
        onToggleFavorite = { handleToggleFavorite(it) },
        onReportSubmitted = {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = strings.reportSubmittedSnackbar,
                    duration = SnackbarDuration.Short
                )
            }
        }
    )

    if (showHowToUseDialog) {
        HowToUseDialogHost(
            viewModel = viewModel,
            onDismiss = { showHowToUseDialog = false }
        )
    }
}

@Composable
private fun CodeDetailSheetHost(
    viewModel: MainViewModel,
    onCopy: (CheatCodeWithDetails) -> Unit,
    onToggleFavorite: (CheatCodeWithDetails) -> Unit,
    onReportSubmitted: () -> Unit
) {
    val selectedCodeForDetail by viewModel.selectedCodeForDetail.collectAsStateWithLifecycle()
    selectedCodeForDetail?.let { activeCheat ->
        CodeDetailModalSheet(
            cheat = activeCheat,
            onDismiss = viewModel::closeCodeDetail,
            onCopy = { onCopy(activeCheat) },
            onToggleFavorite = { onToggleFavorite(activeCheat) },
            onSubmitReport = { reportType, notes ->
                viewModel.submitCodeReport(activeCheat, reportType, notes, onReportSubmitted)
            }
        )
    }
}

@Composable
private fun HowToUseDialogHost(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val tutorials by viewModel.tutorials.collectAsStateWithLifecycle()
    HowToUseCheatsDialog(
        tutorials = tutorials,
        onDismiss = onDismiss
    )
}
