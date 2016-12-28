package jp.cordea.kerberosplayer

import kotlin.properties.Delegates

/**
 * Created by Yoshihiro Tanaka on 2016/12/20.
 */
class Kerberos(private val players: Array<PlayerView>) {

    var playWhenReady: Boolean = false

    var playerStateChanged: (state: PlaybackState) -> Unit = { }

    var userAgent: String? = null
        set(value) {
            players.forEach { it.userAgent = value }
        }

    private val status: Array<PlaybackState> = players.map { it.currentState }.toTypedArray()

    private var isReady by Delegates.observable(false) {
        props, old, new ->
        if (!old && new) {
            if (playWhenReady) {
                play()
                playWhenReady = false
            }
        }
    }

    fun prepare() {
        val position = players.map { it.position ?: 0L }.max()
        players.forEach {
            it.position = position
            it.playWhenReady = false
            it.play()
        }
    }

    private fun play() {
        players.forEach {
            it.playWhenReady = true
            it.play()
        }
    }

    fun pause() {
        players.forEach(PlayerView::pause)
    }

    fun onResume() {
        players.forEach { it.onResume() }
    }

    fun onPause() {
        players.forEach { it.onPause() }
    }

    fun onStop() {
        players.forEach { it.onStop() }
    }

    fun onStart() {
        players.forEach { it.onStart() }
    }

    init {
        for (item in players.withIndex()) {
            item.value.playerStateChanged = {
                status[item.index] = it
                val statusMap = hashMapOf<PlaybackState, Int>()
                for (s in status) {
                    if (statusMap.contains(s)) {
                        statusMap[s] = (statusMap[s] ?: 0) + 1
                    } else {
                        statusMap.put(s, 1)
                    }
                }
                statusMap.maxBy { it.value }?.let {
                    if (it.value == status.size) {
                        isReady = it.key == PlaybackState.READY
                        playerStateChanged(it.key)
                    }
                }
            }
        }
    }
}