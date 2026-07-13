package com.example.glucoserecordbook.export

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.glucoserecordbook.data.Reading
import com.example.glucoserecordbook.data.ReadingStatus
import com.example.glucoserecordbook.data.ReadingType
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Writes a readable, date-grouped copy of the local record book to a caller-owned document Uri. */
object GlucosePdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_TOP = 116f
    private const val CONTENT_BOTTOM = 790f
    private const val DATE_HEIGHT = 24f
    private const val ROW_HEIGHT = 22f
    private const val GROUP_SPACING = 12f

    fun write(
        contentResolver: ContentResolver,
        destination: Uri,
        history: Map<LocalDate, List<Reading>>,
        exportedOn: LocalDate = LocalDate.now()
    ) {
        val document = PdfDocument()
        try {
            var pageNumber = 0
            var page: PdfDocument.Page? = null
            var y = CONTENT_TOP

            fun finishPage() {
                val currentPage = page ?: return
                currentPage.canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - 24f, footerPaint)
                document.finishPage(currentPage)
                page = null
            }

            fun startPage() {
                finishPage()
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                val canvas = page!!.canvas
                canvas.drawText("Glucose Records", MARGIN, 52f, titlePaint)
                canvas.drawText(
                    "Exported ${exportedOn.format(exportDateFormatter)}",
                    MARGIN,
                    76f,
                    subtitlePaint
                )
                canvas.drawText("Reading", MARGIN, 102f, tableHeadingPaint)
                canvas.drawText("Result", RESULT_COLUMN_X, 102f, tableHeadingPaint)
                canvas.drawLine(MARGIN, 108f, PAGE_WIDTH - MARGIN, 108f, rulePaint)
                y = CONTENT_TOP
            }

            startPage()
            history.toSortedMap(compareByDescending { it }).forEach { (date, readings) ->
                val rows = ReadingType.entries
                val groupHeight = DATE_HEIGHT + rows.size * ROW_HEIGHT + GROUP_SPACING
                if (y + groupHeight > CONTENT_BOTTOM) startPage()

                val canvas = page!!.canvas
                canvas.drawText(date.format(recordDateFormatter).uppercase(), MARGIN, y, datePaint)
                y += DATE_HEIGHT

                val byType = readings.associateBy { it.type }
                rows.forEach { type ->
                    canvas.drawText(type.label, MARGIN, y, bodyPaint)
                    canvas.drawText(byType[type].toPdfValue(), RESULT_COLUMN_X, y, valuePaint)
                    y += ROW_HEIGHT
                }
                canvas.drawLine(MARGIN, y - 10f, PAGE_WIDTH - MARGIN, y - 10f, rulePaint)
                y += GROUP_SPACING
            }
            finishPage()

            contentResolver.openOutputStream(destination)?.use { output ->
                document.writeTo(output)
            } ?: throw IOException("Could not open the selected file")
        } finally {
            document.close()
        }
    }

    private fun Reading?.toPdfValue(): String = when {
        this == null -> "Not recorded"
        status == ReadingStatus.SKIPPED -> "Not taken"
        else -> "$value mg/dL"
    }

    private const val RESULT_COLUMN_X = 390f
    private val exportDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
    private val recordDateFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")
        .withLocale(Locale.getDefault())
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(36, 73, 47)
        textSize = 24f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 11f
    }
    private val tableHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 11f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(36, 73, 47)
        textSize = 13f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 11f
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 11f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(210, 210, 210)
        strokeWidth = 1f
    }
    private val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 9f
        textAlign = Paint.Align.CENTER
    }
}
