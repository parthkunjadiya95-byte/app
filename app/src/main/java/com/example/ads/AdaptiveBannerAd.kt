package com.example.ads

import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.LocalViceNightExtras
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val BANNER_TAG = "AdMobBanner"

/**
 * Responsive Anchored Adaptive Banner Ad with full lifecycle management, graceful failure collapse,
 * and clear visual separation ("Advertisement" label + divider + spacing) to prevent accidental clicks.
 */
@Composable
fun AdaptiveBannerAd(
    canRequestAds: Boolean,
    modifier: Modifier = Modifier
) {
    if (!canRequestAds) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val extras = LocalViceNightExtras.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_container")
    ) {
        val availableWidthDp = maxWidth.value.toInt().coerceIn(0, 960)
        if (availableWidthDp < 240) return@BoxWithConstraints

        val adUnitId = remember(context) { AdConfig.bannerAdUnitId(context) } ?: return@BoxWithConstraints
        val adSize = remember(context, availableWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, availableWidthDp)
        }

        var isAdLoaded by remember(adUnitId, adSize) { mutableStateOf(false) }

        val adView = remember(context, adUnitId, adSize) {
            AdView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setAdSize(adSize)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isAdLoaded = true
                        Log.d(BANNER_TAG, "Adaptive banner ad loaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isAdLoaded = false
                        Log.w(
                            BANNER_TAG,
                            "Adaptive banner failed to load [code=${loadAdError.code}]: ${loadAdError.message}"
                        )
                    }
                }
            }
        }

        LaunchedEffect(adView) {
            adView.loadAd(AdRequest.Builder().build())
        }

        DisposableEffect(lifecycleOwner, adView) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.adListener = object : AdListener() {}
                adView.pause()
                (adView.parent as? ViewGroup)?.removeView(adView)
                adView.destroy()
            }
        }

        AnimatedVisibility(
            visible = isAdLoaded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .semantics { contentDescription = "Advertisement banner" },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = extras.cardBorder, thickness = 1.dp)
                Text(
                    text = "Advertisement",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = {
                            (adView.parent as? ViewGroup)?.removeView(adView)
                            adView
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
