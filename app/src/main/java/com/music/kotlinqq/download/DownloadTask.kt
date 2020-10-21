package com.music.kotlinqq.download

import android.os.AsyncTask
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.bean.DownloadInfo
import com.music.kotlinqq.util.DownloadUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.lang.Exception

/**
 * @author cyl
 * @date 2020/9/16
 */
class DownloadTask(downloadListener: DownloadListener) : AsyncTask<DownloadInfo, DownloadInfo, Int>(){
    private val mDownListener = downloadListener
    private var isCanceled : Boolean = false
    private var isPaused : Boolean = false
    private var lastProgress : Long = 0

    override fun doInBackground(vararg downloadInfos: DownloadInfo): Int {
        var iss : InputStream? = null
        var saveFile : RandomAccessFile? = null
        var file : File? = null
        val downloadInfo = downloadInfos[0]
        try {
            var downloadedLength : Long = 0
            val downloadUrl = downloadInfo.url
            val downloadFile = File(Api.STORAGE_SONG_FILE)
            if (!downloadFile.exists()){
                downloadFile.mkdirs()
            }
            val contentLength = getContentLength(downloadUrl) // 实际文件长度
            val fileName = DownloadUtil.getSaveSongFile(downloadInfo.singer, downloadInfo.songName,downloadInfo.duration,downloadInfo.songId, contentLength)
            file = File(downloadFile, fileName)
            if (file.exists()){
                downloadedLength = file.length()
            }
            if (contentLength == 0L){
                return Constant.TYPE_DOWNLOAD_FAILED
            } else if (contentLength == downloadedLength){
                return Constant.TYPE_DOWNLOADED
            }
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("RANGE", "bytes=$downloadedLength-")
                .url(downloadUrl)
                .build()
            val response = client.newCall(request).execute()
            if (response != null){
                iss = response.body().byteStream()
                saveFile = RandomAccessFile(file, "rw")
                saveFile.seek(downloadedLength) // 跳过已经下载的字节
                val b = ByteArray(1024)
                var total : Int = 0
                val len = iss.read(b)
                while (len != -1){
                    when {
                        isCanceled -> return Constant.TYPE_DOWNLOAD_CANCELED
                        isPaused -> return Constant.TYPE_DOWNLOAD_PAUSED
                        else -> {
                            total += len
                            saveFile.write(b, 0, len)
                            val process = ((total + downloadedLength) * 100 / contentLength).toInt()
                            downloadInfo.progress = process
                            downloadInfo.totalSize = contentLength
                            downloadInfo.currentSize = total + downloadedLength
                            publishProgress(downloadInfo)
                        }
                    }
                }
                response.body().close()
                return Constant.TYPE_DOWNLOAD_SUCCESS
            }
        } catch (e : IOException){
            e.printStackTrace()
        } finally {
            try {
                iss!!.close()
                saveFile!!.close()
                if (isCanceled && file != null){
                    file.delete()
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        return Constant.TYPE_DOWNLOAD_FAILED
    }

    override fun onProgressUpdate(vararg downloadInfos : DownloadInfo) {
        val downloadInfo = downloadInfos[0]
        val progress = downloadInfo.progress
        if (progress > lastProgress){
            mDownListener.onProgress(downloadInfo)
            lastProgress = progress.toLong()
        }
    }

    fun pauseDownload(){
        isPaused = true
    }

    fun cancelDownload(){
        isCanceled = true
    }

    override fun onPostExecute(result: Int) {
        when (result){
            Constant.TYPE_DOWNLOAD_SUCCESS -> mDownListener.onSuccess()
            Constant.TYPE_DOWNLOAD_FAILED -> mDownListener.onFailed()
            Constant.TYPE_DOWNLOAD_PAUSED -> mDownListener.onPaused()
            Constant.TYPE_DOWNLOAD_CANCELED -> mDownListener.onCanceled()
            Constant.TYPE_DOWNLOADED -> mDownListener.onDownloaded()
        }
    }
    @Throws(IOException::class)
    fun getContentLength(downloadUrl: String) : Long {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        val response = client.newCall(request).execute()
        if (response != null && response.isSuccessful){
            val contentLength = response.body().contentLength()
            response.body().close()
            return contentLength
        }
        return 0
    }

}