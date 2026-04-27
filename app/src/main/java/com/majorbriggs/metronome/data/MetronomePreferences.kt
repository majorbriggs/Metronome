package com.majorbriggs.metronome.data

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "metronome_prefs")

enum class TimeSignature(
    val key: String,
    val displayName: String,
    val beatsPerBar: Int,
    val accentedBeats: Set<Int>
) {
    FOUR_FOUR("4_4", "4/4", 4, setOf(0)),
    THREE_FOUR("3_4", "3/4", 3, setOf(0)),
    SIX_EIGHT("6_8", "6/8", 6, setOf(0, 3));

    companion object {
        fun fromKey(key: String) = entries.find { it.key == key } ?: FOUR_FOUR
    }
}

enum class FeedbackMode(val key: String, val label: String) {
    VIBRATION("vibration", "VIB"),
    AUDIO("audio", "SND"),
    BOTH("both", "ALL");

    companion object {
        fun fromKey(key: String) = entries.find { it.key == key } ?: BOTH
    }
}

@Singleton
class MetronomePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val BPM_KEY = intPreferencesKey("bpm")
        private val TIME_SIGNATURE_KEY = stringPreferencesKey("time_signature")
        private val FEEDBACK_MODE_KEY = stringPreferencesKey("feedback_mode")
    }

    val bpm: Flow<Int> = dataStore.data.map { it[BPM_KEY] ?: 120 }
    val timeSignature: Flow<TimeSignature> = dataStore.data.map {
        TimeSignature.fromKey(it[TIME_SIGNATURE_KEY] ?: "4_4")
    }
    val feedbackMode: Flow<FeedbackMode> = dataStore.data.map {
        FeedbackMode.fromKey(it[FEEDBACK_MODE_KEY] ?: "both")
    }

    suspend fun setBpm(bpm: Int) {
        dataStore.edit { it[BPM_KEY] = bpm.coerceIn(40, 240) }
    }

    suspend fun setTimeSignature(ts: TimeSignature) {
        dataStore.edit { it[TIME_SIGNATURE_KEY] = ts.key }
    }

    suspend fun setFeedbackMode(mode: FeedbackMode) {
        dataStore.edit { it[FEEDBACK_MODE_KEY] = mode.key }
    }
}
