package com.example.data.model

import com.example.ui.i18n.AppStrings

enum class GamePlatform(val id: String, val displayName: String) {
    PC("PC", "PC"),
    PLAYSTATION("PLAYSTATION", "PlayStation"),
    PSP("PSP", "PSP"),
    XBOX("XBOX", "Xbox"),
    IN_GAME_PHONE("IN_GAME_PHONE", "In-Game Phone"),
    ANDROID("ANDROID", "Android"),
    IOS("IOS", "iOS");

    companion object {
        fun fromId(id: String): GamePlatform =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PC
    }
}

enum class CheatCategory(val id: String) {
    PLAYER("PLAYER"),
    WEAPONS("WEAPONS"),
    VEHICLES("VEHICLES"),
    POLICE("POLICE"),
    WEATHER("WEATHER"),
    SPECIAL("SPECIAL"),
    WORLD("WORLD");

    fun label(strings: AppStrings): String = when (this) {
        PLAYER -> strings.catPlayer
        WEAPONS -> strings.catWeapons
        VEHICLES -> strings.catVehicles
        POLICE -> strings.catPolice
        WEATHER -> strings.catWeather
        SPECIAL -> strings.catSpecial
        WORLD -> strings.catWorld
    }

    companion object {
        fun fromId(id: String): CheatCategory =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SPECIAL
    }
}

enum class ReportType(val id: String) {
    INCORRECT_CODE("INCORRECT_CODE"),
    UNSUPPORTED_PLATFORM("UNSUPPORTED_PLATFORM"),
    MISSING_INFO("MISSING_INFO"),
    OUTDATED_INSTRUCTIONS("OUTDATED_INSTRUCTIONS");

    fun label(strings: AppStrings): String = when (this) {
        INCORRECT_CODE -> strings.reportTypeIncorrect
        UNSUPPORTED_PLATFORM -> strings.reportTypeUnsupported
        MISSING_INFO -> strings.reportTypeMissing
        OUTDATED_INSTRUCTIONS -> strings.reportTypeOutdated
    }
}

enum class VerificationStatus(val id: String) {
    VERIFIED("VERIFIED"),
    NEEDS_VERIFICATION("needs_verification"),
    PENDING_RELEASE("PENDING_RELEASE");

    companion object {
        fun fromId(id: String): VerificationStatus =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NEEDS_VERIFICATION
    }
}
