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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SuccessFragment : Fragment() {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private lateinit var pdfPreviewImageView: ImageView
    private lateinit var downloadManager: DownloadManager
    private var downloadId: Long = 0
    private lateinit var onComplete: BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_success1, container, false)
        pdfPreviewImageView = view.findViewById(R.id.pdfPreviewImageView)
        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        fetchPrescriptionURL()
        val downloadButton: Button = view.findViewById(R.id.downloadButton)
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
                    Toast.makeText(requireContext(), "Download not complete yet. Please wait.", Toast.LENGTH_SHORT).show()
                }
            }
            cursor.close()
        }

        return view
    }

    private fun fetchPrescriptionURL() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val path = "people/$userID/repeats/prescriptions"
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val prescription = snapshot.children.lastOrNull()
                val pdfUrl = prescription?.child("url")?.value.toString()
                if (pdfUrl.isNotEmpty()) {
                    downloadAndOpenPdf(pdfUrl)
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
        requireContext().registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(onComplete)
        currentPage?.close()
        pdfRenderer?.close()
    }
}
