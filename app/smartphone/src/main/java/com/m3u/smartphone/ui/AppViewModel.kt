package com.m3u.smartphone.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map as pagingMap
import androidx.work.WorkManager
import com.m3u.business.playlist.ChannelWithProgramme
import com.m3u.data.api.TvApiDelegate
import com.m3u.data.repository.channel.ChannelRepository
import com.m3u.data.repository.playlist.PlaylistRepository
import com.m3u.data.repository.tv.ConnectionToTvValue
import com.m3u.data.repository.tv.TvRepository
import com.m3u.data.tv.model.RemoteDirection
import com.m3u.data.tv.model.TvInfo
import com.m3u.data.worker.SubscriptionWorker
import com.m3u.smartphone.ui.common.connect.RemoteControlSheetValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val channelRepository: ChannelRepository,
    private val workManager: WorkManager,
    private val tvRepository: TvRepository,
    private val tvApi: TvApiDelegate,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val channels: Flow<PagingData<ChannelWithProgramme>> = snapshotFlow { searchQuery.value }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                emptyFlow()
            } else {
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                        prefetchDistance = 5
                    ),
                    pagingSourceFactory = { channelRepository.search(query) }
                )
                    .flow
                    .map { data ->
                        data.pagingMap { channel ->
                            ChannelWithProgramme(
                                channel = channel,
                                programme = null
                            )
                        }
                    }
            }
        }

    var searchQuery = mutableStateOf("")
    private fun refreshProgrammes() {
        viewModelScope.launch {
            val playlists = playlistRepository.getAllAutoRefresh()
            playlists.forEach { playlist ->
                SubscriptionWorker.epg(
                    workManager = workManager,
                    playlistUrl = playlist.url,
                    ignoreCache = true
                )
            }
        }
    }

    var code by mutableStateOf("")
    var isConnectSheetVisible by mutableStateOf(false)
    private var connectedTv by mutableStateOf<TvInfo?>(null)
    private var connectionToTvValue by mutableStateOf<ConnectionToTvValue>(ConnectionToTvValue.Idle())
    private var connectToTvJob: Job? = null
    private val timber = Timber.tag("RemoteControlVM")

    init {
        loadDefaultPlaylist()
        refreshProgrammes()
        tvRepository.connected
            .onEach {
                timber.d("connected tv changed: $it")
                connectedTv = it
            }
            .launchIn(viewModelScope)
    }

    val remoteControlSheetValue: RemoteControlSheetValue
        get() = connectedTv
            ?.let { RemoteControlSheetValue.DPad(it) }
            ?: RemoteControlSheetValue.Prepare(
                code = code,
                searchingOrConnecting = connectionToTvValue is ConnectionToTvValue.Searching ||
                        connectionToTvValue is ConnectionToTvValue.Connecting,
                subtitle = (connectionToTvValue as? ConnectionToTvValue.Idle)?.reason,
                timedOut = connectionToTvValue is ConnectionToTvValue.Timeout
            )

    fun checkTvCodeOnSmartphone() {
        val pin = code.toIntOrNull() ?: return
        timber.d("connect clicked, pin=$pin, previousState=${connectionToTvValue::class.simpleName}")
        connectToTvJob?.cancel()
        connectToTvJob = tvRepository
            .connectToTv(pin)
            .onEach { value ->
                timber.d("connect state: ${value::class.simpleName}, value=$value")
                connectionToTvValue = value
            }
            .launchIn(viewModelScope)
    }

    fun forgetTvCodeOnSmartphone() {
        timber.d("forget tv code")
        code = ""
        connectionToTvValue = ConnectionToTvValue.Idle()
        connectToTvJob?.cancel()
        connectToTvJob = null
        viewModelScope.launch { tvRepository.disconnectToTv() }
    }

    fun onRemoteDirection(direction: RemoteDirection) {
        timber.d("remote direction: $direction")
        viewModelScope.launch { tvApi.remoteDirection(direction.value) }
    }

    private fun loadDefaultPlaylist() {
        viewModelScope.launch {
            try {
                val prefs = context.getSharedPreferences("m3u_prefs", Context.MODE_PRIVATE)
                if (prefs.getBoolean("default_playlist_loaded", false)) return@launch

                val destFile = File(context.filesDir, "default_channels.m3u")
                if (!destFile.exists()) {
                    context.assets.open("default_channels.m3u").use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                playlistRepository.m3uOrThrow(
                    title = "TV Cyber-Mozess 🇨🇮",
                    url = "file://${destFile.absolutePath}"
                )

                prefs.edit().putBoolean("default_playlist_loaded", true).apply()
                Timber.tag("AppVM").d("Default playlist loaded successfully")
            } catch (e: Exception) {
                Timber.tag("AppVM").e(e, "Failed to load default playlist")
            }
        }
    }
}
