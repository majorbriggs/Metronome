package com.majorbriggs.metronome.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Restores persisted settings context after reboot; does not auto-restart the metronome.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Settings are already persisted in DataStore; no action needed.
        }
    }
}
