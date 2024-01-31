package com.example.ootd2


import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Context
import java.io.IOException
import android.content.Context.DOWNLOAD_SERVICE




class displaypdf: AppCompatActivity() {

    private lateinit var pdfImageView: ImageView
    private lateinit var viewPdfButton: Button
    private lateinit var downloadPdfButton: Button
    private var downloadedPdfUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_displaypdf)


        pdfImageView = findViewById(R.id.pdfImageView)
        viewPdfButton = findViewById(R.id.viewPdfButton)
        downloadPdfButton = findViewById(R.id.downloadPdfButton)

        viewPdfButton.setOnClickListener {
            downloadedPdfUri?.let { uri ->
                displayPdfFromUri(uri)
            }
        }
        downloadPdfButton.setOnClickListener {
            // Code to download PDF
        }
    }
    private fun downloadAndDisplayPDF(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDestinationInExternalFilesDir(this, null, "downloaded.pdf")

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadId = downloadManager.enqueue(request)

        val query = DownloadManager.Query().setFilterById(downloadId)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val c = downloadManager.query(query)
                downloadedPdfUri = downloadManager.getUriForDownloadedFile(downloadId) // Save the URI
                if (c.moveToFirst()) {
                    val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        val uri = downloadManager.getUriForDownloadedFile(downloadId)
                        displayPdfFromUri(uri)
                    }
                }
            }
        }

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    private fun displayPdfFromUri(uri: Uri) {
        try {
            val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor?.let {
                val pdfRenderer = PdfRenderer(it)
                val pageCount = pdfRenderer.pageCount
                if (pageCount > 0) {
                    val page = pdfRenderer.openPage(0) // Open first page
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pdfImageView.setImageBitmap(bitmap)
                    page.close()
                }
                pdfRenderer.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle exception
        }
    }
    private fun fetchPdfUrl() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("your_reference_path")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val url = dataSnapshot.getValue(String::class.java)
                url?.let { downloadAndDisplayPDF(it) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun sharePdf() {
        downloadedPdfUri?.let { uri ->
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/pdf"
            }
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        }
    }


}