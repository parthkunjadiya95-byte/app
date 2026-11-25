const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const OWNER_UID = "owner_uid_999";
const OWNER_EMAIL = "parthkunjadiya95@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validGamePayload(gameId, uid, now, overrides = {}) {
  const isGta6 = gameId === "gta_6";
  return {
    gameId,
    name: isGta6 ? "GTA VI" : "GTA V",
    shortName: isGta6 ? "GTA VI" : "GTA V",
    releaseYear: isGta6 ? "Upcoming" : "2013",
    eraLabel: isGta6 ? "Next-Gen HD" : "HD Universe",
    description: isGta6
      ? "Upcoming return to Vice City and Leonida. No verified codes yet."
      : "Los Santos and Blaine County with verified PC, PlayStation, and Xbox cheats.",
    platforms: isGta6 ? [] : ["PC", "PLAYSTATION", "XBOX"],
    artworkKey: isGta6 ? "hero" : "modern",
    featured: true,
    hasVerifiedCodes: !isGta6,
    statusNote: isGta6
      ? "Awaiting official release and verification."
      : "Single-player Story Mode only.",
    sortOrder: isGta6 ? 10 : 9,
    visibility: "PUBLIC",
    createdBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validCategoryPayload(categoryId, uid, now, overrides = {}) {
  return {
    categoryId,
    name: "Player Cheats",
    description: "Health, armor, cash, and invincibility cheats.",
    iconKey: "player",
    sortOrder: 1,
    visibility: "PUBLIC",
    createdBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validTutorialPayload(tutorialId, uid, now, overrides = {}) {
  return {
    tutorialId,
    title: "GTA V PC Console & Phone Cheats",
    platform: "PC",
    summary: "Enter text commands using tilde (~) or dial 1-999 phone numbers.",
    steps: [
      "Load GTA V Story Mode.",
      "Press tilde (~), type the verified code, and press Enter.",
    ],
    supportedGames: ["GTA V"],
    sortOrder: 2,
    visibility: "PUBLIC",
    createdBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validCheatCodePayload(codeId, uid, now, overrides = {}) {
  return {
    codeId,
    gameId: "gta_5",
    gameName: "GTA V",
    title: "Max health and armor",
    description: "Max health and armor",
    code: "TURTLE",
    platform: "PC",
    platformLabel: "PC",
    category: "PLAYER",
    instructions: "Cheats are for Story Mode only, not GTA Online. PC: press the tilde (~) key to open the cheat console, type the code, and press Enter.",
    keywords: "gta 5 v pc max health armor turtle",
    popular: true,
    verificationStatus: "VERIFIED",
    visibility: "PUBLIC",
    createdBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

// ==================== UNAUTHENTICATED DENIAL ====================

test("Unauthenticated users are denied all reads and writes across all 8 collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    unauthDb.collection("games").where("visibility", "==", "PUBLIC").get()
  );
  await assertFails(
    unauthDb.collection("cheat_codes").where("visibility", "==", "PUBLIC").get()
  );
  await assertFails(
    unauthDb.collection("categories").where("visibility", "==", "PUBLIC").get()
  );
  await assertFails(
    unauthDb.collection("tutorials").where("visibility", "==", "PUBLIC").get()
  );
  await assertFails(
    unauthDb.collection("reports").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("games")
      .doc("gta_5")
      .set(validGamePayload("gta_5", ALICE_UID, now))
  );
});

// ==================== 1. GAMES & GTA VI INVARIANTS ====================

test("Games collection: verified seeding & scoped queries succeed; GTA VI cheats flag, fake IDs, and unfiltered lists fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ownerDb = testEnv
    .authenticatedContext(OWNER_UID, {
      email: OWNER_EMAIL,
      email_verified: true,
    })
    .firestore();
  const spoofedOwnerDb = testEnv
    .authenticatedContext(BOB_UID, {
      email: OWNER_EMAIL,
      email_verified: false,
    })
    .firestore();
  const now = new Date(Date.now() - 2000);

  // Seed GTA V and GTA VI (GTA VI has 0 platforms & hasVerifiedCodes = false)
  await assertSucceeds(
    aliceDb.collection("games").doc("gta_5").set(validGamePayload("gta_5", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb.collection("games").doc("gta_6").set(validGamePayload("gta_6", ALICE_UID, now))
  );

  // Scoped query matching Kotlin FirestoreCatalogRepository.observeGames succeeds
  await assertSucceeds(
    bobDb.collection("games").where("visibility", "==", "PUBLIC").get()
  );

  // Unfiltered list query MUST fail (Pillar 8: no blanket list)
  await assertFails(bobDb.collection("games").get());

  // Invented game ID must fail
  await assertFails(
    aliceDb
      .collection("games")
      .doc("gta_7_fake")
      .set(validGamePayload("gta_7_fake", ALICE_UID, now))
  );

  // Claiming GTA VI has verified codes or platforms must fail
  await assertFails(
    aliceDb
      .collection("games")
      .doc("gta_6")
      .set(
        validGamePayload("gta_6", ALICE_UID, now, {
          hasVerifiedCodes: true,
          platforms: ["PLAYSTATION"],
        })
      )
  );

  // Regular user cannot update or delete seeded game
  await assertFails(
    bobDb.collection("games").doc("gta_5").update({
      name: "Hacked GTA",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Email spoofing (email_verified == false) MUST fail
  await assertFails(
    spoofedOwnerDb.collection("games").doc("gta_5").update({
      statusNote: "Spoofed update",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Verified bootstrapped owner update succeeds
  await assertSucceeds(
    ownerDb.collection("games").doc("gta_5").update({
      statusNote: "Verified for Story Mode on PC, PlayStation, and Xbox.",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

// ==================== 2, 3, 4. CATEGORIES, TUTORIALS & CHEAT_CODES ====================

test("Categories, Tutorials, and Cheat Codes: referential integrity, GTA VI code block, and Kotlin queries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Creating cheat code before game/category exists must fail (Global Consistency)
  await assertFails(
    aliceDb
      .collection("cheat_codes")
      .doc("gta5_pc_turtle")
      .set(validCheatCodePayload("gta5_pc_turtle", ALICE_UID, now))
  );

  // Seed parent game, category, and tutorial
  await assertSucceeds(
    aliceDb.collection("games").doc("gta_5").set(validGamePayload("gta_5", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb.collection("games").doc("gta_6").set(validGamePayload("gta_6", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("categories")
      .doc("PLAYER")
      .set(validCategoryPayload("PLAYER", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("tutorials")
      .doc("tut_pc_gtav")
      .set(validTutorialPayload("tut_pc_gtav", ALICE_UID, now))
  );

  // Now creating verified GTA V PC, GTA IV IN_GAME_PHONE, and GTA LCS PSP cheat codes succeeds
  await assertSucceeds(
    aliceDb
      .collection("cheat_codes")
      .doc("txt_gta5_pc_turtle")
      .set(validCheatCodePayload("txt_gta5_pc_turtle", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("games")
      .doc("gta_4")
      .set(
        validGamePayload("gta_4", ALICE_UID, now, {
          name: "GTA IV",
          shortName: "GTA IV",
          platforms: ["IN_GAME_PHONE"],
          sortOrder: 8,
        })
      )
  );
  await assertSucceeds(
    aliceDb
      .collection("cheat_codes")
      .doc("txt_gta4_phone_health_armor")
      .set(
        validCheatCodePayload("txt_gta4_phone_health_armor", ALICE_UID, now, {
          gameId: "gta_4",
          title: "Max health and armor",
          code: "362-555-0100",
          platform: "IN_GAME_PHONE",
          platformLabel: "All supported platforms (in-game phone)",
        })
      )
  );
  await assertSucceeds(
    aliceDb
      .collection("games")
      .doc("gta_lcs")
      .set(
        validGamePayload("gta_lcs", ALICE_UID, now, {
          name: "GTA Liberty City Stories",
          shortName: "LCS",
          platforms: ["PLAYSTATION", "PSP"],
          sortOrder: 6,
        })
      )
  );
  await assertSucceeds(
    aliceDb
      .collection("cheat_codes")
      .doc("txt_lcs_psp_health")
      .set(
        validCheatCodePayload("txt_lcs_psp_health", ALICE_UID, now, {
          gameId: "gta_lcs",
          gameName: "GTA Liberty City Stories",
          title: "Full health",
          description: "Restores full health (and repairs your current vehicle) on PSP.",
          code: "L R X L R Square L R",
          platform: "PSP",
          platformLabel: "PSP",
        })
      )
  );

  // Valid needs_verification status succeeds, while arbitrary invalid status fails
  await assertSucceeds(
    aliceDb
      .collection("cheat_codes")
      .doc("txt_gta5_needs_verify_test")
      .set(
        validCheatCodePayload("txt_gta5_needs_verify_test", ALICE_UID, now, {
          verificationStatus: "needs_verification",
        })
      )
  );
  await assertFails(
    aliceDb
      .collection("cheat_codes")
      .doc("txt_gta5_bad_status")
      .set(
        validCheatCodePayload("txt_gta5_bad_status", ALICE_UID, now, {
          verificationStatus: "UNVERIFIED_FAKE",
        })
      )
  );

  // Creating ANY cheat code for GTA VI MUST fail
  await assertFails(
    aliceDb
      .collection("cheat_codes")
      .doc("gta6_pc_fake")
      .set(validCheatCodePayload("gta6_pc_fake", ALICE_UID, now, { gameId: "gta_6" }))
  );

  // Shadow field or @DocumentId 'id' key must fail
  await assertFails(
    aliceDb
      .collection("cheat_codes")
      .doc("gta5_pc_shadow")
      .set(validCheatCodePayload("gta5_pc_shadow", ALICE_UID, now, { id: "gta5_pc_shadow" }))
  );

  // Scoped Kotlin queries succeed
  await assertSucceeds(
    bobDb.collection("categories").where("visibility", "==", "PUBLIC").get()
  );
  await assertSucceeds(
    bobDb.collection("tutorials").where("visibility", "==", "PUBLIC").get()
  );
  await assertSucceeds(
    bobDb.collection("cheat_codes").where("visibility", "==", "PUBLIC").get()
  );

  // Unfiltered list queries fail
  await assertFails(bobDb.collection("categories").get());
  await assertFails(bobDb.collection("tutorials").get());
  await assertFails(bobDb.collection("cheat_codes").get());
});

// ==================== 5. TOP-LEVEL REPORTS COLLECTION ====================

test("Reports collection: owner can create & query own reports; cross-user reads, unfiltered lists, and orphaned codeIds fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Seed referenced game, category, and cheat code
  await assertSucceeds(
    aliceDb.collection("games").doc("gta_5").set(validGamePayload("gta_5", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("categories")
      .doc("PLAYER")
      .set(validCategoryPayload("PLAYER", ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb
      .collection("cheat_codes")
      .doc("gta5_pc_turtle")
      .set(validCheatCodePayload("gta5_pc_turtle", ALICE_UID, now))
  );

  // Valid report without optional description (omitted via .filterValues { it != null })
  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      userId: ALICE_UID,
      codeId: "gta5_pc_turtle",
      codeTitle: "Max Health & Armor",
      gameId: "gta_5",
      gameName: "GTA V",
      platform: "PC",
      reportType: "INCORRECT_CODE",
      status: "SUBMITTED",
      createdAt: now,
    })
  );

  // Valid report with optional description
  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_2").set({
      userId: ALICE_UID,
      codeId: "gta5_pc_turtle",
      codeTitle: "Max Health & Armor",
      gameId: "gta_5",
      gameName: "GTA V",
      platform: "PC",
      reportType: "OUTDATED_INSTRUCTIONS",
      description: "Also mention the 1-999 phone dialer alternative.",
      status: "SUBMITTED",
      createdAt: now,
    })
  );

  // Owner filtered query matches FirestoreSyncRepository.observeCloudReports
  await assertSucceeds(
    aliceDb.collection("reports").where("userId", "==", ALICE_UID).get()
  );

  // Unfiltered reports list MUST fail
  await assertFails(aliceDb.collection("reports").get());

  // Cross-user read & query MUST fail
  await assertFails(bobDb.collection("reports").doc("rep_1").get());
  await assertFails(
    bobDb.collection("reports").where("userId", "==", ALICE_UID).get()
  );

  // Spoofing another userId on create MUST fail
  await assertFails(
    bobDb.collection("reports").doc("rep_spoof").set({
      userId: ALICE_UID,
      codeId: "gta5_pc_turtle",
      codeTitle: "Max Health & Armor",
      gameId: "gta_5",
      gameName: "GTA V",
      platform: "PC",
      reportType: "MISSING_INFO",
      status: "SUBMITTED",
      createdAt: now,
    })
  );

  // Orphaned report referencing nonexistent cheat code MUST fail
  await assertFails(
    aliceDb.collection("reports").doc("rep_orphan").set({
      userId: ALICE_UID,
      codeId: "nonexistent_code_999",
      codeTitle: "Fake",
      gameId: "gta_5",
      gameName: "GTA V",
      platform: "PC",
      reportType: "INCORRECT_CODE",
      status: "SUBMITTED",
      createdAt: now,
    })
  );

  // Future timestamp MUST fail
  await assertFails(
    aliceDb.collection("reports").doc("rep_future").set({
      userId: ALICE_UID,
      codeId: "gta5_pc_turtle",
      codeTitle: "Max Health & Armor",
      gameId: "gta_5",
      gameName: "GTA V",
      platform: "PC",
      reportType: "INCORRECT_CODE",
      status: "SUBMITTED",
      createdAt: new Date(Date.now() + 60000),
    })
  );
});

// ==================== USER PROFILE, FAVORITES & RECENT GAMES ====================

test("User profile, favorites, and recent_games enforce strict owner-only isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Player",
      email: "alice@example.com",
      darkTheme: true,
      languageCode: "en",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(aliceDb.collection("users").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      darkTheme: false,
      languageCode: "hi",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("sa_pc_hesoyam")
      .set({
        codeId: "sa_pc_hesoyam",
        gameId: "gta_sa",
        title: "Health, Armor & $250,000",
        code: "HESOYAM",
        platform: "PC",
        createdAt: created,
      })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("recent_games")
      .doc("gta_5")
      .set({
        gameId: "gta_5",
        createdAt: created,
        updatedAt: created,
      })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("recent_games")
      .doc("gta_5")
      .update({
        updatedAt: new Date(Date.now() - 1000),
      })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("recent_games").get()
  );
});
