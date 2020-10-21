package com.music.kotlinqq.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import com.apkfuns.logutils.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.activity.BaseActivity
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.event.OnlineSongErrorEvent
import com.music.kotlinqq.event.SongStatusEvent
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.CommonUtil
import com.music.kotlinqq.util.DisplayUtil
import com.music.kotlinqq.util.FileUtil
import com.music.kotlinqq.util.ServiceUtil
import com.music.kotlinqq.view.main.MainFragment
import com.music.kotlinqq.view.search.SearchContentFragment
import kotlinx.android.synthetic.main.player.*
import kotlinx.android.synthetic.main.player.tv_singer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import java.lang.Exception

/**
 * @author cyl
 * @date 2020/9/16
 */

class MainActivity : BaseActivity(){

    companion object {
        private const val TAG = "MainActivity"
    }

    private var isChange = false // 拖动进度条
    private var isSeek = false // 标记是否在暂停的时候拖动进度条
    private var flag = false // 用做暂停的标记
    private var time = 0 // 用做暂停的时间
    private var isExistService = false // 服务是否存活
    private var mCircleAnimator = ObjectAnimator() // 动画
    private var mSong = Song()
    private var mMediaPlayer = MediaPlayer()
    private var mSeekBarThread = Thread()
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
            if (isExistService) seekBarStart()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun getLayoutId(): Int {
       return R.layout.activity_main
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        EventBus.getDefault().register(this)
        LitePal.getDatabase()
        mCircleAnimator = DisplayUtil.initObjectAnimator(circle_img)
        mCircleAnimator.duration = 30 * 1000
        mCircleAnimator.interpolator = LinearInterpolator()
        mCircleAnimator.repeatCount = -1
        mCircleAnimator.repeatMode = ValueAnimator.RESTART

        mSong = FileUtil.getSong()!!
        if (mSong.songName != null){
            LogUtils.d(TAG, "initView$mSong")
            linear_player.visibility = View.VISIBLE
            tv_song_name.text = mSong.songName
            tv_singer.text = mSong.singer
            Log.i("PlayMain", mSong.duration.toString())
            sb_progress.max = mSong.duration.toInt()
            sb_progress.progress = mSong.currentTime.toInt()
            if (mSong.imgUrl == null){
                CommonUtil.setSingerImg(this, mSong.singer, circle_img)
            } else {
                Glide.with(this)
                    .load(mSong.imgUrl)
                    .apply(RequestOptions.placeholderOf(R.drawable.welcome))
                    .apply(RequestOptions.errorOf(R.drawable.welcome))
                    .into(circle_img)
            }
        } else {
            tv_song_name.text = "YearningMusic"
            tv_singer.text = getString(R.string.welcome_start)
            circle_img.setImageResource(R.drawable.jay)
        }
        if (ServiceUtil.isServiceRunning(this, PlayerService::class.java.name)){
            btn_player.isSelected = true
            mCircleAnimator.start()
            isExistService = true
        }
        initService()
        addMainFragment()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initService(){
        val playIntent = Intent(this, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(playIntent)
        } else {
            startService(playIntent)
        }
        bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onOnlineSongErrorEvent(event : OnlineSongErrorEvent){
        showToast(getString(R.string.error_out_of_copyright))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onSongStatusEvent(event : SongStatusEvent){
        when (event.songStatus){
            Constant.SONG_RESUME -> {
                btn_player.isSelected = true
                mCircleAnimator.resume()
                seekBarStart()
            }
            Constant.SONG_PAUSE -> {
                btn_player.isSelected = false
                mCircleAnimator.pause()
            }
            Constant.SONG_CHANGE -> {
                mSong = FileUtil.getSong()!!
                tv_song_name.text = mSong.songName
                tv_singer.text = mSong.singer
                sb_progress.max = mSong.duration.toInt()
                btn_player.isSelected = true
                mCircleAnimator.start()
                seekBarStart()
                if (!mSong.isOnline){
                    CommonUtil.setSingerImg(this, mSong.singer, circle_img)
                } else {
                    Glide.with(this)
                        .load(mSong.imgUrl)
                        .apply(RequestOptions.placeholderOf(R.drawable.welcome))
                        .apply(RequestOptions.errorOf(R.drawable.welcome))
                        .into(circle_img)
                }
            }
        }
    }

    override fun initData() {
        Thread(Runnable {
            try {
                CommonUtil.getBJTime()
            } catch (e : Exception){
                e.printStackTrace()
            }
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        val playIntent = Intent (this@MainActivity, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(playIntent)
        } else {
            startService(playIntent)
        }
        EventBus.getDefault().unregister(this)
        if (mSeekBarThread.isAlive) mSeekBarThread.interrupt()
        val song = FileUtil.getSong()!!
        song.currentTime = mPlayStatusBinder!!.getCurrentTime()
        FileUtil.saveSong(song)
    }

    override fun onClick() {
        sb_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isChange = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (mPlayStatusBinder!!.isPlaying()){
                    mPlayStatusBinder!!.getMediaPlayer().seekTo(sb_progress.progress * 1000)
                } else {
                    time = sb_progress.progress
                    isSeek = true
                }
                isChange = false
                seekBarStart()
            }
        })
        btn_player.setOnClickListener {
            mMediaPlayer = mPlayStatusBinder!!.getMediaPlayer()
            if (mPlayStatusBinder!!.isPlaying()){
                time = mMediaPlayer.currentPosition
                mPlayStatusBinder!!.pause()
                flag = true
            } else if (flag){
                mPlayStatusBinder!!.resume()
                flag = false
                if (isSeek){
                    mMediaPlayer.seekTo(time * 1000)
                    isSeek = false
                }
            } else {
                // 退出app 重新打开后的情况
                if (FileUtil.getSong()!!.isOnline){
                    mPlayStatusBinder!!.playOnline()
                } else {
                    mPlayStatusBinder!!.play(FileUtil.getSong()!!.listType)
                }
                mMediaPlayer = mPlayStatusBinder!!.getMediaPlayer()
                mMediaPlayer.seekTo(mSong.currentTime.toInt() * 1000)
            }
        }
        // 下一首
        song_next.setOnClickListener {
            if (FileUtil.getSong()!!.songName != null) mPlayStatusBinder!!.next()
            btn_player.isSelected = mPlayStatusBinder!!.isPlaying()
        }

        linear_player.setOnClickListener {
            if (FileUtil.getSong()!!.songName != null){
                val playIntent = Intent (this, PlayActivity::class.java)

                // 播放情况
                if (mPlayStatusBinder!!.isPlaying()){
                    val song = FileUtil.getSong()!!
                    song.currentTime = sb_progress.progress.toLong()
                    FileUtil.saveSong(song)
                    playIntent.putExtra(Constant.PLAYER_STATUS, Constant.SONG_PLAY)
                } else {
                    val song = FileUtil.getSong()!!
                    song.currentTime = sb_progress.progress.toLong()
                    FileUtil.saveSong(song)
                }
                if (FileUtil.getSong()!!.imgUrl != null){
                    playIntent.putExtra(SearchContentFragment.IS_ONLINE, true)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    startActivity(playIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                } else {
                    startActivity(playIntent)
                }
            } else {
                showToast(getString(R.string.welcome_start))
            }
        }

    }

    private fun addMainFragment(){
        val mainFragment = MainFragment()
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, mainFragment)
        transaction.commit()
    }

    private fun seekBarStart(){
        mSeekBarThread = Thread(SeekBarThread())
        mSeekBarThread.start()
    }

    inner class SeekBarThread : Thread(){
        override fun run() {
            if (mPlayStatusBinder != null){
                while (!isChange && mPlayStatusBinder!!.isPlaying()){
                    sb_progress.progress = mPlayStatusBinder!!.getCurrentTime().toInt()
                    try {
                        sleep(1000)
                    } catch (e : InterruptedException){
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}