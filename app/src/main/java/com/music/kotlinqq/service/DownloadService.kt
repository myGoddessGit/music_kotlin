package com.music.kotlinqq.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.bean.DownloadInfo
import com.music.kotlinqq.download.DownloadListener
import com.music.kotlinqq.download.DownloadTaskS
import com.music.kotlinqq.event.DownloadEvent
import com.music.kotlinqq.event.SongListNumEvent
import com.music.kotlinqq.util.CommonUtil
import com.music.kotlinqq.util.DownloadUtil
import com.music.kotlinqq.view.MainActivity
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

/**
 * @author cyl
 * @date 2020/9/16
 */
class DownloadService : Service() {
    private var downloadTaskS : DownloadTaskS? = null
    private val downloadBinder = DownloadBinder()
    private var downloadQueue = LinkedList<DownloadInfo>() // 等待队列
    private var position = 0 // 下载歌曲在下载列表中的位置

    private val listener = object : DownloadListener {
        override fun onProgress(downloadInfo: DownloadInfo) {
            downloadInfo.status = Constant.DOWNLOAD_ING
            EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOADING, downloadInfo))
            if (downloadInfo.progress != 100){
                getNotificationManager().notify(1, getNotification("正在下载: ${downloadInfo.songName}", downloadInfo.progress ))
            } else {
                if (downloadQueue.isEmpty()){
                    getNotificationManager().notify(1, getNotification("下载成功", -1))
                }
            }
        }

        override fun onSuccess() {
            downloadTaskS = null
            val downloadInfo = downloadQueue.poll()
            operateDb(downloadInfo) // 操作数据库
            start() // 下载队列中的其他歌曲
            stopForeground(true)
            if (downloadQueue.isEmpty()) getNotificationManager().notify(1, getNotification("下载成功", -1))
        }

        override fun onDownloaded() {
            downloadTaskS = null
            CommonUtil.showToast(this@DownloadService, "已下载")
        }

        override fun onFailed() {
            downloadTaskS = null
            // 下载失败通知前台服务通知关闭 并创建一个下载失败的通知
            stopForeground(true)
            getNotificationManager().notify(1, getNotification("下载失败", -1))
            CommonUtil.showToast(this@DownloadService, "下载失败")
        }

        override fun onPaused() {
            downloadTaskS = null
            val downloadInfo = downloadQueue.poll()
            updateDbOfPause(downloadInfo.songId)
            getNotificationManager().notify(1, getNotification("下载已暂停: ${downloadInfo.songName}", -1))
            start() // 下载在下载列表中的歌曲
            downloadInfo.status = Constant.DOWNLOAD_PAUSED
            EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOAD_PAUSED, downloadInfo)) // 下载暂停
            CommonUtil.showToast(this@DownloadService, "下载已暂停")
        }

        override fun onCanceled() {
            downloadTaskS = null
            stopForeground(true)
            CommonUtil.showToast(this@DownloadService, "下载已取消")
        }

    }
    override fun onBind(intent: Intent?): IBinder? {
        return downloadBinder
    }

    inner class DownloadBinder : Binder(){
        // 开始下载
        fun startDownload(song: DownloadInfo){
            try {
                postDownloadEvent(song)
            } catch (e: Exception){
                e.printStackTrace()
            }
            if (downloadTaskS != null){
                CommonUtil.showToast(this@DownloadService, "已经加入加载队列")
            } else {
                CommonUtil.showToast(this@DownloadService, "开始下载")
                start()
            }
        }
        // 暂停下载
        fun pauseDownload(songId: String){
            // 暂停下载的歌曲是否为当前歌曲
            if (downloadTaskS != null && downloadQueue.peek().songId == songId){
                downloadTaskS!!.pauseDownload()
            } else {
                // 暂停的歌曲是下载队列的歌曲 就将该歌曲从下载队列中移除
                for (i in 0 until downloadQueue.size){
                    val downloadInfo = downloadQueue[i]
                    if (downloadInfo.songId == songId){
                        downloadQueue.removeAt(i)
                        updateDbOfPause(downloadInfo.songId)
                        downloadInfo.status = Constant.DOWNLOAD_PAUSED
                        EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOAD_PAUSED, downloadInfo)) // 下载暂停
                    }
                }
            }
        }
        // 取消下载
        fun cancelDownload(song: DownloadInfo){
            val songId = song.songId
            // 如果该歌曲正在下载 则需要将downloadTaskS置为空
            if (downloadTaskS != null && downloadQueue.peek().songId == songId){
                downloadTaskS!!.cancelDownload()
            }
            // 将该歌曲从下载队列中移除
            for (i in 0 until downloadQueue.size){
                val downloadInfo = downloadQueue[i]
                if (downloadInfo.songId == songId) downloadQueue.removeAt(i)
            }
            updateDb(songId)
            deleteDb(songId)
            // 取消下载需要将文件删除并将通知关闭
            if (song.url != null){
                checkoutFile(song, song.url) // 实际文件长度
            }
            // 通知正在下载列表
            EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOAD_CANCELED))
        }
    }

    fun start(){
        if (downloadTaskS == null && !downloadQueue.isEmpty()){
            val downloadInfo = downloadQueue.peek()
            val songList = LitePal.where("songId=?", downloadInfo.songId).find(DownloadInfo::class.java)
            val currentDownloadInfo = songList[0]
            currentDownloadInfo.status = Constant.DOWNLOAD_READY
            EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOADING, currentDownloadInfo))
            downloadTaskS = DownloadTaskS(listener)
            downloadTaskS!!.execute(currentDownloadInfo)
            getNotificationManager().notify(1, getNotification("正在下载: ${downloadInfo.songName}", 0))
        }
    }

    fun getNotificationManager() : NotificationManager {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun getNotification(title: String, progress: Int) : Notification{
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val id = "channel_001"
            val name = "下载通知"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            getNotificationManager().createNotificationChannel(mChannel)
            val builder = Notification.Builder(this, id)
            builder.setSmallIcon(R.mipmap.icon)
            builder.setContentIntent(pi)
            builder.setContentTitle(title)
            if (progress > 0){
                builder.setContentText("$progress%")
                builder.setProgress(100, progress, false)
            }
            return builder.build()
        } else {
            val builder = NotificationCompat.Builder(this, "default")
            builder.setSmallIcon(R.mipmap.icon)
            builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.icon))
            builder.setContentIntent(pi)
            builder.setContentTitle(title)
            if (progress > 0){
                builder.setContentText("$progress%")
                builder.setProgress(100, progress, false)
            }
            return builder.build()
        }
    }

    fun operateDb(downloadInfo: DownloadInfo){
        updateDb(downloadInfo.songId)
        deleteDb(downloadInfo.songId)
        EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOAD_SUCCESS)) //通知已下载列表
        EventBus.getDefault().post(SongListNumEvent(Constant.LIST_TYPE_DOWNLOAD)) //通知主界面的下载个数需要改变
    }
    //更新数据库中歌曲列表的位置，即下载完成歌曲后的位置都要减去1
    fun updateDb(songId : String){
        val id = LitePal.select("id").where("songId = ?", songId).find(DownloadInfo::class.java)[0].id
        val songIdList = LitePal.where("id > ?", id.toString()).find(DownloadInfo::class.java)
        for (song in songIdList){
            song.position = song.position - 1
            song.save()
        }
    }
    //暂停时更新列表歌曲状态
    fun updateDbOfPause(songId : String){
        val statusList = LitePal.where("songId = ?", songId).find(DownloadInfo::class.java, true)
        val downloadInfo = statusList[0]
        downloadInfo.status = Constant.DOWNLOAD_PAUSED
        downloadInfo.save()
    }
    //下载完成时要删除下载歌曲表中的数据以及关联表中的数据
    fun deleteDb(songId: String){
        LitePal.deleteAll(DownloadInfo::class.java, "songId=?",songId)
    }

    fun postDownloadEvent(downloadInfo: DownloadInfo){
        val downloadInfoList = LitePal.where("songId = ?", downloadInfo.songId).find(DownloadInfo::class.java,true)
        if (downloadInfoList.size != 0){
            val historyDownloadInfo = downloadInfoList[0]
            historyDownloadInfo.status = Constant.DOWNLOAD_WAIT
            historyDownloadInfo.save()
            EventBus.getDefault().post(DownloadEvent(Constant.DOWNLOAD_PAUSED, historyDownloadInfo))
            downloadQueue.offer(historyDownloadInfo)
            return
        }
        position = LitePal.findAll(DownloadInfo::class.java).size
        downloadInfo.position = position
        downloadInfo.status = Constant.DOWNLOAD_WAIT // 等待
        downloadInfo.save()
        downloadQueue.offer(downloadInfo) // 将歌曲放到等待队列中
        EventBus.getDefault().post(DownloadEvent(Constant.TYPE_DOWNLOAD_ADD))
    }

    /**
     * 获取歌曲的实际大小 判断是否存在于文件中
     */
    fun checkoutFile(song: DownloadInfo, downloadUrl: String){
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback{
            override fun onFailure(call: Call?, e: IOException?) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    val size = response.body().contentLength()
                    val fileName = DownloadUtil.getSaveSongFile(song.singer, song.songName, song.duration, song.songId, size)
                    val downloadFile = File(Api.STORAGE_SONG_FILE)
                    val directory = downloadFile.toString()
                    val file = File(fileName, directory)
                    if (file.exists()){
                        file.delete()
                    }
                    getNotificationManager().cancel(1)
                    stopForeground(true)
                }
            }
        })
    }

}