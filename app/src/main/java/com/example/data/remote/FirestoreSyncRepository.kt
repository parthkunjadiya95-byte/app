package com.example.data.remote

import com.example.data.local.CheatCodeWithDetails
import com.example.data.model.ReportType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private val validIdRegex = Regex("^[a-zA-Z0-9_\\-]+$")
    private val validPlatforms = setOf(
        "PC", "PLAYSTATION", "PSP", "XBOX", "IN_GAME_PHONE", "ANDROID", "IOS"
    )
    private val validLanguages = setOf("en", "hi", "gu")

    private fun requireUserId(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun requireValidId(id: String, label: String) {
        require(id.isNotBlank() && id.length <= 128 && validIdRegex.matches(id)) {
            "Invalid $label identifier"
        }
    }

    fun observeCloudFavorites(userId: String): Flow<List<CloudFavoriteDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/favorites"
        emitAll(
            db.collection("users").document(userId).collection("favorites")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudFavoriteDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
                .flowOn(Dispatchers.IO)
        )
    }

    fun observeCloudRecentGames(userId: String): Flow<List<CloudRecentGameDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/recent_games"
        emitAll(
            db.collection("users").document(userId).collection("recent_games")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudRecentGameDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
                .flowOn(Dispatchers.IO)
        )
    }

    fun observeCloudReports(userId: String): Flow<List<CloudCodeReportDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "reports"
        emitAll(
            db.collection("reports")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudCodeReportDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
                .flowOn(Dispatchers.IO)
        )
    }

    suspend fun upsertUserProfile(
        displayName: String,
        email: String?,
        darkTheme: Boolean,
        languageCode: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var docPath = "users/unauthenticated"
        try {
            val uid = requireUserId()
            requireValidId(uid, "userId")
            val safeName = displayName.trim().ifBlank { "GTA Player" }.take(100)
            val safeEmail = email?.trim()?.takeIf { it.isNotEmpty() }?.take(200)
            val safeLang = if (languageCode in validLanguages) languageCode else "en"

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updatePayload = buildMap<String, Any> {
                    put("displayName", safeName)
                    if (safeEmail != null) put("email", safeEmail)
                    put("darkTheme", darkTheme)
                    put("languageCode", safeLang)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "darkTheme" to darkTheme,
                    "languageCode" to safeLang,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addFavorite(code: CheatCodeWithDetails): Result<Unit> = withContext(Dispatchers.IO) {
        var docPath = "users/unauthenticated/favorites/${code.codeId}"
        try {
            val uid = requireUserId()
            requireValidId(code.codeId, "codeId")
            requireValidId(code.gameId, "gameId")
            val safePlatform = if (code.platform in validPlatforms) code.platform else "PC"
            val docRef = db.collection("users").document(uid)
                .collection("favorites").document(code.codeId)
            docPath = docRef.path
            val payload = mapOf(
                "codeId" to code.codeId,
                "gameId" to code.gameId,
                "title" to code.title.trim().take(200).ifEmpty { "Cheat Code" },
                "code" to code.code.trim().take(250).ifEmpty { "N/A" },
                "platform" to safePlatform,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(codeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        var docPath = "users/unauthenticated/favorites/$codeId"
        try {
            val uid = requireUserId()
            requireValidId(codeId, "codeId")
            val docRef = db.collection("users").document(uid)
                .collection("favorites").document(codeId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun recordRecentGame(gameId: String): Result<Unit> = withContext(Dispatchers.IO) {
        var docPath = "users/unauthenticated/recent_games/$gameId"
        try {
            val uid = requireUserId()
            requireValidId(gameId, "gameId")
            val docRef = db.collection("users").document(uid)
                .collection("recent_games").document(gameId)
            docPath = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(mapOf("updatedAt" to FieldValue.serverTimestamp())).await()
            } else {
                docRef.set(
                    mapOf(
                        "gameId" to gameId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitReport(
        code: CheatCodeWithDetails,
        reportType: ReportType,
        notes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var docPath = "reports"
        try {
            val uid = requireUserId()
            requireValidId(code.codeId, "codeId")
            requireValidId(code.gameId, "gameId")
            val safePlatform = if (code.platform in validPlatforms) code.platform else "PC"
            val trimmedNotes = notes.trim().take(500).takeIf { it.isNotEmpty() }
            val docRef = db.collection("reports").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "codeId" to code.codeId,
                "codeTitle" to code.title.trim().take(200).ifEmpty { "Cheat Code" },
                "gameId" to code.gameId,
                "gameName" to code.gameName.trim().take(120).ifEmpty { "GTA" },
                "platform" to safePlatform,
                "reportType" to reportType.id,
                "description" to trimmedNotes,
                "status" to "SUBMITTED",
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }
}
