package com.example.ootd2

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ootd2.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import android.provider.Settings
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.PowerManager
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.SystemClock

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val TAG = "MainActivity"
    }


    fun scheduleBroadcast(context: Context) {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = "com.example.APPROVAL_NOTIFICATION"
        intent.putExtra("test", "true")  // Add any extra data if needed

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = SystemClock.elapsedRealtime() + 10_000  // 10 seconds from now
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
    }
    override fun onDestroy() {
        super.onDestroy()
        scheduleBroadcast(this)
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            navigateToTabBarActivity()
        } else {
            Log.d(TAG, "Notification permission denied")
            showNotificationPermissionDeniedDialog()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }
        }

        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and send token to server
            Log.d("MainActivity", "FCM Registration Token: $token")
            sendRegistrationToServer(token)
        })

        val button4: Button = findViewById(R.id.register)
        button4.setOnClickListener {

            val email = binding.emailET.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, TabBarActivity::class.java)
                        startActivity(intent)
                        finish() // Close LoginActivity
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }

        val button5: Button = findViewById(R.id.Loginbutton)
        button5.setOnClickListener {
            val email = binding.emailET.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, TabBarActivity::class.java)
                        startActivity(intent)
                        finish() // Close LoginActivity
                    } else {
                        Toast.makeText(this, "Credential error. Please enter the correct email and password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }

        Log.d("MainActivity", "Calling checkNotificationPermission()")
        checkNotificationPermission()


    }




    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                navigateToTabBarActivity()
            }
        } else {
            navigateToTabBarActivity()
        }
    }
    private fun showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Required")
            .setMessage("Notifications are disabled. To enable notifications, go to Settings > Apps > [Your App] > Notifications.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
        private fun sendRegistrationToServer(token: String?) {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID != null && token != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("users").child(userID).child("fcmToken").setValue(token)
                .addOnSuccessListener {
                    Log.d("MainActivity", "FCM token saved successfully for user $userID")
                }
                .addOnFailureListener { e ->
                    Log.d("MainActivity", "Failed to save FCM token for user $userID", e)
                }
        } else {
            Log.d("MainActivity", "User ID or token is null. Cannot save FCM token.")
        }
    }



    private fun navigateToTabBarActivity() {
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, TabBarActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}


