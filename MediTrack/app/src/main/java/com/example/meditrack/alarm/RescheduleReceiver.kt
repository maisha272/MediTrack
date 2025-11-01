package com.example.meditrack.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        // Optional: query all active meds and call DoseScheduler.rescheduleForMedication(id)
    }
}
