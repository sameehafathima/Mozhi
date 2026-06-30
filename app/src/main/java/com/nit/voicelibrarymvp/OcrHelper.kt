package com.nit.voicelibrarymvp

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object OcrHelper {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractText(bitmap: Bitmap): Text? {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            recognizer.process(image).await()
        } catch (e: Exception) {
            Log.e("OcrHelper", "Text extraction failed", e)
            null
        }
    }

    data class BookInfo(
        val title: String = "",
        val author: String = "",
        val publisher: String = "",
        val accession: String = "",
        val isbn: String = "",
        val year: Int? = null
    )

    fun parseBookInfo(result: Text?): BookInfo {
        if (result == null) return BookInfo()
        val blocks = result.textBlocks
        if (blocks.isEmpty()) return BookInfo()

        var title = ""
        var author = ""
        
        // Often the largest text is the title
        val sortedByHeight = blocks.sortedByDescending { it.boundingBox?.height() ?: 0 }
        title = sortedByHeight[0].text.replace("\n", " ")
        
        if (sortedByHeight.size > 1) {
            val second = sortedByHeight[1].text.replace("\n", " ")
            if (second.lowercase().contains("by ") || second.length < title.length) {
                author = second.replace("(?i)by ".toRegex(), "").trim()
            }
        }

        return BookInfo(title = title, author = author)
    }
    
    fun parseRegisterRows(result: Text?): List<BookInfo> {
        if (result == null) return emptyList()
        
        val lines = result.textBlocks.flatMap { it.lines }
        if (lines.isEmpty()) return emptyList()

        val rows = mutableListOf<MutableList<Text.Line>>()
        val sortedLines = lines.sortedBy { it.boundingBox?.top ?: 0 }

        for (line in sortedLines) {
            val lineBounds = line.boundingBox ?: continue
            val centerY = lineBounds.centerY()
            
            var added = false
            for (row in rows) {
                val rowSample = row[0].boundingBox ?: continue
                if (Math.abs(centerY - rowSample.centerY()) < (rowSample.height() * 0.8)) {
                    row.add(line)
                    added = true
                    break
                }
            }
            if (!added) {
                rows.add(mutableListOf(line))
            }
        }

        return rows.mapNotNull { rowLines ->
            val sortedRow = rowLines.sortedBy { it.boundingBox?.left ?: 0 }
            if (sortedRow.size >= 2) {
                val textParts = sortedRow.map { it.text.trim() }
                if (textParts.size >= 3) {
                    val acc = textParts[0].filter { it.isDigit() }
                    BookInfo(accession = acc, title = textParts[1], author = textParts[2])
                } else {
                    BookInfo(title = textParts[0], author = textParts[1])
                }
            } else if (sortedRow.size == 1) {
                val lineText = sortedRow[0].text
                val parts = lineText.split(Regex("\\s{2,}|\\t|\\|")).filter { it.isNotBlank() }
                if (parts.size >= 2) {
                    BookInfo(title = parts[0], author = parts[1])
                } else null
            } else null
        }
    }

}
