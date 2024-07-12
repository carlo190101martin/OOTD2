package com.example.ootd2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ootd2.databinding.ActivityTabBarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class TabBarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTabBarBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.APPROVAL_NOTIFICATION") {
                Log.d("TabBarActivity", "Received approval notification")
                handleApprovalNotification()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTabBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(rootController())
        sharedPreferences = getSharedPreferences("com.example.ootd2", MODE_PRIVATE)

        // Check if the approval notification was received
        Log.d("TabBarActivity", "onCreate called")
        val notificationReceived = sharedPreferences.getBoolean("notification_received", false)
        Log.d("TabBarActivity", "notification_received is $notificationReceived")

        if (notificationReceived) {
            // Reset the flag
            sharedPreferences.edit().putBoolean("notification_received", false).apply()
            Log.d("TabBarActivity", "notification_received reset to false")

            // Navigate to the desired activity
            navigateToToPayFragment()
        }

        binding.bottomNavigationView4.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.rootControllerTab -> replaceFragment(rootController())
                R.id.people -> replaceFragment(people())
                R.id.ranking -> replaceFragment(ranking())
                else -> { /* Do nothing */ }
            }
            true
        }

        // Register the BroadcastReceiver
        val intentFilter = IntentFilter("com.example.APPROVAL_NOTIFICATION")
        registerReceiver(notificationReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    private fun handleApprovalNotification() {
        Log.d("TabBarActivity", "Handling approval notification")
        with(sharedPreferences.edit()) {
            apply()
        }
        navigateToToPayFragment()
    }

    private fun navigateToToPayFragment() {
        Log.d("TabBarActivity", "Navigating to ranking fragment")

        // Set the selected tab to ranking
        binding.bottomNavigationView4.selectedItemId = R.id.ranking

        // Pass the navigation flag to the ranking fragment
        val rankingFragment = ranking().apply {
            arguments = Bundle().apply {
                putBoolean("navigateToToPay", true)
            }
        }
        replaceFragment(rankingFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the result to the fragments
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val REQUEST_CODE_TO_PAY = 1
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }}
