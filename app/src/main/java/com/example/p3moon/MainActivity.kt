package com.example.p3moon

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            startActivity(
                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this, P3WallpaperService::class.java)
                )
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Imposta la live wallpaper dalle impostazioni sfondo", Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
