package com.ext.android_remindly

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RemindlyManager private constructor(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("RemindlyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val reminders = mutableListOf<Reminder>()
    private var nextId = 1L

    companion object {
        @Volatile
        private var INSTANCE: RemindlyManager? = null

        fun getInstance(context: Context): RemindlyManager {
            return INSTANCE ?: synchronized(this) {
                val instance = RemindlyManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    init {
        loadReminders()
    }

    private fun loadReminders() {
        val json = sharedPreferences.getString("reminders", null)
        if (json != null) {
            val type = object : TypeToken<List<Reminder>>() {}.type
            val loadedReminders: List<Reminder> = gson.fromJson(json, type)
            reminders.clear()
            reminders.addAll(loadedReminders)

            // Find max ID
            nextId = (reminders.maxOfOrNull { it.id } ?: 0) + 1

            // Reschedule all active reminders
            reminders.filter { it.hasNotification && it.isEnabled }.forEach { reminder ->
                if (reminder.dateTime > System.currentTimeMillis()) {
                    scheduleReminder(reminder)
                }
            }
        }
    }

    private fun saveReminders() {
        val json = gson.toJson(reminders)
        sharedPreferences.edit().putString("reminders", json).apply()
    }

    fun addReminder(
        title: String,
        details: String = "",
        dateTime: Long,
        repeatInterval: RepeatInterval = RepeatInterval.NONE,
        repeatValue: Int = 1,
        hasNotification: Boolean = true
    ): Reminder {
        val reminder = Reminder(
            id = nextId++,
            title = title,
            details = details,
            dateTime = dateTime,
            repeatInterval = repeatInterval,
            repeatValue = repeatValue,
            hasNotification = hasNotification
        )
        reminders.add(reminder)
        saveReminders()

        if (hasNotification) {
            scheduleReminder(reminder)
        }

        return reminder
    }

    fun updateReminder(reminder: Reminder) {
        val index = reminders.indexOfFirst { it.id == reminder.id }
        if (index != -1) {
            reminders[index] = reminder
            saveReminders()
            cancelReminder(reminder.id)

            if (reminder.hasNotification && reminder.isEnabled) {
                scheduleReminder(reminder)
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        reminders.removeAll { it.id == reminderId }
        saveReminders()
        cancelReminder(reminderId)
    }

    fun getAllReminders(): List<Reminder> {
        return reminders.toList()
    }

    fun getReminderById(id: Long): Reminder? {
        return reminders.firstOrNull { it.id == id }
    }

    private fun scheduleReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_DETAILS", reminder.details)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (reminder.repeatInterval) {
            RepeatInterval.NONE -> {
                val alarmInfo = AlarmManager.AlarmClockInfo(reminder.dateTime, pendingIntent)
                alarmManager.setAlarmClock(alarmInfo, pendingIntent)
            }
            else -> {
                val intervalMillis = getIntervalMillis(reminder.repeatInterval, reminder.repeatValue)
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminder.dateTime,
                    intervalMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun cancelReminder(reminderId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getIntervalMillis(interval: RepeatInterval, value: Int): Long {
        return when (interval) {
            RepeatInterval.MINUTES -> value * 60 * 1000L
            RepeatInterval.HOURS -> value * 60 * 60 * 1000L
            RepeatInterval.DAYS -> value * 24 * 60 * 60 * 1000L
            RepeatInterval.WEEKS -> value * 7 * 24 * 60 * 60 * 1000L
            RepeatInterval.MONTHS -> value * 30 * 24 * 60 * 60 * 1000L
            else -> 0L
        }
    }
}




