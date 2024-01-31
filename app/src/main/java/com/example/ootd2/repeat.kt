package com.example.ootd2

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.Manifest

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.stripe.android.PaymentConfiguration
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher

class ToPayFragment : Fragment() {

    private lateinit var googlePayButton: Button
    private lateinit var googlePayLauncher: GooglePayLauncher

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_repeat, container, false)

        // Initialize Stripe
        PaymentConfiguration.init(requireContext(), "your_stripe_publishable_key")

        // Initialize GooglePayLauncher
        googlePayLauncher = GooglePayLauncher(
            activity = requireActivity(),
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Test,
                merchantCountryCode = "US",
                merchantName = "Example Merchant"
            ),
            readyCallback = ::onGooglePayReady,
            resultCallback = ::onGooglePayResult
        )

        googlePayButton = view.findViewById<Button>(R.id.GooglePay)
        googlePayButton.setOnClickListener {
            val clientSecret = "your_payment_intent_client_secret"
            googlePayLauncher.presentForPaymentIntent(clientSecret)
        }

        return view
    }

    private fun onGooglePayReady(isReady: Boolean) {
        googlePayButton.isEnabled = isReady
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                // Payment succeeded, navigate to the success screen
                navigateToSuccessScreen()
            }
            GooglePayLauncher.Result.Canceled -> {
                // User canceled the operation
            }
            is GooglePayLauncher.Result.Failed -> {
                // Operation failed; handle the error
                val errorMessage = result.error.localizedMessage
                showAlert("Payment failed: $errorMessage")
            }
        }
    }

    private fun navigateToSuccessScreen() {
        // Navigation logic to success screen
    }

    private fun showAlert(message: String) {
        // Show alert dialog with message
    }

    companion object {
        const val REQUEST_CODE = 123 // Define a request code for Google Pay API
    }
}
