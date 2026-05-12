package com.example.hasiru.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.hasiru.MainActivity
import com.example.hasiru.R

class PlantReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val plantName = inputData.getString("plant_name") ?: "your tree"
        val plantId = inputData.getInt("plant_id", -1)

        // 1. Show a notification to the user
        showNotification("Hasiru Check-in", "It's been 90 days! How is your $plantName doing?", plantId)

        return Result.success()
    }

    private fun showNotification(title: String, message: String, plantId: Int) {
        val channelId = "plant_reminders"
        val notificationId = 1

        // Create the NotificationChannel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Plant Reminders"
            val descriptionText = "Notifications to remind you to check on your plants"
            val importance = NotificationManager.IMPORTANCE_HIGH // Increased importance
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Deep link intent
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("hasiru://plant/$plantId"),
            applicationContext,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            plantId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set to High priority
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // Sound, Vibrate, etc.
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            // Check for POST_NOTIFICATIONS permission on Android 13+
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle the case where permission is not granted
            }
        }
    }
}