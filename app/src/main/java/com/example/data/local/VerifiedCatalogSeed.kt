package com.example.data.local

import com.example.data.model.CheatCategory
import com.example.data.model.GamePlatform
import com.example.data.model.VerificationStatus

object VerifiedCatalogSeed {

    const val ARTWORK_CLASSIC = "classic"
    const val ARTWORK_TROPICAL = "tropical"
    const val ARTWORK_MODERN = "modern"
    const val ARTWORK_HERO = "hero"

    fun games(): List<GameEntity> = listOf(
        GameEntity(
            gameId = "gta_1",
            name = "GTA 1",
            shortName = "GTA 1",
            releaseYear = "1997",
            eraLabel = "2D Classic Era",
            description = "PC codes are entered as the player name/code in the original game; PlayStation uses the game's name-entry cheat system. Codes differ by version.",
            platformsCsv = "PC,PLAYSTATION",
            artworkKey = ARTWORK_CLASSIC,
            isFeatured = false,
            hasVerifiedCodes = true,
            statusNote = "PC codes are entered as the player name/code; PlayStation uses name-entry cheats.",
            sortOrder = 1
        ),
        GameEntity(
            gameId = "gta_2",
            name = "GTA 2",
            shortName = "GTA 2",
            releaseYear = "1999",
            eraLabel = "2D Classic Era",
            description = "PC name-entry codes: enter GOURANGA as your player name first to unlock cheat mode, then enter the code. Console editions use different systems.",
            platformsCsv = "PC",
            artworkKey = ARTWORK_CLASSIC,
            isFeatured = false,
            hasVerifiedCodes = true,
            statusNote = "PC name-entry codes only (enter GOURANGA first to unlock cheat mode).",
            sortOrder = 2
        ),
        GameEntity(
            gameId = "gta_3",
            name = "GTA III",
            shortName = "GTA III",
            releaseYear = "2001",
            eraLabel = "3D Universe",
            description = "PC: type during gameplay. Console: enter the button sequence during gameplay. Xbox/Definitive Edition layouts may differ.",
            platformsCsv = "PC,PLAYSTATION",
            artworkKey = ARTWORK_CLASSIC,
            isFeatured = false,
            hasVerifiedCodes = true,
            statusNote = "PC: type during gameplay. PlayStation 2: enter button sequence during gameplay.",
            sortOrder = 3
        ),
        GameEntity(
            gameId = "gta_vc",
            name = "GTA Vice City",
            shortName = "Vice City",
            releaseYear = "2002",
            eraLabel = "3D Universe",
            description = "PC: type during gameplay. Console: enter the sequence during gameplay. Some cheats are edition-specific or removed in Definitive Edition.",
            platformsCsv = "PC,PLAYSTATION",
            artworkKey = ARTWORK_TROPICAL,
            isFeatured = true,
            hasVerifiedCodes = true,
            statusNote = "PC & PlayStation 2 verified quick-reference codes. Some cheats vary by edition.",
            sortOrder = 4
        ),
        GameEntity(
            gameId = "gta_sa",
            name = "GTA San Andreas",
            shortName = "San Andreas",
            releaseYear = "2004",
            eraLabel = "3D Universe",
            description = "PC: type during gameplay. PlayStation: enter sequence during gameplay. Mobile ports differ; some editions require an external keyboard or special cheat-entry option.",
            platformsCsv = "PC,PLAYSTATION",
            artworkKey = ARTWORK_TROPICAL,
            isFeatured = true,
            hasVerifiedCodes = true,
            statusNote = "PC & PlayStation 2 verified codes. Mobile port support varies by edition.",
            sortOrder = 5
        ),
        GameEntity(
            gameId = "gta_lcs",
            name = "GTA Liberty City Stories",
            shortName = "LCS",
            releaseYear = "2005",
            eraLabel = "Stories Era",
            description = "PSP uses L/R shoulder buttons; PS2 uses L1/R1. Codes are entered during gameplay. The Android/iOS mobile ports are not assumed to support these sequences.",
            platformsCsv = "PLAYSTATION,PSP",
            artworkKey = ARTWORK_CLASSIC,
            isFeatured = false,
            hasVerifiedCodes = true,
            statusNote = "PS2 (L1/R1) & PSP (L/R) gameplay sequences. Mobile ports are not assumed to support them.",
            sortOrder = 6
        ),
        GameEntity(
            gameId = "gta_vcs",
            name = "GTA Vice City Stories",
            shortName = "VCS",
            releaseYear = "2006",
            eraLabel = "Stories Era",
            description = "PS2 uses L1/R1; PSP uses L/R. Enter during gameplay. Mobile availability is not universal.",
            platformsCsv = "PLAYSTATION,PSP",
            artworkKey = ARTWORK_TROPICAL,
            isFeatured = false,
            hasVerifiedCodes = true,
            statusNote = "PS2 (L1/R1) & PSP (L/R) sequences entered during gameplay.",
            sortOrder = 7
        ),
        GameEntity(
            gameId = "gta_4",
            name = "GTA IV",
            shortName = "GTA IV",
            releaseYear = "2008",
            eraLabel = "HD Universe",
            description = "Open Niko's phone, dial the number, and call it. Some spawned vehicles/cheats depend on game version and story progress.",
            platformsCsv = "IN_GAME_PHONE",
            artworkKey = ARTWORK_MODERN,
            isFeatured = true,
            hasVerifiedCodes = true,
            statusNote = "All supported platforms (in-game phone): open Niko's phone, dial the number, and call it.",
            sortOrder = 8
        ),
        GameEntity(
            gameId = "gta_5",
            name = "GTA V",
            shortName = "GTA V",
            releaseYear = "2013",
            eraLabel = "HD Universe",
            description = "Cheats are for Story Mode only, not GTA Online. PC: press tilde (~) to open the cheat console, type the code, and press Enter. PS4/PS5 & Xbox One/Series X|S: dial the in-game phone number. Activating cheats disables achievements/trophies for the current session.",
            platformsCsv = "PC,PLAYSTATION,XBOX",
            artworkKey = ARTWORK_MODERN,
            isFeatured = true,
            hasVerifiedCodes = true,
            statusNote = "Story Mode only (not GTA Online). PC uses tilde (~) console; PS4/PS5 & Xbox One/Series X|S support phone cheats.",
            sortOrder = 9
        ),
        GameEntity(
            gameId = "gta_6",
            name = "GTA VI",
            shortName = "GTA VI",
            releaseYear = "Upcoming",
            eraLabel = "Next-Gen HD",
            description = "Coming Soon — Verified codes will be added when available",
            platformsCsv = "",
            artworkKey = ARTWORK_HERO,
            isFeatured = true,
            hasVerifiedCodes = false,
            statusNote = "Coming Soon — Verified codes will be added when available",
            sortOrder = 10
        )
    )

    private val gameNamesById: Map<String, String> by lazy {
        games().associate { it.gameId to it.name }
    }

    fun gameNameForId(gameId: String): String = gameNamesById[gameId] ?: gameId

    fun allCheatCodes(): List<CheatCodeEntity> =
        ClassicEraCodesSeed.codes() + StoriesAndHdCodesSeed.codes()

    internal fun buildCode(
        id: String,
        gameId: String,
        title: String,
        code: String,
        platform: GamePlatform,
        platformLabel: String,
        category: CheatCategory,
        instructions: String,
        keywords: String,
        description: String = title,
        verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
        isPopular: Boolean = false
    ): CheatCodeEntity = CheatCodeEntity(
        codeId = id,
        gameId = gameId,
        gameName = gameNameForId(gameId),
        title = title,
        description = description,
        code = code,
        platform = platform.id,
        platformLabel = platformLabel,
        category = category.id,
        instructions = instructions,
        keywords = keywords,
        isPopular = isPopular,
        verificationStatus = verificationStatus.id,
        updatedAt = if (verificationStatus == VerificationStatus.VERIFIED) {
            "Verified Reference"
        } else {
            "Needs Verification"
        }
    )
}
