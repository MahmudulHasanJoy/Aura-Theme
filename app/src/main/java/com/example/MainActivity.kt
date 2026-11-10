package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.RingtoneActionDialog
import com.example.ui.components.ThemePreviewSheet
import com.example.ui.components.ThemeStudioDialog
import com.example.ui.components.WallpaperPreviewDialog
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.RingtonesScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.screens.WallpapersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ThemeViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val themes by viewModel.allThemes.collectAsState()

    val previewWallpaper by viewModel.previewWallpaper.collectAsState()
    val isLockScreenPreview by viewModel.isLockScreenPreviewMode.collectAsState()
    val isHomeScreenPreview by viewModel.isHomeScreenPreviewMode.collectAsState()

    val actionRingtone by viewModel.actionRingtone.collectAsState()
    val previewTheme by viewModel.previewTheme.collectAsState()
    val showThemeStudio by viewModel.showThemeStudio.collectAsState()

    val isSettingWallpaper by viewModel.isSettingWallpaper.collectAsState()
    val isSettingRingtone by viewModel.isSettingRingtone.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Returned from system settings
    }

    // Handle incoming user feedback messages / snackbars
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { notification ->
            val result = snackbarHostState.showSnackbar(
                message = notification.message,
                actionLabel = notification.actionLabel,
                duration = if (notification.isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && notification.requiresPermission) {
                val intent = viewModel.ringtonePlayer.getSettingsPermissionIntent()
                settingsLauncher.launch(intent)
            }
        }
    }

    // Handle Back Press when modal or sub-sheets are open
    BackHandler(
        enabled = previewWallpaper != null || actionRingtone != null || previewTheme != null || showThemeStudio
    ) {
        when {
            previewWallpaper != null -> viewModel.closeWallpaperPreview()
            actionRingtone != null -> viewModel.closeRingtoneAction()
            previewTheme != null -> viewModel.closeThemePreview()
            showThemeStudio -> viewModel.closeThemeStudio()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.THEMES,
                    onClick = { viewModel.selectTab(AppTab.THEMES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.THEMES) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Themes"
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_themes),
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.THEMES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_themes")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.WALLPAPERS,
                    onClick = { viewModel.selectTab(AppTab.WALLPAPERS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.WALLPAPERS) Icons.Filled.Image else Icons.Outlined.Image,
                            contentDescription = "Wallpapers"
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_wallpapers),
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.WALLPAPERS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_wallpapers")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.RINGTONES,
                    onClick = { viewModel.selectTab(AppTab.RINGTONES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.RINGTONES) Icons.Filled.MusicNote else Icons.Outlined.MusicNote,
                            contentDescription = "Ringtones"
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_ringtones),
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.RINGTONES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_ringtones")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.FAVORITES,
                    onClick = { viewModel.selectTab(AppTab.FAVORITES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.FAVORITES) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorites"
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_favorites),
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.FAVORITES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_tab_favorites")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Content
            when (currentTab) {
                AppTab.THEMES -> {
                    ThemesScreen(
                        themes = themes,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onPreviewTheme = { viewModel.openThemePreview(it) },
                        onApplyTheme = { viewModel.applyFullTheme(it) },
                        onCreateCustomTheme = { viewModel.openThemeStudio() }
                    )
                }
                AppTab.WALLPAPERS -> {
                    WallpapersScreen(
                        wallpapers = viewModel.allWallpapers,
                        favoriteIds = favoriteIds,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onWallpaperClick = { viewModel.openWallpaperPreview(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
                AppTab.RINGTONES -> {
                    RingtonesScreen(
                        ringtones = viewModel.allRingtones,
                        playbackState = playbackState,
                        favoriteIds = favoriteIds,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onPlayClick = { viewModel.playRingtone(it) },
                        onSetClick = { viewModel.openRingtoneAction(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
                AppTab.FAVORITES -> {
                    FavoritesScreen(
                        favoriteIds = favoriteIds,
                        allWallpapers = viewModel.allWallpapers,
                        allRingtones = viewModel.allRingtones,
                        playbackState = playbackState,
                        onWallpaperClick = { viewModel.openWallpaperPreview(it) },
                        onPlayClick = { viewModel.playRingtone(it) },
                        onSetRingtoneClick = { viewModel.openRingtoneAction(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
            }

            // Floating Mini Audio Player Bar (above the navigation bar)
            MiniPlayerBar(
                playbackState = playbackState,
                onPlayPause = { viewModel.playRingtone(it) },
                onClose = { viewModel.stopRingtone() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }

        // Full-screen Wallpaper Preview Dialog
        previewWallpaper?.let { wp ->
            WallpaperPreviewDialog(
                wallpaper = wp,
                isFavorite = viewModel.isFavorite(wp.id),
                isLockScreenPreview = isLockScreenPreview,
                isHomeScreenPreview = isHomeScreenPreview,
                isLoading = isSettingWallpaper,
                onToggleLockScreenPreview = { viewModel.toggleLockScreenPreview() },
                onToggleHomeScreenPreview = { viewModel.toggleHomeScreenPreview() },
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onApplyWallpaper = { targetWp, targetScreen ->
                    viewModel.applyWallpaper(targetWp, targetScreen)
                },
                onDismiss = { viewModel.closeWallpaperPreview() }
            )
        }

        // Ringtone Action Bottom Sheet
        actionRingtone?.let { rt ->
            RingtoneActionDialog(
                ringtone = rt,
                isLoading = isSettingRingtone,
                canModifySystemSettings = viewModel.ringtonePlayer.canModifySystemSettings(),
                onApplyTarget = { targetRt, target ->
                    viewModel.applyRingtone(targetRt, target)
                },
                onRequestPermission = {
                    val intent = viewModel.ringtonePlayer.getSettingsPermissionIntent()
                    settingsLauncher.launch(intent)
                },
                onDismiss = { viewModel.closeRingtoneAction() }
            )
        }

        // Unified Theme Preview Sheet
        previewTheme?.let { theme ->
            ThemePreviewSheet(
                theme = theme,
                playbackState = playbackState,
                isLoading = isSettingWallpaper || isSettingRingtone,
                onPlayRingtone = { viewModel.playRingtone(it) },
                onApplyEntireTheme = { viewModel.applyFullTheme(it) },
                onDismiss = { viewModel.closeThemePreview() }
            )
        }

        // Theme Studio Customizer Dialog
        if (showThemeStudio) {
            ThemeStudioDialog(
                wallpapers = viewModel.allWallpapers,
                ringtones = viewModel.allRingtones,
                onSaveTheme = { name, wp, rt, notif, color ->
                    viewModel.saveCustomTheme(name, wp, rt, notif, color)
                },
                onDismiss = { viewModel.closeThemeStudio() }
            )
        }
    }
}
