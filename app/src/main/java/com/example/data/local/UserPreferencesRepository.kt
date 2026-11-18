package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulserun_prefs")

enum class AppThemeMode(val storageKey: String, val label: String) {
    SYSTEM("SYSTEM", "Sistem"),
    LIGHT("LIGHT", "Terang"),
    DARK("DARK", "Gelap");

    companion object {
        fun fromKey(key: String?): AppThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: SYSTEM
    }
}

data class UserPreferences(
    val isLoaded: Boolean = false,
    val isLoggedIn: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isStravaAutoSync: Boolean = true,
    val athleteName: String = "",
    val accountEmail: String = "",
    val stepGoal: Int = 10_000,
    val calorieGoal: Int = 600,
    val isRunReminderEnabled: Boolean = true,
    val runReminderHour: Int = 6,
    val runReminderMinute: Int = 30,
    val isQuizReminderEnabled: Boolean = true,
    val quizReminderDayOfWeek: Int = Calendar.SUNDAY,
    val quizReminderHour: Int = 9,
    val quizReminderMinute: Int = 0
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val SOUND_ENABLED = booleanPreferencesKey("is_sound_enabled")
        val STRAVA_AUTO_SYNC = booleanPreferencesKey("is_strava_auto_sync")
        val ATHLETE_NAME = stringPreferencesKey("athlete_name")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val STEP_GOAL = intPreferencesKey("step_goal")
        val CALORIE_GOAL = intPreferencesKey("calorie_goal")
        val RUN_REMINDER_ENABLED = booleanPreferencesKey("run_reminder_enabled")
        val RUN_REMINDER_HOUR = intPreferencesKey("run_reminder_hour")
        val RUN_REMINDER_MINUTE = intPreferencesKey("run_reminder_minute")
        val QUIZ_REMINDER_ENABLED = booleanPreferencesKey("quiz_reminder_enabled")
        val QUIZ_REMINDER_DAY = intPreferencesKey("quiz_reminder_day")
        val QUIZ_REMINDER_HOUR = intPreferencesKey("quiz_reminder_hour")
        val QUIZ_REMINDER_MINUTE = intPreferencesKey("quiz_reminder_minute")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val savedName = prefs[Keys.ATHLETE_NAME]?.trim().orEmpty()
        val loggedIn = (prefs[Keys.IS_LOGGED_IN] ?: false) && savedName.isNotEmpty()
        val resolvedMode = when {
            prefs.contains(Keys.THEME_MODE) -> AppThemeMode.fromKey(prefs[Keys.THEME_MODE])
            prefs.contains(Keys.DARK_THEME) -> {
                if (prefs[Keys.DARK_THEME] == true) AppThemeMode.DARK else AppThemeMode.LIGHT
            }
            else -> AppThemeMode.SYSTEM
        }
        UserPreferences(
            isLoaded = true,
            isLoggedIn = loggedIn,
            themeMode = resolvedMode,
            useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            isSoundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            isStravaAutoSync = prefs[Keys.STRAVA_AUTO_SYNC] ?: true,
            athleteName = savedName,
            accountEmail = prefs[Keys.ACCOUNT_EMAIL]?.trim().orEmpty(),
            stepGoal = prefs[Keys.STEP_GOAL] ?: 10_000,
            calorieGoal = prefs[Keys.CALORIE_GOAL] ?: 600,
            isRunReminderEnabled = prefs[Keys.RUN_REMINDER_ENABLED] ?: true,
            runReminderHour = prefs[Keys.RUN_REMINDER_HOUR] ?: 6,
            runReminderMinute = prefs[Keys.RUN_REMINDER_MINUTE] ?: 30,
            isQuizReminderEnabled = prefs[Keys.QUIZ_REMINDER_ENABLED] ?: true,
            quizReminderDayOfWeek = prefs[Keys.QUIZ_REMINDER_DAY] ?: Calendar.SUNDAY,
            quizReminderHour = prefs[Keys.QUIZ_REMINDER_HOUR] ?: 9,
            quizReminderMinute = prefs[Keys.QUIZ_REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun loginAccount(accountName: String, accountEmail: String) {
        val cleanName = accountName.trim()
        if (cleanName.isEmpty()) return
        context.dataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
            it[Keys.ATHLETE_NAME] = cleanName
            it[Keys.ACCOUNT_EMAIL] = accountEmail.trim()
        }
    }

    suspend fun logoutAccount() {
        context.dataStore.edit {
            it[Keys.IS_LOGGED_IN] = false
            it[Keys.ATHLETE_NAME] = ""
            it[Keys.ACCOUNT_EMAIL] = ""
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit {
            it[Keys.THEME_MODE] = mode.storageKey
            if (mode != AppThemeMode.SYSTEM) {
                it[Keys.DARK_THEME] = (mode == AppThemeMode.DARK)
            }
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun toggleSoundEnabled(current: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = !current }
    }

    suspend fun setStravaAutoSync(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STRAVA_AUTO_SYNC] = enabled }
    }

    suspend fun updateProfile(name: String, stepGoal: Int, calorieGoal: Int) {
        context.dataStore.edit {
            val cleanName = name.trim()
            if (cleanName.isNotEmpty()) {
                it[Keys.ATHLETE_NAME] = cleanName
            }
            it[Keys.STEP_GOAL] = stepGoal.coerceIn(1000, 50000)
            it[Keys.CALORIE_GOAL] = calorieGoal.coerceIn(100, 5000)
        }
    }

    suspend fun updateRunReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.RUN_REMINDER_ENABLED] = enabled
            it[Keys.RUN_REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.RUN_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun updateQuizReminder(enabled: Boolean, dayOfWeek: Int, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.QUIZ_REMINDER_ENABLED] = enabled
            it[Keys.QUIZ_REMINDER_DAY] = dayOfWeek
            it[Keys.QUIZ_REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.QUIZ_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }
}
