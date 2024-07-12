package com.example.ootd2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView

class nope : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nope)

        // Optionally, if you want to set the text programmatically, you can use:
        // val messageView: AppCompatTextView = findViewById(R.id.messageView)
        // messageView.text = "You do not qualify for a script"
    }
}