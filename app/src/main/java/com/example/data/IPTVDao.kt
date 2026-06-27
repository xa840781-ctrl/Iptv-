package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IPTVDao {
    // Accounts
    @Query("SELECT * FROM iptv_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<IPTVAccount>>

    @Query("SELECT * FROM iptv_accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): IPTVAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: IPTVAccount): Long

    @Update
    suspend fun updateAccount(account: IPTVAccount)

    @Delete
    suspend fun deleteAccount(account: IPTVAccount)

    @Query("UPDATE iptv_accounts SET isActive = 0")
    suspend fun deactivateAllAccounts()

    @Query("UPDATE iptv_accounts SET isActive = (id = :id)")
    suspend fun activateAccount(id: Int)

    // Bookmarks
    @Query("SELECT * FROM channel_bookmarks WHERE accountId = :accountId ORDER BY createdAt DESC")
    fun getBookmarksForAccount(accountId: Int): Flow<List<ChannelBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: ChannelBookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: ChannelBookmark)

    @Query("DELETE FROM channel_bookmarks WHERE streamUrl = :streamUrl AND accountId = :accountId")
    suspend fun deleteBookmarkByUrl(accountId: Int, streamUrl: String)

    @Query("SELECT EXISTS(SELECT 1 FROM channel_bookmarks WHERE streamUrl = :streamUrl AND accountId = :accountId)")
    suspend fun isBookmarked(accountId: Int, streamUrl: String): Boolean

    // Settings
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)
}
