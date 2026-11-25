package com.example.data.remote

import com.example.data.local.CheatCodeEntity
import com.example.data.local.GameEntity
import com.example.data.local.VerifiedCatalogSeed
import com.example.data.local.VerifiedTutorialsAndCategoriesSeed
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

class FirestoreCatalogRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private fun requireUserId(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun isBootstrappedOwnerSignedIn(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(BOOTSTRAPPED_OWNER_EMAIL, ignoreCase = true) && user.isEmailVerified
    }

    /**
     * Populates and safely maintains the public catalog collections (`games`, `categories`,
     * `tutorials`, and `cheat_codes`) using duplicate-aware, non-destructive updates.
     *
     * Never deletes unrelated games or cheat codes, never writes GTA VI cheat codes,
     * never creates fake user reports, and never creates fake admin accounts.
     */
    suspend fun ensureCatalogPopulated(): Result<Unit> = withContext(Dispatchers.IO) {
        var currentPath = "games"
        try {
            val uid = requireUserId()
            val canOverwriteExisting = isBootstrappedOwnerSignedIn()

            val existingGameIds = db.collection("games")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .get()
                .await()
                .documents
                .mapTo(mutableSetOf()) { it.id }

            currentPath = "categories"
            val existingCategoryIds = db.collection("categories")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .get()
                .await()
                .documents
                .mapTo(mutableSetOf()) { it.id }

            currentPath = "tutorials"
            val existingTutorialIds = db.collection("tutorials")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .get()
                .await()
                .documents
                .mapTo(mutableSetOf()) { it.id }

            // Batch 1: games, categories, and tutorials MUST commit before cheat_codes
            // so Firestore rules `exists()` referential checks pass for games and categories.
            val gamesToCreate = VerifiedCatalogSeed.games().filter { it.gameId !in existingGameIds }
            val gamesToUpdate = if (canOverwriteExisting) {
                VerifiedCatalogSeed.games().filter { it.gameId in existingGameIds }
            } else {
                emptyList()
            }
            val missingCategories = VerifiedTutorialsAndCategoriesSeed.categories()
                .filter { it.categoryId !in existingCategoryIds }
            val missingTutorials = VerifiedTutorialsAndCategoriesSeed.tutorials()
                .filter { it.tutorialId !in existingTutorialIds }

            if (gamesToCreate.isNotEmpty() || gamesToUpdate.isNotEmpty() ||
                missingCategories.isNotEmpty() || missingTutorials.isNotEmpty()
            ) {
                currentPath = "games+categories+tutorials"
                val metaBatch = db.batch()

                gamesToCreate.forEach { game ->
                    val platforms = if (game.gameId == "gta_6") {
                        emptyList()
                    } else {
                        game.supportedPlatforms().map { it.id }
                    }
                    val docRef = db.collection("games").document(game.gameId)
                    metaBatch.set(
                        docRef,
                        mapOf(
                            "gameId" to game.gameId,
                            "name" to game.name,
                            "shortName" to game.shortName,
                            "releaseYear" to game.releaseYear,
                            "eraLabel" to game.eraLabel,
                            "description" to game.description,
                            "platforms" to platforms,
                            "artworkKey" to game.artworkKey,
                            "featured" to game.isFeatured,
                            "hasVerifiedCodes" to (game.gameId != "gta_6" && game.hasVerifiedCodes),
                            "statusNote" to game.statusNote,
                            "sortOrder" to game.sortOrder,
                            "visibility" to VISIBILITY_PUBLIC,
                            "createdBy" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                gamesToUpdate.forEach { game ->
                    val platforms = if (game.gameId == "gta_6") {
                        emptyList()
                    } else {
                        game.supportedPlatforms().map { it.id }
                    }
                    val docRef = db.collection("games").document(game.gameId)
                    metaBatch.update(
                        docRef,
                        mapOf(
                            "name" to game.name,
                            "shortName" to game.shortName,
                            "releaseYear" to game.releaseYear,
                            "eraLabel" to game.eraLabel,
                            "description" to game.description,
                            "platforms" to platforms,
                            "artworkKey" to game.artworkKey,
                            "featured" to game.isFeatured,
                            "hasVerifiedCodes" to (game.gameId != "gta_6" && game.hasVerifiedCodes),
                            "statusNote" to game.statusNote,
                            "sortOrder" to game.sortOrder,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                missingCategories.forEach { cat ->
                    val docRef = db.collection("categories").document(cat.categoryId)
                    metaBatch.set(
                        docRef,
                        mapOf(
                            "categoryId" to cat.categoryId,
                            "name" to cat.name,
                            "description" to cat.description,
                            "iconKey" to cat.iconKey,
                            "sortOrder" to cat.sortOrder,
                            "visibility" to VISIBILITY_PUBLIC,
                            "createdBy" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                missingTutorials.forEach { tut ->
                    val docRef = db.collection("tutorials").document(tut.tutorialId)
                    metaBatch.set(
                        docRef,
                        mapOf(
                            "tutorialId" to tut.tutorialId,
                            "title" to tut.title,
                            "platform" to tut.platform,
                            "summary" to tut.summary,
                            "steps" to tut.steps,
                            "supportedGames" to tut.supportedGames,
                            "sortOrder" to tut.sortOrder,
                            "visibility" to VISIBILITY_PUBLIC,
                            "createdBy" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                metaBatch.commit().await()
            }

            // Batch 2+: Safe, duplicate-aware updates for audited cheat_codes (never GTA VI,
            // never deleting or overwriting unrelated cheat codes).
            currentPath = "cheat_codes"
            val existingCodeSnapshots = db.collection("cheat_codes")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .get()
                .await()
                .toObjects(
                    CheatCodeDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )

            val existingById = existingCodeSnapshots.associateBy { it.codeId.ifBlank { it.id } }
            val existingDuplicateKeys = existingCodeSnapshots
                .mapTo(mutableSetOf()) { duplicateKey(it.gameId, it.platform, it.code) }

            val targetCodes = VerifiedCatalogSeed.allCheatCodes().filter { it.gameId != "gta_6" }
            val missingCodes = targetCodes.filter { code ->
                code.codeId !in existingById &&
                    duplicateKey(code.gameId, code.platform, code.code) !in existingDuplicateKeys
            }

            missingCodes.chunked(MAX_BATCH_OPS).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { code ->
                    val docRef = db.collection("cheat_codes").document(code.codeId)
                    batch.set(
                        docRef,
                        mapOf(
                            "codeId" to code.codeId,
                            "gameId" to code.gameId,
                            "gameName" to code.gameName,
                            "title" to code.title,
                            "description" to code.description,
                            "code" to code.code,
                            "platform" to code.platform,
                            "platformLabel" to code.platformLabel,
                            "category" to code.category,
                            "instructions" to code.instructions,
                            "keywords" to code.keywords,
                            "popular" to code.isPopular,
                            "verificationStatus" to code.verificationStatus,
                            "visibility" to VISIBILITY_PUBLIC,
                            "createdBy" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.commit().await()
            }

            // When the bootstrapped owner signs in, safely update ONLY matching audited code IDs
            // whose content changed (preserving IDs and never deleting unrelated documents).
            if (canOverwriteExisting) {
                val codesToUpdate = targetCodes.filter { code ->
                    val existing = existingById[code.codeId] ?: return@filter false
                    existing.gameName != code.gameName ||
                        existing.title != code.title ||
                        existing.description != code.description ||
                        existing.code != code.code ||
                        existing.platform != code.platform ||
                        existing.platformLabel != code.platformLabel ||
                        existing.category != code.category ||
                        existing.instructions != code.instructions ||
                        existing.keywords != code.keywords ||
                        existing.popular != code.isPopular ||
                        existing.verificationStatus != code.verificationStatus
                }
                codesToUpdate.chunked(MAX_BATCH_OPS).forEach { chunk ->
                    val updateBatch = db.batch()
                    chunk.forEach { code ->
                        val docRef = db.collection("cheat_codes").document(code.codeId)
                        updateBatch.update(
                            docRef,
                            mapOf(
                                "gameName" to code.gameName,
                                "title" to code.title,
                                "description" to code.description,
                                "code" to code.code,
                                "platform" to code.platform,
                                "platformLabel" to code.platformLabel,
                                "category" to code.category,
                                "instructions" to code.instructions,
                                "keywords" to code.keywords,
                                "popular" to code.isPopular,
                                "verificationStatus" to code.verificationStatus,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    updateBatch.commit().await()
                }
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, currentPath)
            Result.failure(e)
        }
    }

    fun observeGames(userId: String): Flow<List<GameDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "games"
        emitAll(
            db.collection("games")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GameDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.sortOrder }
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

    fun observeCheatCodes(userId: String): Flow<List<CheatCodeDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "cheat_codes"
        emitAll(
            db.collection("cheat_codes")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .snapshots()
                .map { snapshot ->
                    val allDocs = snapshot.toObjects(
                        CheatCodeDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).filter { it.gameId != "gta_6" }
                    val seenKeys = mutableSetOf<String>()
                    allDocs
                        .sortedByDescending { (it.codeId.ifBlank { it.id }).startsWith("txt_") }
                        .filter { doc ->
                            seenKeys.add(duplicateKey(doc.gameId, doc.platform, doc.code))
                        }
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

    fun observeCategories(userId: String): Flow<List<CategoryDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "categories"
        emitAll(
            db.collection("categories")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CategoryDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.sortOrder }
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

    fun observeTutorials(userId: String): Flow<List<TutorialDoc>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "tutorials"
        emitAll(
            db.collection("tutorials")
                .whereEqualTo("visibility", VISIBILITY_PUBLIC)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TutorialDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.sortOrder }
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

    companion object {
        const val VISIBILITY_PUBLIC = "PUBLIC"
        private const val BOOTSTRAPPED_OWNER_EMAIL = "parthkunjadiya95@gmail.com"
        private const val MAX_BATCH_OPS = 400

        private fun duplicateKey(gameId: String, platform: String, code: String): String =
            "${gameId.trim().lowercase()}|${platform.trim().uppercase()}|${code.trim().uppercase()}"

        fun GameDoc.toLocalEntity(): GameEntity {
            val resolvedId = gameId.ifBlank { id }
            return GameEntity(
                gameId = resolvedId,
                name = name,
                shortName = shortName,
                releaseYear = releaseYear,
                eraLabel = eraLabel,
                description = description,
                platformsCsv = if (resolvedId == "gta_6") "" else platforms.joinToString(","),
                artworkKey = artworkKey,
                isFeatured = featured,
                hasVerifiedCodes = resolvedId != "gta_6" && hasVerifiedCodes,
                statusNote = statusNote,
                sortOrder = sortOrder
            )
        }

        fun CheatCodeDoc.toLocalEntity(): CheatCodeEntity {
            val resolvedId = codeId.ifBlank { id }
            val resolvedStatus = verificationStatus.ifBlank { "needs_verification" }
            return CheatCodeEntity(
                codeId = resolvedId,
                gameId = gameId,
                gameName = gameName.ifBlank { VerifiedCatalogSeed.gameNameForId(gameId) },
                title = title,
                description = description.ifBlank { title },
                code = code,
                platform = platform,
                platformLabel = platformLabel.ifBlank { platform },
                category = category,
                instructions = instructions,
                keywords = keywords,
                isPopular = popular,
                verificationStatus = resolvedStatus,
                updatedAt = if (resolvedStatus.equals("VERIFIED", ignoreCase = true)) {
                    "Verified via Firestore"
                } else {
                    "Needs Verification"
                }
            )
        }
    }
}
