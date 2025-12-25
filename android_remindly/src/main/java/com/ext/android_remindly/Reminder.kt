package com.ext.android_remindly

import java.io.Serializable

data class Reminder(
    val id: Long,
    val title: String,
    val details: String = "",
    val dateTime: Long,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val repeatValue: Int = 1,
    val isEnabled: Boolean = true,
    val hasNotification: Boolean = true
) : Serializable

enum class RepeatInterval {
    NONE,
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    MONTHS
}