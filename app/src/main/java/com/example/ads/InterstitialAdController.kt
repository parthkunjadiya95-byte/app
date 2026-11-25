package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val INTERSTITIAL_TAG = "AdMobInterstitial"
private const val MAX_AUTO_LOAD_RETRIES = 3

/**
 * Single app-scoped Interstitial Ad manager shared by ALL GTA game screens and natural transitions.
 *
 * - Initialized once with ApplicationContext so preloaded ads, frequency counters, and cooldown
 *   timers survive recompositions and configuration changes.
 * - Reloads the next interstitial strictly AFTER dismissal or show failure (avoiding premature
 *   load races while an active full-screen AdActivity is foregrounded).
 * - Guarantees one-shot main-thread navigation continuation so ad callbacks can never trigger
 *   duplicate navigation or leave a selected game stuck.
 */
object InterstitialAdController {

    private val isInitialized = AtomicBoolean(false)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var appContext: Context? = null

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isShowing = false
    private var retryJob: Job? = null
    private var consecutiveAutoRetries = 0

    private var lastLoadFailureElapsedMs = 0L
    private var completedTransitionsSinceLastAd = 0
    private var lastShownElapsedMs = 0L
    private var lastCodeCopiedElapsedMs = 0L

    fun initialize(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            appContext = context.applicationContext
            Log.i(INTERSTITIAL_TAG, "InterstitialAdController initialized once")
        }
    }

    fun preloadIfEligible(canRequestAds: Boolean, forceRetry: Boolean = false) {
        if (!canRequestAds || !AdConsentManager.state.value.isMobileAdsInitialized) return
        val context = appContext ?: return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainScope.launch { preloadIfEligible(canRequestAds = canRequestAds, forceRetry = forceRetry) }
            return
        }

        if (isLoading || isShowing || interstitialAd != null) return

        val now = SystemClock.elapsedRealtime()
        if (
            !forceRetry &&
            lastLoadFailureElapsedMs > 0L &&
            (now - lastLoadFailureElapsedMs) < AdConfig.INTERSTITIAL_RETRY_COOLDOWN_MS
        ) {
            return
        }

        val adUnitId = AdConfig.interstitialAdUnitId(context)
        if (adUnitId == null) {
            Log.d(INTERSTITIAL_TAG, "Interstitial ad unit ID not configured for release; skipping preload")
            return
        }

        retryJob?.cancel()
        retryJob = null
        isLoading = true
        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    lastLoadFailureElapsedMs = 0L
                    consecutiveAutoRetries = 0
                    interstitialAd = ad
                    Log.d(INTERSTITIAL_TAG, "Interstitial ad loaded and ready for next eligible transition")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                    lastLoadFailureElapsedMs = SystemClock.elapsedRealtime()
                    Log.w(
                        INTERSTITIAL_TAG,
                        "Interstitial failed to load [code=${loadAdError.code}]: ${loadAdError.message}"
                    )
                    scheduleRetryIfAllowed(canRequestAds)
                }
            }
        )
    }

    private fun scheduleRetryIfAllowed(canRequestAds: Boolean) {
        if (!canRequestAds || consecutiveAutoRetries >= MAX_AUTO_LOAD_RETRIES) return
        consecutiveAutoRetries += 1
        retryJob?.cancel()
        retryJob = mainScope.launch {
            delay(AdConfig.INTERSTITIAL_RETRY_COOLDOWN_MS)
            if (AdConsentManager.state.value.canRequestAds) {
                preloadIfEligible(canRequestAds = true, forceRetry = true)
            }
        }
    }

    /**
     * Records a cheat code copy event so we never interrupt users right after copying a code.
     */
    fun recordCodeCopied() {
        lastCodeCopiedElapsedMs = SystemClock.elapsedRealtime()
    }

    /**
     * Shared natural-transition entry point used identically by ALL GTA games
     * (GTA 1, GTA 2, GTA III, Vice City, San Andreas, LCS, VCS, GTA IV, GTA V, GTA VI)
     * and post-browsing navigation.
     *
     * Never blocks navigation if an ad is not ready, fails to load/show, or is frequency-capped.
     */
    fun maybeShowOnNaturalTransition(
        activity: Activity?,
        canRequestAds: Boolean,
        transitionLabel: String = "screen_transition",
        onContinue: () -> Unit
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainScope.launch {
                maybeShowOnNaturalTransition(
                    activity = activity,
                    canRequestAds = canRequestAds,
                    transitionLabel = transitionLabel,
                    onContinue = onContinue
                )
            }
            return
        }

        val continued = AtomicBoolean(false)
        val continueOnce: () -> Unit = {
            if (continued.compareAndSet(false, true)) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    onContinue()
                } else {
                    mainScope.launch { onContinue() }
                }
            }
        }

        if (activity != null && !isInitialized.get()) {
            initialize(activity)
        }

        if (!canRequestAds || isShowing) {
            continueOnce()
            return
        }

        val now = SystemClock.elapsedRealtime()

        // Never interrupt users who recently copied a cheat code.
        if (lastCodeCopiedElapsedMs > 0L && (now - lastCodeCopiedElapsedMs) < AdConfig.COPY_PROTECTION_WINDOW_MS) {
            Log.d(INTERSTITIAL_TAG, "Skipping interstitial ($transitionLabel): copy-protection active")
            preloadIfEligible(canRequestAds)
            continueOnce()
            return
        }

        completedTransitionsSinceLastAd += 1

        val ad = interstitialAd
        if (ad == null) {
            Log.d(
                INTERSTITIAL_TAG,
                "Interstitial not ready yet ($transitionLabel, transitions=$completedTransitionsSinceLastAd); preloading and continuing"
            )
            consecutiveAutoRetries = 0
            preloadIfEligible(canRequestAds)
            continueOnce()
            return
        }

        val hasEnoughTransitions = completedTransitionsSinceLastAd >= AdConfig.MIN_NATURAL_TRANSITIONS_BEFORE_AD
        val minIntervalMs = AdConfig.minInterstitialIntervalMs()
        val elapsedSinceLastAd = if (lastShownElapsedMs == 0L) Long.MAX_VALUE else (now - lastShownElapsedMs)
        val cooldownElapsed = elapsedSinceLastAd >= minIntervalMs

        if (!hasEnoughTransitions || !cooldownElapsed) {
            Log.d(
                INTERSTITIAL_TAG,
                "Frequency cap active ($transitionLabel, transitions=$completedTransitionsSinceLastAd/${AdConfig.MIN_NATURAL_TRANSITIONS_BEFORE_AD}, cooldownElapsed=$cooldownElapsed)"
            )
            continueOnce()
            return
        }

        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            continueOnce()
            return
        }

        // Consume reference before showing so the same InterstitialAd can never be displayed twice.
        interstitialAd = null
        isShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastShownElapsedMs = SystemClock.elapsedRealtime()
                completedTransitionsSinceLastAd = 0
                Log.d(INTERSTITIAL_TAG, "Interstitial ad displayed ($transitionLabel)")
            }

            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                lastShownElapsedMs = SystemClock.elapsedRealtime()
                ad.fullScreenContentCallback = null
                consecutiveAutoRetries = 0
                Log.d(INTERSTITIAL_TAG, "Interstitial dismissed ($transitionLabel); continuing navigation and reloading")
                continueOnce()
                preloadIfEligible(canRequestAds = AdConsentManager.state.value.canRequestAds, forceRetry = true)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isShowing = false
                ad.fullScreenContentCallback = null
                consecutiveAutoRetries = 0
                Log.w(
                    INTERSTITIAL_TAG,
                    "Interstitial failed to show ($transitionLabel) [code=${adError.code}]: ${adError.message}"
                )
                continueOnce()
                preloadIfEligible(canRequestAds = AdConsentManager.state.value.canRequestAds, forceRetry = true)
            }
        }
        try {
            ad.show(activity)
        } catch (e: Exception) {
            isShowing = false
            ad.fullScreenContentCallback = null
            Log.e(INTERSTITIAL_TAG, "Unexpected error showing interstitial ($transitionLabel): ${e.javaClass.simpleName}")
            continueOnce()
            preloadIfEligible(canRequestAds = AdConsentManager.state.value.canRequestAds)
        }
    }
}
