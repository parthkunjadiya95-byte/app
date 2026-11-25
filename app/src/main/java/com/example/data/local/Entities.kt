package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.CheatCategory
import com.example.data.model.GamePlatform
import com.example.data.model.VerificationStatus

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val gameId: String,
    val name: String,
    val shortName: String,
    val releaseYear: String,
    val eraLabel: String,
    val description: String,
    val platformsCsv: String,
    val artworkKey: String,
    val isFeatured: Boolean,
    val hasVerifiedCodes: Boolean,
    val statusNote: String,
    val sortOrder: Int
) {
    fun supportedPlatforms(): List<GamePlatform> =
        platformsCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { GamePlatform.fromId(it) }
}

@Entity(
    tableName = "cheat_codes",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["gameId"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["gameId"]),
        Index(value = ["platform"]),
        Index(value = ["category"])
    ]
)
data class CheatCodeEntity(
    @PrimaryKey val codeId: String,
    val gameId: String,
    val gameName: String,
    val title: String,
    val description: String,
    val code: String,
    val platform: String,
    val platformLabel: String,
    val category: String,
    val instructions: String,
    val keywords: String,
    val isPopular: Boolean,
    val verificationStatus: String,
    val updatedAt: String
)

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = CheatCodeEntity::class,
            parentColumns = ["codeId"],
            childColumns = ["codeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteEntity(
    @PrimaryKey val codeId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recent_games",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["gameId"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecentGameEntity(
    @PrimaryKey val gameId: String,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "code_reports")
data class CodeReportEntity(
    @PrimaryKey(autoGenerate = true) val reportId: Long = 0L,
    val codeId: String,
    val codeTitle: String,
    val gameName: String,
    val platform: String,
    val reportType: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Saved Locally"
)

data class CheatCodeWithDetails(
    val codeId: String,
    val gameId: String,
    val gameName: String,
    val gameShortName: String,
    val title: String,
    val description: String,
    val code: String,
    val platform: String,
    val platformLabel: String,
    val category: String,
    val instructions: String,
    val keywords: String,
    val isPopular: Boolean,
    val verificationStatus: String,
    val updatedAt: String,
    val isFavorite: Boolean
) {
    val platformEnum: GamePlatform get() = GamePlatform.fromId(platform)
    val categoryEnum: CheatCategory get() = CheatCategory.fromId(category)
    val isVerified: Boolean
        get() = verificationStatus.equals(VerificationStatus.VERIFIED.id, ignoreCase = true)
}

data class GameWithCodeCount(
    val gameId: String,
    val name: String,
    val shortName: String,
    val releaseYear: String,
    val eraLabel: String,
    val description: String,
    val platformsCsv: String,
    val artworkKey: String,
    val isFeatured: Boolean,
    val hasVerifiedCodes: Boolean,
    val statusNote: String,
    val sortOrder: Int,
    val codeCount: Int
) {
    fun supportedPlatforms(): List<GamePlatform> =
        platformsCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { GamePlatform.fromId(it) }
}
