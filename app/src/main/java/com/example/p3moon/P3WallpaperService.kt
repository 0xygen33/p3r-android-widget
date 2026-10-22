package com.example.p3moon

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.time.LocalDate

class P3WallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = P3Engine()

    private inner class P3Engine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val t0 = SystemClock.uptimeMillis()
        private var introStart = t0
        private var visible = false
        private val bgPaint = Paint()

        private val tick = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) handler.postDelayed(this, 16)
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setTouchEventsEnabled(true)
        }

        override fun onVisibilityChanged(v: Boolean) {
            visible = v
            handler.removeCallbacks(tick)
            if (v) {
                introStart = SystemClock.uptimeMillis()
                handler.post(tick)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(tick)
            super.onSurfaceDestroyed(holder)
        }

        override fun onTouchEvent(event: MotionEvent) {
            if (event.action == MotionEvent.ACTION_DOWN) introStart = SystemClock.uptimeMillis()
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            val canvas = holder.lockCanvas() ?: return
            try {
                val now = SystemClock.uptimeMillis()
                val t = (now - t0) / 1000f
                val intro = ((now - introStart) / 900f).coerceIn(0f, 1f)

                val cw = canvas.width.toFloat()
                val ch = canvas.height.toFloat()

                bgPaint.shader = LinearGradient(
                    0f, 0f, 0f, ch,
                    Color.parseColor("#010514"), Color.parseColor("#06164F"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, cw, ch, bgPaint)

                val w = cw * 0.9f
                val h = w / 2f
                canvas.save()
                canvas.translate((cw - w) / 2f, ch * 0.2f)
                P3Renderer.draw(canvas, w, h, LocalDate.now(), t, intro)
                canvas.restore()
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
