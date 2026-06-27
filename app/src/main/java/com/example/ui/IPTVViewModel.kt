package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChannelBookmark
import com.example.data.IPTVAccount
import com.example.data.IPTVChannel
import com.example.data.IPTVRepository
import com.example.data.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IPTVViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IPTVRepository

    val accounts: StateFlow<List<IPTVAccount>>
    val settings: StateFlow<UserSettings>

    private val _activeAccount = MutableStateFlow<IPTVAccount?>(null)
    val activeAccount = _activeAccount.asStateFlow()

    private val _channels = MutableStateFlow<List<IPTVChannel>>(emptyList())
    val allChannels = _channels.asStateFlow()

    private val _isLoadingChannels = MutableStateFlow(false)
    val isLoadingChannels = _isLoadingChannels.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("Të Gjitha")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Filtered Channels computed dynamically
    val filteredChannels: StateFlow<List<IPTVChannel>> = combine(
        _channels,
        _searchQuery,
        _selectedCategory
    ) { channels, query, category ->
        var list = channels
        if (!category.isNullOrBlank() && category != "Të Gjitha") {
            list = list.filter { it.category?.lowercase() == category.lowercase() }
        }
        if (query.isNotBlank()) {
            list = list.filter { it.name.lowercase().contains(query.lowercase()) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All distinct categories in the active list
    val categories: StateFlow<List<String>> = _channels
        .combine(MutableStateFlow(Unit)) { channels, _ ->
            val cats = channels.mapNotNull { it.category }.distinct().sorted()
            listOf("Të Gjitha") + cats
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Të Gjitha"))

    private val _activeChannel = MutableStateFlow<IPTVChannel?>(null)
    val activeChannel = _activeChannel.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume = _volume.asStateFlow()

    private val _showSubscriptionPaywall = MutableStateFlow(false)
    val showSubscriptionPaywall = _showSubscriptionPaywall.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<ChannelBookmark>>(emptyList())
    val bookmarks = _bookmarks.asStateFlow()

    private val _isCurrentChannelBookmarked = MutableStateFlow(false)
    val isCurrentChannelBookmarked = _isCurrentChannelBookmarked.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = IPTVRepository(database.iptvDao())
        
        accounts = repository.allAccounts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        settings = repository.userSettings
            .combine(MutableStateFlow(Unit)) { set, _ ->
                set ?: UserSettings(isPremium = false)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings(isPremium = false))

        // Monitor accounts changes to select the active one
        viewModelScope.launch {
            accounts.collect { list ->
                val active = list.find { it.isActive }
                _activeAccount.value = active
                if (active != null) {
                    loadChannelsForAccount(active)
                    loadBookmarks(active.id)
                } else {
                    // Load fallbacks if there are no accounts
                    _channels.value = repository.getBuiltInChannels()
                    if (_activeChannel.value == null && _channels.value.isNotEmpty()) {
                        _activeChannel.value = _channels.value.first()
                    }
                }
            }
        }

        // Monitor active channel to check bookmark status
        viewModelScope.launch {
            combine(_activeChannel, _activeAccount) { channel, account ->
                if (channel != null && account != null) {
                    _isCurrentChannelBookmarked.value = repository.isBookmarked(account.id, channel.url)
                } else {
                    _isCurrentChannelBookmarked.value = false
                }
            }.collect {}
        }
    }

    fun selectChannel(channel: IPTVChannel) {
        _activeChannel.value = channel
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun setVolume(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    private fun loadBookmarks(accountId: Int) {
        viewModelScope.launch {
            repository.getBookmarks(accountId).collect {
                _bookmarks.value = it
            }
        }
    }

    fun toggleBookmark() {
        val channel = _activeChannel.value ?: return
        val account = _activeAccount.value ?: return
        viewModelScope.launch {
            if (_isCurrentChannelBookmarked.value) {
                repository.removeBookmark(account.id, channel.url)
                _isCurrentChannelBookmarked.value = false
            } else {
                repository.addBookmark(
                    ChannelBookmark(
                        accountId = account.id,
                        name = channel.name,
                        streamUrl = channel.url,
                        logoUrl = channel.logoUrl,
                        category = channel.category
                    )
                )
                _isCurrentChannelBookmarked.value = true
            }
            loadBookmarks(account.id)
        }
    }

    fun selectAccount(accountId: Int) {
        viewModelScope.launch {
            repository.activateAccount(accountId)
        }
    }

    fun deleteAccount(account: IPTVAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun dismissPaywall() {
        _showSubscriptionPaywall.value = false
    }

    fun triggerPaywall() {
        _showSubscriptionPaywall.value = true
    }

    fun purchasePremium() {
        viewModelScope.launch {
            repository.setPremium(true)
            _showSubscriptionPaywall.value = false
        }
    }

    fun cancelPremium() {
        viewModelScope.launch {
            repository.setPremium(false)
        }
    }

    fun createAccount(
        name: String,
        type: String,
        url: String,
        user: String? = null,
        pass: String? = null,
        mac: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val currentAccountsCount = accounts.value.size
            val isPrem = settings.value.isPremium
            
            // Limit 1 account on free tier
            if (currentAccountsCount >= 1 && !isPrem) {
                _showSubscriptionPaywall.value = true
                return@launch
            }

            val newAccount = IPTVAccount(
                name = name,
                type = type,
                playlistUrl = url,
                username = user,
                password = pass,
                macAddress = mac,
                isActive = true // Make newly added account active immediately
            )
            
            // Deactivate others first
            repository.addAccount(newAccount)
            // Get database auto-id or select it
            onSuccess()
        }
    }

    private fun loadChannelsForAccount(account: IPTVAccount) {
        viewModelScope.launch {
            _isLoadingChannels.value = true
            try {
                val list = when (account.type.uppercase()) {
                    "M3U" -> repository.parseM3U(account.playlistUrl)
                    "XTREAM" -> repository.getXtreamChannels(
                        serverUrl = account.playlistUrl,
                        user = account.username ?: "",
                        pass = account.password ?: ""
                    )
                    "STALKER" -> repository.getStalkerChannels(
                        portalUrl = account.playlistUrl,
                        macAddress = account.macAddress ?: ""
                    )
                    else -> emptyList()
                }
                
                if (list.isEmpty()) {
                    // Fallback to built-in if parsed content is empty or server unreachable
                    _channels.value = repository.getBuiltInChannels()
                } else {
                    _channels.value = list
                }

                // Auto-select first channel
                if (_channels.value.isNotEmpty()) {
                    _activeChannel.value = _channels.value.first()
                } else {
                    _activeChannel.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _channels.value = repository.getBuiltInChannels()
            } finally {
                _isLoadingChannels.value = false
            }
        }
    }
}
