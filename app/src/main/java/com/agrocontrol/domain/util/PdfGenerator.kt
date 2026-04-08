package com.agrocontrol.domain.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.agrocontrol.domain.model.Cultivo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    // ─── Paleta de colores ───────────────────────────────────────────────────
    private val COLOR_BRAND_DARK   = Color.rgb(20,  83,  45)   // Verde profundo
    private val COLOR_BRAND_MID    = Color.rgb(34, 139,  34)   // Verde medio
    private val COLOR_BRAND_LIGHT  = Color.rgb(187, 247, 208)  // Verde claro
    private val COLOR_ACCENT       = Color.rgb(234, 179,   8)  // Amarillo dorado
    private val COLOR_SURFACE      = Color.rgb(240, 253, 244)  // Fondo verdoso suave
    private val COLOR_TEXT_PRIMARY = Color.rgb(15,  23,  42)   // Casi negro
    private val COLOR_TEXT_MUTED   = Color.rgb(100, 116, 139)  // Gris slate
    private val COLOR_DIVIDER      = Color.rgb(209, 250, 229)  // Verde muy claro
    private val COLOR_ALERT_BG     = Color.rgb(255, 251, 235)  // Fondo alerta
    private val COLOR_ALERT_BORDER = Color.rgb(252, 211,  77)  // Borde alerta
    private val COLOR_WHITE        = Color.WHITE

    // ─── Dimensiones A4 (595 × 842 pt) ──────────────────────────────────────
    private const val PAGE_W  = 595f
    private const val PAGE_H  = 842f
    private const val MARGIN  = 48f
    private const val CONTENT_W = PAGE_W - MARGIN * 2

    fun generateAndShareCultivoReport(
        context: Context,
        cultivo: Cultivo,
        climaContexto: String,
        alertasActivas: Int,
        userName: String
    ) {
        val pdfDocument = PdfDocument()
        val dateString  = SimpleDateFormat("dd 'de' MMMM yyyy, HH:mm", Locale("es")).format(Date())
        val dateSiembra = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(cultivo.fechaSiembra))

        // ── Página 1 ─────────────────────────────────────────────────────────
        val page1 = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val c1 = page1.canvas

        drawBackground(c1)
        val headerBottom = drawHeader(c1, userName, dateString)
        var y = headerBottom + 28f

        y = drawSectionTitle(c1, "Información del Cultivo", y)
        y = drawInfoGrid(c1, y, listOf(
            Pair("Tipo de cultivo",        cultivo.tipoCultivo),
            Pair("Superficie",             "${cultivo.hectareas} ha"),
            Pair("Región",                 cultivo.region),
            Pair("Etapa fenológica",       cultivo.etapaActual.name),
            Pair("Fecha de siembra",       dateSiembra)
        ))

        y += 10f
        y = drawSectionTitle(c1, "Condiciones Actuales", y)
        y = drawAlertBadge(c1, y, alertasActivas)
        y += 10f
        y = drawBodyCard(c1, y, climaContexto)

        drawFooter(c1, 1)
        pdfDocument.finishPage(page1)

        // ── Guardar y compartir ───────────────────────────────────────────────
        val fileDir = File(context.cacheDir, "pdfs").also { if (!it.exists()) it.mkdirs() }
        val file    = File(fileDir, "AgroControl_Reporte_${cultivo.tipoCultivo}_${System.currentTimeMillis()}.pdf")

        try { pdfDocument.writeTo(FileOutputStream(file)) } catch (e: Exception) { e.printStackTrace() }
        pdfDocument.close()
        sharePdf(context, file)
    }

    // ─── Fondo con textura sutil ─────────────────────────────────────────────
    private fun drawBackground(c: Canvas) {
        val bg = Paint().apply { color = COLOR_WHITE }
        c.drawRect(0f, 0f, PAGE_W, PAGE_H, bg)

        // Franja lateral izquierda decorativa
        val stripe = Paint().apply { color = COLOR_SURFACE }
        c.drawRect(0f, 0f, 6f, PAGE_H, stripe)

        // Puntos decorativos esquina superior derecha
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BRAND_LIGHT
            style = Paint.Style.FILL
        }
        val cols = 5; val rows = 5; val spacing = 12f; val startX = PAGE_W - MARGIN - cols * spacing
        for (col in 0 until cols) for (row in 0 until rows) {
            c.drawCircle(startX + col * spacing, 24f + row * spacing, 2f, dot)
        }
    }

    // ─── Cabecera con gradiente ──────────────────────────────────────────────
    private fun drawHeader(c: Canvas, userName: String, dateString: String): Float {
        val headerH = 130f

        // Gradiente de fondo
        val grad = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, PAGE_W, headerH,
                COLOR_BRAND_DARK, COLOR_BRAND_MID, Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, PAGE_W, headerH, grad)

        // Círculo decorativo derecho
        val circle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(30, 255, 255, 255)
            style  = Paint.Style.FILL
        }
        c.drawCircle(PAGE_W - 40f, -20f, 100f, circle)
        c.drawCircle(PAGE_W - 20f, 80f,   60f, circle)

        // Logo text "AC"
        val logoBox = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color  = COLOR_ACCENT
            style  = Paint.Style.FILL
        }
        val logoRect = RectF(MARGIN, 22f, MARGIN + 44f, 66f)
        c.drawRoundRect(logoRect, 8f, 8f, logoBox)

        val logoText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = COLOR_BRAND_DARK
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText("AC", MARGIN + 8f, 52f, logoText)

        // Título
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = COLOR_WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText("Reporte Oficial de Cultivo", MARGIN + 54f, 46f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = Color.argb(180, 255, 255, 255)
            textSize = 11f
        }
        c.drawText("AgroControl — Sistema de Gestión Agrícola Inteligente", MARGIN + 54f, 62f, subPaint)

        // Línea divisoria sutil
        val divLine = Paint().apply { color = Color.argb(60, 255, 255, 255); strokeWidth = 1f }
        c.drawLine(MARGIN, 80f, PAGE_W - MARGIN, 80f, divLine)

        // Productor y fecha
        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = Color.argb(220, 255, 255, 255)
            textSize = 11f
        }
        c.drawText("Productor: $userName", MARGIN, 100f, metaPaint)
        c.drawText("Generado: $dateString", MARGIN, 118f, metaPaint)

        // Badge "OFICIAL"
        val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_ACCENT; style = Paint.Style.FILL
        }
        val bR = RectF(PAGE_W - MARGIN - 70f, 84f, PAGE_W - MARGIN, 106f)
        c.drawRoundRect(bR, 12f, 12f, badgeBg)
        val badgeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BRAND_DARK; textSize = 10f; typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText("OFICIAL", PAGE_W - MARGIN - 56f, 99f, badgeText)

        return headerH
    }

    // ─── Título de sección ───────────────────────────────────────────────────
    private fun drawSectionTitle(c: Canvas, title: String, y: Float): Float {
        // Píldora de color
        val pill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BRAND_LIGHT; style = Paint.Style.FILL
        }
        c.drawRoundRect(RectF(MARGIN, y, MARGIN + 4f, y + 22f), 2f, 2f, pill)

        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = COLOR_BRAND_DARK
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText(title.uppercase(), MARGIN + 12f, y + 16f, tp)

        // Línea divisoria
        val line = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 1f }
        c.drawLine(MARGIN, y + 26f, PAGE_W - MARGIN, y + 26f, line)

        return y + 38f
    }

    // ─── Grilla de datos 2 columnas ──────────────────────────────────────────
    private fun drawInfoGrid(c: Canvas, startY: Float, items: List<Pair<String, String>>): Float {
        val colW   = CONTENT_W / 2f
        val rowH   = 44f
        val padX   = 12f
        val padY   = 10f
        var y      = startY

        items.forEachIndexed { idx, (label, value) ->
            val col = idx % 2
            val x   = MARGIN + col * colW
            if (idx % 2 == 0 && idx > 0) y += rowH

            // Fondo de celda alternado
            val cellBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if ((idx / 2) % 2 == 0) COLOR_SURFACE else COLOR_WHITE
                style = Paint.Style.FILL
            }
            c.drawRoundRect(RectF(x + 2f, y, x + colW - 4f, y + rowH - 4f), 6f, 6f, cellBg)

            // Borde sutil
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_DIVIDER; style = Paint.Style.STROKE; strokeWidth = 1f
            }
            c.drawRoundRect(RectF(x + 2f, y, x + colW - 4f, y + rowH - 4f), 6f, 6f, border)

            // Label
            val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_MUTED; textSize = 9f; typeface = Typeface.DEFAULT_BOLD
            }
            c.drawText(label.uppercase(), x + padX, y + padY + 10f, lp)

            // Value
            val vp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_TEXT_PRIMARY; textSize = 13f; typeface = Typeface.DEFAULT_BOLD
            }
            c.drawText(value, x + padX, y + padY + 28f, vp)
        }

        // Si número impar, avanzar fila final
        val totalRows = Math.ceil(items.size / 2.0).toInt()
        return startY + totalRows * rowH + 8f
    }

    // ─── Badge de alertas ─────────────────────────────────────────────────────
    private fun drawAlertBadge(c: Canvas, y: Float, alertas: Int): Float {
        val (bgColor, borderColor, icon, label) = when {
            alertas == 0 -> arrayOf(Color.rgb(240, 253, 244), Color.rgb(34, 197, 94), "✓", "Sin alertas activas")
            alertas <= 2 -> arrayOf(COLOR_ALERT_BG, COLOR_ALERT_BORDER, "⚠", "$alertas alerta${if (alertas > 1) "s" else ""} activa${if (alertas > 1) "s" else ""}")
            else         -> arrayOf(Color.rgb(255, 241, 242), Color.rgb(252, 100, 100), "!", "$alertas alertas críticas activas")
        }

        val bh = 44f
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor as Int; style = Paint.Style.FILL
        }
        c.drawRoundRect(RectF(MARGIN, y, PAGE_W - MARGIN, y + bh), 8f, 8f, bg)

        val bd = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor as Int; style = Paint.Style.STROKE; strokeWidth = 1.5f
        }
        c.drawRoundRect(RectF(MARGIN, y, PAGE_W - MARGIN, y + bh), 8f, 8f, bd)

        // Círculo ícono
        val ic = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor as Int; style = Paint.Style.FILL
        }
        c.drawCircle(MARGIN + 22f, y + bh / 2f, 12f, ic)

        val iconP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_WHITE; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText(icon as String, MARGIN + 16f, y + bh / 2f + 6f, iconP)

        val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY; textSize = 13f; typeface = Typeface.DEFAULT_BOLD
        }
        c.drawText(label as String, MARGIN + 42f, y + bh / 2f + 5f, lp)

        return y + bh + 8f
    }

    // ─── Tarjeta de texto con wrapping ──────────────────────────────────────
    private fun drawBodyCard(c: Canvas, y: Float, text: String): Float {
        val bodyP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY; textSize = 12f
        }
        val lines = breakText(text, bodyP, CONTENT_W - 24f)
        val cardH = lines.size * 20f + 24f

        // Fondo tarjeta
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_SURFACE; style = Paint.Style.FILL
        }
        c.drawRoundRect(RectF(MARGIN, y, PAGE_W - MARGIN, y + cardH), 8f, 8f, bg)

        // Acento lateral
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BRAND_MID; style = Paint.Style.FILL
        }
        c.drawRoundRect(RectF(MARGIN, y, MARGIN + 4f, y + cardH), 4f, 4f, accent)

        var ty = y + 20f
        for (line in lines) {
            c.drawText(line, MARGIN + 16f, ty, bodyP)
            ty += 20f
        }

        return y + cardH + 8f
    }

    // ─── Footer ──────────────────────────────────────────────────────────────
    private fun drawFooter(c: Canvas, page: Int) {
        val lineP = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 1f }
        c.drawLine(MARGIN, PAGE_H - 38f, PAGE_W - MARGIN, PAGE_H - 38f, lineP)

        val leftP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED; textSize = 9f
        }
        c.drawText("AgroControl — Generado con IA offline", MARGIN, PAGE_H - 20f, leftP)

        val rightP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED; textSize = 9f
        }
        c.drawText("Página $page", PAGE_W - MARGIN - 40f, PAGE_H - 20f, rightP)

        // Punto de marca
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_ACCENT; style = Paint.Style.FILL
        }
        c.drawCircle(PAGE_W / 2f, PAGE_H - 22f, 3f, dot)
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────
    private fun breakText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) current = test
            else { if (current.isNotEmpty()) lines.add(current); current = word }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }

    private fun sharePdf(context: Context, file: File) {
        val uri    = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type   = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, "Reporte_AgroControl.pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte con agrónomo"))
    }
}
