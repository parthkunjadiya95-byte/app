package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CheatCodeWithDetails
import com.example.data.local.CodeReportEntity
import com.example.data.local.GameWithCodeCount
import com.example.data.local.VerifiedTutorialsAndCategoriesSeed
import com.example.data.model.CheatCategory
import com.example.data.model.GamePlatform
import com.example.data.model.ReportType
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.CategoryDoc
import com.example.data.remote.CloudCodeReportDoc
import com.example.data.remote.FirestoreCatalogRepository
import com.example.data.remote.FirestoreCatalogRepository.Companion.toLocalEntity
import com.example.data.remote.FirestoreSyncRepository
import com.example.data.remote.TutorialDoc
import com.example.data.repository.CheatCatalogRepository
import com.example.ui.i18n.AppLanguage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CodesTabUiState(
    val selectedGame: GameWithCodeCount? = null,
    val availablePlatforms: List<GamePlatform> = emptyList(),
    val selectedPlatform: GamePlatform? = null,
    val availableCategories: List<CheatCategory> = emptyList(),
    val selectedCategory: CheatCategory? = null,
    val searchQuery: String = "",
    val filteredCodes: List<CheatCodeWithDetails> = emptyList()
)

sealed interface CloudSyncState {
    data object Syncing : CloudSyncState
    data object Synced : CloudSyncState
    data class Error(val message: String) : CloudSyncState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val currentUserId: String,
    private val userDisplayName: String,
    private val userEmail: String?,
    private val repository: CheatCatalogRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val firestoreCatalogRepository: FirestoreCatalogRepository
) : ViewModel() {

    private val _selectedGameId = MutableStateFlow("gta_5")
    private val _selectedPlatform = MutableStateFlow<GamePlatform?>(null)
    private val _selectedCategory = MutableStateFlow<CheatCategory?>(null)
    private val _codesSearchQuery = MutableStateFlow("")
    private val _homeSearchQuery = MutableStateFlow("")
    private val _favoritesSearchQuery = MutableStateFlow("")
    private val _favoritesGameFilter = MutableStateFlow<String?>(null)
    private val _selectedCodeForDetail = MutableStateFlow<CheatCodeWithDetails?>(null)
    private val _cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Syncing)

    val selectedGameId: StateFlow<String> = _selectedGameId.asStateFlow()
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()
    val favoritesSearchQuery: StateFlow<String> = _favoritesSearchQuery.asStateFlow()
    val favoritesGameFilter: StateFlow<String?> = _favoritesGameFilter.asStateFlow()
    val selectedCodeForDetail: StateFlow<CheatCodeWithDetails?> = _selectedCodeForDetail.asStateFlow()
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UserPreferences())

    val allGames: StateFlow<List<GameWithCodeCount>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val recentGames: StateFlow<List<GameWithCodeCount>> = repository.recentGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val popularCodes: StateFlow<List<CheatCodeWithDetails>> = repository.popularCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val favoriteCodes: StateFlow<List<CheatCodeWithDetails>> = repository.favoriteCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val categories: StateFlow<List<CategoryDoc>> = firestoreCatalogRepository
        .observeCategories(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Falling back to verified offline categories", firestoreError ?: error)
            emit(defaultCategoryDocs())
        }
        .map { list -> list.ifEmpty { defaultCategoryDocs() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), defaultCategoryDocs())

    val tutorials: StateFlow<List<TutorialDoc>> = firestoreCatalogRepository
        .observeTutorials(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Falling back to verified offline tutorials", firestoreError ?: error)
            emit(defaultTutorialDocs())
        }
        .map { list -> list.ifEmpty { defaultTutorialDocs() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), defaultTutorialDocs())

    private val cloudReports: StateFlow<List<CloudCodeReportDoc>> = firestoreSyncRepository
        .observeCloudReports(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing cloud reports", rootError)
            _cloudSyncState.value = CloudSyncState.Error(
                rootError.message ?: "Failed to sync reports"
            )
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val submittedReports: StateFlow<List<CodeReportEntity>> = combine(
        repository.submittedReports,
        cloudReports
    ) { localReports, remoteReports ->
        if (remoteReports.isNotEmpty()) {
            remoteReports.mapIndexed { idx, doc ->
                CodeReportEntity(
                    reportId = idx.toLong() + 1L,
                    codeId = doc.codeId,
                    codeTitle = doc.codeTitle,
                    gameName = doc.gameName,
                    platform = doc.platform,
                    reportType = doc.reportType,
                    description = doc.description.orEmpty(),
                    createdAt = (doc.createdAt?.seconds ?: 0L) * 1000L,
                    status = doc.status
                )
            }
        } else {
            localReports
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val totalCodeCount: StateFlow<Int> = repository.totalCodeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0)

    val homeSearchResults: StateFlow<List<CheatCodeWithDetails>> = _homeSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else repository.searchCodes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val rawCodesForSelectedGame: StateFlow<List<CheatCodeWithDetails>> = _selectedGameId
        .flatMapLatest { gameId -> repository.observeCodesForGame(gameId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val codesTabState: StateFlow<CodesTabUiState> = combine(
        allGames,
        _selectedGameId,
        rawCodesForSelectedGame,
        categories,
        combine(_selectedPlatform, _selectedCategory, _codesSearchQuery) { p, c, q -> Triple(p, c, q) }
    ) { games, gameId, rawCodes, firestoreCategories, filters ->
        val (requestedPlatform, requestedCategory, query) = filters
        val selectedGame = games.firstOrNull { it.gameId == gameId } ?: games.firstOrNull()
        val platforms = selectedGame?.supportedPlatforms().orEmpty().filter { platform ->
            rawCodes.any { it.platform == platform.id }
        }
        val activePlatform = if (requestedPlatform != null && requestedPlatform in platforms) {
            requestedPlatform
        } else {
            platforms.firstOrNull()
        }

        val platformCodes = if (activePlatform != null) {
            rawCodes.filter { it.platform == activePlatform.id }
        } else {
            rawCodes
        }

        val orderedCategories = firestoreCategories
            .sortedBy { it.sortOrder }
            .map { CheatCategory.fromId(it.categoryId.ifBlank { it.id }) }
            .distinct()
            .ifEmpty { CheatCategory.entries }

        val applicableCategories = orderedCategories.filter { cat ->
            platformCodes.any { it.category == cat.id }
        }
        val activeCategory = if (requestedCategory != null && requestedCategory in applicableCategories) {
            requestedCategory
        } else {
            null
        }

        val trimmed = query.trim()
        val filtered = platformCodes.filter { code ->
            val matchesCategory = activeCategory == null || code.category == activeCategory.id
            val matchesQuery = trimmed.isEmpty() ||
                code.title.contains(trimmed, ignoreCase = true) ||
                code.description.contains(trimmed, ignoreCase = true) ||
                code.code.contains(trimmed, ignoreCase = true) ||
                code.keywords.contains(trimmed, ignoreCase = true) ||
                code.category.contains(trimmed, ignoreCase = true) ||
                code.gameName.contains(trimmed, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        CodesTabUiState(
            selectedGame = selectedGame,
            availablePlatforms = platforms,
            selectedPlatform = activePlatform,
            availableCategories = applicableCategories,
            selectedCategory = activeCategory,
            searchQuery = query,
            filteredCodes = filtered
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), CodesTabUiState())

    val filteredFavorites: StateFlow<List<CheatCodeWithDetails>> = combine(
        favoriteCodes,
        _favoritesGameFilter,
        _favoritesSearchQuery
    ) { favorites, gameFilter, query ->
        val trimmed = query.trim()
        favorites.filter { item ->
            val matchesGame = gameFilter == null || item.gameId == gameFilter
            val matchesQuery = trimmed.isEmpty() ||
                item.title.contains(trimmed, ignoreCase = true) ||
                item.description.contains(trimmed, ignoreCase = true) ||
                item.code.contains(trimmed, ignoreCase = true) ||
                item.gameName.contains(trimmed, ignoreCase = true) ||
                item.keywords.contains(trimmed, ignoreCase = true)
            matchesGame && matchesQuery
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeeded()
            // Start real-time Firestore listeners without blocking local Room readiness.
            startCloudCatalogListeners()
            startCloudSyncListeners()

            val catalogDeferred = async { firestoreCatalogRepository.ensureCatalogPopulated() }
            val profileDeferred = async {
                val prefs = preferencesRepository.userPreferences.first()
                firestoreSyncRepository.upsertUserProfile(
                    displayName = userDisplayName,
                    email = userEmail,
                    darkTheme = prefs.isDarkTheme,
                    languageCode = prefs.language.code
                )
            }
            val catalogResult = catalogDeferred.await()
            val profileResult = profileDeferred.await()
            _cloudSyncState.value = when {
                catalogResult.isFailure -> CloudSyncState.Error(
                    catalogResult.exceptionOrNull()?.message ?: "Catalog sync paused (offline mode)"
                )
                profileResult.isFailure -> CloudSyncState.Error(
                    profileResult.exceptionOrNull()?.message ?: "Cloud sync paused (offline mode)"
                )
                else -> CloudSyncState.Synced
            }
        }
    }

    private fun startCloudCatalogListeners() {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreCatalogRepository.observeGames(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing Firestore games", firestoreError ?: error)
                    _cloudSyncState.value = CloudSyncState.Error(
                        (firestoreError ?: error).message ?: "Games sync unavailable"
                    )
                }
                .collect { gameDocs ->
                    if (gameDocs.isNotEmpty()) {
                        repository.syncGamesFromFirestore(gameDocs.map { it.toLocalEntity() })
                        _cloudSyncState.value = CloudSyncState.Synced
                    }
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            firestoreCatalogRepository.observeCheatCodes(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing Firestore cheat_codes", firestoreError ?: error)
                    _cloudSyncState.value = CloudSyncState.Error(
                        (firestoreError ?: error).message ?: "Cheat codes sync unavailable"
                    )
                }
                .collect { codeDocs ->
                    if (codeDocs.isNotEmpty()) {
                        repository.syncCheatCodesFromFirestore(codeDocs.map { it.toLocalEntity() })
                        _cloudSyncState.value = CloudSyncState.Synced
                    }
                }
        }
    }

    private fun startCloudSyncListeners() {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreSyncRepository.observeCloudFavorites(currentUserId)
                .map { docs ->
                    docs.map { doc ->
                        val id = doc.codeId.ifBlank { doc.id }
                        val ts = (doc.createdAt?.seconds ?: (System.currentTimeMillis() / 1000L)) * 1000L
                        id to ts
                    }.filter { it.first.isNotBlank() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing cloud favorites", rootError)
                    _cloudSyncState.value = CloudSyncState.Error(
                        rootError.message ?: "Favorites cloud sync unavailable"
                    )
                }
                .collect { pairs ->
                    repository.mergeCloudFavorites(pairs)
                    _cloudSyncState.value = CloudSyncState.Synced
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            firestoreSyncRepository.observeCloudRecentGames(currentUserId)
                .map { docs ->
                    docs.map { doc ->
                        val id = doc.gameId.ifBlank { doc.id }
                        val ts = (doc.updatedAt?.seconds ?: doc.createdAt?.seconds ?: 0L) * 1000L
                        id to ts
                    }.filter { it.first.isNotBlank() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing cloud recent games", rootError)
                }
                .collect { pairs ->
                    repository.mergeCloudRecentGames(pairs)
                }
        }
    }

    fun selectGame(gameId: String) {
        _selectedGameId.value = gameId
        _selectedPlatform.value = null
        _selectedCategory.value = null
        _codesSearchQuery.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordGameViewed(gameId)
            firestoreSyncRepository.recordRecentGame(gameId)
        }
    }

    fun selectPlatform(platform: GamePlatform) {
        _selectedPlatform.value = platform
        _selectedCategory.value = null
    }

    fun selectCategory(category: CheatCategory?) {
        _selectedCategory.value = category
    }

    fun updateCodesSearchQuery(query: String) {
        _codesSearchQuery.value = query
    }

    fun updateHomeSearchQuery(query: String) {
        _homeSearchQuery.value = query
    }

    fun updateFavoritesSearchQuery(query: String) {
        _favoritesSearchQuery.value = query
    }

    fun selectFavoritesGameFilter(gameId: String?) {
        _favoritesGameFilter.value = gameId
    }

    fun openCodeDetail(code: CheatCodeWithDetails) {
        _selectedCodeForDetail.value = code
    }

    fun closeCodeDetail() {
        _selectedCodeForDetail.value = null
    }

    fun toggleFavorite(code: CheatCodeWithDetails, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(code.codeId)
            val currentDetail = _selectedCodeForDetail.value
            if (currentDetail != null && currentDetail.codeId == code.codeId) {
                _selectedCodeForDetail.value = currentDetail.copy(isFavorite = isNowFavorite)
            }
            onResult(isNowFavorite)
            if (isNowFavorite) {
                firestoreSyncRepository.addFavorite(code)
            } else {
                firestoreSyncRepository.removeFavorite(code.codeId)
            }
        }
    }

    fun restoreFavorite(code: CheatCodeWithDetails) {
        viewModelScope.launch {
            repository.restoreFavorite(code.codeId)
            firestoreSyncRepository.addFavorite(code)
        }
    }

    fun submitCodeReport(
        code: CheatCodeWithDetails,
        reportType: ReportType,
        notes: String,
        onSubmitted: () -> Unit
    ) {
        viewModelScope.launch {
            repository.submitCodeReport(code, reportType, notes)
            val cloudResult = firestoreSyncRepository.submitReport(code, reportType, notes)
            if (cloudResult.isFailure) {
                _cloudSyncState.value = CloudSyncState.Error(
                    cloudResult.exceptionOrNull()?.message ?: "Report saved locally"
                )
            }
            onSubmitted()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(enabled)
            val lang = userPreferences.value.language.code
            firestoreSyncRepository.upsertUserProfile(
                displayName = userDisplayName,
                email = userEmail,
                darkTheme = enabled,
                languageCode = lang
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
            val isDark = userPreferences.value.isDarkTheme
            firestoreSyncRepository.upsertUserProfile(
                displayName = userDisplayName,
                email = userEmail,
                darkTheme = isDark,
                languageCode = language.code
            )
        }
    }

    private fun defaultCategoryDocs(): List<CategoryDoc> =
        VerifiedTutorialsAndCategoriesSeed.categories().map {
            CategoryDoc(
                id = it.categoryId,
                categoryId = it.categoryId,
                name = it.name,
                description = it.description,
                iconKey = it.iconKey,
                sortOrder = it.sortOrder
            )
        }

    private fun defaultTutorialDocs(): List<TutorialDoc> =
        VerifiedTutorialsAndCategoriesSeed.tutorials().map {
            TutorialDoc(
                id = it.tutorialId,
                tutorialId = it.tutorialId,
                title = it.title,
                platform = it.platform,
                summary = it.summary,
                steps = it.steps,
                supportedGames = it.supportedGames,
                sortOrder = it.sortOrder
            )
        }

    private companion object {
        const val TAG = "MainViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
