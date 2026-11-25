package com.example.ui.components

import android.content.ComponentCallbacks2
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.os.Build
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PlaceholderSurfaceColor = Color(0xFF1A1032)

/**
 * Memory-bounded in-memory cache of downsampled drawable ImageBitmaps keyed by
 * drawable resource ID and requested dimensions. Uses Application resources only
 * and responds to Android memory trim callbacks to avoid retaining stale references.
 */
object DrawableBitmapCache {
    // 12 MB cache budget (size tracked in KB)
    private val cache = object : LruCache<String, ImageBitmap>(12 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return ((value.width.toLong() * value.height.toLong() * 4L) / 1024L)
                .toInt()
                .coerceAtLeast(1)
        }
    }

    fun get(key: String): ImageBitmap? = cache.get(key)

    fun put(key: String, bitmap: ImageBitmap) {
        cache.put(key, bitmap)
    }

    fun onTrimMemory(level: Int) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> cache.evictAll()
            level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> cache.trimToSize(cache.maxSize() / 2)
        }
    }
}

private fun calculateInSampleSize(
    srcWidth: Int,
    srcHeight: Int,
    reqWidth: Int,
    reqHeight: Int
): Int {
    var inSampleSize = 1
    if (srcHeight > reqHeight || srcWidth > reqWidth) {
        val halfHeight = srcHeight / 2
        val halfWidth = srcWidth / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize.coerceAtLeast(1)
}

private fun decodeSampledImageBitmap(
    resources: Resources,
    @DrawableRes resId: Int,
    reqWidth: Int,
    reqHeight: Int
): ImageBitmap? {
    val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(resources, resId, boundsOptions)
    if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
        return null
    }
    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(
            srcWidth = boundsOptions.outWidth,
            srcHeight = boundsOptions.outHeight,
            reqWidth = reqWidth,
            reqHeight = reqHeight
        )
        // Use standard 8-bit sRGB ARGB_8888 so HWUI always receives a valid SRGB dataspace
        // and avoids 16-bit RGB_565 color-space ambiguity or GPU format conversion fallbacks.
        inPreferredConfig = Bitmap.Config.ARGB_8888
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
        }
    }
    return BitmapFactory.decodeResource(resources, resId, decodeOptions)?.asImageBitmap()
}

/**
 * Decodes a bundled drawable asynchronously on [Dispatchers.IO] using the Application's
 * [Resources] (never pinning an Activity context across threads) at a downsampled sRGB size,
 * caching the resulting [ImageBitmap] in [DrawableBitmapCache].
 */
@Composable
fun AsyncDrawableImage(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    targetWidthPx: Int = 720,
    targetHeightPx: Int = 420,
    placeholderColor: Color = PlaceholderSurfaceColor
) {
    val appContext = LocalContext.current.applicationContext
    val cacheKey = "$resId:${targetWidthPx}x$targetHeightPx"
    var imageBitmap by remember(cacheKey) { mutableStateOf(DrawableBitmapCache.get(cacheKey)) }

    LaunchedEffect(cacheKey) {
        if (imageBitmap != null) return@LaunchedEffect
        val decoded = withContext(Dispatchers.IO) {
            DrawableBitmapCache.get(cacheKey) ?: decodeSampledImageBitmap(
                resources = appContext.resources,
                resId = resId,
                reqWidth = targetWidthPx,
                reqHeight = targetHeightPx
            )?.also { DrawableBitmapCache.put(cacheKey, it) }
        }
        if (decoded != null) {
            imageBitmap = decoded
        }
    }

    val loaded = imageBitmap
    if (loaded != null) {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(placeholderColor))
    }
}
