package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val AUTH_TAG = "Auth"
private const val AUTO_SIGN_IN_TIMEOUT_MS = 8_000L
private const val CREDENTIAL_REQUEST_TIMEOUT_MS = 45_000L
private const val FIREBASE_AUTH_TIMEOUT_MS = 20_000L

enum class SignInStage {
    CHECKING_ENVIRONMENT,
    WAITING_FOR_GOOGLE,
    VERIFYING_WITH_FIREBASE
}

sealed interface GoogleSignInResult {
    data class Success(val user: FirebaseUser) : GoogleSignInResult
    data class Cancelled(val message: String) : GoogleSignInResult
    data class TimedOut(val message: String) : GoogleSignInResult
    data class Error(val message: String) : GoogleSignInResult
}

private sealed interface PlayServicesStatus {
    data object Available : PlayServicesStatus
    data class Unavailable(val code: Int, val message: String) : PlayServicesStatus
}

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun checkGooglePlayServices(context: Context): PlayServicesStatus {
    val availability = GoogleApiAvailability.getInstance()
    val statusCode = availability.isGooglePlayServicesAvailable(context)
    if (statusCode == ConnectionResult.SUCCESS) {
        return PlayServicesStatus.Available
    }

    val statusName = availability.getErrorString(statusCode)
    Log.e(AUTH_TAG, "Google Play Services unavailable: code=$statusCode ($statusName)")
    val userMessage = when (statusCode) {
        ConnectionResult.SERVICE_MISSING ->
            "Google Play Services is missing on this device/emulator. Install Google Play Services or use a Google Play emulator image."
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ->
            "Google Play Services is outdated and must be updated before Google Sign-In can complete its security check."
        ConnectionResult.SERVICE_DISABLED ->
            "Google Play Services is disabled on this device. Enable Google Play Services in Android Settings and retry."
        ConnectionResult.SERVICE_UPDATING ->
            "Google Play Services is currently updating. Please wait a moment and try signing in again."
        ConnectionResult.SERVICE_INVALID ->
            "Google Play Services on this device is invalid or unverified. Test on a Play-certified device or Google Play emulator."
        else ->
            "Google Play Services is unavailable ($statusName). Update Google Play Services and try again."
    }
    return PlayServicesStatus.Unavailable(statusCode, userMessage)
}

private fun hasActiveNetwork(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
        Log.w(AUTH_TAG, "Network pre-check skipped: ${e.javaClass.simpleName}")
        true
    }
}

private fun resolveWebClientId(context: Context): String? {
    return try {
        val raw = context.getString(R.string.default_web_client_id).trim()
        if (raw.isEmpty() || !raw.contains("-") || raw.length < 20) {
            Log.e(AUTH_TAG, "default_web_client_id is empty or malformed")
            null
        } else {
            raw
        }
    } catch (e: Resources.NotFoundException) {
        Log.e(AUTH_TAG, "R.string.default_web_client_id resource not found")
        null
    } catch (e: Exception) {
        Log.e(AUTH_TAG, "Failed to resolve default_web_client_id: ${e.javaClass.simpleName}")
        null
    }
}

/**
 * Silent auto sign-in using strictly a single GetGoogleIdOption per GetCredentialRequest.
 * Bounded by a short timeout so startup never stalls Play Services or blocks manual sign-in.
 */
suspend fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager
): GoogleSignInResult? {
    Firebase.auth.currentUser?.let { existingUser ->
        return GoogleSignInResult.Success(existingUser)
    }

    val appContext = context.applicationContext
    val envReady = withContext(Dispatchers.IO) {
        if (checkGooglePlayServices(appContext) !is PlayServicesStatus.Available) {
            return@withContext false
        }
        if (!hasActiveNetwork(appContext)) {
            Log.d(AUTH_TAG, "Silent auto sign-in skipped: device is offline")
            return@withContext false
        }
        true
    }
    if (!envReady) return null

    val clientId = resolveWebClientId(appContext) ?: return null
    val activity = context.findActivity() ?: return null

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val cred = withTimeout(AUTO_SIGN_IN_TIMEOUT_MS) {
            credentialManager.getCredential(activity, request).credential
        }
        if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val idToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
            if (idToken.isBlank()) {
                Log.w(AUTH_TAG, "Silent auto sign-in returned an empty Google ID token")
                return null
            }
            val authResult = withTimeout(FIREBASE_AUTH_TIMEOUT_MS) {
                Firebase.auth
                    .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                    .await()
            }
            val signedInUser = authResult.user ?: Firebase.auth.currentUser
            if (signedInUser != null) {
                Log.i(AUTH_TAG, "Silent auto sign-in succeeded (provider=google.com)")
                GoogleSignInResult.Success(signedInUser)
            } else {
                Log.w(AUTH_TAG, "Silent auto sign-in completed without a FirebaseUser")
                null
            }
        } else {
            null
        }
    } catch (_: TimeoutCancellationException) {
        Log.w(AUTH_TAG, "Silent auto sign-in timed out after ${AUTO_SIGN_IN_TIMEOUT_MS}ms; falling back to manual sign-in")
        null
    } catch (e: CancellationException) {
        throw e
    } catch (_: NoCredentialException) {
        Log.d(AUTH_TAG, "No previously authorized Google credential for silent sign-in")
        null
    } catch (_: GetCredentialCancellationException) {
        Log.d(AUTH_TAG, "Silent auto sign-in dismissed")
        null
    } catch (e: Exception) {
        Log.w(AUTH_TAG, "Silent auto sign-in skipped: ${e.javaClass.simpleName}")
        null
    }
}

