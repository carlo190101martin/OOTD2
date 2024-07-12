package com.example.ootd2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
    private lateinit var databaseReference: DatabaseReference

    private var isSmokingSelected = false
    private var isBloodIssueSelected = false
    private var isAllergySelected = false

    private var smokingResponse: Boolean? = null
    private var bloodIssueResponse: Boolean? = null
    private var allergyResponse: Boolean? = null

    // ... other components as per your iOS app

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().getReference()
        setContentView(R.layout.activity_finasteridetreatmentactivity)

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
        setupButtonListeners()

        val submitButton: Button = findViewById(R.id.submit_button)
        submitButton.setOnClickListener {
            // Validate input fields here before submitting...
            if (validateInputs()) {
                submitFormData()
            } else {
                // Optionally, show an error alert or toast if validation fails
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            }
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


    private fun setupButtonListeners() {
        smokeyes.setOnClickListener {
            smokingResponse = true
            toggleButtonColors(smokeyes, smokeno)
        }

        smokeno.setOnClickListener {
            smokingResponse = false
            toggleButtonColors(smokeno, smokeyes)
        }

        bloodYes.setOnClickListener {
            bloodIssueResponse = true
            toggleButtonColors(bloodYes, bloodNo)
        }

        bloodNo.setOnClickListener {
            bloodIssueResponse = false
            toggleButtonColors(bloodNo, bloodYes)
        }

        allerg3.setOnClickListener {
            allergyResponse = true
            toggleButtonColors(allerg3, allerg2)
        }

        allerg2.setOnClickListener {
            allergyResponse = false
            toggleButtonColors(allerg2, allerg3)
        }
    }

    private fun toggleButtonColors(selectedButton: Button, otherButton: Button) {
        selectedButton.setBackgroundColor(Color.RED) // Selected
        otherButton.setBackgroundColor(Color.GRAY) // Not selected
    }


    private fun validateInputs(): Boolean {
        val isMedicalHistoryValid = medH.text.isNotBlank()
        val isMenstValid = menst.text.isNotBlank()
        val isNameValid = name.text.isNotBlank()
        val isPhoneValid = phone.text.isNotBlank()
        val isSelectionMade = smokingResponse != null && bloodIssueResponse != null && allergyResponse != null

        // Log to see which checks are failing if needed
        Log.d("Validation", "isMedicalHistoryValid: $isMedicalHistoryValid, isMenstValid: $isMenstValid, isNameValid: $isNameValid, isPhoneValid: $isPhoneValid, isSelectionMade: $isSelectionMade")

        return isMedicalHistoryValid && isMenstValid && isNameValid && isPhoneValid && isSelectionMade
    }
    private fun navigateToEndActivity() {
        val intent = Intent(this, nope::class.java)
        startActivity(intent)
    }

    private fun submitFormData() {
        // Check if any of the conditions are met to redirect to the EndActivity
        if (smokingResponse == true || bloodIssueResponse == true || allergyResponse == true) {
            navigateToEndActivity()
        } else {
            // Existing logic to submit form data
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val uid = user.uid

                val dataToUpload = hashMapOf<String, Any>(
                    "medicalHistory" to medH.text.toString(),
                    "menstruationDetails" to menst.text.toString(),
                    "name" to name.text.toString(),
                    "contactPhone" to phone.text.toString(),
                    "smokingStatus" to if (isSmokingSelected) "Yes" else "No",
                    "bloodIssue" to if (isBloodIssueSelected) "Yes" else "No",
                    "allergyStatus" to if (isAllergySelected) "Yes" else "No"
                    // Include any additional fields as needed
                )

                databaseReference.child("people").child(uid).updateChildren(dataToUpload)
                    .addOnSuccessListener {
                        // Handle success
                        showThankYouAlert() // Show a success message or navigate to next screen
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Log.e("FirebaseDB", "Failed to update user data", e)
                    }
            } else {
                // Handle case where there is no authenticated user
                Log.e("FirebaseDB", "No authenticated user found")
            }
        }
    }

    private fun showThankYouAlert() {
        AlertDialog.Builder(this)
        // Navigate to toPay Activity

        val intent = Intent(this, Payment::class.java)
        intent.putExtra("medication", "Finpecia") // Replace "Finpecia" with the actual medication type

        startActivity(intent)
    }


    private fun sendDataToServer(vararg data: String) {
        // Logic to send data to the server
        // You need to implement this based on your server's API
        // Example: Create a network request to send data
    }



}