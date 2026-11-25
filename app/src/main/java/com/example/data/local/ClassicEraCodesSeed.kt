package com.example.data.local

import com.example.data.local.VerifiedCatalogSeed.buildCode
import com.example.data.model.CheatCategory
import com.example.data.model.GamePlatform

object ClassicEraCodesSeed {

    fun codes(): List<CheatCodeEntity> = buildList {
        // ======================== GTA 1 (1997) — 6 codes ========================
        val gta1PcInst = "PC codes are entered as the player name/code in the original game. Codes differ by version."
        val gta1PsInst = "PlayStation uses the game's name-entry cheat system. Codes differ by version."

        add(
            buildCode(
                id = "txt_gta1_pc_itsgallus",
                gameId = "gta_1",
                title = "Unlock all cities and chapters",
                description = "Unlocks all cities and levels on classic PC releases (alternative PC code: NINEINAROW).",
                code = "ITSGALLUS",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.SPECIAL,
                instructions = gta1PcInst,
                keywords = "gta 1 pc cities chapters unlock itsgallus nineinarow"
            )
        )
        add(
            buildCode(
                id = "txt_gta1_pc_iamthelaw",
                gameId = "gta_1",
                title = "No police",
                description = "Disables police pursuit and wanted level on PC.",
                code = "IAMTHELAW",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.POLICE,
                instructions = gta1PcInst,
                keywords = "gta 1 pc police cops wanted iamthelaw"
            )
        )
        add(
            buildCode(
                id = "txt_gta1_pc_suckmyrocket",
                gameId = "gta_1",
                title = "All weapons and armor",
                description = "Grants all weapons, full armor, and a Get Out of Jail Free card on PC.",
                code = "SUCKMYROCKET",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.WEAPONS,
                instructions = gta1PcInst,
                keywords = "gta 1 pc weapons guns armor suckmyrocket"
            )
        )
        add(
            buildCode(
                id = "txt_gta1_ps_bstard",
                gameId = "gta_1",
                title = "All cities + weapons + 99 lives",
                description = "Unlocks all cities, grants all weapons with ammo, and sets lives to 99 on PlayStation.",
                code = "BSTARD",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation",
                category = CheatCategory.SPECIAL,
                instructions = gta1PsInst,
                keywords = "gta 1 playstation cities weapons lives bstard"
            )
        )
        add(
            buildCode(
                id = "txt_gta1_ps_satanlives",
                gameId = "gta_1",
                title = "99 lives",
                description = "Sets player lives to 99 on PlayStation.",
                code = "SATANLIVES",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation",
                category = CheatCategory.PLAYER,
                instructions = gta1PsInst,
                keywords = "gta 1 playstation 99 lives health satanlives"
            )
        )
        add(
            buildCode(
                id = "txt_gta1_ps_skybabies",
                gameId = "gta_1",
                title = "Level select",
                description = "Enables the level select screen on PlayStation.",
                code = "SKYBABIES",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation",
                category = CheatCategory.SPECIAL,
                instructions = gta1PsInst,
                keywords = "gta 1 playstation level select skybabies"
            )
        )

        // ======================== GTA 2 — 5 codes ========================
        val gta2PcInst = "PC name-entry code: first enter GOURANGA as your player name and press Enter to enable cheat mode, then replace it with this code. Console editions use different systems."

        add(
            buildCode(
                id = "txt_gta2_pc_godofgta",
                gameId = "gta_2",
                title = "All weapons with maximum ammo",
                description = "Grants all weapons with maximum ammunition on PC (requires GOURANGA entered first).",
                code = "GODOFGTA",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.WEAPONS,
                instructions = gta2PcInst,
                keywords = "gta 2 pc weapons ammo godofgta"
            )
        )
        add(
            buildCode(
                id = "txt_gta2_pc_livelong",
                gameId = "gta_2",
                title = "Invincibility",
                description = "Grants player invincibility on PC (requires GOURANGA entered first).",
                code = "LIVELONG",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = gta2PcInst,
                keywords = "gta 2 pc invincibility god mode health livelong"
            )
        )
        add(
            buildCode(
                id = "txt_gta2_pc_losefeds",
                gameId = "gta_2",
                title = "No police",
                description = "Disables police pursuit on PC (requires GOURANGA entered first).",
                code = "LOSEFEDS",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.POLICE,
                instructions = gta2PcInst,
                keywords = "gta 2 pc police cops wanted losefeds"
            )
        )
        add(
            buildCode(
                id = "txt_gta2_pc_itsallup",
                gameId = "gta_2",
                title = "Level select",
                description = "Unlocks level select on PC (requires GOURANGA entered first).",
                code = "ITSALLUP",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.SPECIAL,
                instructions = gta2PcInst,
                keywords = "gta 2 pc level select itsallup"
            )
        )
        add(
            buildCode(
                id = "txt_gta2_pc_segarulz",
                gameId = "gta_2",
                title = "10× score multiplier",
                description = "Activates a 10x score multiplier on PC (requires GOURANGA entered first).",
                code = "SEGARULZ",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.SPECIAL,
                instructions = gta2PcInst,
                keywords = "gta 2 pc score multiplier 10x segarulz"
            )
        )

        // ======================== GTA III — 6 codes ========================
        val gta3PcInst = "PC: type during gameplay. Xbox/Definitive Edition layouts may differ."
        val gta3Ps2Inst = "PlayStation 2: enter the button sequence during gameplay. Xbox/Definitive Edition layouts may differ."

        add(
            buildCode(
                id = "txt_gta3_pc_gesundheit",
                gameId = "gta_3",
                title = "Full health",
                code = "GESUNDHEIT",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = gta3PcInst,
                keywords = "gta 3 pc full health heal gesundheit"
            )
        )
        add(
            buildCode(
                id = "txt_gta3_pc_gunsgunsguns",
                gameId = "gta_3",
                title = "All weapons",
                code = "GUNSGUNSGUNS",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.WEAPONS,
                instructions = gta3PcInst,
                keywords = "gta 3 pc weapons guns gunsgunsguns",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta3_pc_nopoliceplease",
                gameId = "gta_3",
                title = "Clear wanted level",
                code = "NOPOLICEPLEASE",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.POLICE,
                instructions = gta3PcInst,
                keywords = "gta 3 pc police clear wanted nopoliceplease"
            )
        )
        add(
            buildCode(
                id = "txt_gta3_pc_giveusatank",
                gameId = "gta_3",
                title = "Spawn Rhino tank",
                code = "GIVEUSATANK",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.VEHICLES,
                instructions = gta3PcInst,
                keywords = "gta 3 pc rhino tank vehicle giveusatank",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_gta3_ps2_weapons",
                gameId = "gta_3",
                title = "All weapons",
                code = "R2 R2 L1 R2 ← ↓ → ↑ ← ↓ → ↑",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 2",
                category = CheatCategory.WEAPONS,
                instructions = gta3Ps2Inst,
                keywords = "gta 3 ps2 playstation 2 weapons guns"
            )
        )
        add(
            buildCode(
                id = "txt_gta3_ps2_clearwanted",
                gameId = "gta_3",
                title = "Clear wanted level",
                code = "R2 R2 L1 R2 ↑ ↓ ↑ ↓ ↑ ↓",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 2",
                category = CheatCategory.POLICE,
                instructions = gta3Ps2Inst,
                keywords = "gta 3 ps2 playstation 2 police clear wanted"
            )
        )

        // ======================== GTA Vice City — 7 codes ========================
        val vcPcInst = "PC: type during gameplay. Some cheats are edition-specific or removed in Definitive Edition."
        val vcPs2Inst = "PlayStation 2: enter the sequence during gameplay. Some cheats are edition-specific or removed in Definitive Edition."

        add(
            buildCode(
                id = "txt_vc_pc_aspirine",
                gameId = "gta_vc",
                title = "Full health",
                code = "ASPIRINE",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = vcPcInst,
                keywords = "vice city pc full health aspirine",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_vc_pc_preciousprotection",
                gameId = "gta_vc",
                title = "Full armor",
                code = "PRECIOUSPROTECTION",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = vcPcInst,
                keywords = "vice city pc full armor preciousprotection"
            )
        )
        add(
            buildCode(
                id = "txt_vc_pc_thugstools",
                gameId = "gta_vc",
                title = "Weapon set 1",
                code = "THUGSTOOLS",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.WEAPONS,
                instructions = vcPcInst,
                keywords = "vice city pc weapons set 1 thugstools"
            )
        )
        add(
            buildCode(
                id = "txt_vc_pc_leavemealone",
                gameId = "gta_vc",
                title = "Clear wanted level",
                code = "LEAVEMEALONE",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.POLICE,
                instructions = vcPcInst,
                keywords = "vice city pc clear wanted police leavemealone",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_vc_pc_panzer",
                gameId = "gta_vc",
                title = "Spawn Rhino tank",
                code = "PANZER",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.VEHICLES,
                instructions = vcPcInst,
                keywords = "vice city pc rhino tank vehicle panzer",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_vc_ps2_health",
                gameId = "gta_vc",
                title = "Full health",
                description = "Restores Tommy's health to maximum and repairs the vehicle you are driving.",
                code = "R1 R2 L1 Circle ← ↓ → ↑ ← ↓ → ↑",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 2",
                category = CheatCategory.PLAYER,
                instructions = vcPs2Inst,
                keywords = "vice city ps2 playstation 2 full health circle"
            )
        )
        add(
            buildCode(
                id = "txt_vc_ps2_tank",
                gameId = "gta_vc",
                title = "Spawn Rhino tank",
                description = "Spawns a military Rhino tank in front of the player.",
                code = "Circle Circle L1 Circle Circle Circle L1 L2 R1 Triangle Circle Triangle",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 2",
                category = CheatCategory.VEHICLES,
                instructions = vcPs2Inst,
                keywords = "vice city ps2 playstation 2 rhino tank vehicle circle triangle"
            )
        )

        // ======================== GTA San Andreas — 7 codes ========================
        val saPcInst = "PC: type during gameplay. Mobile ports differ; some editions require an external keyboard or special cheat-entry option."
        val saPs2Inst = "PlayStation 2: enter sequence during gameplay. Mobile ports differ; some editions require an external keyboard or special cheat-entry option."

        add(
            buildCode(
                id = "txt_sa_pc_hesoyam",
                gameId = "gta_sa",
                title = "Health, armor and \$250,000",
                code = "HESOYAM",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = saPcInst,
                keywords = "san andreas pc health armor money 250000 hesoyam",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_sa_pc_baguvix",
                gameId = "gta_sa",
                title = "Near-infinite health (not all damage types)",
                code = "BAGUVIX",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.PLAYER,
                instructions = saPcInst,
                keywords = "san andreas pc infinite health god mode baguvix"
            )
        )
        add(
            buildCode(
                id = "txt_sa_pc_lxgiwyl",
                gameId = "gta_sa",
                title = "Weapon set 1",
                code = "LXGIWYL",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.WEAPONS,
                instructions = saPcInst,
                keywords = "san andreas pc weapons set 1 lxgiwyl"
            )
        )
        add(
            buildCode(
                id = "txt_sa_pc_rocketman",
                gameId = "gta_sa",
                title = "Spawn jetpack",
                code = "ROCKETMAN",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.VEHICLES,
                instructions = saPcInst,
                keywords = "san andreas pc jetpack vehicle fly rocketman",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_sa_pc_aezakmi",
                gameId = "gta_sa",
                title = "Never wanted",
                code = "AEZAKMI",
                platform = GamePlatform.PC,
                platformLabel = "PC",
                category = CheatCategory.POLICE,
                instructions = saPcInst,
                keywords = "san andreas pc never wanted police cops aezakmi",
                isPopular = true
            )
        )
        add(
            buildCode(
                id = "txt_sa_ps2_hesoyam",
                gameId = "gta_sa",
                title = "Health, armor and \$250,000",
                code = "R1 R2 L1 X ← ↓ → ↑ ← ↓ → ↑",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 2",
                category = CheatCategory.PLAYER,
                instructions = saPs2Inst,
                keywords = "san andreas ps2 playstation 2 health armor money 250000"
            )
        )
        add(
            buildCode(
                id = "txt_sa_ps2_jetpack",
                gameId = "gta_sa",
                title = "Spawn jetpack",
                code = "← → L1 L2 R1 R2 ↑ ↓ ← →",
                platform = GamePlatform.PLAYSTATION,
                platformLabel = "PlayStation 2",
                category = CheatCategory.VEHICLES,
                instructions = saPs2Inst,
                keywords = "san andreas ps2 playstation 2 jetpack vehicle"
            )
        )
    }
}
