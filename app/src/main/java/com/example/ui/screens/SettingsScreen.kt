package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.local.CodeReportEntity
import com.example.data.preferences.UserPreferences
import com.example.data.remote.TutorialDoc
import com.example.ui.components.DisclaimerBannerCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalViceNightExtras
import com.example.ui.viewmodel.CloudSyncState

private enum class SettingsDialogType {
    ABOUT, PRIVACY, TERMS, CONTACT, REPORTS
}

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    userDisplayName: String,
    userEmail: String?,
    cloudSyncState: CloudSyncState,
    totalGamesCount: Int,
    totalCodesCount: Int,
    favoriteCount: Int,
    submittedReports: List<CodeReportEntity>,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenHowToUse: () -> Unit,
    onSignOut: () -> Unit,
    isPrivacyOptionsRequired: Boolean = false,
    onOpenPrivacyOptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current
    var activeDialog by remember { mutableStateOf<SettingsDialogType?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Google Account & Firebase Cloud Sync
        item {
            SettingsGroupCard(title = "Google Account & Cloud Sync") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsIconBadge(icon = Icons.Filled.AccountCircle, highlightNeon = true)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = userDisplayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!userEmail.isNullOrBlank()) {
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val syncStatusLabel = when (cloudSyncState) {
                            CloudSyncState.Synced -> "Firebase Cloud Sync Active"
                            CloudSyncState.Syncing -> "Syncing with Firebase..."
                            is CloudSyncState.Error -> "Offline Mode (${cloudSyncState.message})"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (cloudSyncState is CloudSyncState.Error) Icons.Filled.CloudOff else Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = if (cloudSyncState is CloudSyncState.Error) MaterialTheme.colorScheme.error else extras.neonAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = syncStatusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (cloudSyncState is CloudSyncState.Error) MaterialTheme.colorScheme.error else extras.neonAccent
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, extras.cardBorder),
                        modifier = Modifier.testTag("settings_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Appearance & Language
        item {
            SettingsGroupCard(title = strings.appearanceSection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsIconBadge(icon = Icons.Filled.DarkMode)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.darkThemeTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.darkThemeSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preferences.isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                            checkedTrackColor = extras.neonAccent,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            uncheckedBorderColor = extras.cardBorder
                        ),
                        modifier = Modifier.testTag("theme_toggle_switch")
                    )
                }

                HorizontalDivider(color = extras.cardBorder)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsIconBadge(icon = Icons.Filled.Language)
                        Text(
                            text = strings.languageTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val selected = preferences.language == lang
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectLanguage(lang) }
                                    .testTag("lang_option_${lang.code}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) {
                                    extras.neonContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) extras.neonAccent else extras.cardBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (selected) extras.neonAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.englishName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Offline Storage & Local Reports
        item {
            SettingsGroupCard(title = strings.offlineStorageSection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsIconBadge(icon = Icons.Filled.CloudOff, highlightNeon = true)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = strings.offlineReadyTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${strings.offlineReadySubtitle} ($totalGamesCount games • $totalCodesCount codes • $favoriteCount ${strings.savedCodesCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = extras.cardBorder)
                SettingsNavRow(
                    icon = Icons.Filled.Flag,
                    title = strings.localReportsTitle,
                    trailingText = "${submittedReports.size}",
                    onClick = { activeDialog = SettingsDialogType.REPORTS },
                    testTag = "settings_local_reports_row"
                )
            }
        }

        // Guide Section
        item {
            SettingsGroupCard(title = strings.guideSection) {
                SettingsNavRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = strings.howToUseTitle,
                    subtitle = strings.howToUseSubtitle,
                    onClick = onOpenHowToUse,
                    testTag = "settings_howto_row"
                )
            }
        }

        // Information & Legal
        item {
            SettingsGroupCard(title = strings.aboutSection) {
                SettingsNavRow(
                    icon = Icons.Filled.Info,
                    title = strings.aboutApp,
                    onClick = { activeDialog = SettingsDialogType.ABOUT },
                    testTag = "settings_about_row"
                )
                HorizontalDivider(color = extras.cardBorder)
                SettingsNavRow(
                    icon = Icons.Filled.PrivacyTip,
                    title = strings.privacyPolicy,
                    onClick = { activeDialog = SettingsDialogType.PRIVACY },
                    testTag = "settings_privacy_row"
                )
                if (isPrivacyOptionsRequired) {
                    HorizontalDivider(color = extras.cardBorder)
                    SettingsNavRow(
                        icon = Icons.Filled.PrivacyTip,
                        title = "Privacy & Ad Preferences",
                        subtitle = "Manage Google AdMob consent settings",
                        onClick = onOpenPrivacyOptions,
                        testTag = "settings_ad_privacy_row"
                    )
                }
                HorizontalDivider(color = extras.cardBorder)
                SettingsNavRow(
                    icon = Icons.Filled.Description,
                    title = strings.termsConditions,
                    onClick = { activeDialog = SettingsDialogType.TERMS },
                    testTag = "settings_terms_row"
                )
                HorizontalDivider(color = extras.cardBorder)
                SettingsNavRow(
                    icon = Icons.Filled.Email,
                    title = strings.contactUs,
                    onClick = { activeDialog = SettingsDialogType.CONTACT },
                    testTag = "settings_contact_row"
                )
                HorizontalDivider(color = extras.cardBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.appVersionLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} (Firestore + Offline)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            DisclaimerBannerCard()
        }
    }

    when (activeDialog) {
        SettingsDialogType.ABOUT -> SimpleInfoDialog(
            title = strings.aboutApp,
            body = "${strings.appTitle} v${BuildConfig.VERSION_NAME}\n\n" +
                "A cloud-synced, offline-ready reference guide providing strictly verified cheat codes, platform instructions, tutorials, instant search, one-tap copy, and saved favorites for Grand Theft Auto titles.\n\n" +
                strings.disclaimerText,
            onDismiss = { activeDialog = null }
        )

        SettingsDialogType.PRIVACY -> SimpleInfoDialog(
            title = strings.privacyPolicy,
            body = "GTA Code Master respects your privacy:\n\n" +
                "• Google Sign-In & Cloud Sync: Your favorites, recently viewed games, theme and language preferences, and submitted code reports are isolated strictly to your Google account in Cloud Firestore.\n" +
                "• Offline Access: Verified games, cheat codes, and favorites are cached on-device so they work even without an internet connection.\n" +
                "• Advertising & Consent: This app displays Google AdMob ads to support development. Where required by law (such as EEA, UK, or regulated US states), Google's User Messaging Platform (UMP) lets you review and manage your ad privacy preferences.\n" +
                "• Clipboard Safety: Clipboard writes happen strictly when you tap Copy.",
            onDismiss = { activeDialog = null }
        )

        SettingsDialogType.TERMS -> SimpleInfoDialog(
            title = strings.termsConditions,
            body = "By using GTA Code Master, you acknowledge:\n\n" +
                "1. This is an unofficial fan-made reference utility and is not affiliated with or endorsed by Rockstar Games or Take-Two Interactive.\n" +
                "2. Cheat codes apply strictly to single-player modes where officially supported. Using cheats may temporarily disable achievements/trophies during a session.\n" +
                "3. Always save your game in a separate slot before experimenting with world or physics cheat codes.",
            onDismiss = { activeDialog = null }
        )

        SettingsDialogType.CONTACT -> SimpleInfoDialog(
            title = strings.contactUs,
            body = "Need to report an inaccurate code or outdated platform instruction?\n\n" +
                "• Open any cheat code's detail sheet and tap \"Report Incorrect Code\" to submit a verified issue report directly to Cloud Firestore.\n" +
                "• You can review your submitted reports anytime in Settings.\n" +
                "• Community Support: support@gtacodemaster.app",
            onDismiss = { activeDialog = null }
        )

        SettingsDialogType.REPORTS -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text(strings.localReportsTitle, style = MaterialTheme.typography.titleLarge) },
                text = {
                    if (submittedReports.isEmpty()) {
                        Text(
                            text = strings.localReportsEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            submittedReports.forEach { report ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, extras.cardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${report.gameName} • ${report.codeTitle}",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${report.platform} • ${report.reportType.replace('_', ' ')}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (report.description.isNotBlank()) {
                                            Text(
                                                text = report.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text(strings.close)
                    }
                }
            )
        }

        null -> Unit
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    val extras = LocalViceNightExtras.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, extras.cardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsIconBadge(
    icon: ImageVector,
    highlightNeon: Boolean = false
) {
    val extras = LocalViceNightExtras.current
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (highlightNeon) extras.neonContainer else MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlightNeon) extras.neonAccent else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIconBadge(icon = icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HowToUseCheatsDialog(
    tutorials: List<TutorialDoc>,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val extras = LocalViceNightExtras.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = strings.howToUseTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tutorials.forEach { tutorial ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, extras.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tutorial.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = extras.neonContainer
                                ) {
                                    Text(
                                        text = tutorial.platform,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = extras.neonAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (tutorial.summary.isNotBlank()) {
                                Text(
                                    text = tutorial.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            tutorial.steps.forEachIndexed { idx, step ->
                                Text(
                                    text = "${idx + 1}. $step",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (tutorial.supportedGames.isNotEmpty()) {
                                Text(
                                    text = "Applies to: ${tutorial.supportedGames.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}

@Composable
private fun SimpleInfoDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}
