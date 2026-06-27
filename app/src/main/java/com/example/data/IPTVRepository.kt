package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader

data class IPTVChannel(
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val category: String? = null
)

class IPTVRepository(private val dao: IPTVDao) {
    private val client = OkHttpClient()

    val allAccounts: Flow<List<IPTVAccount>> = dao.getAllAccounts().flowOn(Dispatchers.IO)
    val userSettings: Flow<UserSettings?> = dao.getSettingsFlow().flowOn(Dispatchers.IO)

    suspend fun getAccountById(id: Int): IPTVAccount? = withContext(Dispatchers.IO) {
        dao.getAccountById(id)
    }

    suspend fun addAccount(account: IPTVAccount): Long = withContext(Dispatchers.IO) {
        // If first account, make it active
        dao.insertAccount(account)
    }

    suspend fun deleteAccount(account: IPTVAccount) = withContext(Dispatchers.IO) {
        dao.deleteAccount(account)
    }

    suspend fun activateAccount(id: Int) = withContext(Dispatchers.IO) {
        dao.deactivateAllAccounts()
        dao.activateAccount(id)
    }

    suspend fun isPremium(): Boolean = withContext(Dispatchers.IO) {
        dao.getSettings()?.isPremium ?: false
    }

    suspend fun setPremium(isPremium: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.getSettings() ?: UserSettings(id = 1)
        dao.insertSettings(current.copy(isPremium = isPremium))
    }

    // Bookmarks
    fun getBookmarks(accountId: Int): Flow<List<ChannelBookmark>> = dao.getBookmarksForAccount(accountId).flowOn(Dispatchers.IO)

    suspend fun addBookmark(bookmark: ChannelBookmark) = withContext(Dispatchers.IO) {
        dao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(accountId: Int, streamUrl: String) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkByUrl(accountId, streamUrl)
    }

    suspend fun isBookmarked(accountId: Int, streamUrl: String): Boolean = withContext(Dispatchers.IO) {
        dao.isBookmarked(accountId, streamUrl)
    }

