package com.example.data.local

import com.example.data.local.VerifiedCatalogSeed.buildCode
import com.example.data.model.CheatCategory
import com.example.data.model.GamePlatform

object StoriesAndHdCodesSeed {

    fun codes(): List<CheatCodeEntity> = buildList {
        // ======================== GTA Liberty City Stories — 5 codes ========================
        val lcsPs2Inst = "PS2 uses L1/R1. Codes are entered during gameplay. The Android/iOS mobile ports are not assumed to support these sequences."
        val lcsPspInst = "PSP uses L/R shoulder buttons. Codes are entered during gameplay. The Android/iOS mobile ports are not assumed to support these sequences."

        add(
            buildCode(
                id = "txt_lcs_ps2_health",
                gameId = "gta_lcs",
                title = "Full health",
                description = "Restores full health (and repairs your current vehicle) on PS2.",
                code = "L1 R1 X L1 R1 Square L1 R1",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PS2",
                category = CheatCategory.PLAYER,
                instructions = lcsPs2Inst,
                keywords = "liberty city stories ps2 full health square"
            )
        )
        add(
            buildCode(
                id = "txt_lcs_ps2_armor",
                gameId = "gta_lcs",
                title = "Full armor",
                description = "Restores full body armor on PS2.",
                code = "L1 R1 Circle L1 R1 X L1 R1",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PS2",
                category = CheatCategory.PLAYER,
                instructions = lcsPs2Inst,
                keywords = "liberty city stories ps2 full armor circle"
            )
        )
        add(
            buildCode(
                id = "txt_lcs_ps2_money",
                gameId = "gta_lcs",
                title = "Get \$250,000",
                description = "Adds $250,000 cash on PS2.",
                code = "L1 R1 Triangle L1 R1 Circle L1 R1",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PS2",
                category = CheatCategory.PLAYER,
                instructions = lcsPs2Inst,
                keywords = "liberty city stories ps2 money cash 250000 triangle circle"
            )
        )
        add(
            buildCode(
                id = "txt_lcs_psp_health",
                gameId = "gta_lcs",
                title = "Full health",
                description = "Restores full health (and repairs your current vehicle) on PSP.",
                code = "L R X L R Square L R",
                platform = GamePlatform.PSP,
                platformLabel = "PSP",
                category = CheatCategory.PLAYER,
                instructions = lcsPspInst,
                keywords = "liberty city stories psp full health square"
            )
        )
        add(
            buildCode(
                id = "txt_lcs_psp_armor",
                gameId = "gta_lcs",
                title = "Full armor",
                description = "Restores full body armor on PSP.",
                code = "L R Circle L R X L R",
                platform = GamePlatform.PSP,
                platformLabel = "PSP",
                category = CheatCategory.PLAYER,
                instructions = lcsPspInst,
                keywords = "liberty city stories psp full armor circle"
            )
        )

        // ======================== GTA Vice City Stories — 4 codes ========================
        val vcsPs2Inst = "PS2 uses L1/R1. Enter during gameplay. Mobile availability is not universal."
        val vcsPspInst = "PSP uses L/R. Enter during gameplay. Mobile availability is not universal."

        add(
            buildCode(
                id = "txt_vcs_ps2_health",
                gameId = "gta_vcs",
                title = "Full health",
                description = "Restores full health (and repairs your current vehicle) on PS2.",
                code = "↑ ↓ ← → Circle Circle L1 R1",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PS2",
                category = CheatCategory.PLAYER,
                instructions = vcsPs2Inst,
                keywords = "vice city stories ps2 full health circle"
            )
        )
        add(
            buildCode(
                id = "txt_vcs_ps2_armor",
                gameId = "gta_vcs",
                title = "Full armor",
                description = "Restores full body armor on PS2.",
                code = "↑ ↓ ← → Square Square L1 R1",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PS2",
                category = CheatCategory.PLAYER,
                instructions = vcsPs2Inst,
                keywords = "vice city stories ps2 full armor square"
            )
        )
        add(
            buildCode(
                id = "txt_vcs_ps2_money",
                gameId = "gta_vcs",
                title = "Get \$250,000",
                description = "Adds $250,000 cash on PS2.",
                code = "↑ ↓ ← → X X L1 R1",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PS2",
                category = CheatCategory.PLAYER,
                instructions = vcsPs2Inst,
                keywords = "vice city stories ps2 money cash 250000"
            )
        )
        add(
            buildCode(
                id = "txt_vcs_psp_health",
                gameId = "gta_vcs",
                title = "Full health",
                description = "Restores full health (and repairs your current vehicle) on PSP.",
                code = "↑ ↓ ← → Circle Circle L R",
                platform = GamePlatform.PSP,
                platformLabel = "PSP",
                category = CheatCategory.PLAYER,
                instructions = vcsPspInst,
                keywords = "vice city stories psp full health circle"
            )
        )

        // ======================== GTA IV — 5 codes ========================
        val gta4Inst = "Open Niko's phone, dial the number, and call it. Some spawned vehicles/cheats depend on game version and story progress."
        val gta4PlatformLabel = "All supported platforms (in-game phone)"

        add(
            buildCode(
                id = "txt_gta4_phone_health_armor",
                gameId = "gta_4",
                title = "Max health and armor",
                description = "Restores Niko's health to maximum, refills body armor, and repairs the current vehicle.",
                code = "362-555-0100",
                platform = GamePlatform.IN_GAME_PHONE,
                platformLabel = gta4PlatformLabel,
                category = CheatCategory.PLAYER,
                instructions = gta4Inst,
                keywords = "gta 4 iv phone health armor repair 362-555-0100",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta4_phone_weapons",
                gameId = "gta_4",
                title = "Advanced weapon set",
                code = "486-555-0100",
                platform = GamePlatform.IN_GAME_PHONE,
                platformLabel = gta4PlatformLabel,
                category = CheatCategory.WEAPONS,
                instructions = gta4Inst,
                keywords = "gta 4 iv phone weapons guns 486-555-0100",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta4_phone_remove_wanted",
                gameId = "gta_4",
                title = "Remove wanted level",
                code = "267-555-0100",
                platform = GamePlatform.IN_GAME_PHONE,
                platformLabel = gta4PlatformLabel,
                category = CheatCategory.POLICE,
                instructions = gta4Inst,
                keywords = "gta 4 iv phone remove wanted police 267-555-0100"
            )
        )
        add(
            buildCode(
                id = "txt_gta4_phone_raise_wanted",
                gameId = "gta_4",
                title = "Raise wanted level",
                code = "267-555-0150",
                platform = GamePlatform.IN_GAME_PHONE,
                platformLabel = gta4PlatformLabel,
                category = CheatCategory.POLICE,
                instructions = gta4Inst,
                keywords = "gta 4 iv phone raise wanted police 267-555-0150"
            )
        )
        add(
            buildCode(
                id = "txt_gta4_phone_weather",
                gameId = "gta_4",
                title = "Change weather",
                code = "468-555-0100",
                platform = GamePlatform.IN_GAME_PHONE,
                platformLabel = gta4PlatformLabel,
                category = CheatCategory.WEATHER,
                instructions = gta4Inst,
                keywords = "gta 4 iv phone weather 468-555-0100"
            )
        )

        // ======================== GTA V — 7 codes ========================
        val gta5PcInst = "Cheats are for Story Mode only, not GTA Online. PC: press the tilde (~) key to open the cheat console, type the code, and press Enter. Activating cheats disables achievements for the current session."
        val gta5PsPhoneInst = "Cheats are for Story Mode only, not GTA Online. PlayStation 4/5: open your in-game phone keypad and dial this number (phone cheats are not supported on PS3). Disables trophies for the current session."
        val gta5XbPhoneInst = "Cheats are for Story Mode only, not GTA Online. Xbox One / Series X|S: open your in-game phone keypad and dial this number (phone cheats are not supported on Xbox 360). Disables achievements for the current session."

        add(
            buildCode(
                id = "txt_gta5_pc_painkiller",
                gameId = "gta_5",
                title = "Invincibility for 5 minutes",
                code = "PAINKILLER",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = gta5PcInst,
                keywords = "gta 5 v pc invincibility god mode painkiller",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta5_pc_turtle",
                gameId = "gta_5",
                title = "Max health and armor",
                code = "TURTLE",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = gta5PcInst,
                keywords = "gta 5 v pc max health armor turtle",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta5_pc_toolup",
                gameId = "gta_5",
                title = "All weapons and ammo",
                code = "TOOLUP",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.WEAPONS,
                instructions = gta5PcInst,
                keywords = "gta 5 v pc weapons guns ammo toolup",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta5_pc_lawyerup",
                gameId = "gta_5",
                title = "Lower wanted level",
                code = "LAWYERUP",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.POLICE,
                instructions = gta5PcInst,
                keywords = "gta 5 v pc lower wanted police lawyerup"
            )
        )
        add(
            buildCode(
                id = "txt_gta5_pc_buzzoff",
                gameId = "gta_5",
                title = "Spawn Buzzard helicopter",
                code = "BUZZOFF",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.VEHICLES,
                instructions = gta5PcInst,
                keywords = "gta 5 v pc buzzard helicopter vehicle buzzoff",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta5_ps_phone_painkiller",
                gameId = "gta_5",
                title = "Invincibility for 5 minutes",
                code = "1-999-724-654-5537",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 4/5 phone",
                category = CheatCategory.PLAYER,
                instructions = gta5PsPhoneInst,
                keywords = "gta 5 v playstation ps4 ps5 phone invincibility 1-999-724-654-5537"
            )
        )
        add(
            buildCode(
                id = "txt_gta5_xb_phone_painkiller",
                gameId = "gta_5",
                title = "Invincibility for 5 minutes",
                description = "Grants temporary invincibility for 5 minutes via in-game phone on Xbox One and Series X|S.",
                code = "1-999-724-654-5537",
                platform = GamePlatform.XBOX,
                platformLabel = "Xbox One / Series X|S phone",
                category = CheatCategory.PLAYER,
                instructions = gta5XbPhoneInst,
                keywords = "gta 5 v xbox one series phone invincibility 1-999-724-654-5537"
            )
        )
    }
}
