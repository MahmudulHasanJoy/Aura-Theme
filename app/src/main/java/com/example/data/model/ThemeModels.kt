package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.audio.AudioSynthEngine

data class WallpaperItem(
    val id: String,
    val title: String,
    val category: String,
    @DrawableRes val drawableRes: Int,
    val accentColor: Long,
    val tags: List<String>,
    val resolution: String = "4K UHD",
    val downloadCount: Int = 1240,
    val likesCount: Int = 450,
    val isAmoled: Boolean = false
)

data class RingtoneItem(
    val id: String,
    val title: String,
    val category: String,
    val toneType: AudioSynthEngine.ToneType,
    val durationSeconds: Int,
    val accentColor: Long,
    val isNotificationSound: Boolean = false,
    val tags: List<String> = emptyList(),
    val downloadCount: Int = 890
)

data class ThemePack(
    val id: String,
    val title: String,
    val tagline: String,
    val category: String,
    val wallpaper: WallpaperItem,
    val ringtone: RingtoneItem,
    val notificationTone: RingtoneItem,
    val primaryColor: Long,
    val secondaryColor: Long,
    val isFeatured: Boolean = false,
    val isUserCustom: Boolean = false
)

enum class WallpaperScreenTarget {
    HOME,
    LOCK,
    BOTH
}

enum class RingtoneTarget {
    PHONE_CALL,
    NOTIFICATION,
    ALARM
}
