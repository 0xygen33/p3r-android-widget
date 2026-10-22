package com.example.p3moon

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object P3Renderer {

    private val DARK = Color.parseColor("#030A24")
    private val MID = Color.parseColor("#0B2C9A")
    private val BRIGHT = Color.parseColor("#33A0FF")
    private val ICE = Color.parseColor("#D8F6FF")
    private val DISC = Color.parseColor("#0A1233")

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val moonPath = Path()
    private val oval = RectF()
    private val matrix = Matrix()

    private fun ease(x: Float): Float {
        val c = x.coerceIn(0f, 1f)
        return 1f - (1f - c) * (1f - c) * (1f - c)
    }

    private fun fresh() {
        p.reset()
        p.isAntiAlias = true
    }

    @Synchronized
    fun draw(canvas: Canvas, w: Float, h: Float, date: LocalDate, t: Float, intro: Float) {
        val e = ease(intro)

        canvas.save()
        path.reset()
        path.addRoundRect(RectF(0f, 0f, w, h), h * 0.1f, h * 0.1f, Path.Direction.CW)
        canvas.clipPath(path)

        drawBackground(canvas, w, h, t)
        drawMoon(canvas, h * 0.5f, h * 0.5f, h * 0.29f * (0.6f + 0.4f * e), MoonPhase.phase(date), t, e)
        drawDate(canvas, w, h, date, e)

        fresh()
        p.style = Paint.Style.STROKE
        p.strokeWidth = h * 0.014f
        p.color = BRIGHT
        canvas.drawRoundRect(RectF(0f, 0f, w, h), h * 0.1f, h * 0.1f, p)
        canvas.restore()
    }

    private fun drawBackground(c: Canvas, w: Float, h: Float, t: Float) {
        fresh()
        p.shader = LinearGradient(0f, 0f, w, h, DARK, MID, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p)

        fresh()
        p.color = BRIGHT
        p.alpha = 28
        val spacing = h * 0.55f
        var x = -h + (t * h * 0.06f) % spacing
        while (x < w + h) {
            path.reset()
            path.moveTo(x, h)
            path.lineTo(x + h * 0.18f, h)
            path.lineTo(x + h * 0.78f, 0f)
            path.lineTo(x + h * 0.6f, 0f)
            path.close()
            c.drawPath(path, p)
            x += spacing
        }
    }

    private fun drawMoon(c: Canvas, cx: Float, cy: Float, r: Float, phase: Float, t: Float, e: Float) {
        val pulse = 0.5f + 0.5f * sin(t * 2.2f)

        fresh()
        p.shader = RadialGradient(
            cx, cy, r * 2.0f,
            intArrayOf(Color.argb((70 + 70 * pulse).toInt(), 51, 160, 255), Color.TRANSPARENT),
            floatArrayOf(0.35f, 1f), Shader.TileMode.CLAMP
        )
        c.drawCircle(cx, cy, r * 2.0f, p)

        fresh()
        p.style = Paint.Style.STROKE
        p.strokeWidth = r * 0.05f
        p.color = ICE
        p.alpha = (200 * e).toInt()
        c.drawCircle(cx, cy, r * 1.18f, p)

        p.strokeWidth = r * 0.04f
        p.alpha = (150 * e).toInt()
        p.pathEffect = DashPathEffect(floatArrayOf(r * 0.25f, r * 0.18f), 0f)
        c.save()
        c.rotate(t * 24f, cx, cy)
        c.drawCircle(cx, cy, r * 1.42f, p)
        c.restore()

        fresh()
        p.color = DISC
        c.drawCircle(cx, cy, r, p)

        if (buildLit(moonPath, cx, cy, r, phase)) {
            fresh()
            p.color = ICE
            c.drawPath(moonPath, p)

            c.save()
            c.clipPath(moonPath)
            fresh()
            p.color = Color.parseColor("#7FA8D8")
            p.alpha = 70
            c.drawCircle(cx - r * 0.30f, cy - r * 0.25f, r * 0.18f, p)
            c.drawCircle(cx + r * 0.25f, cy + r * 0.10f, r * 0.24f, p)
            c.drawCircle(cx - r * 0.10f, cy + r * 0.50f, r * 0.12f, p)
            c.drawCircle(cx + r * 0.40f, cy - r * 0.40f, r * 0.10f, p)
            c.restore()
        }

        fresh()
        p.style = Paint.Style.STROKE
        p.strokeWidth = r * 0.04f
        p.color = ICE
        p.alpha = 180
        c.drawCircle(cx, cy, r, p)
    }

    private fun buildLit(out: Path, cx: Float, cy: Float, r: Float, phase: Float): Boolean {
        val k = cos(2.0 * PI * phase).toFloat()
        if (k > 0.995f) return false

        out.reset()
        val rx = (r * abs(k)).coerceAtLeast(0.5f)

        oval.set(cx - r, cy - r, cx + r, cy + r)
        out.arcTo(oval, -90f, 180f, true)
        oval.set(cx - rx, cy - r, cx + rx, cy + r)
        if (k > 0f) out.arcTo(oval, 90f, -180f, false)
        else out.arcTo(oval, 90f, 180f, false)
        out.close()

        if (phase >= 0.5f) {
            matrix.setScale(-1f, 1f, cx, cy)
            out.transform(matrix)
        }
        return true
    }

    private fun drawDate(c: Canvas, w: Float, h: Float, date: LocalDate, e: Float) {
        val locale = Locale.getDefault()
        val x0 = h * 1.12f
        val avail = w - x0 - h * 0.1f
        val alpha = (255 * e).toInt()

        val month = date.month.getDisplayName(TextStyle.SHORT, locale)
            .uppercase(locale).trimEnd('.')
        val day = date.dayOfMonth.toString()
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).uppercase(locale)

        c.save()
        c.translate((1f - e) * h * 0.5f, 0f)

        fresh()
        p.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        p.textSkewX = -0.2f
        p.textSize = h * 0.15f
        p.letterSpacing = 0.15f
        p.color = ICE
        p.alpha = alpha
        c.drawText(month, x0, h * 0.27f, p)

        p.textSize = h * 0.58f
        p.letterSpacing = 0f
        while (p.measureText(day) > avail && p.textSize > 10f) p.textSize *= 0.95f
        p.color = BRIGHT
        p.alpha = alpha
        c.drawText(day, x0 + h * 0.03f, h * 0.74f + h * 0.03f, p)
        p.color = Color.WHITE
        p.alpha = alpha
        c.drawText(day, x0, h * 0.74f, p)

        p.textSize = h * 0.12f
        p.letterSpacing = 0.08f
        while (p.measureText(weekday) > avail - h * 0.1f && p.textSize > 8f) p.textSize *= 0.93f
        val tw = p.measureText(weekday)

        val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BRIGHT
            this.alpha = (230 * e).toInt()
        }
        path.reset()
        path.moveTo(x0 - h * 0.02f, h * 0.95f)
        path.lineTo(x0 + h * 0.04f, h * 0.79f)
        path.lineTo(x0 + tw + h * 0.12f, h * 0.79f)
        path.lineTo(x0 + tw + h * 0.06f, h * 0.95f)
        path.close()
        c.drawPath(path, bar)

        p.color = DARK
        p.alpha = alpha
        c.drawText(weekday, x0 + h * 0.04f, h * 0.91f, p)

        c.restore()
    }
}
