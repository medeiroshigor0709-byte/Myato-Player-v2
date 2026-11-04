package com.example.player

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

@OptIn(UnstableApi::class)
class PlayerController private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var exoPlayer: ExoPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    // Playback state
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    // Musicolet Multiple Queues (up to 20 queues)
    private val _activeQueueId = MutableStateFlow(1L)
    val activeQueueId: StateFlow<Long> = _activeQueueId.asStateFlow()

    private val _activeQueueName = MutableStateFlow("Fila Principal 1")
    val activeQueueName: StateFlow<String> = _activeQueueName.asStateFlow()

    private val _queuesList = MutableStateFlow<List<PlaybackQueue>>(
        listOf(PlaybackQueue(id = 1L, name = "Fila Principal 1"))
    )
    val queuesList: StateFlow<List<PlaybackQueue>> = _queuesList.asStateFlow()

    private var previousQueueId: Long? = null

    // Speed & Pitch
    private val _speedPitch = MutableStateFlow(PlaybackSpeedPitch(speed = 1.0f, pitch = 1.0f))
    val speedPitch: StateFlow<PlaybackSpeedPitch> = _speedPitch.asStateFlow()

    // A-B Repeat Mode
    private val _abRepeatState = MutableStateFlow(ABRepeatState())
    val abRepeatState: StateFlow<ABRepeatState> = _abRepeatState.asStateFlow()

    // Device-Specific Equalizer Profiles (Section 7)
    private val _currentDevice = MutableStateFlow(AudioOutputDevice.SPEAKER)
    val currentDevice: StateFlow<AudioOutputDevice> = _currentDevice.asStateFlow()

    private val deviceProfiles = mutableMapOf<AudioOutputDevice, EqualizerConfig>()

    private val _equalizerConfig = MutableStateFlow(
        EqualizerConfig(isEnabled = true, presetName = "Rock", bassBoost = 400, virtualizer = 300)
    )
    val equalizerConfig: StateFlow<EqualizerConfig> = _equalizerConfig.asStateFlow()

    // Sleep timer state
    private val _sleepTimer = MutableStateFlow(SleepTimerConfig())
    val sleepTimer: StateFlow<SleepTimerConfig> = _sleepTimer.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var tracksRemainingToStop: Int = 0

    // Bluetooth automation settings
    private val _playOnBluetoothConnect = MutableStateFlow(false)
    val playOnBluetoothConnect: StateFlow<Boolean> = _playOnBluetoothConnect.asStateFlow()

    private val _pauseOnBluetoothDisconnect = MutableStateFlow(true)
    val pauseOnBluetoothDisconnect: StateFlow<Boolean> = _pauseOnBluetoothDisconnect.asStateFlow()

    // Headset Controls Config
    private val _headsetControls = MutableStateFlow(HeadsetControlsConfig())
    val headsetControls: StateFlow<HeadsetControlsConfig> = _headsetControls.asStateFlow()

    // Callback when song finishes/plays for repository record
    var onSongPlayed: ((Long) -> Unit)? = null
    var onQueueChangedCallback: ((PlaybackQueue) -> Unit)? = null

    private val progressRunnable = object : Runnable {
        override fun run() {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    val pos = player.currentPosition
                    _currentPosition.value = pos
                    val dur = player.duration
                    if (dur > 0) _duration.value = dur

                    // Handle A-B Repeat
                    val ab = _abRepeatState.value
                    if (ab.isEnabled && ab.pointA != null && ab.pointB != null) {
                        if (pos >= ab.pointB) {
                            player.seekTo(ab.pointA)
                        }
                    }
                }
            }
            handler.postDelayed(this, 250)
        }
    }

    init {
        initPlayer()
        initDeviceAudioListener()
    }

    fun getExoPlayer(): ExoPlayer {
        if (exoPlayer == null) initPlayer()
        return exoPlayer!!
    }

    private fun initPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            val dur = duration
                            if (dur > 0) _duration.value = dur
                            setupAudioEffects(audioSessionId)
                        } else if (playbackState == Player.STATE_ENDED) {
                            handleSongEnded()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        updateCurrentFromPlayer()
                    }
                })
            }

        handler.post(progressRunnable)
    }

    private fun initDeviceAudioListener() {
        detectCurrentAudioDevice()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    detectCurrentAudioDevice()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    detectCurrentAudioDevice()
                }
            }, handler)
        }
    }

    private fun detectCurrentAudioDevice() {
        val dev = if (audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn) {
            AudioOutputDevice.BLUETOOTH
        } else if (audioManager.isWiredHeadsetOn) {
            AudioOutputDevice.WIRED_HEADSET
        } else {
            AudioOutputDevice.SPEAKER
        }
        _currentDevice.value = dev
        applyDeviceProfile(dev)
    }

    private fun applyDeviceProfile(device: AudioOutputDevice) {
        val config = deviceProfiles[device] ?: EqualizerConfig(
            isEnabled = true,
            presetName = if (device == AudioOutputDevice.BLUETOOTH) "Rock" else "Normal",
            bassBoost = if (device == AudioOutputDevice.BLUETOOTH) 450 else 200,
            virtualizer = if (device == AudioOutputDevice.WIRED_HEADSET) 350 else 150
        )
        _equalizerConfig.value = config
        applyEqualizerToEffects(config)
    }

    private fun setupAudioEffects(sessionId: Int) {
        if (sessionId == C.AUDIO_SESSION_ID_UNSET || sessionId == 0) return
        try {
            if (equalizer == null) {
                equalizer = Equalizer(0, sessionId).apply {
                    enabled = _equalizerConfig.value.isEnabled
                }
            }
            if (bassBoost == null) {
                bassBoost = BassBoost(0, sessionId).apply {
                    enabled = _equalizerConfig.value.isEnabled
                    setStrength(_equalizerConfig.value.bassBoost.toShort())
                }
            }
            if (virtualizer == null) {
                virtualizer = Virtualizer(0, sessionId).apply {
                    enabled = _equalizerConfig.value.isEnabled
                    setStrength(_equalizerConfig.value.virtualizer.toShort())
                }
            }
            applyEqualizerToEffects(_equalizerConfig.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyEqualizerToEffects(config: EqualizerConfig) {
        runCatching {
            equalizer?.enabled = config.isEnabled
            bassBoost?.enabled = config.isEnabled
            virtualizer?.enabled = config.isEnabled
            bassBoost?.setStrength(config.bassBoost.toShort())
            virtualizer?.setStrength(config.virtualizer.toShort())
            config.bandLevels.forEach { (band, lvl) ->
                equalizer?.setBandLevel(band.toShort(), lvl.toShort())
            }
            // Channel balance: apply volume balance
            val bal = config.channelBalance.coerceIn(-1.0f, 1.0f)
            val leftVol = if (bal > 0f) (1f - bal) else 1f
            val rightVol = if (bal < 0f) (1f + bal) else 1f
            exoPlayer?.volume = ((leftVol + rightVol) / 2f).coerceIn(0f, 1f)
        }
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, songs.size - 1)
        _queue.value = songs
        _queueIndex.value = validIndex
        val targetSong = songs[validIndex]
        _currentSong.value = targetSong

        val player = exoPlayer ?: return
        player.stop()
        player.clearMediaItems()

        songs.forEach { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(song.dataUri))
                .setMediaId(song.id.toString())
                .setMediaMetadata(metadata)
                .build()

            player.addMediaItem(mediaItem)
        }

        player.seekTo(validIndex, 0L)
        player.prepare()
        player.play()
        _isPlaying.value = true
        _currentPosition.value = 0L
        _duration.value = targetSong.durationMs

        onSongPlayed?.invoke(targetSong.id)
        notifyQueueChanged()
    }

    fun playSingleSong(song: Song) {
        val currentQueue = _queue.value
        val existingIndex = currentQueue.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            skipToQueueItem(existingIndex)
        } else {
            val newQueue = currentQueue + song
            playSongList(newQueue, newQueue.size - 1)
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            if (player.playbackState == Player.STATE_IDLE && _queue.value.isNotEmpty()) {
                val index = _queueIndex.value.coerceAtLeast(0)
                playSongList(_queue.value, index)
            } else {
                player.play()
                _isPlaying.value = true
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
        _isPlaying.value = false
    }

    fun play() {
        exoPlayer?.play()
        _isPlaying.value = true
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return
        val currIdx = _queueIndex.value
        val nextIdx = if (_isShuffle.value) {
            (q.indices - currIdx).randomOrNull() ?: 0
        } else {
            if (currIdx + 1 < q.size) currIdx + 1 else 0
        }
        skipToQueueItem(nextIdx)
    }

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return
        val player = exoPlayer
        if (player != null && player.currentPosition > 3000) {
            player.seekTo(0)
            return
        }
        val currIdx = _queueIndex.value
        val prevIdx = if (currIdx - 1 >= 0) currIdx - 1 else q.size - 1
        skipToQueueItem(prevIdx)
    }

    fun skipToQueueItem(index: Int) {
        val q = _queue.value
        if (index in q.indices) {
            _queueIndex.value = index
            val song = q[index]
            _currentSong.value = song
            val player = exoPlayer ?: return
            if (player.mediaItemCount > index) {
                player.seekTo(index, 0L)
                player.play()
                _isPlaying.value = true
                onSongPlayed?.invoke(song.id)
            } else {
                playSongList(q, index)
            }
            notifyQueueChanged()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun seekBy(seconds: Int) {
        val cur = _currentPosition.value
        val dur = _duration.value
        val newPos = (cur + (seconds * 1000L)).coerceIn(0L, dur.coerceAtLeast(0L))
        seekTo(newPos)
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        exoPlayer?.let { player ->
            when (_repeatMode.value) {
                RepeatMode.OFF -> player.repeatMode = Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> player.repeatMode = Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> player.repeatMode = Player.REPEAT_MODE_ONE
            }
        }
        notifyQueueChanged()
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffle.value
        _isShuffle.value = newShuffle
        exoPlayer?.shuffleModeEnabled = newShuffle
        notifyQueueChanged()
    }

    // Playback Speed & Pitch Controls (Section 9)
    fun setPlaybackSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.25f, 3.0f)
        val curPitch = _speedPitch.value.pitch
        _speedPitch.value = PlaybackSpeedPitch(speed = safeSpeed, pitch = curPitch)
        exoPlayer?.playbackParameters = PlaybackParameters(safeSpeed, curPitch)
    }

    fun setPlaybackPitch(pitch: Float) {
        val safePitch = pitch.coerceIn(0.5f, 2.0f)
        val curSpeed = _speedPitch.value.speed
        _speedPitch.value = PlaybackSpeedPitch(speed = curSpeed, pitch = safePitch)
        exoPlayer?.playbackParameters = PlaybackParameters(curSpeed, safePitch)
    }

    fun resetSpeedAndPitch() {
        _speedPitch.value = PlaybackSpeedPitch(speed = 1.0f, pitch = 1.0f)
        exoPlayer?.playbackParameters = PlaybackParameters(1.0f, 1.0f)
    }

    // A-B Repeat Mode (Section 5)
    fun setPointA(posMs: Long) {
        _abRepeatState.value = _abRepeatState.value.copy(
            isEnabled = _abRepeatState.value.pointB != null && _abRepeatState.value.pointB!! > posMs,
            pointA = posMs
        )
    }

    fun setPointB(posMs: Long) {
        val pA = _abRepeatState.value.pointA ?: 0L
        if (posMs > pA) {
            _abRepeatState.value = _abRepeatState.value.copy(
                isEnabled = true,
                pointB = posMs
            )
        }
    }

    fun clearABRepeat() {
        _abRepeatState.value = ABRepeatState(isEnabled = false, pointA = null, pointB = null)
    }

    // Musicolet Multiple Queues Management (Section 1: up to 20 queues)
    fun switchQueue(targetQueueId: Long, allAvailableSongs: List<Song> = emptyList()) {
        if (targetQueueId == _activeQueueId.value) return

        // 1. Save current active queue state into memory list
        val currentQueue = PlaybackQueue(
            id = _activeQueueId.value,
            name = _activeQueueName.value,
            songs = _queue.value,
            currentSongIndex = _queueIndex.value.coerceAtLeast(0),
            currentPositionMs = _currentPosition.value,
            isShuffle = _isShuffle.value,
            repeatMode = _repeatMode.value,
            updatedAt = System.currentTimeMillis()
        )
        previousQueueId = _activeQueueId.value

        val updatedQueues = _queuesList.value.map {
            if (it.id == _activeQueueId.value) currentQueue else it
        }.toMutableList()

        // 2. Find target queue
        val targetQueue = updatedQueues.firstOrNull { it.id == targetQueueId }
            ?: PlaybackQueue(id = targetQueueId, name = "Fila $targetQueueId").also {
                updatedQueues.add(it)
            }

        _queuesList.value = updatedQueues
        _activeQueueId.value = targetQueue.id
        _activeQueueName.value = targetQueue.name

        // 3. Load target queue into ExoPlayer
        if (targetQueue.songs.isNotEmpty()) {
            _queue.value = targetQueue.songs
            _queueIndex.value = targetQueue.currentSongIndex.coerceIn(0, targetQueue.songs.size - 1)
            _isShuffle.value = targetQueue.isShuffle
            _repeatMode.value = targetQueue.repeatMode
            val song = targetQueue.songs[_queueIndex.value]
            _currentSong.value = song

            val player = exoPlayer ?: return
            player.stop()
            player.clearMediaItems()
            targetQueue.songs.forEach { s ->
                val meta = MediaMetadata.Builder().setTitle(s.title).setArtist(s.artist).setAlbumTitle(s.album).build()
                val item = MediaItem.Builder().setUri(Uri.parse(s.dataUri)).setMediaId(s.id.toString()).setMediaMetadata(meta).build()
                player.addMediaItem(item)
            }
            player.seekTo(_queueIndex.value, targetQueue.currentPositionMs)
            player.prepare()
            player.play()
            _isPlaying.value = true
        } else {
            clearQueue()
        }
    }

    fun createNewQueue(name: String, songs: List<Song>): Long? {
        val currentList = _queuesList.value
        if (currentList.size >= 20) return null // Max 20 queues as specified
        val nextId = (1L..20L).firstOrNull { id -> currentList.none { it.id == id } } ?: return null
        val newQueue = PlaybackQueue(
            id = nextId,
            name = name.trim().ifEmpty { "Fila $nextId" },
            songs = songs,
            currentSongIndex = 0,
            currentPositionMs = 0L
        )
        _queuesList.value = currentList + newQueue
        return nextId
    }

    fun duplicateQueue(queueId: Long, newName: String): Long? {
        val source = _queuesList.value.firstOrNull { it.id == queueId } ?: return null
        return createNewQueue(newName, source.songs)
    }

    fun renameQueue(queueId: Long, newName: String) {
        val name = newName.trim().ifEmpty { "Fila $queueId" }
        _queuesList.value = _queuesList.value.map {
            if (it.id == queueId) it.copy(name = name) else it
        }
        if (_activeQueueId.value == queueId) {
            _activeQueueName.value = name
        }
    }

    fun deleteQueue(queueId: Long) {
        if (_queuesList.value.size <= 1) return // Keep at least one queue
        val remaining = _queuesList.value.filterNot { it.id == queueId }
        _queuesList.value = remaining
        if (_activeQueueId.value == queueId) {
            switchQueue(remaining.first().id)
        }
    }

    fun restorePreviousQueue() {
        val prev = previousQueueId
        if (prev != null && _queuesList.value.any { it.id == prev }) {
            switchQueue(prev)
        }
    }

    private fun notifyQueueChanged() {
        val q = PlaybackQueue(
            id = _activeQueueId.value,
            name = _activeQueueName.value,
            songs = _queue.value,
            currentSongIndex = _queueIndex.value.coerceAtLeast(0),
            currentPositionMs = _currentPosition.value,
            isShuffle = _isShuffle.value,
            repeatMode = _repeatMode.value
        )
        onQueueChangedCallback?.invoke(q)
    }

    fun addToQueueNext(song: Song) {
        val currentQ = _queue.value.toMutableList()
        val insertIndex = (_queueIndex.value + 1).coerceIn(0, currentQ.size)
        currentQ.add(insertIndex, song)
        _queue.value = currentQ
        exoPlayer?.let { player ->
            val metadata = MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build()
            val item = MediaItem.Builder().setUri(Uri.parse(song.dataUri)).setMediaId(song.id.toString()).setMediaMetadata(metadata).build()
            player.addMediaItem(insertIndex, item)
        }
        notifyQueueChanged()
    }

    fun addToQueueEnd(songs: List<Song>) {
        val currentQ = _queue.value.toMutableList()
        currentQ.addAll(songs)
        _queue.value = currentQ
        exoPlayer?.let { player ->
            songs.forEach { song ->
                val metadata = MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build()
                val item = MediaItem.Builder().setUri(Uri.parse(song.dataUri)).setMediaId(song.id.toString()).setMediaMetadata(metadata).build()
                player.addMediaItem(item)
            }
        }
        notifyQueueChanged()
    }

    fun removeFromQueue(index: Int) {
        val currentQ = _queue.value.toMutableList()
        if (index in currentQ.indices) {
            currentQ.removeAt(index)
            _queue.value = currentQ
            exoPlayer?.removeMediaItem(index)
            if (index == _queueIndex.value) {
                if (currentQ.isNotEmpty()) {
                    skipToQueueItem(index.coerceIn(0, currentQ.size - 1))
                } else {
                    _currentSong.value = null
                    _isPlaying.value = false
                }
            } else if (index < _queueIndex.value) {
                _queueIndex.value = _queueIndex.value - 1
            }
        }
        notifyQueueChanged()
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _queueIndex.value = -1
        _currentSong.value = null
        exoPlayer?.clearMediaItems()
        _isPlaying.value = false
        notifyQueueChanged()
    }

    private fun handleSongEnded() {
        // Handle Sleep Timer with track count
        if (_sleepTimer.value.isRunning) {
            if (_sleepTimer.value.stopAfterCurrentTrack) {
                pause()
                cancelSleepTimer()
                return
            } else if (tracksRemainingToStop > 0) {
                tracksRemainingToStop--
                if (tracksRemainingToStop <= 0) {
                    if (_sleepTimer.value.fadeOutVolume) {
                        fadeOutVolumeAndStop()
                    } else {
                        pause()
                    }
                    cancelSleepTimer()
                    return
                }
            }
        }

        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                exoPlayer?.seekTo(0)
                exoPlayer?.play()
            }
            RepeatMode.ALL -> next()
            RepeatMode.OFF -> {
                if (_queueIndex.value + 1 < _queue.value.size) {
                    next()
                } else {
                    _isPlaying.value = false
                }
            }
        }
    }

    private fun updateCurrentFromPlayer() {
        val player = exoPlayer ?: return
        val currentIdx = player.currentMediaItemIndex
        val q = _queue.value
        if (currentIdx in q.indices) {
            _queueIndex.value = currentIdx
            _currentSong.value = q[currentIdx]
            _duration.value = q[currentIdx].durationMs
        }
    }

    // Equalizer & Device Profiles (Section 7)
    fun setEqualizerEnabled(enabled: Boolean) {
        _equalizerConfig.value = _equalizerConfig.value.copy(isEnabled = enabled)
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
        applyEqualizerToEffects(_equalizerConfig.value)
    }

    fun setBassBoost(level: Int) {
        _equalizerConfig.value = _equalizerConfig.value.copy(bassBoost = level)
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
        runCatching { bassBoost?.setStrength(level.toShort()) }
    }

    fun setVirtualizer(level: Int) {
        _equalizerConfig.value = _equalizerConfig.value.copy(virtualizer = level)
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
        runCatching { virtualizer?.setStrength(level.toShort()) }
    }

    fun setBandLevel(band: Int, level: Int) {
        val currentBands = _equalizerConfig.value.bandLevels.toMutableMap()
        currentBands[band] = level
        _equalizerConfig.value = _equalizerConfig.value.copy(bandLevels = currentBands, presetName = "Personalizado")
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
        runCatching { equalizer?.setBandLevel(band.toShort(), level.toShort()) }
    }

    fun setChannelBalance(balance: Float) {
        _equalizerConfig.value = _equalizerConfig.value.copy(channelBalance = balance)
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
        applyEqualizerToEffects(_equalizerConfig.value)
    }

    fun selectDeviceProfile(device: AudioOutputDevice) {
        _currentDevice.value = device
        applyDeviceProfile(device)
    }

    fun applyPreset(presetName: String) {
        val bands = when (presetName) {
            "Bass Boost" -> mapOf(0 to 600, 1 to 500, 2 to 350, 3 to 150, 4 to 0, 5 to 0, 6 to 0, 7 to 0, 8 to 0, 9 to 0)
            "Reduced Bass" -> mapOf(0 to -600, 1 to -500, 2 to -350, 3 to -150, 4 to 0, 5 to 0, 6 to 0, 7 to 0, 8 to 0, 9 to 0)
            "Rock" -> mapOf(0 to 500, 1 to 350, 2 to 200, 3 to 50, 4 to -100, 5 to 50, 6 to 200, 7 to 350, 8 to 450, 9 to 500)
            "Pop" -> mapOf(0 to -150, 1 to 50, 2 to 250, 3 to 400, 4 to 350, 5 to 200, 6 to 50, 7 to -50, 8 to -100, 9 to -150)
            "Clássico" -> mapOf(0 to 400, 1 to 300, 2 to 200, 3 to 100, 4 to -50, 5 to -50, 6 to 100, 7 to 200, 8 to 300, 9 to 400)
            "Jazz" -> mapOf(0 to 250, 1 to 200, 2 to 150, 3 to 50, 4 to -100, 5 to 50, 6 to 150, 7 to 250, 8 to 300, 9 to 350)
            "Eletrônica" -> mapOf(0 to 600, 1 to 450, 2 to 200, 3 to 0, 4 to -150, 5 to 100, 6 to 250, 7 to 400, 8 to 550, 9 to 600)
            "Hip-Hop" -> mapOf(0 to 650, 1 to 500, 2 to 300, 3 to 100, 4 to -50, 5 to 0, 6 to 100, 7 to 200, 8 to 300, 9 to 400)
            "Vocal" -> mapOf(0 to -300, 1 to -150, 2 to 100, 3 to 300, 4 to 450, 5 to 400, 6 to 250, 7 to 100, 8 to -50, 9 to -150)
            else -> {
                // Check if custom preset
                _equalizerConfig.value.customPresets[presetName]
                    ?: mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 6 to 0, 7 to 0, 8 to 0, 9 to 0)
            }
        }
        val bass = when (presetName) {
            "Bass Boost" -> 750
            "Hip-Hop" -> 600
            "Eletrônica" -> 550
            "Rock" -> 400
            "Reduced Bass" -> 0
            else -> 200
        }
        val virt = when (presetName) {
            "Eletrônica", "Rock" -> 400
            "Jazz", "Clássico" -> 300
            else -> 150
        }

        val newCfg = _equalizerConfig.value.copy(
            isEnabled = true,
            presetName = presetName,
            bassBoost = bass,
            virtualizer = virt,
            bandLevels = bands
        )
        _equalizerConfig.value = newCfg
        deviceProfiles[_currentDevice.value] = newCfg
        applyEqualizerToEffects(newCfg)
    }

    fun saveCustomPreset(presetName: String) {
        val currentLevels = _equalizerConfig.value.bandLevels
        val updated = _equalizerConfig.value.customPresets.toMutableMap()
        updated[presetName] = currentLevels
        _equalizerConfig.value = _equalizerConfig.value.copy(
            customPresets = updated,
            presetName = presetName
        )
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
    }

    fun deleteCustomPreset(presetName: String) {
        val updated = _equalizerConfig.value.customPresets.toMutableMap()
        updated.remove(presetName)
        _equalizerConfig.value = _equalizerConfig.value.copy(
            customPresets = updated,
            presetName = if (_equalizerConfig.value.presetName == presetName) "Normal" else _equalizerConfig.value.presetName
        )
        deviceProfiles[_currentDevice.value] = _equalizerConfig.value
    }

    // Advanced Sleep Timer (Section 19 & Section 6)
    fun startSleepTimer(
        minutes: Int,
        stopAfterTrack: Boolean = false,
        stopAfterNextTrack: Boolean = false,
        tracksCount: Int = 0,
        fadeOut: Boolean = true
    ) {
        cancelSleepTimer()
        val totalSeconds = minutes * 60L
        tracksRemainingToStop = if (stopAfterNextTrack) 2 else if (stopAfterTrack) 1 else tracksCount
        _sleepTimer.value = SleepTimerConfig(
            isRunning = true,
            remainingSeconds = totalSeconds,
            stopAfterCurrentTrack = stopAfterTrack,
            stopAfterNextTrack = stopAfterNextTrack,
            stopAfterTracksCount = tracksRemainingToStop,
            fadeOutVolume = fadeOut
        )

        if (totalSeconds > 0) {
            sleepTimerJob = scope.launch {
                var rem = totalSeconds
                while (rem > 0) {
                    delay(1000)
                    rem--
                    _sleepTimer.value = _sleepTimer.value.copy(remainingSeconds = rem)
                    if (rem == 30L && fadeOut) {
                        fadeOutVolumeAndStop()
                    }
                }
                if (!_sleepTimer.value.stopAfterCurrentTrack && !_sleepTimer.value.stopAfterNextTrack && tracksRemainingToStop <= 0) {
                    pause()
                }
                cancelSleepTimer()
            }
        }
    }

    fun stopAndReleaseForExit() {
        cancelSleepTimer()
        exoPlayer?.stop()
        _isPlaying.value = false
        _currentSong.value = null
    }

    private fun fadeOutVolumeAndStop() {
        scope.launch {
            val player = exoPlayer ?: return@launch
            val origVol = player.volume
            var v = origVol
            while (v > 0.05f) {
                delay(200)
                v -= 0.1f
                player.volume = v.coerceAtLeast(0f)
            }
            player.pause()
            player.volume = origVol
            _isPlaying.value = false
        }
    }

    fun setStopAfterCurrentTrack(enabled: Boolean) {
        _sleepTimer.value = _sleepTimer.value.copy(
            isRunning = enabled,
            stopAfterCurrentTrack = enabled,
            remainingSeconds = if (enabled) 0L else _sleepTimer.value.remainingSeconds
        )
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        tracksRemainingToStop = 0
        _sleepTimer.value = SleepTimerConfig(isRunning = false, remainingSeconds = 0L, stopAfterCurrentTrack = false)
    }

    // Bluetooth automation settings
    fun setPlayOnBluetoothConnect(enabled: Boolean) {
        _playOnBluetoothConnect.value = enabled
    }

    fun setPauseOnBluetoothDisconnect(enabled: Boolean) {
        _pauseOnBluetoothDisconnect.value = enabled
    }

    companion object {
        @Volatile
        private var INSTANCE: PlayerController? = null

        fun getInstance(context: Context): PlayerController {
            return INSTANCE ?: synchronized(this) {
                val instance = PlayerController(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
