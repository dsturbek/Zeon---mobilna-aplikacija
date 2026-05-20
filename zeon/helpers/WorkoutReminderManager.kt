package com.example.zeon.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.example.zeon.services.WorkoutReminderService

object WorkoutReminderManager {

    private const val TAG = "WorkoutReminderManager"
    private const val REQUEST_CODE = 100
    private const val CHECK_INTERVAL_MS = 15 * 60 * 1000L

    fun startPeriodicReminder(context: Context, clientId: Int) {
        Log.d(TAG, "startPeriodicReminder() called with clientId: $clientId")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WorkoutReminderService::class.java).apply {
            putExtra("client_id", clientId)
        }

        val pendingIntent = PendingIntent.getService(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
            }
        }

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + CHECK_INTERVAL_MS,
            CHECK_INTERVAL_MS,
            pendingIntent
        )

        Log.d(TAG, "Periodic reminder scheduled every ${CHECK_INTERVAL_MS / 60000} minutes")
    }

    fun stopPeriodicReminder(context: Context) {
        Log.d(TAG, "stopPeriodicReminder() called")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WorkoutReminderService::class.java)
        val pendingIntent = PendingIntent.getService(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Periodic reminder cancelled")
    }

    // Jednokratno pokretanje - samo za testiranje
    fun checkNow(context: Context, clientId: Int) {
        Log.d(TAG, "checkNow() called with clientId: $clientId")

        val intent = Intent(context, WorkoutReminderService::class.java).apply {
            putExtra("client_id", clientId)
        }
        context.startService(intent)
        Log.d(TAG, "WorkoutReminderService started immediately")
    }
}
