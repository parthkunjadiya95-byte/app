package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gta_code_master_prefs"
)

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    val userPreferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE_CODE])
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = enabled
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = language.code
        }
    }
}
