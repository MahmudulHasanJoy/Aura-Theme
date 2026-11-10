package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PlaybackState
import com.example.audio.RingtonePlayerManager
import com.example.data.model.RingtoneItem
import com.example.data.model.RingtoneTarget
import com.example.data.model.ThemePack
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperScreenTarget
import com.example.data.repository.ThemeRepository
import com.example.wallpaper.WallpaperHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    THEMES,
    WALLPAPERS,
    RINGTONES,
    FAVORITES
}

data class UiNotification(
    val message: String,
    val isError: Boolean = false,
    val actionLabel: String? = null,
    val requiresPermission: Boolean = false
)

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ThemeRepository(application)
    val ringtonePlayer = RingtonePlayerManager(application)

    val playbackState: StateFlow<PlaybackState> = ringtonePlayer.playbackState

    private val _currentTab = MutableStateFlow(AppTab.THEMES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Wallpaper Preview State
    private val _previewWallpaper = MutableStateFlow<WallpaperItem?>(null)
    val previewWallpaper: StateFlow<WallpaperItem?> = _previewWallpaper.asStateFlow()

    private val _isLockScreenPreviewMode = MutableStateFlow(true)
    val isLockScreenPreviewMode: StateFlow<Boolean> = _isLockScreenPreviewMode.asStateFlow()

    private val _isHomeScreenPreviewMode = MutableStateFlow(false)
    val isHomeScreenPreviewMode: StateFlow<Boolean> = _isHomeScreenPreviewMode.asStateFlow()

    // Ringtone Action State
    private val _actionRingtone = MutableStateFlow<RingtoneItem?>(null)
    val actionRingtone: StateFlow<RingtoneItem?> = _actionRingtone.asStateFlow()

    // Unified Theme Preview State
    private val _previewTheme = MutableStateFlow<ThemePack?>(null)
    val previewTheme: StateFlow<ThemePack?> = _previewTheme.asStateFlow()

    // Custom Theme Builder Sheet
    private val _showThemeStudio = MutableStateFlow(false)
    val showThemeStudio: StateFlow<Boolean> = _showThemeStudio.asStateFlow()

    // Loading / Progress state
    private val _isSettingWallpaper = MutableStateFlow(false)
    val isSettingWallpaper: StateFlow<Boolean> = _isSettingWallpaper.asStateFlow()

    private val _isSettingRingtone = MutableStateFlow(false)
    val isSettingRingtone: StateFlow<Boolean> = _isSettingRingtone.asStateFlow()

    // Notification toast events
    private val _userMessage = MutableSharedFlow<UiNotification>()
    val userMessage: SharedFlow<UiNotification> = _userMessage.asSharedFlow()

    val favoriteIds: StateFlow<Set<String>> = repository.favoriteIds

    // Filtered lists
    val allWallpapers: List<WallpaperItem> get() = repository.wallpapers
    val allRingtones: List<RingtoneItem> get() = repository.ringtones
    val allThemes: StateFlow<List<ThemePack>> = combine(
        MutableStateFlow(repository.themes),
        repository.customThemes
    ) { defaultThemes, customThemes ->
        customThemes + defaultThemes
    }.stateIn(viewModelScope, SharingStarted.Lazily, repository.themes)

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        _selectedCategory.value = "All"
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openWallpaperPreview(wallpaper: WallpaperItem) {
        _previewWallpaper.value = wallpaper
    }

    fun closeWallpaperPreview() {
        _previewWallpaper.value = null
    }

    fun toggleLockScreenPreview() {
        _isLockScreenPreviewMode.value = !_isLockScreenPreviewMode.value
        if (_isLockScreenPreviewMode.value) {
            _isHomeScreenPreviewMode.value = false
        }
    }

    fun toggleHomeScreenPreview() {
        _isHomeScreenPreviewMode.value = !_isHomeScreenPreviewMode.value
        if (_isHomeScreenPreviewMode.value) {
            _isLockScreenPreviewMode.value = false
        }
    }

    fun openRingtoneAction(ringtone: RingtoneItem) {
        _actionRingtone.value = ringtone
    }

    fun closeRingtoneAction() {
        _actionRingtone.value = null
    }

    fun openThemePreview(theme: ThemePack) {
        _previewTheme.value = theme
        // Play theme tone automatically as preview
        ringtonePlayer.play(theme.ringtone)
    }

    fun closeThemePreview() {
        _previewTheme.value = null
        ringtonePlayer.stop()
    }

    fun openThemeStudio() {
        _showThemeStudio.value = true
    }

    fun closeThemeStudio() {
        _showThemeStudio.value = false
    }

    fun toggleFavorite(itemId: String) {
        val isFav = repository.toggleFavorite(itemId)
        viewModelScope.launch {
            _userMessage.emit(
                UiNotification(
                    message = if (isFav) "Added to Favorites" else "Removed from Favorites"
                )
            )
        }
    }

    fun isFavorite(itemId: String): Boolean = repository.isFavorite(itemId)

    fun playRingtone(ringtone: RingtoneItem) {
        ringtonePlayer.play(ringtone)
    }

    fun stopRingtone() {
        ringtonePlayer.stop()
    }

    fun applyWallpaper(wallpaper: WallpaperItem, target: WallpaperScreenTarget) {
        viewModelScope.launch {
            _isSettingWallpaper.value = true
            val result = WallpaperHelper.applyWallpaper(getApplication(), wallpaper, target)
            _isSettingWallpaper.value = false
            result.fold(
                onSuccess = { msg ->
                    _userMessage.emit(UiNotification(message = msg))
                    _previewWallpaper.value = null
                },
                onFailure = { err ->
                    _userMessage.emit(
                        UiNotification(
                            message = err.localizedMessage ?: "Failed to apply wallpaper",
                            isError = true
                        )
                    )
                }
            )
        }
    }

    fun applyRingtone(ringtone: RingtoneItem, target: RingtoneTarget) {
        viewModelScope.launch {
            _isSettingRingtone.value = true
            val result = ringtonePlayer.applyRingtone(ringtone, target)
            _isSettingRingtone.value = false
            result.fold(
                onSuccess = { msg ->
                    _userMessage.emit(UiNotification(message = msg))
                    _actionRingtone.value = null
                },
                onFailure = { err ->
                    val isPerm = err is SecurityException
                    _userMessage.emit(
                        UiNotification(
                            message = err.localizedMessage ?: "Failed to set ringtone",
                            isError = true,
                            actionLabel = if (isPerm) "Settings" else null,
                            requiresPermission = isPerm
                        )
                    )
                }
            )
        }
    }

    fun applyFullTheme(theme: ThemePack, target: WallpaperScreenTarget = WallpaperScreenTarget.BOTH) {
        viewModelScope.launch {
            _isSettingWallpaper.value = true
            val wpResult = WallpaperHelper.applyWallpaper(getApplication(), theme.wallpaper, target)
            _isSettingWallpaper.value = false

            var msg = "Theme applied! Wallpaper set to Home & Lock screens."
            if (ringtonePlayer.canModifySystemSettings()) {
                ringtonePlayer.applyRingtone(theme.ringtone, RingtoneTarget.PHONE_CALL)
                ringtonePlayer.applyRingtone(theme.notificationTone, RingtoneTarget.NOTIFICATION)
                msg += " Ringtone & Notification sound updated."
            } else {
                msg += " (Grant Write Settings permission to also set ringtone automatically)"
            }

            _userMessage.emit(UiNotification(message = msg))
            closeThemePreview()
        }
    }

    fun saveCustomTheme(
        name: String,
        wallpaper: WallpaperItem,
        ringtone: RingtoneItem,
        notificationTone: RingtoneItem,
        primaryColor: Long
    ) {
        val newTheme = ThemePack(
            id = "custom_${System.currentTimeMillis()}",
            title = name.ifBlank { "My Custom Theme" },
            tagline = "Personal theme crafted with ${wallpaper.title} & ${ringtone.title}",
            category = "Custom",
            wallpaper = wallpaper,
            ringtone = ringtone,
            notificationTone = notificationTone,
            primaryColor = primaryColor,
            secondaryColor = 0xFF06B6D4,
            isUserCustom = true
        )
        repository.addCustomTheme(newTheme)
        closeThemeStudio()
        viewModelScope.launch {
            _userMessage.emit(UiNotification(message = "Custom theme '$name' created!"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        ringtonePlayer.stop()
    }
}
