package com.example.ootd2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class rootController : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_rootcontroller, container, false)

        // Setup click listener for the Vitamin D button
        val vitaminDButton = view.findViewById<Button>(R.id.button2)
        vitaminDButton.setOnClickListener {
            navigateToTreatment("Vitamin D 5000 iU")
        }

        // Setup click listener for the Finasteride button
        val finasterideButton = view.findViewById<Button>(R.id.button5)
        finasterideButton.setOnClickListener {
            navigateToTreatment("Finasteride")
        }

        // Setup click listener for the Viagra button
        val viagraButton = view.findViewById<Button>(R.id.button8)
        viagraButton.setOnClickListener {
            navigateToTreatment("Viagra")
        }

        return view
    }

    private fun navigateToTreatment(medication: String) {
        val intent = when (medication) {
            "Vitamin D 5000 iU" -> Intent(requireActivity(), VitaminDTreatmentActivity::class.java)
            "Finasteride" -> Intent(requireActivity(), FinasterideTreatmentActivity::class.java)
            "Viagra" -> Intent(requireActivity(), ViagraTreatmentActivity::class.java)
            else -> return
        }
        startActivity(intent)
    }
}
