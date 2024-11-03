package com.aatorque.carstats

import android.content.ComponentName
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class AlbumArt : CarFragment() {

    val registed = HashMap<Int, () -> Unit>()
    private val updateChannel = MutableSharedFlow<MediaMetadata?>()

    override fun onResume() {
        super.onResume()
        setupListenMedia()
    }

    override fun onPause() {
        super.onPause()
        updateSessions(emptyList())
    }

    private fun setupListenMedia() {
        lifecycleScope.launch {
            updateChannel.asSharedFlow()
                .distinctUntilChangedBy {
                    metaDataToArt(it).hashCode()
                }
                .collect {
                    Timber.i("Sending new metadata ${it}")
                    onMediaChanged(it)
                }
        }
        registerMedia()
    }

    private fun registerMedia() {
        val mediaMan = ContextCompat.getSystemService(
            requireContext(),
            MediaSessionManager::class.java
        ) ?: return
        val component = ComponentName(requireContext(), NotiService::class.java)
        try {
            mediaMan.addOnActiveSessionsChangedListener({
                updateSessions(it ?: emptyList())
            }, component)
            updateSessions(mediaMan.getActiveSessions(component))
        } catch (e: SecurityException) {
            Timber.e("No permission to read media", e)
        }
    }

    private fun updateSessions(mediaControllers: List<MediaController>) {
        val found = mediaControllers.map {
            val token = it.sessionToken
            val code = token.hashCode()
            if (!registed.containsKey(code)) {
                val callback = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        super.onMetadataChanged(metadata)
                        lifecycleScope.launch {
                            updateChannel.emit(metadata)
                        }
                    }
                }
                it.registerCallback(callback)
                registed[code] = {
                    it.unregisterCallback(callback)
                    registed.remove(code)
                }
                if (isActive(it.playbackState)) {
                    lifecycleScope.launch {
                        updateChannel.emit(it.metadata)
                        Timber.i("Sending initial metadata ${it.metadata}")
                    }
                }
            }
            code
        }.toSet()
        registed.filterNot { found.contains(it.key) }.forEach {
            it.value()
        }
    }

    open fun isActive(state: PlaybackState?): Boolean {
        return when (state?.state) {
            PlaybackState.STATE_FAST_FORWARDING,
            PlaybackState.STATE_REWINDING,
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS,
            PlaybackState.STATE_SKIPPING_TO_NEXT,
            PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM,
            PlaybackState.STATE_BUFFERING,
            PlaybackState.STATE_CONNECTING,
            PlaybackState.STATE_PLAYING -> true

            else -> false
        }
    }

    fun metaDataToArt(medadata: MediaMetadata?): Bitmap? {
        return medadata?.getBitmap(
            MediaMetadata.METADATA_KEY_ART
        ) ?: medadata?.getBitmap(
            MediaMetadata.METADATA_KEY_ALBUM_ART
        )
    }

    abstract suspend fun onMediaChanged(medadata: MediaMetadata?)
}