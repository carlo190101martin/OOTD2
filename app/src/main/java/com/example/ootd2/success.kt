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
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class success : AppCompatActivity() {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private lateinit var pdfPreviewImageView: ImageView
    private lateinit var downloadManager: DownloadManager
    private var downloadId: Long = 0
    private lateinit var onComplete: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_success1)
        pdfPreviewImageView = findViewById(R.id.pdfPreviewImageView)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        fetchPrescriptionURL()
        val downloadButton: Button = findViewById(R.id.downloadButton)
        downloadButton.setOnClickListener {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                    val uri = downloadManager.getUriForDownloadedFile(downloadId)
                    // Call the share function directly here
                    shareDownloadedFile(uri)
                } else {
                    Toast.makeText(
                        this,
                        "Download not complete yet. Please wait.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            cursor.close()
        }

    }

    private fun fetchPrescriptionURL() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val path = "people/$userID/prescriptions"
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val thirtySecondsAgo = System.currentTimeMillis() - 300000 // 30 seconds ago no 5 min
                val latestPrescription = snapshot.children.filter { child ->
                    val timestamp = (child.child("timestamp").value as? Long) ?: Long.MIN_VALUE
                    timestamp > thirtySecondsAgo // Filter prescriptions within the last 30 seconds
                }.maxByOrNull { child ->
                    (child.child("timestamp").value as? Long) ?: Long.MIN_VALUE
                }

                val pdfUrl = latestPrescription?.child("url")?.value.toString()
                if (pdfUrl.isNotEmpty() && pdfUrl != "null") {
                    downloadAndOpenPdf(pdfUrl)
                } else {
                    Toast.makeText(this@success, "No recent prescription found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching PDF URL", error.toException())
            }
        })
    }

    private fun downloadAndOpenPdf(pdfUrl: String) {
        val request = DownloadManager.Request(Uri.parse(pdfUrl))
            .setTitle("Download PDF")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        downloadId = downloadManager.enqueue(request)

        onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                            val uri = downloadManager.getUriForDownloadedFile(downloadId)
                            openPdfFromUri(uri)
                            shareDownloadedFile(uri) // Call the share function here
                        }
                    }
                }
            }
        }
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun openPdfFromUri(uri: Uri) {
        contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
            pdfRenderer?.close()
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            currentPage?.close()
            currentPage = pdfRenderer?.openPage(0)
            renderCurrentPage()
        }
    }

    private fun renderCurrentPage() {
        currentPage?.let { page ->
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Fill the canvas with white before rendering
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
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
        currentPage?.close()
        pdfRenderer?.close()
    }
}