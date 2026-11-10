package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlaybackState
import com.example.data.model.RingtoneItem
import com.example.data.model.WallpaperItem
import com.example.ui.components.RingtoneRow
import com.example.ui.components.WallpaperCard

@Composable
fun FavoritesScreen(
    favoriteIds: Set<String>,
    allWallpapers: List<WallpaperItem>,
    allRingtones: List<RingtoneItem>,
    playbackState: PlaybackState,
    onWallpaperClick: (WallpaperItem) -> Unit,
    onPlayClick: (RingtoneItem) -> Unit,
    onSetRingtoneClick: (RingtoneItem) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Wallpapers", "Ringtones")

    val favWallpapers = allWallpapers.filter { favoriteIds.contains(it.id) }
    val favRingtones = allRingtones.filter { favoriteIds.contains(it.id) }
    val isEmpty = favWallpapers.isEmpty() && favRingtones.isEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Saved Favorites",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your curated collection of wallpapers & sounds",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filters
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { f ->
                        val isSelected = f == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = f },
                            label = { Text(f, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        if (isEmpty) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No favorites yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the heart on any wallpaper or ringtone to save it here",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Wallpapers Section
            if ((selectedFilter == "All" || selectedFilter == "Wallpapers") && favWallpapers.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorite Wallpapers (${favWallpapers.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favWallpapers, key = { it.id }) { wp ->
                            Box(modifier = Modifier.size(width = 160.dp, height = 280.dp)) {
                                WallpaperCard(
                                    wallpaper = wp,
                                    isFavorite = true,
                                    onWallpaperClick = onWallpaperClick,
                                    onFavoriteToggle = onFavoriteToggle
                                )
                            }
                        }
                    }
                }
            }

            // Ringtones Section
            if ((selectedFilter == "All" || selectedFilter == "Ringtones") && favRingtones.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorite Ringtones (${favRingtones.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(favRingtones, key = { it.id }) { rt ->
                    val isPlaying = playbackState.currentRingtone?.id == rt.id && playbackState.isPlaying
                    RingtoneRow(
                        ringtone = rt,
                        isPlaying = isPlaying,
                        progress = if (isPlaying) playbackState.progress else 0f,
                        isFavorite = true,
                        onPlayClick = onPlayClick,
                        onSetClick = onSetRingtoneClick,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
