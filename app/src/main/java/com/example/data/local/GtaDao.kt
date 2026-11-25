package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GtaDao {

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int

    @Query("SELECT codeId FROM cheat_codes")
    suspend fun getAllCheatCodeIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheatCodes(codes: List<CheatCodeEntity>)

    @Query("DELETE FROM cheat_codes WHERE codeId NOT IN (:keepCodeIds)")
    suspend fun deleteCheatCodesNotIn(keepCodeIds: List<String>)

    @Transaction
    suspend fun replaceCheatCodesPreservingFavorites(codes: List<CheatCodeEntity>) {
        insertCheatCodes(codes)
        if (codes.isNotEmpty()) {
            deleteCheatCodesNotIn(codes.map { it.codeId })
        }
    }

    @Query(
        """
        SELECT g.*, COUNT(c.codeId) AS codeCount
        FROM games g
        LEFT JOIN cheat_codes c ON g.gameId = c.gameId
        GROUP BY g.gameId
        ORDER BY g.sortOrder ASC
        """
    )
    fun observeAllGamesWithCounts(): Flow<List<GameWithCodeCount>>

    @Query(
        """
        SELECT g.*, COUNT(c.codeId) AS codeCount
        FROM games g
        INNER JOIN recent_games r ON g.gameId = r.gameId
        LEFT JOIN cheat_codes c ON g.gameId = c.gameId
        GROUP BY g.gameId
        ORDER BY r.viewedAt DESC
        LIMIT 6
        """
    )
    fun observeRecentGames(): Flow<List<GameWithCodeCount>>

    @Query(
        """
        SELECT c.codeId, c.gameId, g.name AS gameName, g.shortName AS gameShortName,
               c.title, c.description, c.code, c.platform, c.platformLabel, c.category, c.instructions, c.keywords,
               c.isPopular, c.verificationStatus, c.updatedAt,
               CASE WHEN f.codeId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite
        FROM cheat_codes c
        INNER JOIN games g ON c.gameId = g.gameId
        LEFT JOIN favorites f ON c.codeId = f.codeId
        WHERE c.isPopular = 1
        ORDER BY g.sortOrder DESC, c.title ASC
        LIMIT 12
        """
    )
    fun observePopularCodes(): Flow<List<CheatCodeWithDetails>>

    @Query(
        """
        SELECT c.codeId, c.gameId, g.name AS gameName, g.shortName AS gameShortName,
               c.title, c.description, c.code, c.platform, c.platformLabel, c.category, c.instructions, c.keywords,
               c.isPopular, c.verificationStatus, c.updatedAt,
               CASE WHEN f.codeId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite
        FROM cheat_codes c
        INNER JOIN games g ON c.gameId = g.gameId
        LEFT JOIN favorites f ON c.codeId = f.codeId
        WHERE c.gameId = :gameId
        ORDER BY c.category ASC, c.title ASC
        """
    )
    fun observeCodesByGame(gameId: String): Flow<List<CheatCodeWithDetails>>

    @Query(
        """
        SELECT c.codeId, c.gameId, g.name AS gameName, g.shortName AS gameShortName,
               c.title, c.description, c.code, c.platform, c.platformLabel, c.category, c.instructions, c.keywords,
               c.isPopular, c.verificationStatus, c.updatedAt,
               CASE WHEN f.codeId IS NOT NULL THEN 1 ELSE 0 END AS isFavorite
        FROM cheat_codes c
        INNER JOIN games g ON c.gameId = g.gameId
        LEFT JOIN favorites f ON c.codeId = f.codeId
        WHERE c.title LIKE '%' || :query || '%'
           OR c.description LIKE '%' || :query || '%'
           OR c.code LIKE '%' || :query || '%'
           OR c.keywords LIKE '%' || :query || '%'
           OR c.category LIKE '%' || :query || '%'
           OR c.platform LIKE '%' || :query || '%'
           OR c.platformLabel LIKE '%' || :query || '%'
           OR g.name LIKE '%' || :query || '%'
           OR g.shortName LIKE '%' || :query || '%'
        ORDER BY g.sortOrder DESC, c.isPopular DESC, c.title ASC
        LIMIT 80
        """
    )
    fun searchAllCodes(query: String): Flow<List<CheatCodeWithDetails>>

    @Query(
        """
        SELECT c.codeId, c.gameId, g.name AS gameName, g.shortName AS gameShortName,
               c.title, c.description, c.code, c.platform, c.platformLabel, c.category, c.instructions, c.keywords,
               c.isPopular, c.verificationStatus, c.updatedAt,
               1 AS isFavorite
        FROM favorites f
        INNER JOIN cheat_codes c ON f.codeId = c.codeId
        INNER JOIN games g ON c.gameId = g.gameId
        ORDER BY f.savedAt DESC
        """
    )
    fun observeFavoriteCodes(): Flow<List<CheatCodeWithDetails>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE codeId = :codeId)")
    suspend fun isFavorite(codeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE codeId = :codeId")
    suspend fun deleteFavorite(codeId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentGame(recentGame: RecentGameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentGames(recentGames: List<RecentGameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeReport(report: CodeReportEntity)

    @Query("SELECT * FROM code_reports ORDER BY createdAt DESC")
    fun observeAllReports(): Flow<List<CodeReportEntity>>

    @Query("SELECT COUNT(*) FROM cheat_codes")
    fun observeTotalCodeCount(): Flow<Int>
}
