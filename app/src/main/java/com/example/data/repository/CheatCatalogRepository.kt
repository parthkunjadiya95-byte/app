package com.example.data.repository

import com.example.data.local.CheatCodeWithDetails
import com.example.data.local.CodeReportEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.GameWithCodeCount
import com.example.data.local.GtaDao
import com.example.data.local.RecentGameEntity
import com.example.data.local.VerifiedCatalogSeed
import com.example.data.model.ReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CheatCatalogRepository(private val dao: GtaDao) {

    val allGames: Flow<List<GameWithCodeCount>> = dao.observeAllGamesWithCounts()
    val recentGames: Flow<List<GameWithCodeCount>> = dao.observeRecentGames()
    val popularCodes: Flow<List<CheatCodeWithDetails>> = dao.observePopularCodes()
    val favoriteCodes: Flow<List<CheatCodeWithDetails>> = dao.observeFavoriteCodes()
    val submittedReports: Flow<List<CodeReportEntity>> = dao.observeAllReports()
    val totalCodeCount: Flow<Int> = dao.observeTotalCodeCount()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val seedGames = VerifiedCatalogSeed.games()
        if (dao.getGameCount() < seedGames.size) {
            dao.insertGames(seedGames)
        }
        val seedCodes = VerifiedCatalogSeed.allCheatCodes()
        val existingIds = dao.getAllCheatCodeIds().toHashSet()
        val expectedIds = seedCodes.mapTo(HashSet(seedCodes.size)) { it.codeId }
        if (existingIds != expectedIds) {
            dao.replaceCheatCodesPreservingFavorites(seedCodes)
        }
    }

    suspend fun syncGamesFromFirestore(games: List<com.example.data.local.GameEntity>) {
        if (games.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                dao.insertGames(games)
            }
        }
    }

    suspend fun syncCheatCodesFromFirestore(codes: List<com.example.data.local.CheatCodeEntity>) {
        if (codes.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                dao.replaceCheatCodesPreservingFavorites(codes)
            }
        }
    }

    fun observeCodesForGame(gameId: String): Flow<List<CheatCodeWithDetails>> =
        dao.observeCodesByGame(gameId)

    fun searchCodes(query: String): Flow<List<CheatCodeWithDetails>> =
        dao.searchAllCodes(query.trim())

    suspend fun toggleFavorite(codeId: String): Boolean = withContext(Dispatchers.IO) {
        val currentlyFavorite = dao.isFavorite(codeId)
        if (currentlyFavorite) {
            dao.deleteFavorite(codeId)
            false
        } else {
            dao.insertFavorite(FavoriteEntity(codeId = codeId))
            true
        }
    }

    suspend fun restoreFavorite(codeId: String) = withContext(Dispatchers.IO) {
        dao.insertFavorite(FavoriteEntity(codeId = codeId))
    }

    suspend fun recordGameViewed(gameId: String) = withContext(Dispatchers.IO) {
        dao.upsertRecentGame(RecentGameEntity(gameId = gameId, viewedAt = System.currentTimeMillis()))
    }

    suspend fun mergeCloudFavorites(codeIdsWithTimestamps: List<Pair<String, Long>>) {
        if (codeIdsWithTimestamps.isEmpty()) return
        withContext(Dispatchers.IO) {
            dao.insertFavorites(
                codeIdsWithTimestamps.map { (codeId, ts) -> FavoriteEntity(codeId = codeId, savedAt = ts) }
            )
        }
    }

    suspend fun mergeCloudRecentGames(gameIdsWithTimestamps: List<Pair<String, Long>>) {
        if (gameIdsWithTimestamps.isEmpty()) return
        withContext(Dispatchers.IO) {
            dao.upsertRecentGames(
                gameIdsWithTimestamps.map { (gameId, ts) -> RecentGameEntity(gameId = gameId, viewedAt = ts) }
            )
        }
    }

    suspend fun submitCodeReport(
        code: CheatCodeWithDetails,
        reportType: ReportType,
        notes: String
    ) = withContext(Dispatchers.IO) {
        dao.insertCodeReport(
            CodeReportEntity(
                codeId = code.codeId,
                codeTitle = code.title,
                gameName = code.gameName,
                platform = code.platform,
                reportType = reportType.id,
                description = notes.trim(),
                status = "Synced to Cloud"
            )
        )
    }
}
