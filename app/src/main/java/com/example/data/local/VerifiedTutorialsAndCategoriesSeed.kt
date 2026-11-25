package com.example.data.local

data class VerifiedCategorySeed(
    val categoryId: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val sortOrder: Int
)

data class VerifiedTutorialSeed(
    val tutorialId: String,
    val title: String,
    val platform: String,
    val summary: String,
    val steps: List<String>,
    val supportedGames: List<String>,
    val sortOrder: Int
)

object VerifiedTutorialsAndCategoriesSeed {

    fun categories(): List<VerifiedCategorySeed> = listOf(
        VerifiedCategorySeed(
            categoryId = "PLAYER",
            name = "Player Cheats",
            description = "Health, armor, cash, invincibility, sprint speed, and player stat boosts.",
            iconKey = "player",
            sortOrder = 1
        ),
        VerifiedCategorySeed(
            categoryId = "WEAPONS",
            name = "Weapon Cheats",
            description = "Weapon sets, infinite ammunition, explosive rounds, and combat gear.",
            iconKey = "weapons",
            sortOrder = 2
        ),
        VerifiedCategorySeed(
            categoryId = "VEHICLES",
            name = "Vehicle Cheats",
            description = "Spawn Rhino tanks, helicopters, jets, sports cars, bikes, and vehicle mods.",
            iconKey = "vehicles",
            sortOrder = 3
        ),
        VerifiedCategorySeed(
            categoryId = "POLICE",
            name = "Police / Wanted Level",
            description = "Lower, clear, lock, or raise your wanted star rating during free roam.",
            iconKey = "police",
            sortOrder = 4
        ),
        VerifiedCategorySeed(
            categoryId = "WEATHER",
            name = "Weather Cheats",
            description = "Switch instantly between sunny skies, rainstorms, fog, and clear weather.",
            iconKey = "weather",
            sortOrder = 5
        ),
        VerifiedCategorySeed(
            categoryId = "SPECIAL",
            name = "Special Cheats",
            description = "Super jump, parachutes, level select, and classic era unlock commands.",
            iconKey = "special",
            sortOrder = 6
        ),
        VerifiedCategorySeed(
            categoryId = "WORLD",
            name = "World & Misc",
            description = "Moon gravity, traffic explosions, and sandbox world modifiers.",
            iconKey = "world",
            sortOrder = 7
        )
    )

    fun tutorials(): List<VerifiedTutorialSeed> = listOf(
        VerifiedTutorialSeed(
            tutorialId = "tut_pc_classic",
            title = "PC Classic Keyboard Cheats",
            platform = "PC",
            summary = "Type classic GTA codes directly during active gameplay without pausing.",
            steps = listOf(
                "Load into active single-player gameplay (do not open the pause menu).",
                "Type the verified cheat word cleanly on your keyboard (case-insensitive).",
                "Look for the 'Cheat Activated' notification in the top-left corner.",
                "For GTA 1 & GTA 2 on PC, enter codes on the character name screen (in GTA 2, enter GOURANGA first)."
            ),
            supportedGames = listOf("GTA 1", "GTA 2", "GTA III", "GTA Vice City", "GTA San Andreas"),
            sortOrder = 1
        ),
        VerifiedTutorialSeed(
            tutorialId = "tut_pc_gtav",
            title = "GTA V PC Console & Phone Cheats",
            platform = "PC",
            summary = "Enter text commands using the tilde (~) key or dial 1-999 phone numbers in Story Mode.",
            steps = listOf(
                "Ensure you are in GTA V Story Mode (cheats are disabled in GTA Online).",
                "Press the tilde (~) key on your keyboard to open the in-game cheat command bar.",
                "Type the verified code (for example, PAINKILLER or TURTLE) and press Enter.",
                "Alternatively, open your character's phone, go to Contacts, press Spacebar for the keypad, and dial the 1-999 number."
            ),
            supportedGames = listOf("GTA V"),
            sortOrder = 2
        ),
        VerifiedTutorialSeed(
            tutorialId = "tut_console_buttons",
            title = "PlayStation & Xbox Button Sequences",
            platform = "PLAYSTATION",
            summary = "Enter directional D-Pad and controller button combinations smoothly in gameplay.",
            steps = listOf(
                "Stay in live single-player gameplay—do not pause the game while inputting.",
                "Always use the directional D-Pad (not the analog stick) for Up, Down, Left, and Right.",
                "Enter the entire button sequence at a steady pace without long pauses.",
                "A confirmation banner appears on-screen once the sequence registers."
            ),
            supportedGames = listOf(
                "GTA III",
                "GTA Vice City",
                "GTA San Andreas",
                "GTA Liberty City Stories",
                "GTA Vice City Stories",
                "GTA V"
            ),
            sortOrder = 3
        ),
        VerifiedTutorialSeed(
            tutorialId = "tut_gta4_phone",
            title = "GTA IV In-Game Cell Phone Dialer",
            platform = "ALL",
            summary = "Dial verified 10-digit cheat numbers on Niko Bellic's mobile phone across PC, PlayStation, and Xbox.",
            steps = listOf(
                "Complete the opening intro so Niko has access to his cell phone.",
                "Press Up twice (D-Pad Up or Up Arrow) to open the numeric phone keypad.",
                "Dial the verified 10-digit number (such as 362-555-0100) and press Call.",
                "Once activated, the cheat is permanently saved inside Niko's phone 'Cheats' menu for quick reuse."
            ),
            supportedGames = listOf("GTA IV"),
            sortOrder = 4
        ),
        VerifiedTutorialSeed(
            tutorialId = "tut_mobile_ports",
            title = "Android & iOS Port Compatibility",
            platform = "ANDROID",
            summary = "Do not assume PC or console cheats work on Android/iOS ports without verifying your exact edition.",
            steps = listOf(
                "Mobile versions of GTA III, Vice City, and San Andreas vary by release and often require an external keyboard or edition-specific input method.",
                "GTA Liberty City Stories mobile ports do not include a native on-screen cheat keyboard; PS2/PSP button sequences are not universally supported on mobile.",
                "GTA Vice City Stories, GTA IV, and GTA V do not have official Android or iOS releases.",
                "Always check the platform badge on each cheat code before trying it on a mobile device."
            ),
            supportedGames = listOf("GTA III", "GTA Vice City", "GTA San Andreas", "GTA Liberty City Stories"),
            sortOrder = 5
        ),
        VerifiedTutorialSeed(
            tutorialId = "tut_safety_tips",
            title = "Save Files, Achievements & GTA VI Policy",
            platform = "ALL",
            summary = "Protect your save slots and understand how verification works in GTA Code Master.",
            steps = listOf(
                "Always keep a separate clean save slot before activating world or vehicle cheats.",
                "In GTA IV and GTA V, cheats temporarily disable trophies and achievements for your active play session—reload a clean save to re-enable them.",
                "Cheats apply strictly to single-player Story Mode and never work in GTA Online.",
                "GTA VI is listed in the catalog with zero cheat codes until Rockstar officially releases the game and codes are verified."
            ),
            supportedGames = listOf("All GTA Titles"),
            sortOrder = 6
        )
    )
}
