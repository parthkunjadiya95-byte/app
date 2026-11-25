package com.example.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.components.AsyncDrawableImage
import com.example.ui.components.DisclaimerBannerCard
import com.example.ui.components.VerifiedPillBadge
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalViceNightExtras
import com.example.ui.theme.viceNightBackdrop
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private sealed interface AuthUiState {
    data object Idle : AuthUiState
    data class Loading(val stage: SignInStage) : AuthUiState
    data object Success : AuthUiState
    data class Cancelled(val message: String) : AuthUiState
    data class TimedOut(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

private val AuthHeroScrimBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x55120824),
        Color(0xE8120824)
    )
)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var authUiState by remember { mutableStateOf<AuthUiState>(AuthUiState.Idle) }
    var autoSignInJob by remember { mutableStateOf<Job?>(null) }
    var interactiveSignInJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(context, credentialManager) {
        autoSignInJob = launch {
            val result = attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager
            )
            if (result is GoogleSignInResult.Success) {
                authUiState = AuthUiState.Success
                onAuthSuccess()
            }
        }
    }

    val loadingStage = (authUiState as? AuthUiState.Loading)?.stage
    val isLoading = loadingStage != null
    val isSignedIn = authUiState is AuthUiState.Success

    Box(
        modifier = modifier
            .fillMaxSize()
            .viceNightBackdrop(extras)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Vice Night Hero Visual Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AsyncDrawableImage(
                    resId = R.drawable.img_hero_vice_night,
                    contentDescription = strings.appTitle,
                    contentScale = ContentScale.Crop,
                    targetWidthPx = 800,
                    targetHeightPx = 450,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AuthHeroScrimBrush)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncDrawableImage(
                        resId = R.drawable.ic_app_logo_fg,
                        contentDescription = strings.appTitle,
                        contentScale = ContentScale.Crop,
                        targetWidthPx = 256,
                        targetHeightPx = 256,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = strings.appTagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD6CEEC),
                        textAlign = TextAlign.Center
                    )
                }
            }

            VerifiedPillBadge(text = "Cloud Sync • Offline Verified Catalog", isVerified = true)

            // Feature Highlights
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extras.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AuthFeatureRow(
                        icon = Icons.Filled.Verified,
                        title = "All 10 GTA Titles",
                        subtitle = "Strictly verified codes across PC, PlayStation, Xbox, Android & iOS."
                    )
                    AuthFeatureRow(
                        icon = Icons.Filled.Favorite,
                        title = "Synced Favorites & History",
                        subtitle = "Sign in with Google to back up your favorites and recently viewed games to Firestore."
                    )
                    AuthFeatureRow(
                        icon = Icons.Filled.CloudDone,
                        title = "Offline Ready + Cloud Reports",
                        subtitle = "Browse verified cheats offline anytime and sync submitted code reports securely."
                    )
                }
            }

            when (val state = authUiState) {
                is AuthUiState.Error -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_status_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is AuthUiState.TimedOut -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_status_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is AuthUiState.Cancelled -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_status_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, extras.cardBorder)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is AuthUiState.Success -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_status_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = extras.neonAccent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, extras.neonAccent.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Signed in with Google! Opening GTA CODE MASTER...",
                            style = MaterialTheme.typography.bodySmall,
                            color = extras.neonAccent,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                AuthUiState.Idle, is AuthUiState.Loading -> Unit
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isLoading || isSignedIn) return@Button
                        interactiveSignInJob?.cancel()
                        interactiveSignInJob = scope.launch {
                            authUiState = AuthUiState.Loading(SignInStage.CHECKING_ENVIRONMENT)
                            autoSignInJob?.cancel()
                            withTimeoutOrNull(1_500L) {
                                autoSignInJob?.join()
                            }
                            when (
                                val result = performGoogleSignIn(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onStageChanged = { stage ->
                                        authUiState = AuthUiState.Loading(stage)
                                    }
                                )
                            ) {
                                is GoogleSignInResult.Success -> {
                                    authUiState = AuthUiState.Success
                                    onAuthSuccess()
                                }
                                is GoogleSignInResult.Cancelled -> {
                                    authUiState = AuthUiState.Cancelled(result.message)
                                }
                                is GoogleSignInResult.TimedOut -> {
                                    authUiState = AuthUiState.TimedOut(result.message)
                                }
                                is GoogleSignInResult.Error -> {
                                    authUiState = AuthUiState.Error(result.message)
                                }
                            }
                        }
                    },
                    enabled = !isLoading && !isSignedIn,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extras.neonAccent,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("google_sign_in_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        val loadingLabel = when (loadingStage) {
                            SignInStage.CHECKING_ENVIRONMENT -> "Checking Google services..."
                            SignInStage.WAITING_FOR_GOOGLE -> "Waiting for Google..."
                            SignInStage.VERIFYING_WITH_FIREBASE -> "Finishing sign-in..."
                            null -> "Signing in..."
                        }
                        Text(
                            text = loadingLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else if (isSignedIn) {
                        Text(
                            text = "Signed in",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                if (isLoading) {
                    TextButton(
                        onClick = {
                            interactiveSignInJob?.cancel()
                            interactiveSignInJob = null
                            authUiState = AuthUiState.Cancelled(
                                "Sign-in cancelled. Tap 'Sign in with Google' to try again."
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_google_sign_in_button")
                    ) {
                        Text(
                            text = "Cancel sign-in",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            DisclaimerBannerCard()
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val extras = LocalViceNightExtras.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = extras.neonAccent,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
