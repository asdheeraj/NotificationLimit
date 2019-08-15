package com.dheeraj.notiificationlimit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val compareNotificationByPostTime = Comparator<StatusBarNotification> { o1, o2 ->
        return@Comparator (o1.postTime).compareTo(o2.postTime)
    }
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //If the Sdk version is greater than 'O', configure a Notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            // Configure the notification channel
            with(notificationChannel) {
                description = NOTIFICATION_CHANNEL_DESCRIPTION
                enableLights(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                enableVibration(true)
                notificationManager.createNotificationChannel(this)
            }
        }
        for (i in 1..100) {
            Thread.sleep(1000)
            checkAndScheduleNotifications(notificationManager = notificationManager)
        }

    }

    /**
     * Checks for the Maximum notifications and Notify the user
     * @param notificationManager The NotificationManager instance
     */
    private fun checkAndScheduleNotifications(notificationManager: NotificationManager) {
        val currentNotifications = arrayListOf<StatusBarNotification>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentNotifications.addAll(
                notificationManager.activeNotifications.toCollection(ArrayList()))
        } else {
            /**
             * Note: User is required to enable notification permission from "Settings > Security > Notification
             * access".
             */
            currentNotifications.addAll(NotificationListener.activeNotificationsList)
        }
        when (currentNotifications.size) {
            in NOTIFICATION_START_LIMIT..getMaxNotificationsCount() -> {
                notifyUser(notificationManager)
            }
            else -> {
                Collections.sort(currentNotifications, compareNotificationByPostTime)
                        if (currentNotifications.isNotEmpty()) {
                            notificationManager.cancel(currentNotifications.first().tag,
                                currentNotifications.first().id)
                            notifyUser(notificationManager)
                        }
            }
        }
    }

    /**
     * A Helper function to prepare Notification
     * @return Notification
     */
    private fun prepareNotification(): Notification {
        val notifyIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, Companion.NOTIFICATION_REQUEST_CODE, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder =
            NotificationCompat.Builder(this, Companion.NOTIFICATION_CHANNEL_ID)
        with(notificationBuilder) {
            setContentTitle("Sample Notification: ${counter}")
            setSmallIcon(R.drawable.ic_launcher_background)
            setContentIntent(pendingIntent)
            return this.build()
        }
    }

    /**
     * Notifies the user with the respective Notification data
     * @param notificationManager Reference of the notification Manager
     */
    private fun notifyUser(notificationManager: NotificationManager) {
        notificationManager.notify("${counter++}", NOTIFICATION_ID, prepareNotification())
    }

    /**
     * Fetches the Maximum count of notifications possible for the Device
     *
     * @return the value of maximum number of Notifications
     */
    private fun getMaxNotificationsCount(): Int {
        return 2
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "001"
        private const val NOTIFICATION_CHANNEL_NAME = "Notifications"
        private const val NOTIFICATION_REQUEST_CODE = 100
        private const val NOTIFICATION_ID = 0
        private const val NOTIFICATION_CHANNEL_DESCRIPTION =
            "This Channel is used to display notifications"
        private const val NOTIFICATION_START_LIMIT = 0
    }
}
