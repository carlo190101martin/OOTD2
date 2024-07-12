package com.example.ootd2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.SharedPreferences

class ThankYou : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thankyou)

        // Save state in SharedPreferences
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("endedOnThankYouPage", true)
            apply()
        }
    }
}
