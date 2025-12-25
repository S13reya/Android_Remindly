package com.ext.android_remindly

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ReminderListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val scrollView: ScrollView
    private val containerLayout: LinearLayout
    private val fabAdd: FloatingActionButton
    private val remindlyManager = RemindlyManager.getInstance(context)
    private var selectedDateTime = Calendar.getInstance()

    init {
        // Set background color
        setBackgroundColor(Color.parseColor("#303030"))

        // Create ScrollView
        scrollView = ScrollView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        // Create container for reminder items
        containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(containerLayout)
        addView(scrollView)

        // Create FAB
        fabAdd = FloatingActionButton(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END or Gravity.BOTTOM
                setMargins(0, 0, 48, 48)
            }
            setImageResource(android.R.drawable.ic_input_add)
            setOnClickListener {
                showAddReminderDialog()
            }
        }
        addView(fabAdd)

        loadReminders()
    }

    fun loadReminders() {
        containerLayout.removeAllViews()
        val reminders = remindlyManager.getAllReminders()

        if (reminders.isEmpty()) {
            showEmptyState()
        } else {
            reminders.forEach { reminder ->
                val itemView = createReminderItem(reminder)
                containerLayout.addView(itemView)
            }
        }
    }

    private fun showEmptyState() {
        val emptyText = TextView(context).apply {
            text = "No reminders yet.\nTap + to add one!"
            textSize = 16f
            setTextColor(Color.parseColor("#B0B0B0"))
            gravity = Gravity.CENTER
            setPadding(32, 100, 32, 32)
        }
        containerLayout.addView(emptyText)
    }

    private fun createReminderItem(reminder: Reminder): View {
        val itemLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#424242"))

            val params = layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, 0, 0, 2)
            layoutParams = params
        }

        // Circular indicator with first letter
        val circleView = createCircleWithLetter(reminder.title)
        itemLayout.addView(circleView)

        // Content layout
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleText = TextView(context).apply {
            text = reminder.title
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        contentLayout.addView(titleText)

        if (reminder.details.isNotEmpty()) {
            val detailsText = TextView(context).apply {
                text = reminder.details
                textSize = 14f
                setTextColor(Color.parseColor("#90A4AE"))
                maxLines = 2
            }
            contentLayout.addView(detailsText)
        }

        val dateFormat = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
        val dateText = TextView(context).apply {
            text = dateFormat.format(Date(reminder.dateTime))
            textSize = 14f
            setTextColor(Color.parseColor("#B0B0B0"))
        }
        contentLayout.addView(dateText)

        val repeatStr = when (reminder.repeatInterval) {
            RepeatInterval.NONE -> ""
            RepeatInterval.MINUTES -> "Every ${reminder.repeatValue} Minute(s)"
            RepeatInterval.HOURS -> "Every ${reminder.repeatValue} Hour(s)"
            RepeatInterval.DAYS -> "Every ${reminder.repeatValue} Day(s)"
            RepeatInterval.WEEKS -> "Every ${reminder.repeatValue} Week(s)"
            RepeatInterval.MONTHS -> "Every ${reminder.repeatValue} Month(s)"
        }

        if (repeatStr.isNotEmpty()) {
            val repeatText = TextView(context).apply {
                text = repeatStr
                textSize = 14f
                setTextColor(Color.parseColor("#B0B0B0"))
            }
            contentLayout.addView(repeatText)
        }

        itemLayout.addView(contentLayout)

        // Notification icon
        val notificationIcon = android.widget.ImageView(context).apply {
            val iconRes = if (reminder.hasNotification) {
                context.resources.getIdentifier("ic_notification_enabled", "drawable", context.packageName)
            } else {
                context.resources.getIdentifier("ic_notification_disabled", "drawable", context.packageName)
            }
            setImageResource(iconRes)
            alpha = if (reminder.hasNotification) 1.0f else 0.4f
            layoutParams = LinearLayout.LayoutParams(72, 72).apply {
                setMargins(16, 0, 0, 0)
                gravity = Gravity.CENTER_VERTICAL
            }
        }
        itemLayout.addView(notificationIcon)

        itemLayout.setOnClickListener {
            showEditReminderDialog(reminder)
        }

        itemLayout.setOnLongClickListener {
            showDeleteConfirmation(reminder.id)
            true
        }

        return itemLayout
    }

    private fun createCircleWithLetter(title: String): View {
        return object : View(context) {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 80f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            init {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    setMargins(0, 8, 32, 0)
                }

                paint.color = Color.parseColor("#F44336")
                paint.style = Paint.Style.FILL
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)

                val centerX = width / 2f
                val centerY = height / 2f
                val radius = Math.min(width, height) / 2f

                canvas.drawCircle(centerX, centerY, radius, paint)

                val letter = title.firstOrNull()?.uppercaseChar()?.toString() ?: "R"
                val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(letter, centerX, textY, textPaint)
            }
        }
    }

    private fun showAddReminderDialog() {
        showReminderDialog(null)
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        showReminderDialog(reminder)
    }

    private fun showReminderDialog(existingReminder: Reminder?) {
        val dialogLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val titleInput = EditText(context).apply {
            hint = "Reminder Title"
            setText(existingReminder?.title ?: "")
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        dialogLayout.addView(titleInput)

        val detailsInput = EditText(context).apply {
            hint = "Details (Optional)"
            setText(existingReminder?.details ?: "")
            minLines = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        dialogLayout.addView(detailsInput)

        val dateButton = Button(context).apply {
            text = "Select Date"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        dialogLayout.addView(dateButton)

        val timeButton = Button(context).apply {
            text = "Select Time"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        dialogLayout.addView(timeButton)

        val repeatLabel = TextView(context).apply {
            text = "Repeat:"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        dialogLayout.addView(repeatLabel)

        val repeatOptions = arrayOf("None", "Minutes", "Hours", "Days", "Weeks", "Months")
        val repeatSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, repeatOptions).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(existingReminder?.repeatInterval?.ordinal ?: 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        dialogLayout.addView(repeatSpinner)

        val repeatValueInput = EditText(context).apply {
            hint = "Repeat Interval Value"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText((existingReminder?.repeatValue ?: 1).toString())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        dialogLayout.addView(repeatValueInput)

        val notificationSwitch = Switch(context).apply {
            text = "Enable Notification"
            isChecked = existingReminder?.hasNotification ?: true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        dialogLayout.addView(notificationSwitch)

        selectedDateTime = if (existingReminder != null) {
            Calendar.getInstance().apply {
                timeInMillis = existingReminder.dateTime
            }
        } else {
            Calendar.getInstance()
        }

        // Update button text if editing existing reminder
        if (existingReminder != null) {
            val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            dateButton.text = dateFormat.format(Date(existingReminder.dateTime))

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeButton.text = timeFormat.format(Date(existingReminder.dateTime))
        }

        dateButton.setOnClickListener {
            showDatePicker { year, month, day ->
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, month)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day)
                dateButton.text = "$day/${month + 1}/$year"
            }
        }

        timeButton.setOnClickListener {
            showTimePicker { hour, minute ->
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedDateTime.set(Calendar.MINUTE, minute)
                timeButton.text = String.format("%02d:%02d", hour, minute)
            }
        }

        AlertDialog.Builder(context)
            .setTitle(if (existingReminder != null) "Edit Reminder" else "Add Reminder")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString()
                val details = detailsInput.text.toString()
                val repeatInterval = when (repeatSpinner.selectedItemPosition) {
                    0 -> RepeatInterval.NONE
                    1 -> RepeatInterval.MINUTES
                    2 -> RepeatInterval.HOURS
                    3 -> RepeatInterval.DAYS
                    4 -> RepeatInterval.WEEKS
                    5 -> RepeatInterval.MONTHS
                    else -> RepeatInterval.NONE
                }
                val repeatValue = repeatValueInput.text.toString().toIntOrNull() ?: 1
                val hasNotification = notificationSwitch.isChecked

                if (title.isNotBlank()) {
                    if (existingReminder != null) {
                        // Update existing reminder
                        val updatedReminder = existingReminder.copy(
                            title = title,
                            details = details,
                            dateTime = selectedDateTime.timeInMillis,
                            repeatInterval = repeatInterval,
                            repeatValue = repeatValue,
                            hasNotification = hasNotification
                        )
                        remindlyManager.updateReminder(updatedReminder)
                        Toast.makeText(context, "Reminder updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Add new reminder
                        remindlyManager.addReminder(
                            title = title,
                            details = details,
                            dateTime = selectedDateTime.timeInMillis,
                            repeatInterval = repeatInterval,
                            repeatValue = repeatValue,
                            hasNotification = hasNotification
                        )
                        Toast.makeText(context, "Reminder added!", Toast.LENGTH_SHORT).show()
                    }
                    loadReminders()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
        val calendar = selectedDateTime
        DatePickerDialog(
            context,
            { _, year, month, day -> onDateSet(year, month, day) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val calendar = selectedDateTime
        TimePickerDialog(
            context,
            { _, hour, minute -> onTimeSet(hour, minute) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showDeleteConfirmation(reminderId: Long) {
        AlertDialog.Builder(context)
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Delete") { _, _ ->
                remindlyManager.deleteReminder(reminderId)
                loadReminders()
                Toast.makeText(context, "Reminder deleted!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}



//package com.ext.android_remindly
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.drawable.ShapeDrawable
//import android.graphics.drawable.shapes.OvalShape
//import android.util.AttributeSet
//import android.view.Gravity
//import android.view.View
//import android.widget.LinearLayout
//import android.widget.ScrollView
//import android.widget.TextView
//import java.text.SimpleDateFormat
//import java.util.*
//
//class ReminderListView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : ScrollView(context, attrs, defStyleAttr) {
//
//    private val containerLayout: LinearLayout
//    private val remindlyManager = RemindlyManager.getInstance(context)
//
//    var onReminderClick: ((Reminder) -> Unit)? = null
//    var onReminderLongClick: ((Reminder) -> Unit)? = null
//
//    init {
//        containerLayout = LinearLayout(context).apply {
//            orientation = LinearLayout.VERTICAL
//            layoutParams = LayoutParams(
//                LayoutParams.MATCH_PARENT,
//                LayoutParams.WRAP_CONTENT
//            )
//        }
//        addView(containerLayout)
//
//        loadReminders()
//    }
//
//    fun loadReminders() {
//        containerLayout.removeAllViews()
//        val reminders = remindlyManager.getAllReminders()
//
//        reminders.forEach { reminder ->
//            val itemView = createReminderItem(reminder)
//            containerLayout.addView(itemView)
//        }
//    }
//
//    private fun createReminderItem(reminder: Reminder): View {
//        val itemLayout = LinearLayout(context).apply {
//            orientation = LinearLayout.HORIZONTAL
//            setPadding(32, 24, 32, 24)
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//            setBackgroundColor(Color.parseColor("#424242"))
//
//            val params = layoutParams as LinearLayout.LayoutParams
//            params.setMargins(0, 0, 0, 2)
//            layoutParams = params
//        }
//
//        // Circular indicator with first letter
//        val circleView = createCircleWithLetter(reminder.title)
//        itemLayout.addView(circleView)
//
//        // Content layout
//        val contentLayout = LinearLayout(context).apply {
//            orientation = LinearLayout.VERTICAL
//            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
//        }
//
//        val titleText = TextView(context).apply {
//            text = reminder.title
//            textSize = 18f
//            setTextColor(Color.WHITE)
//        }
//        contentLayout.addView(titleText)
//
//        val dateFormat = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
//        val dateText = TextView(context).apply {
//            text = dateFormat.format(Date(reminder.dateTime))
//            textSize = 14f
//            setTextColor(Color.parseColor("#B0B0B0"))
//        }
//        contentLayout.addView(dateText)
//
//        val repeatStr = when (reminder.repeatInterval) {
//            RepeatInterval.NONE -> ""
//            RepeatInterval.MINUTES -> "Every ${reminder.repeatValue} Minute(s)"
//            RepeatInterval.HOURS -> "Every ${reminder.repeatValue} Hour(s)"
//            RepeatInterval.DAYS -> "Every ${reminder.repeatValue} Day(s)"
//            RepeatInterval.WEEKS -> "Every ${reminder.repeatValue} Week(s)"
//            RepeatInterval.MONTHS -> "Every ${reminder.repeatValue} Month(s)"
//        }
//
//        if (repeatStr.isNotEmpty()) {
//            val repeatText = TextView(context).apply {
//                text = repeatStr
//                textSize = 14f
//                setTextColor(Color.parseColor("#B0B0B0"))
//            }
//            contentLayout.addView(repeatText)
//        }
//
//        itemLayout.addView(contentLayout)
//
//        // Notification icon
//        if (reminder.hasNotification) {
//            val notificationIcon = TextView(context).apply {
//                text = "🔔"
//                textSize = 24f
//                gravity = Gravity.CENTER
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    setMargins(16, 0, 0, 0)
//                }
//            }
//            itemLayout.addView(notificationIcon)
//        }
//
//        itemLayout.setOnClickListener {
//            onReminderClick?.invoke(reminder)
//        }
//
//        itemLayout.setOnLongClickListener {
//            onReminderLongClick?.invoke(reminder)
//            true
//        }
//
//        return itemLayout
//    }
//
//    private fun createCircleWithLetter(title: String): View {
//        return object : View(context) {
//            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//            private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//                color = Color.WHITE
//                textSize = 80f
//                textAlign = Paint.Align.CENTER
//                isFakeBoldText = true
//            }
//
//            init {
//                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
//                    setMargins(0, 8, 32, 0)
//                }
//
//                // Set red background color
//                paint.color = Color.parseColor("#F44336")
//                paint.style = Paint.Style.FILL
//            }
//
//            override fun onDraw(canvas: Canvas) {
//                super.onDraw(canvas)
//
//                val centerX = width / 2f
//                val centerY = height / 2f
//                val radius = Math.min(width, height) / 2f
//
//                // Draw circle
//                canvas.drawCircle(centerX, centerY, radius, paint)
//
//                // Draw first letter
//                val letter = title.firstOrNull()?.uppercaseChar()?.toString() ?: "R"
//                val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
//                canvas.drawText(letter, centerX, textY, textPaint)
//            }
//        }
//    }
//}