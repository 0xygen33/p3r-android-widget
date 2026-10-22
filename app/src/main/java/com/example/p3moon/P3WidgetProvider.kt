package com.example.p3moon

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.RemoteViews
import java.time.LocalDate

class P3WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) mgr.updateAppWidget(id, buildViews(context, t = 0f, intro = 1f))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_ANIMATE -> animate(context)
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> refreshAll(context)
        }
    }

    private fun refreshAll(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, P3WidgetProvider::class.java))
        onUpdate(context, mgr, ids)
    }

    private fun animate(context: Context) {
        val pending = goAsync()
        Thread {
            try {
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, P3WidgetProvider::class.java))
                val frames = 16
                for (i in 0..frames) {
                    val intro = i / frames.toFloat()
                    val views = buildViews(context, t = i * 0.07f, intro = intro)
                    for (id in ids) mgr.updateAppWidget(id, views)
                    Thread.sleep(60)
                }
                for (id in ids) mgr.updateAppWidget(id, buildViews(context, 0f, 1f))
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun buildViews(context: Context, t: Float, intro: Float): RemoteViews {
        val bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        P3Renderer.draw(Canvas(bmp), WIDTH.toFloat(), HEIGHT.toFloat(), LocalDate.now(), t, intro)

        val views = RemoteViews(context.packageName, R.layout.widget)
        views.setImageViewBitmap(R.id.img, bmp)

        val tap = Intent(context, P3WidgetProvider::class.java).setAction(ACTION_ANIMATE)
        val pi = PendingIntent.getBroadcast(
            context, 0, tap, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.img, pi)
        return views
    }

    companion object {
        const val ACTION_ANIMATE = "com.example.p3moon.ANIMATE"
        private const val WIDTH = 640
        private const val HEIGHT = 320
    }
}
