package com.music.kotlinqq.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.music.kotlinqq.R
import com.music.kotlinqq.util.CommonUtil
import com.music.kotlinqq.util.DisplayUtil.*

/**
 * @author cyl
 * @date 2020/9/15
 */
open class DiscView(context: Context, attr: AttributeSet, defS : Int = 0) : RelativeLayout(context, attr, defS){

    private var mIvNeedle : ImageView? = null
    private var mNeedleAnimator: ObjectAnimator? = null
    private var mObjectAnimator: ObjectAnimator? = null
    // 标记ViewPager是否处于偏移的状态
    private var mViewPagerIsOffset = false
    // 标记唱针复位后 是否需要重新偏移到唱片处
    private var mIsNeed2StartPlayAnimator = false
    private var musicStatus = MusicStatus.STOP
    companion object {
       const val DURATION_NEEDLE_ANIMATOR = 500
    }
    private var needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END
    private var mScreenWidth: Int = 0
    private var mScreenHeight: Int = 0


    /*唱针当前所处的状态*/
    enum class NeedleAnimatorStatus {
        /*移动时：从唱盘往远处移动*/
        TO_FAR_END,
        /*移动时：从远处往唱盘移动*/
        TO_NEAR_END,
        /*静止时：离开唱盘*/
        IN_FAR_END,
        /*静止时：贴近唱盘*/
        IN_NEAR_END
    }
    /*音乐当前的状态：只有播放、暂停、停止三种*/
    enum class MusicStatus {
        PLAY, PAUSE, STOP
    }
    init {
        mScreenHeight = CommonUtil.getScreenHeight(context)
        mScreenWidth = CommonUtil.getScreenWidth(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initDiscImg()
        initNeedle()
        initObjectAnimator()
    }

    private fun initDiscImg(){
        val mDiscBackground = findViewById<ImageView>(R.id.iv_disc_background)
        mObjectAnimator = getDiscObjectAnimator(mDiscBackground)
        mDiscBackground.setImageDrawable(getDiscDrawable(BitmapFactory.decodeResource(resources, R.drawable.default_disc)))
        val marginTop = (SCALE_DISC_MARGIN_TOP.times(mScreenWidth)).toInt()
        val layoutParams = mDiscBackground.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(0, marginTop, 0, 0)
        mDiscBackground.layoutParams = layoutParams
    }

    private fun initNeedle(){
        mIvNeedle = findViewById(R.id.iv_needle)
        val needleWidth = SCALE_NEEDLE_WIDTH.times(mScreenWidth).toInt()
        val needleHeight = SCALE_NEEDLE_HEIGHT.times(mScreenHeight).toInt()
        // 设置手柄的外边距为负数 -> 让其隐藏一部分
        val marginTop = (SCALE_DISC_MARGIN_TOP.times(mScreenHeight).toInt()).times(-1)
        val marginLeft = SCALE_NEEDLE_MARGIN_LEFT.times(mScreenWidth).toInt()
        val originBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_needle)
        val bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, false)
        val layoutParams = mIvNeedle!!.layoutParams as LayoutParams
        layoutParams.setMargins(marginLeft, marginTop, 0, 0)
        val pivotX = SCALE_NEEDLE_PIVOT_X * mScreenWidth
        val pivotY = SCALE_NEEDLE_PIVOT_Y * mScreenHeight
        mIvNeedle!!.pivotX = pivotX
        mIvNeedle!!.pivotY = pivotY
        mIvNeedle!!.rotation = ROTATION_INIT_NEEDLE
        mIvNeedle!!.setImageBitmap(bitmap)
        mIvNeedle!!.layoutParams = layoutParams
    }

    private fun initObjectAnimator(){
        mNeedleAnimator = initAnimator(mIvNeedle)
        mNeedleAnimator!!.duration = (DURATION_NEEDLE_ANIMATOR).toLong()
        mNeedleAnimator!!.interpolator = AccelerateInterpolator()
        mNeedleAnimator!!.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {

                if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END){
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_NEAR_END
                    playDiscAnimator()
                    musicStatus = MusicStatus.PLAY
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END){
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END
                    if (musicStatus == MusicStatus.STOP){
                        mIsNeed2StartPlayAnimator = true
                    }
                }
                if (mIsNeed2StartPlayAnimator){
                    mIsNeed2StartPlayAnimator = false

                    if (!mViewPagerIsOffset){
                        postDelayed({
                            playAnimator()
                        }, 50)
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                /**
                 * 根据动画开始前NeedleAnimatorStatus的状态，
                 * 即可得出动画进行时NeedleAnimatorStatus的状态
                 * */
                if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END){
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_NEAR_END
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END){
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END
                }
            }

        })
    }
    /**
     * 得到唱片图片
     */
    open fun getDiscDrawable(bitmap: Bitmap): Drawable {
        val discSize = (mScreenWidth.times(SCALE_DISC_SIZE)).toInt()
        val musicPicSize = (mScreenWidth.times(SCALE_MUSIC_PIC_SIZE)).toInt()
        val bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_disc), discSize, discSize, false)
        val bitmapMusicPic = Bitmap.createScaledBitmap(bitmap, musicPicSize, musicPicSize, true)
        val discDrawable = BitmapDrawable(bitmapDisc)
        val roundMusicDrawable = RoundedBitmapDrawableFactory.create(resources, bitmapMusicPic)
        // 抗锯齿
        discDrawable.setAntiAlias(true)
        roundMusicDrawable.setAntiAlias(true)
        val drawables = arrayOfNulls<Drawable>(2)
        drawables[0] = roundMusicDrawable
        drawables[1] = discDrawable
        val layerDrawable = LayerDrawable(drawables)
        val musicPicMargin = ((SCALE_DISC_SIZE - SCALE_MUSIC_PIC_SIZE).times(mScreenWidth!! / 2)).toInt()
        // 调整专辑图片四周的边距 -> 显示在正中
        layerDrawable.setLayerInset(0, musicPicMargin, musicPicMargin, musicPicMargin, musicPicMargin)
        return layerDrawable
    }


    /**
     * 播放动画
     */
    private fun playAnimator(){
        // 唱针处于远端时 直接播放动画
        if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END){
            mNeedleAnimator!!.start()
        }
        // 唱针处于远端移动时 设置标记 等动画结束后再播放动画
        else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END){
            mIsNeed2StartPlayAnimator = true
        }
    }
    /**
     * 暂停动画
     */
    private fun pauseAnimator(){
        // 播放时暂停动画
        if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END){
            pauseDiscAnimator()
        }
        // 唱针往唱盘移动时暂停动画
        else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END){
            mNeedleAnimator!!.reverse()
            // 若动画在没结束时执行reverse方法 则不会执行监听器的onStart方法 需要手动设置
            needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END
        }
    }

    /**
     * 播放唱盘动画
     */
    private fun playDiscAnimator(){
        if (mObjectAnimator!!.isPaused){
            mObjectAnimator!!.resume()
        } else {
            mObjectAnimator!!.start()
        }
    }

    /**
     * 暂停唱盘动画
     */
    private fun pauseDiscAnimator(){
        mObjectAnimator!!.pause()
        mNeedleAnimator!!.reverse()
    }

    open fun play(){
        playAnimator()
    }

    fun pause(){
        musicStatus = MusicStatus.PAUSE
        pauseAnimator()
    }

    fun stop(){
        musicStatus = MusicStatus.STOP
    }

    fun next(){
        playAnimator()
        selectMusicWithButton()
    }

    fun last(){
        playAnimator()
        selectMusicWithButton()
    }

    fun isPlaying() : Boolean {
        return musicStatus == MusicStatus.PLAY
    }

    private fun selectMusicWithButton(){
        if (musicStatus == MusicStatus.PLAY){
            mIsNeed2StartPlayAnimator = true
            pauseAnimator()
        } else if (musicStatus == MusicStatus.PAUSE){
            play()
        }
    }
}