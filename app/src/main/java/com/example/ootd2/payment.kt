package com.example.ootd2

import android.app.Activity
import org.json.JSONException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import java.nio.charset.Charset
import java.util.UUID

class Payment : AppCompatActivity() {


    /////Please note we should revisit to check why duplicate payments are attempted.

    private lateinit var paymentsClient: PaymentsClient
    private val loadPaymentDataRequestCode = 991
    private var isPaymentInProgress = false
    private var paymentTokenSent = false
    private var medication: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         medication = intent.getStringExtra("medication") ?: "DefaultMedication" // Use a default value or handle the null case as needed


        savedInstanceState?.let {
            isPaymentInProgress = it.getBoolean("isPaymentInProgress", false)
            paymentTokenSent = it.getBoolean("paymentTokenSent", false)
        }

        setContentView(R.layout.fragment_topay)

        // Initialize the flags to false to ensure a fresh start for the payment process
        isPaymentInProgress = false
        paymentTokenSent = false

        paymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        )

        findViewById<Button>(R.id.googlePayButton).setOnClickListener {
            if (!isPaymentInProgress && !paymentTokenSent) {
                it.isEnabled = false
                requestPayment()
            }
        }
    }
    private fun navigateToSuccessPage() {
        Log.d("PaymentFlow", "navigateToSuccessPage called")
        val intent = Intent(this, success::class.java)
        startActivity(intent)
        finish() // Optional: Call finish() if you don't want users to return to the payment screen
    }


    private fun createPrescription(medication: String) {
        val url = "https://3698-197-245-44-141.ngrok-free.app/create-prescription" // Replace with your actual server URL
        val requestQueue = Volley.newRequestQueue(this)

        val jsonBody = JSONObject().apply {
            put("userID", FirebaseAuth.getInstance().currentUser?.uid)
            put("medication", medication)
        }

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                // Handle response
                Log.d("PrescriptionSuccess", "Prescription created: $response")
                navigateToSuccessPage()
                 // Navigate to success page or perform other actions on success
            },
            { error ->
                // Handle error
                Log.e("PrescriptionError", "Error creating prescription: $error")
                // Optionally, show an error message or handle the error gracefully
            })

        requestQueue.add(jsonObjectRequest)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state of the payment process
        outState.putBoolean("isPaymentInProgress", isPaymentInProgress)
        outState.putBoolean("paymentTokenSent", paymentTokenSent)
    }
    private fun requestPayment() {





        if (isPaymentInProgress || paymentTokenSent) {
            Log.d("PaymentFlow", "Payment request or token submission already in progress.")
            return
        }
        Log.d("PaymentFlow", "requestPayment - isPaymentInProgress: $isPaymentInProgress")
        if (isPaymentInProgress) {
            Log.d("PaymentFlow", "Payment request skipped because another payment is in progress")
            // Payment is already in progress, so don't initiate a new one
            return
        }

        isPaymentInProgress = true // Payment is now in progress
        val paymentDataRequest = createPaymentDataRequest() ?: return
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest),
            this,
            loadPaymentDataRequestCode
        )
        isPaymentInProgress = true
    }

    private fun createPaymentDataRequest(): PaymentDataRequest? {
        val paymentDataRequestJson = GooglePay.getPaymentDataRequest()
        return PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
    }

    private fun sendPaymentTokenToServer(payload: JSONObject) {
        Log.d("PaymentFlow", "sendPaymentTokenToServer - Payload: $payload")
        Log.d("PaymentRequest", "Attempting to send payment token to server. Payload: $payload")
        val idempotencyKey = UUID.randomUUID().toString() // Generate a unique idempotency key
        payload.put("idempotencyKey", idempotencyKey) // Include it in the payload

        if (!paymentTokenSent) {
            Log.d("PaymentFlow", "sendPaymentTokenToServer - Payload: $payload")
            val requestQueue = Volley.newRequestQueue(this)
            val url =
                "https://3698-197-245-44-141.ngrok-free.app/create-payment-intent" // Update with your server's URL

            Log.d("PaymentRequest", "Sending payload to server: $payload")

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, payload,
                { response ->
                    val isSuccess = response.optBoolean("success", false)
                    if (isSuccess) {
                        medication?.let { med ->
                            createPrescription(med)
                        }

                        // Payment was successful on the server-side. Do not navigate here.
                        // Navigation will be handled in onActivityResult based on the success response.
                        Log.d(
                            "PaymentRequest",
                            "Payment token successfully sent to server and processed."
                        )
                    } else {
                        // Handle failure
                        Log.e("PaymentError", "Payment token sent to server but failed to process.")
                    }
                },
                { error ->
                    // Handle network or server error
                    Log.e(
                        "PaymentError",
                        "Error sending payment token to server: ${error.toString()}"
                    )
                    error.networkResponse?.let {
                        val responseBody = String(it.data, Charset.forName("UTF-8"))
                        Log.e("PaymentError", "Server response body: $responseBody")
                    }
                    // Optionally, show an error message to the user
                }
            )

            requestQueue.add(jsonObjectRequest)
        } else {

            Log.d("PaymentFlow", "Payment token already sent, skipping...")

        }
    }


    private fun extractPaymentToken(paymentInformation: String?): String? {
        return try {
            paymentInformation?.let {
                val paymentData = JSONObject(it)
                val paymentMethodData = paymentData.getJSONObject("paymentMethodData")
                val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")
                tokenizationData.getString("token")
            }
        } catch (e: JSONException) {
            Log.e("GooglePay", "Error extracting payment token", e)
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("PaymentFlow", "onActivityResult - requestCode: $requestCode, resultCode: $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            loadPaymentDataRequestCode -> {
                isPaymentInProgress = false // Assuming this flag is correctly defined elsewhere in your code

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val paymentInfo = PaymentData.getFromIntent(it)?.toJson()
                            val paymentToken = extractPaymentToken(paymentInfo)
                            Log.d("GooglePayToken", "Payment token: $paymentToken")
                            paymentToken?.let { token ->
                                val payload = JSONObject().apply {
                                    put("paymentToken", token)
                                }
                                Log.d("GooglePayPayload", "Sending to server: $payload")
                                sendPaymentTokenToServer(payload)
                            }

                            // Handle successful payment by navigating to the SuccessActivity
                        } ?: run {
                            // Handle the case where data is null
                            Log.e("GooglePay", "Intent data is null")
                        }
                    }
                    else -> {
                        // Handle cancellation or failure
                        // Optionally, show an error message or navigate the user to an error screen
                    }
                }
            }
        }
    }



    companion object {
        fun getPaymentDataRequest(): JSONObject {
            // Define and return the PaymentDataRequest object here
            // This method should construct the PaymentDataRequest JSON similar to your GooglePay object method
            return JSONObject()
        }
    }
}