package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.IPTVAccount
import com.example.data.IPTVChannel
import com.example.ui.IPTVViewModel
import com.example.ui.components.ThreeDButton
import com.example.ui.components.ThreeDTiltCard
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DashboardScreen(
    viewModel: IPTVViewModel,
    onNavigateToAddAccount: () -> Unit,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val activeAccount by viewModel.activeAccount.collectAsState()
    val channels by viewModel.filteredChannels.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val activeChannel by viewModel.activeChannel.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val isBookmarked by viewModel.isCurrentChannelBookmarked.collectAsState()
    val isLoadingChannels by viewModel.isLoadingChannels.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showAccountsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var viewBookmarksOnly by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712)) // Deep starry dark theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 10.dp)
        ) {
            // App Header
            AppHeader(
                activeAccount = activeAccount,
                isPremium = settings.isPremium,
                onToggleAccounts = { showAccountsSheet = true },
                onToggleSettings = { showSettingsSheet = true }
            )

            // Streaming 3D Video Player
            ThreeDStreamPlayer(
                activeChannel = activeChannel,
                isPlaying = isPlaying,
                isMuted = isMuted,
                volume = volume,
                isBookmarked = isBookmarked,
                onTogglePlay = { viewModel.togglePlayPause() },
                onToggleMute = { viewModel.toggleMute() },
                onVolumeChange = { viewModel.setVolume(it) },
                onToggleBookmark = { viewModel.toggleBookmark() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Filter Options (Category Chips & Search & Bookmarks Toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glassmorphic Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Kërko kanal...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Kerko", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("channel_search_bar"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Toggle Bookmarks Only Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (viewBookmarksOnly) Color(0xFFEF4444).copy(alpha = 0.2f)
                            else Color(0xFF1F2937).copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (viewBookmarksOnly) Color(0xFFEF4444) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewBookmarksOnly = !viewBookmarksOnly }
                        .testTag("bookmarks_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (viewBookmarksOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Fas",
                        tint = if (viewBookmarksOnly) Color(0xFFEF4444) else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Selector Horizontal Row
            if (!viewBookmarksOnly) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1F2937).copy(alpha = 0.5f)
                                )
                                .clickable { viewModel.setCategory(cat) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Kanalet e Preferuara ❤️",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel List Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoadingChannels) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Duke ngarkuar kanalet...", color = Color.White.copy(alpha = 0.6f))
                    }
                } else {
                    val listToShow = if (viewBookmarksOnly) {
                        // Gather channels from active account that are bookmarked
                        val bookmarkedUrls = viewModel.bookmarks.value.map { it.streamUrl }
                        channels.filter { it.url in bookmarkedUrls }
                    } else {
                        channels
                    }

                    if (listToShow.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = "Bosh", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (viewBookmarksOnly) "Nuk keni asnjë kanal të preferuar!" else "Nuk u gjet asnjë kanal!",
                                color = Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(listToShow) { channel ->
                                val isActive = activeChannel?.url == channel.url
                                ChannelItemCard(
                                    channel = channel,
                                    isPlayingActive = isActive && isPlaying,
                                    onClick = { viewModel.selectChannel(channel) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING ACCOUNTS DRAWER overlay
        AnimatedVisibility(showAccountsSheet) {
            AccountsDrawer(
                accounts = accounts,
                activeAccount = activeAccount,
                isPremium = settings.isPremium,
                onClose = { showAccountsSheet = false },
                onAddAccount = {
                    showAccountsSheet = false
                    onNavigateToAddAccount()
                },
                onSelectAccount = {
                    viewModel.selectAccount(it)
                    showAccountsSheet = false
                },
                onDeleteAccount = {
                    viewModel.deleteAccount(it)
                },
                onShowPaywall = onShowPaywall
            )
        }

        // FLOATING SETTINGS DRAWER overlay
        AnimatedVisibility(showSettingsSheet) {
            SettingsDrawer(
                isPremium = settings.isPremium,
                onClose = { showSettingsSheet = false },
                onCancelPremium = { viewModel.cancelPremium() },
                onShowPaywall = onShowPaywall
            )
        }
    }
}

@Composable
fun AppHeader(
    activeAccount: IPTVAccount?,
    isPremium: Boolean,
    onToggleAccounts: () -> Unit,
    onToggleSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AeroStream 3D",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "PRO", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = activeAccount?.name ?: "Llogari Fallback (Lajme)",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Accounts Selector Button
        IconButton(
            onClick = onToggleAccounts,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .testTag("header_accounts_button")
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Llogarite",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Settings Button
        IconButton(
            onClick = onToggleSettings,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .testTag("header_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Cilesimet",
                tint = Color.White
            )
        }
    }
}

/**
 * Streaming Player UI container with beautiful 3D live signal visualizer.
 */
@Composable
fun ThreeDStreamPlayer(
    activeChannel: IPTVChannel?,
    isPlaying: Boolean,
    isMuted: Boolean,
    volume: Float,
    isBookmarked: Boolean,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleBookmark: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Simulated Screen with depth border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF020617)) // Slate darkest color
                .border(2.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            if (activeChannel != null) {
                if (isPlaying) {
                    // Modern 3D Wave Visualizer to simulate stream active output
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF030712), Color(0xFF0B1226))
                                )
                            )
                    ) {
                        val path = Path()
                        val halfHeight = size.height / 2f
                        val width = size.width
                        val points = 100
                        val step = width / points

                        path.moveTo(0f, halfHeight)
                        for (i in 0..points) {
                            val x = i * step
                            // Complex sine formula mimicking digital signal packets
                            val angle = (i.toFloat() / points) * 4f * PI.toFloat() + phase
                            val sineVal = sin(angle) * sin(angle * 0.5f)
                            val y = halfHeight + sineVal * (if (isMuted) 5f else 80f)
                            path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                            ),
                            style = Stroke(width = 4.dp.toPx())
                        )

                        // Secondary grid pattern
                        for (x in 0..width.toInt() step 60) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(x.toFloat(), 0f),
                                end = Offset(x.toFloat(), size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // Top-Left Stream Metadata Info overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeChannel.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "1080p • 60fps • Buffer 100%",
                            color = Color(0xFF06B6D4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Logo Icon thumbnail watermark on player
                    SubcomposeAsyncImage(
                        model = activeChannel.logoUrl,
                        contentDescription = "Kanal Logo",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        loading = { CircularProgressIndicator(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp)) },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeChannel.name.take(1),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                } else {
                    // Paused State Screen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF8B5CF6), CircleShape)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Luaj", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Luajtja u ndalua", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = activeChannel.name, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            } else {
                // No channel playing yet
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Tv, contentDescription = "Tv", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Zgjidhni një kanal më poshtë", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }
        }

        // Player Controls (Play/Pause, Volume, Mute, Bookmark)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .testTag("player_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.VolumeMute else Icons.Default.PlayArrow,
                    contentDescription = "Luaj/Ndalo",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Mute / Unmute button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .testTag("player_mute")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                    contentDescription = "Zeri",
                    tint = if (isMuted) Color.Red else Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Volume Slider
            Slider(
                value = if (isMuted) 0f else volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF8B5CF6),
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                    thumbColor = Color(0xFF06B6D4)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("player_volume_slider")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Bookmark button
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier
                    .background(
                        if (isBookmarked) Color(0xFFEF4444).copy(alpha = 0.15f)
                        else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isBookmarked) Color(0xFFEF4444) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("player_bookmark")
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Prefero",
                    tint = if (isBookmarked) Color(0xFFEF4444) else Color.White
                )
            }
        }
    }
}

