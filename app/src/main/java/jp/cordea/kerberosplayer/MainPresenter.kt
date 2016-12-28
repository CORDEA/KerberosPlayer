package jp.cordea.kerberosplayer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Size
import android.view.View
import android.widget.FrameLayout
import com.google.android.exoplayer2.util.Util

/**
 * Created by Yoshihiro Tanaka on 2016/12/20.
 */
class MainPresenter(private val activity: MainActivity) {

    companion object {
        const val AnimationDuration: Long = 300
    }

    enum class State {
        EXPAND,
        COLLAPSE
    }

    private var currentStatus = State.COLLAPSE

    fun userAgent(): String {
        return Util.getUserAgent(activity, activity.resources.getString(R.string.app_name))
    }

    fun expand(player1: View, player2: View, baseWidth: Int, baseHeight: Int) {
        if (currentStatus == State.EXPAND) {
            return
        }
        currentStatus = State.EXPAND
        val animator1 = expandAnimator(player1, baseHeight)
        val animator2 = expandAnimator(player2, baseHeight)
        val moveAnimator1 = expandMoveAnimator(player1, baseWidth, baseHeight)
        val moveAnimator2 = expandMoveAnimator(player2, baseWidth, baseHeight, true)
        val alphaAnimator = alphaAnimator(player2, 1f, 0.5f)

        val animatorSet = AnimatorSet()
        animatorSet.play(animator1).with(animator2).with(moveAnimator1).with(moveAnimator2).with(alphaAnimator)
        animatorSet.duration = AnimationDuration
        animatorSet.start()
    }

    fun collapse(player1: View, player2: View, baseWidth: Int) {
        if (currentStatus == State.COLLAPSE) {
            return
        }
        currentStatus = State.COLLAPSE
        val animator1 = collapseAnimator(player1, baseWidth)
        val animator2 = collapseAnimator(player2, baseWidth)
        val moveAnimator1 = collapseMoveAnimator(player1)
        val moveAnimator2 = collapseMoveAnimator(player2, true)
        val alphaAnimator = alphaAnimator(player2, 0.5f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.play(animator1).with(animator2).with(moveAnimator1).with(moveAnimator2).with(alphaAnimator)
        animatorSet.duration = AnimationDuration
        animatorSet.start()
    }

    private fun expandAnimator(view: View, baseHeight: Int): ValueAnimator {
        val aspect = view.measuredWidth.toFloat() / view.measuredHeight.toFloat()
        val animator = ValueAnimator.ofInt(view.measuredHeight, baseHeight)
        animator.addUpdateListener {
            val h = it.animatedValue as Int
            val width = h * aspect
            val lp = view.layoutParams
            lp.width = width.toInt()
            lp.height = h
            view.layoutParams = lp
        }
        return animator
    }

    private fun expandMoveAnimator(view: View, baseWidth: Int, baseHeight: Int, isEnd: Boolean = false): ValueAnimator {
        val aspect = view.measuredWidth.toFloat() / view.measuredHeight.toFloat()
        val margin: Int
        if (isEnd) {
            margin = (view.layoutParams as FrameLayout.LayoutParams).rightMargin
        } else {
            margin = (view.layoutParams as FrameLayout.LayoutParams).leftMargin
        }
        val lastWidth = baseHeight * aspect
        val moveAnimator = ValueAnimator.ofInt(margin, ((baseWidth / 2) - (lastWidth / 2)).toInt())
        moveAnimator.addUpdateListener {
            val x = it.animatedValue as Int
            val lp = view.layoutParams as FrameLayout.LayoutParams
            if (isEnd) {
                lp.rightMargin = x
            } else {
                lp.leftMargin = x
            }
            view.layoutParams = lp
        }
        return moveAnimator
    }

    private fun collapseAnimator(view: View, baseWidth: Int): ValueAnimator {
        val aspect = view.measuredHeight.toFloat() / view.measuredWidth.toFloat()
        val width = baseWidth / 2
        val animator = ValueAnimator.ofInt(view.measuredWidth, width)
        animator.addUpdateListener {
            val w = it.animatedValue as Int
            val h = w * aspect
            val lp = view.layoutParams
            lp.height = h.toInt()
            lp.width = w
            view.layoutParams = lp
        }
        return animator
    }

    private fun collapseMoveAnimator(view: View, isEnd: Boolean = false): ValueAnimator {
        val margin: Int
        if (isEnd) {
            margin = (view.layoutParams as FrameLayout.LayoutParams).rightMargin
        } else {
            margin = (view.layoutParams as FrameLayout.LayoutParams).leftMargin
        }
        val moveAnimator = ValueAnimator.ofInt(margin, 0)
        moveAnimator.addUpdateListener {
            val x = it.animatedValue as Int
            val lp = view.layoutParams as FrameLayout.LayoutParams
            if (isEnd) {
                lp.rightMargin = x
            } else {
                lp.leftMargin = x
            }
            view.layoutParams = lp
        }
        return moveAnimator
    }

    private fun alphaAnimator(view: View, from: Float, to: Float): ValueAnimator {
        return ObjectAnimator.ofFloat(view, "alpha", from, to)
    }
}