/**
 * Interactive Google Sign-In using strictly GetSignInWithGoogleOption per GetCredentialRequest.
 * Includes Play Services availability check, connectivity check, staged timeouts, and redacted error logging.
 */
suspend fun performGoogleSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onStageChanged: (SignInStage) -> Unit = {}
): GoogleSignInResult {
    onStageChanged(SignInStage.CHECKING_ENVIRONMENT)

    val appContext = context.applicationContext
    val clientId = resolveWebClientId(appContext)
        ?: return GoogleSignInResult.Error(
            "Google Sign-In is not configured: default_web_client_id is missing. " +
                "Verify app/google-services.json includes a Web OAuth client and Google Sign-In is enabled in Firebase Console."
        )

    val activity = context.findActivity()
        ?: return GoogleSignInResult.Error("Unable to launch Google Sign-In from the current screen.")

    val (playStatus, isOnline) = withContext(Dispatchers.IO) {
        checkGooglePlayServices(appContext) to hasActiveNetwork(appContext)
    }

    when (playStatus) {
        is PlayServicesStatus.Unavailable -> {
            return GoogleSignInResult.Error(playStatus.message)
        }
        PlayServicesStatus.Available -> Unit
    }

    if (!isOnline) {
        Log.w(AUTH_TAG, "Interactive Google Sign-In blocked: no active internet connection")
        return GoogleSignInResult.Error(
            "No internet connection detected. Connect to Wi-Fi or mobile data and try signing in again."
        )
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    onStageChanged(SignInStage.WAITING_FOR_GOOGLE)
    Log.i(AUTH_TAG, "Launching Credential Manager Google Sign-In (stage=credential_manager)")

    val credential = try {
        withTimeout(CREDENTIAL_REQUEST_TIMEOUT_MS) {
            credentialManager.getCredential(activity, request).credential
        }
    } catch (_: TimeoutCancellationException) {
        Log.e(
            AUTH_TAG,
            "Google Sign-In timed out at stage=credential_manager after ${CREDENTIAL_REQUEST_TIMEOUT_MS}ms " +
                "(Play Services 'Security checking' did not return a credential)"
        )
        return GoogleSignInResult.TimedOut(
            "Google Sign-In timed out while waiting on Google's security check. " +
                "Make sure Google Play Services and Android System WebView are up to date, a Google account is signed in under Android Settings, " +
                "the device date/time is set to Automatic, and this build's SHA-1 & SHA-256 fingerprints are registered in Firebase Console."
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: GetCredentialCancellationException) {
        Log.w(AUTH_TAG, "Google Sign-In cancelled at stage=credential_manager (type=${e.type})")
        return GoogleSignInResult.Cancelled(
            "Google Sign-In was cancelled. Tap 'Sign in with Google' to try again."
        )
    } catch (e: NoCredentialException) {
        Log.e(AUTH_TAG, "NoCredentialException at stage=credential_manager (type=${e.type})")
        return GoogleSignInResult.Error(
            "No Google account is available for sign-in, or this build's SHA-1/SHA-256 fingerprint is not registered in Firebase Console. " +
                "Verify a Google account is signed in on the device and SHA-1 & SHA-256 are added in Firebase."
        )
    } catch (e: GetCredentialProviderConfigurationException) {
        Log.e(AUTH_TAG, "Provider configuration error at stage=credential_manager (type=${e.type})")
        return GoogleSignInResult.Error(
            "Google Play Services credential provider is unavailable or outdated. " +
                "Update Google Play Services or test on a Google Play-enabled device/emulator."
        )
    } catch (e: GetCredentialUnsupportedException) {
        Log.e(AUTH_TAG, "Credential Manager unsupported at stage=credential_manager (type=${e.type})")
        return GoogleSignInResult.Error(
            "Google Sign-In via Credential Manager is not supported on this device/emulator image. " +
                "Use a device or emulator with Google Play Services."
        )
    } catch (e: GetCredentialInterruptedException) {
        Log.w(AUTH_TAG, "Google Sign-In interrupted at stage=credential_manager (type=${e.type})")
        return GoogleSignInResult.Error("Google Sign-In was interrupted. Please tap 'Sign in with Google' again.")
    } catch (e: GetCredentialException) {
        Log.e(AUTH_TAG, "GetCredentialException at stage=credential_manager (type=${e.type})")
        return GoogleSignInResult.Error(
            "Google Sign-In could not complete (${e.type}). Verify Google Play Services, internet access, and Firebase SHA-1/SHA-256 configuration."
        )
    } catch (e: Exception) {
        Log.e(AUTH_TAG, "Unexpected error at stage=credential_manager: ${e.javaClass.simpleName}")
        return GoogleSignInResult.Error("Google Sign-In failed unexpectedly. Please try again.")
    }

    if (credential !is CustomCredential || credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        Log.e(AUTH_TAG, "Unexpected credential type returned: ${credential.type}")
        return GoogleSignInResult.Error("Unexpected sign-in credential received. Please try again.")
    }

    val idToken = try {
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    } catch (e: GoogleIdTokenParsingException) {
        Log.e(AUTH_TAG, "Failed to parse Google ID token: ${e.javaClass.simpleName}")
        return GoogleSignInResult.Error("Received an invalid Google ID token. Please try signing in again.")
    }

    if (idToken.isBlank()) {
        Log.e(AUTH_TAG, "Google ID token was blank")
        return GoogleSignInResult.Error("Google did not return a valid ID token. Please try again.")
    }

    onStageChanged(SignInStage.VERIFYING_WITH_FIREBASE)
    Log.i(AUTH_TAG, "Credential received; exchanging with Firebase Auth (stage=firebase_auth)")

    return try {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = withTimeout(FIREBASE_AUTH_TIMEOUT_MS) {
            Firebase.auth.signInWithCredential(firebaseCredential).await()
        }
        val signedInUser = authResult.user ?: Firebase.auth.currentUser
        if (signedInUser != null) {
            Log.i(AUTH_TAG, "Firebase Google Sign-In succeeded (provider=google.com)")
            GoogleSignInResult.Success(signedInUser)
        } else {
            Log.e(AUTH_TAG, "signInWithCredential completed without an active FirebaseUser")
            GoogleSignInResult.Error("Authentication completed without an active user session. Please try again.")
        }
    } catch (_: TimeoutCancellationException) {
        Log.e(AUTH_TAG, "Firebase signInWithCredential timed out after ${FIREBASE_AUTH_TIMEOUT_MS}ms")
        GoogleSignInResult.TimedOut(
            "Firebase authentication timed out while verifying your Google sign-in. " +
                "Check your internet connection and tap 'Sign in with Google' to retry."
        )
    } catch (e: CancellationException) {
        throw e
    } catch (_: FirebaseNetworkException) {
        Log.e(AUTH_TAG, "FirebaseNetworkException at stage=firebase_auth")
        GoogleSignInResult.Error("Network error while verifying with Firebase. Check your internet connection and try again.")
    } catch (e: FirebaseAuthException) {
        Log.e(AUTH_TAG, "FirebaseAuthException at stage=firebase_auth (errorCode=${e.errorCode})")
        val friendlyMessage = when (e.errorCode) {
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "Google Sign-In is disabled in Firebase Console. Enable the Google provider under Authentication > Sign-in method."
            "ERROR_INVALID_CREDENTIAL" ->
                "Google credential was rejected by Firebase. Verify your Web Client ID, SHA-1/SHA-256 fingerprints, and google-services.json."
            "ERROR_USER_DISABLED" ->
                "This Google account has been disabled in Firebase Authentication."
            else ->
                "Firebase authentication failed (${e.errorCode}). Please try again."
        }
        GoogleSignInResult.Error(friendlyMessage)
    } catch (e: Exception) {
        Log.e(AUTH_TAG, "Unexpected error at stage=firebase_auth: ${e.javaClass.simpleName}")
        GoogleSignInResult.Error("Firebase sign-in failed unexpectedly. Please try again.")
    }
}

/**
 * Non-cancellable sign-out flow that clears Credential Manager state before signing out Firebase.
 */
fun signOutUser(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(AUTH_TAG, "Failed to clear credential state: ${e.javaClass.simpleName}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}
