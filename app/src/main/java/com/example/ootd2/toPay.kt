package com.example.ootd2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.UUID

///toDoNotes make it so that if it fails it doesnt just say network error and go dead. make it so that it can reset the page


class toPay : Fragment() {

    private lateinit var paymentsClient: PaymentsClient
    private val loadPaymentDataRequestCode = 991
    private var isPaymentInProgress = false
    private var paymentTokenSent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_topay, container, false)

        paymentsClient = Wallet.getPaymentsClient(
            requireActivity(),
            Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        )

        view.findViewById<Button>(R.id.googlePayButton).setOnClickListener {
            if (!isPaymentInProgress && !paymentTokenSent) {
                it.isEnabled = false
                requestPayment()
            }
        }

        return view
    }

    private fun navigateToSuccessPage() {
        Log.d("PaymentFlow", "navigateToSuccessPage called")
        val successFragment = SuccessFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, successFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun requestPayment() {
        Log.d("PaymentFlow", "requestPayment - isPaymentInProgress: $isPaymentInProgress")
        if (isPaymentInProgress || paymentTokenSent) {
            Log.d("PaymentFlow", "Payment request or token submission already in progress.")
            return
        }

        isPaymentInProgress = true // Payment is now in progress
        val paymentDataRequest = createPaymentDataRequest() ?: return
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest),
            requireActivity(),
            loadPaymentDataRequestCode
        )
    }

    private fun createPaymentDataRequest(): PaymentDataRequest? {
        val paymentDataRequestJson = GooglePay.getPaymentDataRequest()
        Log.d("PaymentFlow", "PaymentDataRequest JSON: $paymentDataRequestJson")
        return PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
    }

    private fun sendPaymentTokenToServer(payload: JSONObject) {
        if (paymentTokenSent || !isAdded) {
            Log.d("PaymentFlow", "Payment token already sent or fragment not attached, skipping...")
            return
        }

        Log.d("PaymentFlow", "sendPaymentTokenToServer - Payload: $payload")
        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "https://4f64-197-245-44-141.ngrok-free.app/create-payment-intent" // Update with your server's URL

        Log.d("PaymentRequest", "Sending payload to server: $payload")

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, payload,
            { response ->
                val isSuccess = response.optBoolean("success", false)
                if (isSuccess) {
                    paymentTokenSent = true
                    Log.d("PaymentRequest", "Payment token successfully sent to server and processed.")
                    navigateToSuccessPage()  // Navigate to success page only on success
                } else {
                    // Handle failure
                    val errorMessage = response.optString("error_message", "Unknown error")
                    Log.e("PaymentError", "Payment token sent to server but failed to process. Error message: $errorMessage")
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Payment failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            },
            { error ->
                // Handle network or server error
                Log.e("PaymentError", "Error sending payment token to server: ${error.toString()}")
                error.networkResponse?.let {
                    val responseBody = String(it.data, Charset.forName("UTF-8"))
                    Log.e("PaymentError", "Server response body: $responseBody")
                }
                if (isAdded) {
                    Toast.makeText(requireContext(), "Network error occurred", Toast.LENGTH_LONG).show()
                }
            }
        )
        requestQueue.add(jsonObjectRequest)
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
                isPaymentInProgress = false // Reset the flag

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val paymentInfo = PaymentData.getFromIntent(it)?.toJson()
                            val paymentToken = extractPaymentToken(paymentInfo)
                            paymentToken?.let { token ->
                                val payload = JSONObject().apply {
                                    put("paymentToken", token)
                                    put("idempotencyKey", UUID.randomUUID().toString()) // Add idempotency key
                                }
                                sendPaymentTokenToServer(payload)
                            }
                        } ?: run {
                            Log.e("GooglePay", "Intent data is null")
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        // Handle payment cancellation
                        Log.d("PaymentFlow", "Payment canceled by user")
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        // Handle error
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Log.e("PaymentFlow", "Error during payment: $status")
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Payment failed: ${status?.statusMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
