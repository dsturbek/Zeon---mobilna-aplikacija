package com.example.zeon.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.zeon.helpers.NotificationHelper
import com.example.zeon.ws.DetailedWorkout
import com.example.zeon.ws.NetworkModule
import com.example.zeon.ws.WorkoutAssignment
import com.example.zeon.ws.WorkoutDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class WorkoutReminderService : Service() {

    companion object {
        private const val TAG = "WorkoutReminderService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called")

        val clientId = intent?.getIntExtra("client_id", -1) ?: -1
        Log.d(TAG, "Received clientId: $clientId")

        if (clientId == -1) {
            Log.e(TAG, "Invalid clientId (-1), stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        checkForUpcomingWorkouts(clientId)

        return START_NOT_STICKY
    }

    private fun checkForUpcomingWorkouts(clientId: Int) {
        NetworkModule.zeonApiService.getActiveWorkouts(clientId).enqueue(object : Callback<List<WorkoutAssignment>> {
            override fun onResponse(call: Call<List<WorkoutAssignment>>, response: Response<List<WorkoutAssignment>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val assignment = response.body()!!.first()
                    fetchDetailsAndNotify(assignment.idWorkoutAssigned, assignment.dateStart)
                } else {
                    stopSelf()
                }
            }
            override fun onFailure(call: Call<List<WorkoutAssignment>>, t: Throwable) { stopSelf() }
        })
    }

    private fun fetchDetailsAndNotify(assignedId: Int, dateStart: String) {
        NetworkModule.zeonApiService.getAssignmentDetails(assignedId).enqueue(object : Callback<WorkoutDetailsResponse> {
            override fun onResponse(call: Call<WorkoutDetailsResponse>, response: Response<WorkoutDetailsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    processTodaysWorkout(response.body()!!, dateStart)
                }
                stopSelf()
            }
            override fun onFailure(call: Call<WorkoutDetailsResponse>, t: Throwable) { stopSelf() }
        })
    }

    private fun processTodaysWorkout(details: WorkoutDetailsResponse, dateStart: String) {
        val programDay = try {
            val cleanDate = dateStart.take(10)
            val startDate = LocalDate.parse(cleanDate)
            val today = LocalDate.now()
            ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        } catch (e: Exception) { 1 }

        val todaysWorkout = details.workouts.find { it.workoutPlanDay == programDay }

        if (todaysWorkout != null) {
            showNotification(todaysWorkout, programDay)
        } else {
            showRestDayNotification(programDay)
        }
    }

    private fun showNotification(workout: DetailedWorkout, day: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        }

        val exerciseList = workout.exercises.joinToString { it.name }
        val title = "Dan $day: ${workout.workoutName}"
        val message = "Vježbe: $exerciseList"

        val notification = NotificationHelper.buildWorkoutNotification(this, title, message)
        NotificationManagerCompat.from(this)
            .notify(NotificationHelper.NOTIFICATION_ID, notification)
    }

    private fun showRestDayNotification(day: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        }

        val title = "Dan $day: Odmor"
        val message = "Danas nema planiranog treninga. Iskoristi dan za oporavak!"

        val notification = NotificationHelper.buildWorkoutNotification(this, title, message)
        NotificationManagerCompat.from(this)
            .notify(NotificationHelper.NOTIFICATION_ID, notification)
    }
}
