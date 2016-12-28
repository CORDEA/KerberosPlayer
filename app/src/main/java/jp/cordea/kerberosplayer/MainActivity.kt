package jp.cordea.kerberosplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewTreeObserver
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val presenter: MainPresenter = MainPresenter(this)

    private lateinit var kerberos: Kerberos

    private val container by lazy {
        findViewById(R.id.container)
    }

    private val player1 by lazy {
        findViewById(R.id.player1) as PlayerView
    }

    private val player2 by lazy {
        findViewById(R.id.player2) as PlayerView
    }

    private val playButton by lazy {
        findViewById(R.id.play_button) as Button
    }

    private val pauseButton by lazy {
        findViewById(R.id.pause_button) as Button
    }

    private val expandButton by lazy {
        findViewById(R.id.expand_button) as Button
    }

    private val collapseButton by lazy {
        findViewById(R.id.collapse_button) as Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playButton.setOnClickListener {
            kerberos.playWhenReady = true
            kerberos.prepare()
            playButton.isEnabled = false
            pauseButton.isEnabled = true
        }

        pauseButton.setOnClickListener {
            kerberos.pause()
            playButton.isEnabled = true
            pauseButton.isEnabled = false
        }
        expandButton.setOnClickListener {
            presenter.expand(player1, player2, container.measuredWidth, container.measuredHeight)
        }
        collapseButton.setOnClickListener {
            presenter.collapse(player1, player2, container.measuredWidth)
        }

        kerberos = Kerberos(arrayOf(player1, player2))
        kerberos.userAgent = presenter.userAgent()

        var isFirst = true
        kerberos.playerStateChanged = {
            if (it == PlaybackState.READY) {
                if (isFirst) {
                    expandButton.isEnabled = true
                    collapseButton.isEnabled = true
                    isFirst = false
                }
            }
        }

        container.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val lp = player1.layoutParams
                lp.width = container.measuredWidth / 2
                player1.layoutParams = lp
                val lp2 = player2.layoutParams
                lp2.width = container.measuredWidth / 2
                player2.layoutParams = lp2
            }
        })
    }

    override fun onResume() {
        super.onResume()
        kerberos.onResume()

        player1.url = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"
        player2.url = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"

        playButton.isEnabled = true
        pauseButton.isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        kerberos.onPause()
    }

    override fun onStop() {
        super.onStop()
        kerberos.onStop()
    }

    override fun onStart() {
        super.onStart()
        kerberos.onStart()
    }
}
