package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.audio.AudioSynthEngine
import com.example.data.model.RingtoneItem
import com.example.data.model.ThemePack
import com.example.data.model.WallpaperItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aura_theme_prefs", Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow<Set<String>>(loadFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _customThemes = MutableStateFlow<List<ThemePack>>(emptyList())
    val customThemes: StateFlow<List<ThemePack>> = _customThemes.asStateFlow()

    // 1. Wallpapers Catalog
    val wallpapers: List<WallpaperItem> = listOf(
        WallpaperItem(
            id = "wp_cyberpunk",
            title = "Cyberpunk Neo-Tokyo",
            category = "Cyberpunk",
            drawableRes = R.drawable.img_cyberpunk_neon,
            accentColor = 0xFFEC4899, // Pink neon
            tags = listOf("Neon", "Sci-Fi", "Night", "City", "Ultra HD"),
            resolution = "4K UHD",
            downloadCount = 8420,
            likesCount = 3120,
            isAmoled = true
        ),
        WallpaperItem(
            id = "wp_zen",
            title = "Zen Misty Mountain",
            category = "Nature",
            drawableRes = R.drawable.img_zen_misty_mountain,
            accentColor = 0xFF10B981, // Emerald green
            tags = listOf("Misty", "Sunrise", "Calm", "Peaceful", "Water"),
            resolution = "4K UHD",
            downloadCount = 6190,
            likesCount = 2780
        ),
        WallpaperItem(
            id = "wp_cosmic",
            title = "Deep Cosmos Nebula",
            category = "Space",
            drawableRes = R.drawable.img_cosmic_nebula,
            accentColor = 0xFF8B5CF6, // Deep violet
            tags = listOf("Galaxies", "Stars", "Astronomy", "Deep Space"),
            resolution = "8K Master",
            downloadCount = 9810,
            likesCount = 4450,
            isAmoled = true
        ),
        WallpaperItem(
            id = "wp_dunes",
            title = "Golden Sahara Dunes",
            category = "Minimalist",
            drawableRes = R.drawable.img_golden_sand_dunes,
            accentColor = 0xFFF59E0B, // Amber gold
            tags = listOf("Desert", "Minimal", "Sunset", "Warm", "Clean"),
            resolution = "4K UHD",
            downloadCount = 5230,
            likesCount = 1890
        )
    )

    // 2. Ringtones Catalog
    val ringtones: List<RingtoneItem> = listOf(
        RingtoneItem(
            id = "rt_cyber_pulse",
            title = "Cyber Pulse 2077",
            category = "Synth & Neon",
            toneType = AudioSynthEngine.ToneType.CYBER_PULSE,
            durationSeconds = 7,
            accentColor = 0xFFEC4899,
            tags = listOf("Synthwave", "Arp", "Cyberpunk", "Bass"),
            downloadCount = 4320
        ),
        RingtoneItem(
            id = "rt_zen_bell",
            title = "Zen Sanctuary Bowl",
            category = "Calm & Ambient",
            toneType = AudioSynthEngine.ToneType.ZEN_BELL,
            durationSeconds = 6,
            accentColor = 0xFF10B981,
            tags = listOf("Meditation", "Singing Bowl", "Peaceful"),
            downloadCount = 3890
        ),
        RingtoneItem(
            id = "rt_cosmic_orbit",
            title = "Cosmic Orbit Chime",
            category = "Space & Ethereal",
            toneType = AudioSynthEngine.ToneType.COSMIC_ORBIT,
            durationSeconds = 7,
            accentColor = 0xFF8B5CF6,
            tags = listOf("Celestial", "Pad", "Dreamy", "Stars"),
            downloadCount = 5120
        ),
        RingtoneItem(
            id = "rt_marimba_sunrise",
            title = "Marimba Sunrise",
            category = "Acoustic Pop",
            toneType = AudioSynthEngine.ToneType.MARIMBA_SUNRISE,
            durationSeconds = 6,
            accentColor = 0xFFF59E0B,
            tags = listOf("Marimba", "Upbeat", "Wooden", "Bright"),
            downloadCount = 6200
        ),
        RingtoneItem(
            id = "rt_retro_8bit",
            title = "Retro 8-Bit Quest",
            category = "Retro & Arcade",
            toneType = AudioSynthEngine.ToneType.RETRO_8BIT,
            durationSeconds = 5,
            accentColor = 0xFF3B82F6,
            tags = listOf("Chiptune", "Arcade", "NES", "Game"),
            downloadCount = 2950
        ),
        RingtoneItem(
            id = "rt_neon_ring",
            title = "Neon Executive Ring",
            category = "Modern Phone",
            toneType = AudioSynthEngine.ToneType.NEON_RING,
            durationSeconds = 6,
            accentColor = 0xFF06B6D4,
            tags = listOf("Phone Ring", "Modern", "Business", "Crisp"),
            downloadCount = 7410
        ),
        RingtoneItem(
            id = "rt_crystal_alert",
            title = "Crystal Drop Alert",
            category = "Notifications",
            toneType = AudioSynthEngine.ToneType.CRYSTAL_ALERT,
            durationSeconds = 2,
            accentColor = 0xFF06B6D4,
            isNotificationSound = true,
            tags = listOf("Chime", "Snappy", "Glass", "Alert"),
            downloadCount = 8920
        ),
        RingtoneItem(
            id = "rt_lofi_chime",
            title = "Midnight Lo-Fi Chime",
            category = "Lo-Fi & Chill",
            toneType = AudioSynthEngine.ToneType.LOFI_CHIME,
            durationSeconds = 4,
            accentColor = 0xFFA855F7,
            tags = listOf("Rhodes", "Chill", "Warm Bell", "Night"),
            downloadCount = 3490
        ),
        RingtoneItem(
            id = "rt_hyperdrive_alarm",
            title = "Hyperdrive Wake Alarm",
            category = "Alarms",
            toneType = AudioSynthEngine.ToneType.HYPERDRIVE_ALARM,
            durationSeconds = 4,
            accentColor = 0xFFEF4444,
            tags = listOf("Alarm", "Futuristic", "Rising Sweep"),
            downloadCount = 3100
        ),
        RingtoneItem(
            id = "rt_gentle_acoustic",
            title = "Acoustic Harp Breeze",
            category = "Calm & Ambient",
            toneType = AudioSynthEngine.ToneType.GENTLE_ACOUSTIC,
            durationSeconds = 5,
            accentColor = 0xFF14B8A6,
            tags = listOf("Harp", "Pluck", "Relaxing", "Soft"),
            downloadCount = 2810
        )
    )

    // 3. Pre-bundled Curated Unified Themes
    val themes: List<ThemePack> = listOf(
        ThemePack(
            id = "theme_cyber_neon",
            title = "Neo Cyberpunk",
            tagline = "Neon towers, rain-slicked streets, and synthwave pulse",
            category = "Cyberpunk",
            wallpaper = wallpapers[0], // Cyberpunk
            ringtone = ringtones[0],   // Cyber Pulse
            notificationTone = ringtones[6], // Crystal alert
            primaryColor = 0xFFEC4899,
            secondaryColor = 0xFF06B6D4,
            isFeatured = true
        ),
        ThemePack(
            id = "theme_zen_harmony",
            title = "Zen Sanctuary",
            tagline = "Misty dawns, still waters, and harmonic singing bowls",
            category = "Nature",
            wallpaper = wallpapers[1], // Zen mountain
            ringtone = ringtones[1],   // Zen bell
            notificationTone = ringtones[7], // Lo-Fi chime
            primaryColor = 0xFF10B981,
            secondaryColor = 0xFF34D399,
            isFeatured = true
        ),
        ThemePack(
            id = "theme_deep_cosmos",
            title = "Cosmic Odyssey",
            tagline = "Interstellar dust, star clusters, and ethereal celestial pads",
            category = "Space",
            wallpaper = wallpapers[2], // Cosmic nebula
            ringtone = ringtones[2],   // Cosmic orbit
            notificationTone = ringtones[6], // Crystal alert
            primaryColor = 0xFF8B5CF6,
            secondaryColor = 0xFFA855F7,
            isFeatured = true
        ),
        ThemePack(
            id = "theme_sahara_minimal",
            title = "Sahara Golden Hour",
            tagline = "Clean wind-swept sand ridges and acoustic wooden melodies",
            category = "Minimalist",
            wallpaper = wallpapers[3], // Dunes
            ringtone = ringtones[3],   // Marimba
            notificationTone = ringtones[9], // Gentle acoustic
            primaryColor = 0xFFF59E0B,
            secondaryColor = 0xFFFBBF24,
            isFeatured = false
        )
    )

    private fun loadFavorites(): Set<String> {
        return prefs.getStringSet("fav_ids", emptySet()) ?: emptySet()
    }

    fun toggleFavorite(itemId: String): Boolean {
        val current = _favoriteIds.value.toMutableSet()
        val isNowFav = if (current.contains(itemId)) {
            current.remove(itemId)
            false
        } else {
            current.add(itemId)
            true
        }
        prefs.edit().putStringSet("fav_ids", current).apply()
        _favoriteIds.value = current
        return isNowFav
    }

    fun isFavorite(itemId: String): Boolean {
        return _favoriteIds.value.contains(itemId)
    }

    fun addCustomTheme(theme: ThemePack) {
        val list = _customThemes.value.toMutableList()
        list.add(0, theme)
        _customThemes.value = list
    }
}
