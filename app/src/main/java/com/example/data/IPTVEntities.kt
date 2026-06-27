package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "iptv_accounts")
data class IPTVAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "M3U", "XTREAM", "STALKER"
    val playlistUrl: String,
    val username: String? = null,
    val password: String? = null,
    val macAddress: String? = null,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_bookmarks")
data class ChannelBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val isPremium: Boolean = false,
    val selectedTheme: String = "DARK_3D"
)