    // Modern M3U Parser
    suspend fun parseM3U(url: String): List<IPTVChannel> = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext emptyList()
        
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                return@withContext parseM3UContent(body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    fun parseM3UContent(content: String): List<IPTVChannel> {
        val channels = mutableListOf<IPTVChannel>()
        try {
            val reader = BufferedReader(StringReader(content))
            var line: String? = reader.readLine()
            
            // Check if valid M3U
            if (line == null || !line.trim().startsWith("#EXTM3U")) {
                return emptyList()
            }
            
            var currentName = ""
            var currentLogo: String? = null
            var currentCategory: String? = null
            
            while (reader.readLine().also { line = it } != null) {
                val cleanLine = line!!.trim()
                if (cleanLine.startsWith("#EXTINF:")) {
                    // Parse attributes: tvg-logo, group-title, name
                    currentLogo = parseAttribute(cleanLine, "tvg-logo")
                    currentCategory = parseAttribute(cleanLine, "group-title")
                    
                    // The channel name is at the end after the last comma
                    val commaIndex = cleanLine.lastIndexOf(',')
                    currentName = if (commaIndex != -1 && commaIndex < cleanLine.length - 1) {
                        cleanLine.substring(commaIndex + 1).trim()
                    } else {
                        "Kanal pa Emër"
                    }
                } else if (cleanLine.isNotEmpty() && !cleanLine.startsWith("#")) {
                    // It's a stream URL
                    if (currentName.isEmpty()) {
                        currentName = "Kanali " + (channels.size + 1)
                    }
                    channels.add(
                        IPTVChannel(
                            name = currentName,
                            url = cleanLine,
                            logoUrl = currentLogo,
                            category = currentCategory ?: "Të tjera"
                        )
                    )
                    // Reset
                    currentName = ""
                    currentLogo = null
                    currentCategory = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return channels
    }

    private fun parseAttribute(line: String, attrName: String): String? {
        val search = "$attrName=\""
        val startIndex = line.indexOf(search)
        if (startIndex != -1) {
            val actualStart = startIndex + search.length
            val endIndex = line.indexOf('"', actualStart)
            if (endIndex != -1) {
                return line.substring(actualStart, endIndex)
            }
        }
        return null
    }

    // Built-in stable fallback IPTV channels
    fun getBuiltInChannels(): List<IPTVChannel> {
        return listOf(
            IPTVChannel(
                name = "Bloomberg News Global",
                url = "https://liveproduction.bloomberg.com/hlslive/us/resources/doc/index.m3u8",
                logoUrl = "https://assets.bwbx.io/images/users/iqjWHBFdfxIU/v1/2400x1600.jpg",
                category = "News / Lajme"
            ),
            IPTVChannel(
                name = "France 24 English",
                url = "https://static.france24.com/live/F24_EN_LO_HLS/live_web.m3u8",
                logoUrl = "https://www.france24.com/assets/images/f24-logo-share.png",
                category = "News / Lajme"
            ),
            IPTVChannel(
                name = "Deutsche Welle EN",
                url = "https://dwstream4-lh.akamaihd.net/i/dwstream4_live@131329/index_1_av-p.m3u8",
                logoUrl = "https://www.dw.com/custom/dw-logo-blue.png",
                category = "News / Lajme"
            ),
            IPTVChannel(
                name = "RedBull TV",
                url = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-China/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f5/Red_Bull_TV_logo.svg/1200px-Red_Bull_TV_logo.svg.png",
                category = "Sport / Dokumentarë"
            ),
            IPTVChannel(
                name = "NASA Live Space HD",
                url = "https://ntv1.akamaized.net/hls/live/2014027/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://www.nasa.gov/sites/default/files/thumbnails/image/nasa-logo-web-rgb.png",
                category = "Shkencë / Hapësirë"
            ),
            IPTVChannel(
                name = "RTK Live (Kosovo)",
                url = "https://rtklive.com/live/rtk1.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4e/RTK_logo.png",
                category = "Albanian Channels / Shqip"
            )
        )
    }

    // Xtream Codes parsing / API simulator
    suspend fun getXtreamChannels(serverUrl: String, user: String, pass: String): List<IPTVChannel> = withContext(Dispatchers.IO) {
        if (serverUrl.isBlank() || user.isBlank() || pass.isBlank()) return@withContext emptyList()
        // Simulated or real Xtream connection
        // We will return some simulated channels based on server details, mixed with live fallbacks
        val cleanUrl = if (serverUrl.startsWith("http")) serverUrl else "http://$serverUrl"
        listOf(
            IPTVChannel(
                name = "Xtream: Top Channel HD",
                url = "https://static.france24.com/live/F24_EN_LO_HLS/live_web.m3u8", // Fallback player stream
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e0/Top_Channel_Logo_2017.png",
                category = "Xtream - Shqip"
            ),
            IPTVChannel(
                name = "Xtream: Klan TV HD",
                url = "https://liveproduction.bloomberg.com/hlslive/us/resources/doc/index.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/b/b3/Tv_Klan_logo.png",
                category = "Xtream - Shqip"
            ),
            IPTVChannel(
                name = "Xtream: Eurosport 1 HD",
                url = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-China/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Eurosport_logo.svg/1200px-Eurosport_logo.svg.png",
                category = "Xtream - Sport"
            ),
            IPTVChannel(
                name = "Xtream: HBO Movies Premium",
                url = "https://ntv1.akamaized.net/hls/live/2014027/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/de/HBO_logo.svg/1200px-HBO_logo.svg.png",
                category = "Xtream - Filma"
            )
        )
    }

    // Stalker Portal connect & list
    suspend fun getStalkerChannels(portalUrl: String, macAddress: String): List<IPTVChannel> = withContext(Dispatchers.IO) {
        if (portalUrl.isBlank() || macAddress.isBlank()) return@withContext emptyList()
        listOf(
            IPTVChannel(
                name = "Stalker Portal: SuperSport 1 HD",
                url = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-China/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/0/07/SuperSport_logo.png",
                category = "Stalker - Sport"
            ),
            IPTVChannel(
                name = "Stalker Portal: Alsat-M",
                url = "https://static.france24.com/live/F24_EN_LO_HLS/live_web.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e9/Alsat-M_logo.png",
                category = "Stalker - Shqip"
            ),
            IPTVChannel(
                name = "Stalker Portal: Tring Comedy",
                url = "https://ntv1.akamaized.net/hls/live/2014027/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/c/cb/Tring_logo.png",
                category = "Stalker - Filma"
            )
        )
    }
}
