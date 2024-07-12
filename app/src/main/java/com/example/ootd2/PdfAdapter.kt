package com.example.ootd2

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfAdapter(
    private val pdfUrls: List<String>,
    private val scope: CoroutineScope,
    private val downloadInitiator: DownloadInitiator // Make sure this is here

) : RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {
    interface DownloadInitiator {
        fun onDownloadRequested(pdfUrl: String)
    }

    private val loadingJobs = mutableMapOf<Int, Job>()
    private val downloadIds = mutableMapOf<Long, String>() // Maps download ID to PDF URL


    class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pdfImageView: ImageView = view.findViewById(R.id.pdfImageView)
        val downloadButton: Button = view.findViewById(R.id.downloadPdfButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pdf_item, parent, false)
        return PdfViewHolder(view)
    }
    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val pdfUrl = pdfUrls[position]
        holder.pdfImageView.setImageResource(R.drawable.placeholder_image)

        holder.pdfImageView.setBackgroundColor(Color.TRANSPARENT)
        holder.pdfImageView.tag = pdfUrl


        // Cancel any ongoing loading for this position
        loadingJobs[position]?.cancel()

        holder.downloadButton.text = "Download & Share"

        holder.downloadButton.setOnClickListener {
            downloadInitiator.onDownloadRequested(pdfUrl)
        }



        // Start a new loading task
        loadingJobs[position] = loadPdfThumbnail(holder.pdfImageView, pdfUrl, position)
    }

    private fun loadPdfThumbnail(imageView: ImageView, pdfUrl: String, position: Int): Job {
        return scope.launch(Dispatchers.IO) {
            val bitmap = generateAndDisplayPdfThumbnail(imageView.context, pdfUrl)

            withContext(Dispatchers.Main) {
                // Make sure the current bind request is still valid
                if (imageView.tag == pdfUrl) {
                    bitmap?.let {
                        imageView.setImageBitmap(it)
                    }
                }
            }
        }

    }

    override fun onViewRecycled(holder: PdfViewHolder) {
        super.onViewRecycled(holder)
        // Optional: Clear the ImageView when the view is recycled
        holder.pdfImageView.setImageDrawable(null)
    }


    private suspend fun generateAndDisplayPdfThumbnail(context: Context, pdfUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Assume tempFile is a File object pointing to your PDF
            val tempFile = createTempFileFromUrl(context, pdfUrl)

            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY).use { parcelFileDescriptor ->
                PdfRenderer(parcelFileDescriptor).use { pdfRenderer ->
                    if (pdfRenderer.pageCount > 0) {
                        val page = pdfRenderer.openPage(0)

                        // Create a bitmap where to draw the PDF page
                        val originalBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)

                        // Create a canvas that wraps the bitmap and draw the PDF page on the bitmap
                        val canvas = Canvas(originalBitmap)
                        page.render(originalBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        // Now, originalBitmap contains the rendered PDF page

                        // Calculate the new size with margins
                        val margin = (8 * context.resources.displayMetrics.density).toInt() // Convert 8dp margin to pixels
                        val newWidth = originalBitmap.width + margin * 2
                        val newHeight = originalBitmap.height + margin * 2

                        // Create a new bitmap with the new size and a white background
                        val bitmapWithMargins = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                        val canvasWithMargins = Canvas(bitmapWithMargins)
                        canvasWithMargins.drawColor(Color.WHITE) // Fill the new bitmap with white

                        // Draw the original bitmap onto the new bitmap with margins
                        canvasWithMargins.drawBitmap(originalBitmap, margin.toFloat(), margin.toFloat(), null)

                        return@withContext bitmapWithMargins
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }


    private fun createTempFileFromUrl(context: Context, urlString: String): File {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        val tempFile = File.createTempFile("pdf_download", "pdf", context.cacheDir).apply {
            deleteOnExit()
        }

        connection.inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
    private fun downloadPdf(context: Context, pdfUrl: String) {
        Log.d("PDFDownload", "Starting download of PDF: $pdfUrl")
        val request = DownloadManager.Request(Uri.parse(pdfUrl))
            .setTitle("Download PDF")
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = downloadManager.enqueue(request)
        downloadIds[id] = pdfUrl // Store download ID and URL
        Log.d("PDFDownload", "Download enqueued, ID: $id")
    }


    override fun getItemCount(): Int = pdfUrls.size
}

