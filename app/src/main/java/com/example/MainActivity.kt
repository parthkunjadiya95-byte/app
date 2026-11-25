package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.FirestoreCatalogRepository
import com.example.data.remote.FirestoreSyncRepository
import com.example.data.repository.CheatCatalogRepository
import com.example.ui.GtaCodeMasterApp
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutUser
import com.example.ui.components.DrawableBitmapCache
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.stringsForLanguage
import com.example.ui.theme.GtaCodeMasterTheme
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val prefs by preferencesRepository.userPreferences
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            LaunchedEffect(prefs.isDarkTheme) {
                val barStyle = if (prefs.isDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }

            CompositionLocalProvider(
                LocalAppStrings provides stringsForLanguage(prefs.language)
            ) {
                GtaCodeMasterTheme(darkTheme = prefs.isDarkTheme) {
                    AppNavigationRoot(preferencesRepository = preferencesRepository)
                }
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        DrawableBitmapCache.onTrimMemory(level)
    }
}

@Composable
private fun AppNavigationRoot(
    preferencesRepository: UserPreferencesRepository
) {
    val auth = remember { Firebase.auth }
    val authStateFlow = remember(auth) { auth.authStateFlow() }
    val currentUser by authStateFlow
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { /* AuthStateListener updates currentUser reactively */ })
    } else {
        val uid = user.uid
        val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "GTA Player"
        val email = user.email

        key(uid) {
            val mainViewModel: MainViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val firestore = FirebaseFirestore.getInstance(databaseId)
                        val localDb = AppDatabase.getInstance(app)
                        MainViewModel(
                            currentUserId = uid,
                            userDisplayName = displayName,
                            userEmail = email,
                            repository = CheatCatalogRepository(localDb.gtaDao()),
                            preferencesRepository = preferencesRepository,
                            firestoreSyncRepository = FirestoreSyncRepository(firestore),
                            firestoreCatalogRepository = FirestoreCatalogRepository(firestore)
                        )
                    }
                }
            )
            GtaCodeMasterApp(
                viewModel = mainViewModel,
                userDisplayName = displayName,
                userEmail = email,
                onSignOut = {
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }
    }
}
