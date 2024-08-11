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
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.json.JSONObject
import java.io.IOException


class ranking : Fragment() {

    ///////NOTE USE THIS NOT THE REPEAT.KT

    // Firebase references
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var checkBox: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)


        // Initialize Firebase
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference

        checkBox = view.findViewById(R.id.checkboxNegativeTest)

        val navigateToToPay = arguments?.getBoolean("navigateToToPay", false) ?: false
        if (navigateToToPay) {
            navigateToToPayFragment1()
        }


        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        uploadButton.setOnClickListener {
            openDocumentPicker()
        }

        return view
    }
    private fun navigateToToPayFragment1() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, toPay())
            .addToBackStack(null)
            .commit()
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE)
    }

    fun renderPdfThumbnail(pdfUri: Uri, imageView: ImageView) {
        val context = imageView.context
        try {
            context.contentResolver.openFileDescriptor(pdfUri, "r")?.use { parcelFileDescriptor ->
                PdfRenderer(parcelFileDescriptor).use { pdfRenderer ->
                    val page = pdfRenderer.openPage(0) // Open the first page
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    imageView.setImageBitmap(bitmap) // Set the rendered bitmap to ImageView
                    page.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle exceptions
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                uploadScript(uri)
            }
        }
    }

    private fun uploadScript(fileUri: Uri) {

        val fullNameEditText = view?.findViewById<EditText>(R.id.fullNameEditText)
        val fullName = fullNameEditText?.text.toString() // Retrieve the full name

        if (fullName.isEmpty()) {
            // Show an alert dialog or toast message indicating that the full name is required
            showAlert("Please enter your full name before uploading a document.")
            return // Stop the upload process
        }


        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val uniqueFileName = "repeatScript_${System.currentTimeMillis()}"
        val storageRef = FirebaseStorage.getInstance().reference.child("people").child(uid).child("repeatScript").child(uniqueFileName)

        FirebaseDatabase.getInstance().reference.child("people").child(uid).child("name").setValue(fullName)

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                // Handle success
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateDatabaseWithScriptURL(uri.toString(), uid)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun updateDatabaseWithScriptURL(url: String, userId: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val scriptRef = databaseRef.child("people").child(userId).child("repeat")
        val negativeTestConfirmed = checkBox.isChecked

        scriptRef.push().setValue(mapOf("url" to url))
            .addOnSuccessListener {
                // Database update successful
                triggerServerScript(userId, url, negativeTestConfirmed)
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
    private fun triggerServerScript(userId: String, scriptUrl: String, negativeTestConfirmed: Boolean) {
        val thread = Thread {
            try {
                val url = URL("https://a2ba-197-245-44-141.ngrok-free.app/process-prescription")
                Log.d("ceferer", "process pre called")

                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                httpURLConnection.doOutput = true

                val jsonInputString = "{\"userID\": \"$userId\", \"negativeTestConfirmed\": $negativeTestConfirmed, \"documentUrl\": \"$scriptUrl\"}"
                BufferedWriter(OutputStreamWriter(httpURLConnection.outputStream, "utf-8")).use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = httpURLConnection.responseCode
                val response = httpURLConnection.inputStream.bufferedReader().use { it.readText() }
                Log.d("ServerUpdate", "Server response: $response")

                val jsonResponse = JSONObject(response)
                if (responseCode == HttpURLConnection.HTTP_OK && jsonResponse.getBoolean("success")) {
                    activity?.runOnUiThread {
                        navigateToToPayFragment()
                    }
                } else {
                    val errorMessage = jsonResponse.optString("error", "Unknown error")
                    Log.e("ServerUpdate", "Error: $errorMessage")
                    activity?.runOnUiThread {
                        showAlert("Error: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ServerUpdate", "Exception during server update: ${e.message}")
                activity?.runOnUiThread {
                    showAlert("Exception: ${e.message}")
                }
            }
        }
        thread.start()
    }

    private fun navigateToToPayFragment() {
        val fragmentManager = activity?.supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()

        // Instantiate your toPay Fragment
        val toPayFragment = toPay() // Make sure 'toPay' matches the class name of your Fragment




        // Perform the transaction to replace the current Fragment with toPay Fragment
        val intent = Intent(requireContext(), ThankYou::class.java)
        startActivity(intent)
        fragmentTransaction?.addToBackStack(null) // Optional, adds the transaction to the back stack
        fragmentTransaction?.commit() // Commit the transaction
    }

    private fun showAlert(message: String) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("OK", null)
        alertDialog.show()
    }

    companion object {
        private const val REQUEST_CODE = 123
    }
}