package com.example.ads

import android.content.Context
import com.example.BuildConfig
import com.example.R

/**
 * Centralized AdMob configuration and frequency-capping policy.
 *
 * Debug builds ALWAYS enforce Google's official test ad unit IDs so real ads are never served
 * during development or testing. Release builds load IDs from res/values/admob_config.xml.
 */
object AdConfig {
    // Official Google Mobile Ads Android TEST Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Frequency capping & UX safeguards for Interstitial Ads
    const val MIN_INTERSTITIAL_INTERVAL_RELEASE_MS = 60_000L // 60s cooldown in production
    const val MIN_INTERSTITIAL_INTERVAL_DEBUG_MS = 15_000L   // 15s cooldown in debug builds for testing across games
    const val MIN_NATURAL_TRANSITIONS_BEFORE_AD = 2          // Never show on app launch or every single tap
    const val COPY_PROTECTION_WINDOW_MS = 15_000L            // Never interrupt within 15s of copying a cheat code
    const val INTERSTITIAL_RETRY_COOLDOWN_MS = 10_000L       // Cooldown before retrying a failed interstitial load

    fun minInterstitialIntervalMs(): Long =
        if (BuildConfig.DEBUG) MIN_INTERSTITIAL_INTERVAL_DEBUG_MS else MIN_INTERSTITIAL_INTERVAL_RELEASE_MS

    fun bannerAdUnitId(context: Context): String? {
        if (BuildConfig.DEBUG) return TEST_BANNER_AD_UNIT_ID
        return context.getString(R.string.admob_banner_unit_id).trim().takeIf { it.isNotEmpty() }
    }

    fun interstitialAdUnitId(context: Context): String? {
        if (BuildConfig.DEBUG) return TEST_INTERSTITIAL_AD_UNIT_ID
        return context.getString(R.string.admob_interstitial_unit_id).trim().takeIf { it.isNotEmpty() }
    }
}
