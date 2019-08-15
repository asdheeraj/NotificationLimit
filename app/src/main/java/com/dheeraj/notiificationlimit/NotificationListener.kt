package com.dheeraj.notiificationlimit

import android.app.Service
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Listener service to listen to notifications for below API 23 version
 */
class NotificationListener : NotificationListenerService() {

    companion object {
        var activeNotificationsList = arrayListOf<StatusBarNotification>()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveNotifications()
    }
    /**
       Ensure that the service is back alive if it is killed by the Os and there is
       enough memory available
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    private fun updateActiveNotifications() {
        activeNotificationsList = activeNotifications.filter {
            it.packageName.equals(applicationContext.packageName)
        }.toCollection(ArrayList())
    }
}