package com.example.ootd2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment

class rootController : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_rootcontroller, container, false)

        val spinner = view.findViewById<Spinner>(R.id.medicationSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.medication_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter
        // No initial selection
        spinner.setSelection(Adapter.NO_SELECTION, true)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Assume position starts at 0 for actual selections
                val medication = parent.getItemAtPosition(position).toString()
                navigateToTreatment(medication)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optionally handle the case where nothing is selected
            }
        }

        return view
    }

    private fun navigateToTreatment(medication: String) {
        val intent = when (medication) {
            "Vitamin D 5000 IU" -> Intent(requireActivity(), VitaminDTreatmentActivity::class.java)
            "Finasteride" -> Intent(requireActivity(), FinasterideTreatmentActivity::class.java)
            "Viagra" -> Intent(requireActivity(), ViagraTreatmentActivity::class.java)
            "Finpecia (Hairloss)" -> Intent(requireActivity(), FinpeciaTreatmentActivity::class.java)
            "Pseudoephedrine (Seasonal viruses)" -> Intent(requireActivity(), PseudoephedrineTreatmentActivity::class.java)
            "Minoxidil (Hairloss)" -> Intent(requireActivity(), MinoxidilTreatmentActivity::class.java)
            "Zovirax (Cold Sores)" -> Intent(requireActivity(), ZoviraxTreatmentActivity::class.java)
            "Retin-A (Acne)" -> Intent(requireActivity(), RetinATreatmentActivity::class.java)
            "Truvada (Prep)" -> Intent(requireActivity(), TruvadaTreatmentActivity::class.java)
            "Detryp (Depression)" -> Intent(requireActivity(), DetrypTreatmentActivity::class.java)
           "Ventese (Asthma)" -> Intent(requireActivity(), VenteseTreatmentActivity::class.java)
            "Ozempic (Diabetes/Weight loss)" -> Intent(requireActivity(), OzempicTreatmentActivity::class.java)
            "Xanax (Anxiety)" -> Intent(requireActivity(), XanaxTreatmentActivity::class.java)
            "Zolpidem (Sleep aid)" -> Intent(requireActivity(), ZolpidemTreatmentActivity::class.java)
            "Vitamin D (Vit D deficiency)" -> Intent(requireActivity(), VitaminDTreatmentActivity::class.java)  // Assuming same activity as "Vitamin D 5000 IU"
            else -> null  // Use null for the default case where no action is needed
        }
        intent?.let { startActivity(it) }  // Only start the activity if the intent is not null
    }
}