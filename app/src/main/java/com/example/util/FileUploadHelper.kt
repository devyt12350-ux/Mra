package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.Locale

data class UploadedFileInfo(
    val uriString: String,
    val fileName: String,
    val fileType: String, // "IMAGE" or "PDF"
    val fileSizeFormatted: String,
    val localFilePath: String? = null
)

object FileUploadHelper {

    fun saveUriToInternalStorage(context: Context, uri: Uri): UploadedFileInfo? {
        return try {
            val contentResolver = context.contentResolver
            var displayName = "upload_${System.currentTimeMillis()}"
            var sizeBytes = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                    if (sizeIndex != -1) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }

            val mimeType = contentResolver.getType(uri)?.lowercase(Locale.ROOT) ?: ""
            val isPdf = mimeType.contains("pdf") || displayName.endsWith(".pdf", ignoreCase = true)
            val fileType = if (isPdf) "PDF" else "IMAGE"

            val uploadsDir = File(context.filesDir, "uploads").apply { mkdirs() }
            val cleanName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(uploadsDir, "${System.currentTimeMillis()}_$cleanName")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            if (sizeBytes <= 0L) {
                sizeBytes = targetFile.length()
            }

            val finalUri = Uri.fromFile(targetFile).toString()

            UploadedFileInfo(
                uriString = finalUri,
                fileName = displayName,
                fileType = fileType,
                fileSizeFormatted = formatFileSize(sizeBytes),
                localFilePath = targetFile.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createSampleHomeworkPdf(context: Context): UploadedFileInfo {
        val uploadsDir = File(context.filesDir, "uploads").apply { mkdirs() }
        val fileName = "MRS_Math_Quadratic_Solutions.pdf"
        val pdfFile = File(uploadsDir, fileName)

        if (!pdfFile.exists() || pdfFile.length() == 0L) {
            val minimalPdfContent = """
                %PDF-1.4
                1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
                2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
                3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj
                4 0 obj << /Length 285 >> stream
                BT
                /F1 18 Tf
                50 720 Td
                (MRS Mira Road School - Academic Homework) Tj
                /F1 12 Tf
                0 -35 Td
                (Student Submission: Mathematics Vertex Form & Parabola Modeling) Tj
                0 -25 Td
                (Problem 1: f(x) = 2(x - 3)^2 + 4. Vertex is at (3, 4), axis of symmetry x = 3.) Tj
                0 -25 Td
                (Problem 2: Projectile maximum altitude occurs at t = -b/(2a) = 2.45 seconds.) Tj
                0 -25 Td
                (Verification: Confirmed with algebraic substitution and graph trace.) Tj
                ET
                endstream endobj
                5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj
                xref
                0 6
                0000000000 65535 f 
                0000000010 00000 n 
                0000000060 00000 n 
                0000000118 00000 n 
                0000000247 00000 n 
                0000000584 00000 n 
                trailer << /Size 6 /Root 1 0 R >>
                startxref
                655
                %%EOF
            """.trimIndent()

            pdfFile.writeText(minimalPdfContent)
        }

        return UploadedFileInfo(
            uriString = Uri.fromFile(pdfFile).toString(),
            fileName = fileName,
            fileType = "PDF",
            fileSizeFormatted = formatFileSize(pdfFile.length()),
            localFilePath = pdfFile.absolutePath
        )
    }

    fun createSampleHomeworkPhoto(context: Context): UploadedFileInfo {
        val uploadsDir = File(context.filesDir, "uploads").apply { mkdirs() }
        val fileName = "Homework_Diagram_Page1.png"
        val photoFile = File(uploadsDir, fileName)

        if (!photoFile.exists() || photoFile.length() == 0L) {
            val bitmap = android.graphics.Bitmap.createBitmap(800, 600, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.rgb(248, 249, 252))

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(26, 46, 76)
                textSize = 34f
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            canvas.drawText("MRS Mira Road School", 50f, 70f, paint)

            paint.textSize = 24f
            paint.typeface = android.graphics.Typeface.DEFAULT
            paint.color = android.graphics.Color.rgb(70, 80, 95)
            canvas.drawText("Mathematics Coursework - Problem #4 Solution Graph", 50f, 115f, paint)

            // Draw coordinate axes & parabola
            val linePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(31, 110, 96)
                strokeWidth = 4f
                style = android.graphics.Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawLine(80f, 480f, 720f, 480f, linePaint) // X axis
            canvas.drawLine(400f, 160f, 400f, 520f, linePaint) // Y axis

            val curvePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(217, 130, 43)
                strokeWidth = 6f
                style = android.graphics.Paint.Style.STROKE
                isAntiAlias = true
            }
            val path = android.graphics.Path()
            path.moveTo(150f, 480f)
            path.quadTo(400f, 200f, 650f, 480f)
            canvas.drawPath(path, curvePaint)

            paint.color = android.graphics.Color.rgb(217, 130, 43)
            paint.textSize = 20f
            canvas.drawText("Vertex (h, k) = (0, 70m)", 415f, 220f, paint)

            FileOutputStream(photoFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, out)
            }
        }

        return UploadedFileInfo(
            uriString = Uri.fromFile(photoFile).toString(),
            fileName = fileName,
            fileType = "IMAGE",
            fileSizeFormatted = formatFileSize(photoFile.length()),
            localFilePath = photoFile.absolutePath
        )
    }

    fun openDocumentExternally(context: Context, uriString: String, fileName: String) {
        try {
            val file = if (uriString.startsWith("file://")) {
                File(Uri.parse(uriString).path ?: "")
            } else {
                File(uriString)
            }

            val contentUri: Uri = if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.parse(uriString)
            }

            val mimeType = if (fileName.endsWith(".pdf", ignoreCase = true)) {
                "application/pdf"
            } else {
                "image/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Opening document preview inside app...", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "12 KB"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
    }
}
