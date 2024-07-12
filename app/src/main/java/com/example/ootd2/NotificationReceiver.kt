package com.example.ootd2



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotificationReceiver", "onReceive called")
        if (intent != null && intent.action == "com.example.APPROVAL_NOTIFICATION") {
            val status = intent.getStringExtra("prescriptionStatus")
            if (status == "approved" || status == "rejected") {
                Log.d("NotificationReceiver", "Handling APPROVAL_NOTIFICATION action")

                // Update the boolean flag in SharedPreferences
                val sharedPreferences = context?.getSharedPreferences("com.example.ootd2", Context.MODE_PRIVATE)
                sharedPreferences?.edit()?.putBoolean("notification_received", true)?.apply()

                val notificationReceived = sharedPreferences?.getBoolean("notification_received", false)
                Log.d("NotificationReceiver", "notification_received set to $notificationReceived")
            } else {
                Log.d("NotificationReceiver", "Invalid or missing prescriptionStatus in intent")
            }
        } else {
            Log.d("NotificationReceiver", "Received unknown action or null intent")
        }
    }
}
