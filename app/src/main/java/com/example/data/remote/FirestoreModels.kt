package com.example.data.remote

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class GameDoc(
    @DocumentId val id: String = "",
    val gameId: String = "",
    val name: String = "",
    val shortName: String = "",
    val releaseYear: String = "",
    val eraLabel: String = "",
    val description: String = "",
    val platforms: List<String> = emptyList(),
    val artworkKey: String = "classic",
    val featured: Boolean = false,
    val hasVerifiedCodes: Boolean = true,
    val statusNote: String = "",
    val sortOrder: Int = 1,
    val visibility: String = "PUBLIC",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CheatCodeDoc(
    @DocumentId val id: String = "",
    val codeId: String = "",
    val gameId: String = "",
    val gameName: String = "",
    val title: String = "",
    val description: String = "",
    val code: String = "",
    val platform: String = "PC",
    val platformLabel: String = "",
    val category: String = "PLAYER",
    val instructions: String = "",
    val keywords: String = "",
    val popular: Boolean = false,
    val verificationStatus: String = "VERIFIED",
    val visibility: String = "PUBLIC",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CategoryDoc(
    @DocumentId val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val iconKey: String = "special",
    val sortOrder: Int = 1,
    val visibility: String = "PUBLIC",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TutorialDoc(
    @DocumentId val id: String = "",
    val tutorialId: String = "",
    val title: String = "",
    val platform: String = "ALL",
    val summary: String = "",
    val steps: List<String> = emptyList(),
    val supportedGames: List<String> = emptyList(),
    val sortOrder: Int = 1,
    val visibility: String = "PUBLIC",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val darkTheme: Boolean = true,
    val languageCode: String = "en",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudFavoriteDoc(
    @DocumentId val id: String = "",
    val codeId: String = "",
    val gameId: String = "",
    val title: String = "",
    val code: String = "",
    val platform: String = "PC",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudRecentGameDoc(
    @DocumentId val id: String = "",
    val gameId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudCodeReportDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val codeId: String = "",
    val codeTitle: String = "",
    val gameId: String = "",
    val gameName: String = "",
    val platform: String = "PC",
    val reportType: String = "INCORRECT_CODE",
    val description: String? = null,
    val status: String = "SUBMITTED",
    @ServerTimestamp val createdAt: Timestamp? = null
)
