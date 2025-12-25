package com.ext.android_remindly

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1)
        val title = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val details = intent.getStringExtra("REMINDER_DETAILS") ?: ""

        if (reminderId != -1L) {
            val serviceIntent = Intent(context, ReminderNotificationService::class.java).apply {
                putExtra("REMINDER_ID", reminderId)
                putExtra("REMINDER_TITLE", title)
                putExtra("REMINDER_DETAILS", details)
            }
            context.startService(serviceIntent)
        }
    }
}
