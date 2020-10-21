package com.music.kotlinqq.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.music.kotlinqq.R

/**
 * @author cyl
 * @date 2020/9/15
 */
class BackgroundAnimationRelativeLayout(context: Context, attr: AttributeSet, defS : Int = 0) : RelativeLayout(context, attr, defS) {

    private val INDEX_BACKGROUND : Int = 0
    private val INDEX_FOREGROUND : Int = 1
    private var layerDrawable : LayerDrawable? = null
    private var objectAnimator : ObjectAnimator? = null

    companion object {}
    init {
        initLayerDrawable()
        initObjectAnimator()
    }
    private fun initLayerDrawable(){
        val backgroundDrawable = context.getDrawable(R.drawable.ic_blackground)
        val drawables = arrayOfNulls<Drawable>(2)
        drawables[INDEX_BACKGROUND] = backgroundDrawable
        drawables[INDEX_FOREGROUND] = backgroundDrawable
        layerDrawable = LayerDrawable(drawables)
    }
    @SuppressLint("ObjectAnimatorBinding")
    private fun initObjectAnimator(){
        objectAnimator = ObjectAnimator.ofFloat(Companion, "number", 0f, 1.0f)
        objectAnimator?.duration = 500
        objectAnimator?.interpolator = AccelerateInterpolator()
        objectAnimator?.addUpdateListener {
            val foregroundAlpha = ((it.animatedValue).toString().toFloat() * 255).toInt()
            layerDrawable?.getDrawable(INDEX_FOREGROUND)?.alpha = foregroundAlpha
            Companion.let { background = layerDrawable }
        }
        objectAnimator?.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onAnimationEnd(animation: Animator?) {
               layerDrawable?.setDrawable(INDEX_BACKGROUND, layerDrawable?.getDrawable(INDEX_FOREGROUND))
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }

    @TargetApi(23)
    override fun setForeground(foreground: Drawable?) {
        layerDrawable?.setDrawable(INDEX_FOREGROUND, foreground)
    }
    // 开始渐变
    fun beginAnimation(){
        objectAnimator?.start()
    }
}