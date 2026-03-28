package com.linksink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val WEBHOOK_URL = stringPreferencesKey("webhook_url")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val TOPIC_SECTION_STATES = stringPreferencesKey("topic_section_states")
    }

    val webhookUrl: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[Keys.WEBHOOK_URL] }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETE] ?: false }

    val sectionStates: Flow<Map<String, Boolean>> = context.dataStore.data
        .map { preferences ->
            val raw = preferences[Keys.TOPIC_SECTION_STATES] ?: return@map emptyMap()
            SectionStateSerializer.decodeFromJson(raw)
        }

    suspend fun setSectionExpanded(key: String, expanded: Boolean) {
        context.dataStore.edit { preferences ->
            val current = SectionStateSerializer.decodeFromJson(
                preferences[Keys.TOPIC_SECTION_STATES] ?: ""
            )
            preferences[Keys.TOPIC_SECTION_STATES] =
                SectionStateSerializer.encodeToJson(
                    SectionStateSerializer.withSectionExpanded(current, key, expanded)
                )
        }
    }

    suspend fun removeSectionState(key: String) {
        context.dataStore.edit { preferences ->
            val current = SectionStateSerializer.decodeFromJson(
                preferences[Keys.TOPIC_SECTION_STATES] ?: ""
            )
            preferences[Keys.TOPIC_SECTION_STATES] =
                SectionStateSerializer.encodeToJson(
                    SectionStateSerializer.withSectionRemoved(current, key)
                )
        }
    }

    suspend fun setWebhookUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WEBHOOK_URL] = url
        }
    }

    suspend fun clearWebhookUrl() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.WEBHOOK_URL)
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETE] = complete
        }
    }

    companion object {
        private val DISCORD_WEBHOOK_REGEX = Regex(
            """^https://discord\.com/api/webhooks/\d+/[\w-]+$"""
        )

        fun isValidWebhookUrl(url: String): Boolean {
            return url.isNotBlank() && DISCORD_WEBHOOK_REGEX.matches(url.trim())
        }
    }
}
