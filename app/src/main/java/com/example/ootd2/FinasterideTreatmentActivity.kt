package com.example.ootd2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.graphics.Color
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FinasterideTreatmentActivity: AppCompatActivity(){

    private lateinit var medH: EditText
    private lateinit var smokeyes: Button
    private lateinit var smokeno: Button
    private lateinit var menst: EditText
    private lateinit var bloodYes: Button
    private lateinit var bloodNo: Button
    private lateinit var allerg3: Button
    private lateinit var allerg2: Button

    private lateinit var name: EditText
    private lateinit var phone: EditText
    private lateinit var submitButton: Button
    // ... other components as per your iOS app

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viagratreatmentactivity)

        // Initialize UI components
        medH = findViewById(R.id.medH)
        smokeyes = findViewById(R.id.smokeyes)
        smokeno = findViewById(R.id.smokeno)
        menst =
            findViewById(R.id.menst)  // Assuming you have a TextView or EditText for menstruation
        bloodYes =
            findViewById(R.id.bloodyes)  // A button for selecting 'Yes' for a blood-related question
        bloodNo =
            findViewById(R.id.bloodno)  // A button for selecting 'No' for a blood-related question
        allerg2 =
            findViewById(R.id.allerg2)  // A button for selecting 'Yes' for a blood-related question
        allerg3 =
            findViewById(R.id.allerg3)  // A button for selecting 'No' for a blood-related question

        name = findViewById(R.id.name)  // EditText for name
        phone = findViewById(R.id.phone)  // EditText for phone
        submitButton = findViewById(R.id.submit_button)  // A button to submit the form
        // ... initialize other components

        // Setup listeners
        smokeyes.setOnClickListener {
            smokeyes.setBackgroundColor(Color.RED) // Highlight this button
            smokeno.setBackgroundColor(Color.YELLOW) // Reset the other button
            // Additional logic for when "Smoke: Yes" is clicked
        }

        smokeno.setOnClickListener {
            smokeno.setBackgroundColor(Color.RED) // Highlight this button
            smokeyes.setBackgroundColor(Color.YELLOW) // Reset the other button
            // Additional logic for when "Smoke: No" is clicked
        }

// Similarly, setup listeners for other buttons
        bloodYes.setOnClickListener {
            // Logic for the bloodYes button
            // For example, change button colors or handle user choice
        }

        bloodNo.setOnClickListener {
            // Logic for the bloodNo button
            // For example, change button colors or handle user choice
        }
        allerg3.setOnClickListener {
            // Logic for the bloodYes button
            // For example, change button colors or handle user choice
        }

        allerg2.setOnClickListener {
            // Logic for the bloodNo button
            // For example, change button colors or handle user choice
        }


        submitButton.setOnClickListener {
            // Logic to handle the form submission
            // Here, you would typically collect data from all fields and process it
        }

// Add TextChangedListeners to EditTexts if needed to handle text input
        medH.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Logic after text changed in medH
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Logic before text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Logic while text is changing
            }
        })


        medH.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Logic to handle text changed in medH
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Logic to handle before text change in medH
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Logic to handle text is changing in medH
            }
        })






        //
        //
        // ... additional methods as needed, such as for handling form submission
    }

    private fun validateInputs(): Boolean {
        // Validate each input field
        return medH.text.isNotBlank() &&
                menst.text.isNotBlank() &&
                name.text.isNotBlank() &&
                phone.text.isNotBlank() &&
                // Assuming smokeyes and smokeno are toggle buttons for a yes/no question
                (smokeyes.isSelected || smokeno.isSelected) &&
                // Assuming bloodYes and bloodNo are for another yes/no question
                (bloodYes.isSelected || bloodNo.isSelected) &&
                (allerg2.isSelected || allerg3.isSelected)
        // Add additional checks for other fields if there are any
    }

    private fun submitFormData() {
        // Collect data from the fields
        val medHText = medH.text.toString()
        val menstText = menst.text.toString()
        val nameText = name.text.toString()
        val phoneText = phone.text.toString()
        val smokeStatus = if (smokeyes.isSelected) "Yes" else "No"
        val bloodStatus = if (bloodYes.isSelected) "Yes" else "No"
        val allergStatus = if (allerg2.isSelected) "Yes" else "No"
        //
        // Collect additional data as needed

        // Implement your data submission logic here
        sendDataToServer(medHText, menstText, nameText, phoneText, smokeStatus, bloodStatus, allergStatus)
        // You can navigate to another screen or show a confirmation message after this
        showConfirmation()
    }

    private fun sendDataToServer(vararg data: String) {
        // Logic to send data to the server
        // You need to implement this based on your server's API
        // Example: Create a network request to send data
    }

    private fun showConfirmation() {
        // Show a confirmation message to the user
        Toast.makeText(this, "Form submitted successfully", Toast.LENGTH_LONG).show()
        // Optionally, navigate to another screen or activity
    }

}