package com.meikenn.tama.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DARK_MODE_SYSTEM = 0
        const val DARK_MODE_LIGHT = 1
        const val DARK_MODE_DARK = 2

        val DARK_MODE = intPreferencesKey("dark_mode") // 0=system, 1=light, 2=dark
        val SEMESTER_YEAR = intPreferencesKey("semester_year")
        val SEMESTER_TERM_NO = intPreferencesKey("semester_term_no")
        val SEMESTER_TERM_NAME = stringPreferencesKey("semester_term_name")
        val ASSIGNMENT_FINGERPRINTS = stringSetPreferencesKey("assignment_fingerprints")
    }

    val darkMode: Flow<Int> = context.dataStore.data.map { it[DARK_MODE] ?: 0 }

    suspend fun setDarkMode(mode: Int) {
        context.dataStore.edit { it[DARK_MODE] = mode }
    }

    suspend fun getSemesterYear(): Int {
        return context.dataStore.data.first()[SEMESTER_YEAR] ?: 2026
    }

    suspend fun getSemesterTermNo(): Int {
        return context.dataStore.data.first()[SEMESTER_TERM_NO] ?: 1
    }

    suspend fun getSemesterTermName(): String {
        return context.dataStore.data.first()[SEMESTER_TERM_NAME] ?: "春学期"
    }

    suspend fun saveSemester(year: Int, termNo: Int, termName: String) {
        context.dataStore.edit {
            it[SEMESTER_YEAR] = year
            it[SEMESTER_TERM_NO] = termNo
            it[SEMESTER_TERM_NAME] = termName
        }
    }

    suspend fun getAssignmentFingerprints(): Set<String> {
        return context.dataStore.data.first()[ASSIGNMENT_FINGERPRINTS] ?: emptySet()
    }

    suspend fun saveAssignmentFingerprints(fingerprints: Set<String>) {
        context.dataStore.edit { it[ASSIGNMENT_FINGERPRINTS] = fingerprints }
    }
}