/**
 * Beautiful 3D tilted card for each channel in the listing
 */
@Composable
fun ChannelItemCard(
    channel: IPTVChannel,
    isPlayingActive: Boolean,
    onClick: () -> Unit
) {
    ThreeDTiltCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        onClick = onClick,
        glowColor = if (isPlayingActive) Color(0xFF06B6D4) else Color(0xFF8B5CF6),
        accentColor = if (isPlayingActive) Color(0xFF10B981) else Color(0xFF3B82F6)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Image
            SubcomposeAsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(8.dp)),
                loading = { CircularProgressIndicator(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp)) },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = channel.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Channel Names / Meta details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.category ?: "Të tjera",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Indicator
            if (isPlayingActive) {
                // Pulsating Active Stream Indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Luaj",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Overlay Drawer: Manage multiple IPTV profiles / accounts.
 */
@Composable
fun AccountsDrawer(
    accounts: List<IPTVAccount>,
    activeAccount: IPTVAccount?,
    isPremium: Boolean,
    onClose: () -> Unit,
    onAddAccount: () -> Unit,
    onSelectAccount: (Int) -> Unit,
    onDeleteAccount: (IPTVAccount) -> Unit,
    onShowPaywall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() }
    ) {
        // Slide out container from bottom/right.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF0B0F19))
                .clickable(enabled = false) { }
                .border(2.dp, Color(0xFF8B5CF6).copy(alpha = 0.2f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Llogaritë IPTV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                // Add account trigger with premium limit check
                IconButton(
                    onClick = {
                        val limitReached = accounts.size >= 1 && !isPremium
                        if (limitReached) {
                            onShowPaywall()
                        } else {
                            onAddAccount()
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFF8B5CF6), RoundedCornerShape(50))
                        .testTag("drawer_add_account_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Shto Llogari",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium alert banner
            if (!isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFBBF24).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onShowPaywall() }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = "Kyç", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keni arritur limitin (1 llogari). Përmirëso tani në PRO!",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // List of Accounts
            if (accounts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Folder, contentDescription = "Bosh", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Nuk ka llogari të lidhura.\nLidheni të parën falas!", color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(accounts) { acc ->
                        val isActive = activeAccount?.id == acc.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isActive) Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                    else Color(0xFF1F2937).copy(alpha = 0.4f)
                                )
                                .border(
                                    1.dp,
                                    if (isActive) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectAccount(acc.id) }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "Llogari",
                                    tint = if (isActive) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = acc.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Tipi: ${acc.type} • ${acc.playlistUrl}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                
                                // Delete button
                                IconButton(
                                    onClick = { onDeleteAccount(acc) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Fshij",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Add account quick trigger button
            Spacer(modifier = Modifier.height(14.dp))
            ThreeDButton(
                text = "SHTO LLOGARI TË RE",
                onClick = {
                    val limitReached = accounts.size >= 1 && !isPremium
                    if (limitReached) {
                        onShowPaywall()
                    } else {
                        onAddAccount()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Overlay Drawer: App configuration & Settings panel.
 */
@Composable
fun SettingsDrawer(
    isPremium: Boolean,
    onClose: () -> Unit,
    onCancelPremium: () -> Unit,
    onShowPaywall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF0B0F19))
                .clickable(enabled = false) { }
                .border(2.dp, Color(0xFF8B5CF6).copy(alpha = 0.2f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Cilësimet e Aplikacionit",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isPremium) Color(0xFF065F46).copy(alpha = 0.15f)
                        else Color(0xFF1F2937).copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp,
                        if (isPremium) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPremium) "AEROSTREAM PREMIUM AKTIV" else "AEROSTREAM STANDARD",
                            color = if (isPremium) Color(0xFF34D399) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isPremium) "Keni akses të plotë pafund." else "Kufizuar në 1 llogari.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    if (!isPremium) {
                        IconButton(
                            onClick = {
                                onClose()
                                onShowPaywall()
                            },
                            modifier = Modifier.background(Color(0xFFFBBF24), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Shto", tint = Color.Black)
                        }
                    } else {
                        // Option to reset/cancel premium for testing
                        Text(
                            text = "Rivendos",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onCancelPremium() }
                                .padding(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Version info & Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "AeroStream IPTV v1.0.0 (3D Engine)", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                Text(text = "Sistemi i transmetimit të shpejtë të të dhënave", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(16.dp))
                ThreeDButton(
                    text = "MBYLL",
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
