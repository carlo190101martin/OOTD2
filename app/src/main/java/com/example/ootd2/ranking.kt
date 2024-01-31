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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ranking : Fragment() {

    // Firebase references
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference






    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)


        // Initialize Firebase
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().reference

        val uploadButton = view.findViewById<Button>(R.id.uploadButton)
        uploadButton.setOnClickListener {
            openDocumentPicker()
        }

        return view
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE)
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val uniqueFileName = "repeatScript_${System.currentTimeMillis()}"
        val storageRef = FirebaseStorage.getInstance().reference.child("people").child(uid).child("repeatScript").child(uniqueFileName)

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
        scriptRef.push().setValue(mapOf("url" to url))
            .addOnSuccessListener {
                // Database update successful
                triggerServerScript(userId)
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun triggerServerScript(userId: String) {
        val thread = Thread {
            try {
                val url = URL("https://your-ngrok-url.app/process-prescription")
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                httpURLConnection.doOutput = true

                val jsonInputString = "{\"userID\": \"$userId\"}"
                BufferedWriter(OutputStreamWriter(httpURLConnection.outputStream, "utf-8")).use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = httpURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle success - read response, etc.
                    val response = httpURLConnection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("ServerScript", "Response: $response")
                } else {
                    // Handle error - read error stream
                    val errorStream = httpURLConnection.errorStream.bufferedReader().use { it.readText() }
                    Log.e("ServerScript", "Error Response: $errorStream")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ServerScript", "Exception occurred: ${e.message}")
            }
        }
        thread.start()
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