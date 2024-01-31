package com.example.ootd2

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.privacysandbox.tools.core.model.Method
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.IsReadyToPayRequest
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import com.android.volley.Request
import com.android.volley.RequestQueue
import org.json.JSONException


class toPay: Fragment() {


        // ... other code ...

        private lateinit var paymentsClient: PaymentsClient

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // Initialize Google Pay API
            paymentsClient = Wallet.getPaymentsClient(
                requireActivity(),
                Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
            )

            // Inflate the layout for this fragment
            // ...

            // Setup Google Pay Button
            setupGooglePayButton()

            return view
        }

    private fun getPaymentDataRequest(): JSONObject {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
            put("transactionInfo", getTransactionInfo())
            put("merchantInfo", getMerchantInfo())

            // To request email address
            put("emailRequired", true)
        }
    }

    private fun getBaseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
                put("allowedCardNetworks", JSONArray().put("MASTERCARD").put("VISA"))
            })
        }
    }

    private fun getTransactionInfo(): JSONObject {
        return JSONObject().apply {
            put("totalPrice", "1.00")
            put("totalPriceStatus", "FINAL")
            put("currencyCode", "USD")
        }
    }

    private fun getMerchantInfo(): JSONObject {
        return JSONObject().apply {
            put("merchantName", "Example Merchant")
        }
    }
    private fun getIsReadyToPayRequest(): JSONObject {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
        }
    }


    private fun setupGooglePayButton() {
        val googlePayButton = view?.findViewById<Button>(R.id.GooglePay)
        googlePayButton?.setOnClickListener {
            requestPayment()
        }

        // Check if Google Pay is available
        val isReadyToPayJson = IsReadyToPayRequest.fromJson(getIsReadyToPayRequest().toString())
        paymentsClient.isReadyToPay(isReadyToPayJson).addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    googlePayButton?.visibility = View.VISIBLE
                } else {
                    // Hide Google Pay button if not available
                    googlePayButton?.visibility = View.INVISIBLE
                }
            } catch (exception: Exception) {
                // Log exception
            }
        }
    }

    private fun requestPayment() {
        // Define payment amount, currency, etc.
        val paymentDataRequestJson = getPaymentDataRequest().toString() // Convert JSONObject to String

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson)
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request),
                requireActivity(), // Use requireActivity() instead of 'this'
                REQUEST_CODE // REQUEST_CODE is a constant integer you define to track the request
            )
        }
    }

        // onActivityResult to handle the payment result
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQUEST_CODE -> {
                    when (resultCode) {
                        Activity.RESULT_OK ->
                            data?.let { intent ->
                                PaymentData.getFromIntent(intent)?.let { handlePaymentSuccess(it) }
                            }
                        Activity.RESULT_CANCELED -> {
                            // The user canceled the payment.
                        }
                        AutoResolveHelper.RESULT_ERROR -> {
                            // Handle errors
                        }
                    }
                }
            }
        }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return
        val paymentToken = extractPaymentToken(paymentInformation)

        sendPaymentInformationToServer(paymentToken)
    }
        // Define methods getIsReadyToPayRequest and getPaymentDataRequest to create the requests
        // ...
        private fun extractPaymentToken(paymentInformation: String): String {
            try {
                val paymentData = JSONObject(paymentInformation)
                val paymentMethodData = paymentData.getJSONObject("paymentMethodData")
                val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")
                return tokenizationData.getString("token")
            } catch (e: JSONException) {
                e.printStackTrace()
                // Handle error appropriately
            }
            return ""
        }
    private fun sendPaymentInformationToServer(paymentToken: String) {
        val url = "https://1a56-197-245-12-27.ngrok-free.app/create-payment-intent" // Replace with your server's URL

        // Create JSON body with the payment token and other necessary data
        val jsonBody = JSONObject()
        jsonBody.put("paymentToken", paymentToken)
        // Add any other necessary data

        // Create a network request to your server
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                // Handle server response here
            },
            { error ->
                // Handle error here
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    companion object {
            private const val REQUEST_CODE = 123 // Define a request code for Google Pay API
        }


}