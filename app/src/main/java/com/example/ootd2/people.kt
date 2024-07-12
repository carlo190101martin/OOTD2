

package com.example.ootd2

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.registerReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference


import java.io.File

class people : Fragment(), PdfAdapter.DownloadInitiator {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private lateinit var pdfPreviewImageView: ImageView
    private lateinit var downloadManager: DownloadManager
    private var downloadId: Long = 0
    private lateinit var onComplete: BroadcastReceiver
    private var pdfUrls: MutableList<String> = mutableListOf()
    private lateinit var pdfRecyclerView: RecyclerView
    private val downloadIds = mutableMapOf<Long, String>()
    data class Prescription(val url: String, val timestamp: Long)



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)




        return inflater.inflate(R.layout.fragment_people, container, false)

    }

    override fun onDownloadRequested(pdfUrl: String) {
        val request = DownloadManager.Request(Uri.parse(pdfUrl))
            .setTitle("Download PDF")
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        downloadIds[downloadId] = pdfUrl // Store the download ID and URL for later reference

        Log.d("PDFDownload", "Download enqueued, ID: $downloadId")
    }


    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(receiver)
    }



    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
            if (downloadIds.containsKey(id)) {
                // Download has completed, get the URI and share the file
                val downloadUri = downloadManager.getUriForDownloadedFile(id)
                if (downloadUri != null) {
                    shareDownloadedFile(downloadUri)
                } else {
                    // Handle error or invalid URI
                }
                downloadIds.remove(id) // Optionally remove the entry from the map
            }
        }
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pdfRecyclerView = view.findViewById(R.id.pdfRecyclerView)

        pdfRecyclerView.layoutManager = LinearLayoutManager(context)
        pdfPreviewImageView = view.findViewById(R.id.pdfPreviewImageView)
        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        fetchPrescriptionURL()




        pdfPreviewImageView = view.findViewById(R.id.pdfPreviewImageView)

        // Wait for the layout to be drawn to get accurate measurements
    }



    private fun fetchPDFURLsFromPath(userID: String, path: String, onComplete: (List<String>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("people/$userID/$path")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val urls = mutableListOf<String>()
                for (childSnapshot in snapshot.children) {
                    val pdfUrl = childSnapshot.child("url").value.toString()
                    if (pdfUrl.isNotEmpty() && pdfUrl != "null") {
                        urls.add(pdfUrl)
                    }
                }
                onComplete(urls)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching PDF URLs", error.toException())
            }
        })
    }

    private fun fetchPrescriptionURL() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val prescriptionsPath = "people/$userID/prescriptions"
        val repeatsPrescriptionsPath = "people/$userID/repeats/prescriptions" // New path for repeats-prescriptions
        val database = FirebaseDatabase.getInstance()
        val prescriptionsReference = database.getReference(prescriptionsPath)
        val repeatsPrescriptionsReference = database.getReference(repeatsPrescriptionsPath)

        val allPrescriptions = mutableListOf<Prescription>()

        // Combined listener for both paths
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val pdfUrl = childSnapshot.child("url").value.toString()
                    val timestamp = childSnapshot.child("timestamp").value as Long? ?: 0L
                    if (pdfUrl.isNotEmpty() && pdfUrl != "null") {
                        allPrescriptions.add(Prescription(pdfUrl, timestamp))
                    }
                }
                if (allPrescriptions.isNotEmpty()) {
                    // Sort by timestamp, latest first
                    val sortedPrescriptions = allPrescriptions.sortedByDescending { it.timestamp }
                    pdfUrls.clear()
                    pdfUrls.addAll(sortedPrescriptions.map { it.url }) // Assuming pdfUrls is a MutableList<String>
                    updateRecyclerView() // Call a method to update the RecyclerView
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching PDF URLs", error.toException())
            }
        }

        // Fetch prescriptions
        prescriptionsReference.addListenerForSingleValueEvent(valueEventListener)

        // Fetch repeats-prescriptions
        repeatsPrescriptionsReference.addListenerForSingleValueEvent(valueEventListener)
    }



    private fun fetchFromPath(databaseReference: DatabaseReference) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val pdfUrl = childSnapshot.child("url").value.toString()
                    if (pdfUrl.isNotEmpty() && pdfUrl != "null") {
                        pdfUrls.add(pdfUrl)
                    }
                }
                if (pdfUrls.isNotEmpty()) {
                    updateRecyclerView() // Call a method to update the RecyclerView
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching PDF URLs", error.toException())
            }
        })
    }
    private fun updateRecyclerView() {
        if (!::pdfRecyclerView.isInitialized) {
            return // Make sure pdfRecyclerView is initialized
        }
        val adapter = PdfAdapter(pdfUrls, lifecycleScope, this)
        pdfRecyclerView.adapter = adapter
    }


    private fun openPdfFromUri(uri: Uri) {
        requireContext().contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
            pdfRenderer?.close()
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            currentPage?.close()
            currentPage = pdfRenderer?.openPage(0)
            renderCurrentPage()
        }
    }

    private fun renderCurrentPage() {
        currentPage?.let { page ->
            Log.d("PDFRenderer", "Rendering current page.")
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Ensure background is white
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPreviewImageView.setImageBitmap(bitmap)
        }
    }


    private fun shareDownloadedFile(fileUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    }






    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close()
        pdfRenderer?.close()
    }
}
