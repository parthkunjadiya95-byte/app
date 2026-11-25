package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

private const val ADS_TAG = "AdMobConsent"

data class AdConsentState(
    val canRequestAds: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false,
    val isMobileAdsInitialized: Boolean = false
)

/**
 * Coordinates Google User Messaging Platform (UMP) consent gathering and Mobile Ads SDK
 * initialization. Never invoked on the Google Sign-In screen.
 */
object AdConsentManager {

    private val isMobileAdsInitStarted = AtomicBoolean(false)
    private val isConsentRequestInFlight = AtomicBoolean(false)
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(AdConsentState())
    val state: StateFlow<AdConsentState> = _state.asStateFlow()

    fun gatherConsentAndInitialize(activity: Activity) {
        val appContext = activity.applicationContext
        val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)

        // If prior consent was already obtained in an earlier session, initialize MobileAds immediately.
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsIfNeeded(appContext, consentInformation)
        }

        if (!isConsentRequestInFlight.compareAndSet(false, true)) {
            return
        }

        val activityRef = WeakReference(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                val currentActivity = activityRef.get()
                if (currentActivity == null || currentActivity.isFinishing || currentActivity.isDestroyed) {
                    isConsentRequestInFlight.set(false)
                    return@requestConsentInfoUpdate
                }
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(currentActivity) { formError ->
                    isConsentRequestInFlight.set(false)
                    if (formError != null) {
                        Log.w(ADS_TAG, "UMP consent form issue [${formError.errorCode}]: ${formError.message}")
                    }
                    syncConsentState(consentInformation)
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsIfNeeded(appContext, consentInformation)
                    }
                }
            },
            { requestError ->
                isConsentRequestInFlight.set(false)
                Log.w(ADS_TAG, "UMP consent info update failed [${requestError.errorCode}]: ${requestError.message}")
                syncConsentState(consentInformation)
                if (consentInformation.canRequestAds()) {
                    initializeMobileAdsIfNeeded(appContext, consentInformation)
                }
            }
        )
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: (String?) -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        val appContext = activity.applicationContext
        val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            syncConsentState(consentInformation)
            if (consentInformation.canRequestAds()) {
                initializeMobileAdsIfNeeded(appContext, consentInformation)
            }
            if (formError != null) {
                Log.w(ADS_TAG, "Privacy options form error [${formError.errorCode}]: ${formError.message}")
                onDismissed(formError.message)
            } else {
                onDismissed(null)
            }
        }
    }

    private fun syncConsentState(consentInformation: ConsentInformation) {
        val privacyRequired = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        _state.value = _state.value.copy(
            canRequestAds = consentInformation.canRequestAds(),
            isPrivacyOptionsRequired = privacyRequired
        )
    }

    private fun initializeMobileAdsIfNeeded(
        appContext: Context,
        consentInformation: ConsentInformation
    ) {
        syncConsentState(consentInformation)
        if (!isMobileAdsInitStarted.compareAndSet(false, true)) {
            return
        }
        backgroundScope.launch {
            try {
                MobileAds.initialize(appContext) {
                    Log.i(ADS_TAG, "Google Mobile Ads SDK initialized")
                    val canRequest = consentInformation.canRequestAds()
                    _state.value = _state.value.copy(
                        canRequestAds = canRequest,
                        isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED,
                        isMobileAdsInitialized = true
                    )
                    if (canRequest) {
                        InterstitialAdController.initialize(appContext)
                        InterstitialAdController.preloadIfEligible(canRequestAds = true, forceRetry = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(ADS_TAG, "MobileAds initialization failed: ${e.javaClass.simpleName}")
                isMobileAdsInitStarted.set(false)
            }
        }
    }
}
