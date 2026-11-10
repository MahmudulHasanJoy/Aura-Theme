package com.example.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperScreenTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperHelper {

    suspend fun applyWallpaper(
        context: Context,
        wallpaper: WallpaperItem,
        target: WallpaperScreenTarget
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context.applicationContext)
            val bitmap = BitmapFactory.decodeResource(context.resources, wallpaper.drawableRes)
                ?: return@withContext Result.failure(Exception("Failed to decode wallpaper bitmap"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val flags = when (target) {
                    WallpaperScreenTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                    WallpaperScreenTarget.LOCK -> WallpaperManager.FLAG_LOCK
                    WallpaperScreenTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, flags)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            val targetLabel = when (target) {
                WallpaperScreenTarget.HOME -> "Home Screen"
                WallpaperScreenTarget.LOCK -> "Lock Screen"
                WallpaperScreenTarget.BOTH -> "Home & Lock Screens"
            }
            Result.success("Wallpaper applied to $targetLabel successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
