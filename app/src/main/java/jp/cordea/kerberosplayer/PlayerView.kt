package jp.cordea.kerberosplayer

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.video.VideoRendererEventListener

/**
 * Created by Yoshihiro Tanaka on 2016/12/19.
 */

class PlayerView : FrameLayout, ExoPlayer.EventListener, VideoRendererEventListener {

    private val aspectRatioFrameLayout: AspectRatioFrameLayout by lazy {
        findViewById(R.id.aspect_ratio_frame) as AspectRatioFrameLayout
    }

    private val textureView: TextureView by lazy {
        findViewById(R.id.texture) as TextureView
    }

    private val mainHandler = Handler()

    private var player: SimpleExoPlayer? = null

    private var factory: DataSource.Factory? = null

    private var trackSelector: TrackSelector? = null

    private var currentPosition: Long = 0

    var url: String = ""

    var priority: Int = 0

    var position: Long?
        get() {
            return player?.currentPosition
        }
        set(value) {
            value?.let {
                player?.seekTo(it)
            }
        }

    var userAgent: String? = null

    var bufferedPosition: Long? = null
        get() {
            return player?.bufferedPosition
        }

    var playWhenReady: Boolean = false
        set(value) {
            player?.playWhenReady = value
        }

    var currentState: PlaybackState = PlaybackState.IDLE
        get() {
            val state: PlaybackState
            when (player?.playbackState) {
                ExoPlayer.STATE_BUFFERING ->
                    state = PlaybackState.BUFFERING
                ExoPlayer.STATE_ENDED ->
                    state = PlaybackState.ENDED
                ExoPlayer.STATE_IDLE ->
                    state = PlaybackState.IDLE
                ExoPlayer.STATE_READY ->
                    state = PlaybackState.READY
                else ->
                    state = PlaybackState.IDLE
            }
            return state
        }

    var playerStateChanged: (playbackState: PlaybackState) -> Unit = { }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        inflate(context, R.layout.player, this)
    }

    fun onResume() {
        if (Build.VERSION.SDK_INT > 23) {
            initializePlayer()
        }
    }

    fun onStart() {
        if (Build.VERSION.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    fun onPause() {
        if (Build.VERSION.SDK_INT <= 23) {
            release()
        }
    }

    fun onStop() {
        if (Build.VERSION.SDK_INT > 23) {
            release()
        }
    }

    fun play() {
        player?.let {
            prepare()
        }
    }

    fun pause() {
        player?.stop()
    }

    fun prepare() {
        factory = factory ?: buildDataSourceFactory()
        player?.let {
            val mediaSource = HlsMediaSource(Uri.parse(url), factory, mainHandler, null)
            it.prepare(mediaSource, false, false)
            it.seekTo(position ?: currentPosition)
        }
    }

    private fun initializePlayer() {
        val factory = AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER)
        trackSelector = DefaultTrackSelector(factory)
        trackSelector?.let { selector ->
            @SimpleExoPlayer.ExtensionRendererMode val extensionRendererMode = SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON
            player = ExoPlayerFactory.newSimpleInstance(context,
                    selector,
                    DefaultLoadControl(),
                    null,
                    extensionRendererMode)
            player?.let {
                it.addListener(this)
                it.setVideoDebugListener(this)
                it.setVideoTextureView(textureView)
            }
        }
    }

    fun release() {
        player?.let {
            it.stop()
            val timeline = it.currentTimeline
            currentPosition = C.TIME_UNSET
            if (!timeline.isEmpty) {
                currentPosition = it.currentPosition
            }
            it.release()
            player = null
        }
    }

    private fun buildDataSourceFactory(): DataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent, BANDWIDTH_METER)
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any) {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        var state: PlaybackState = PlaybackState.IDLE
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING ->
                state = PlaybackState.BUFFERING
            ExoPlayer.STATE_ENDED ->
                state = PlaybackState.ENDED
            ExoPlayer.STATE_IDLE ->
                state = PlaybackState.IDLE
            ExoPlayer.STATE_READY ->
                state = PlaybackState.READY
        }
        playerStateChanged(state)
    }

    override fun onPlayerError(error: ExoPlaybackException) {

    }

    override fun onPositionDiscontinuity() {

    }

    override fun onVideoEnabled(counters: DecoderCounters) {

    }

    override fun onVideoDecoderInitialized(decoderName: String, initializedTimestampMs: Long, initializationDurationMs: Long) {

    }

    override fun onVideoInputFormatChanged(format: Format) {

    }

    override fun onDroppedFrames(count: Int, elapsedMs: Long) {

    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        aspectRatioFrameLayout.setAspectRatio(width.toFloat() / height.toFloat())
    }

    override fun onRenderedFirstFrame(surface: Surface) {

    }

    override fun onVideoDisabled(counters: DecoderCounters) {

    }

    companion object {
        private val BANDWIDTH_METER = DefaultBandwidthMeter()
    }

}