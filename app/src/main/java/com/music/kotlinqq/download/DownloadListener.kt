package com.music.kotlinqq.download

import com.music.kotlinqq.bean.DownloadInfo

/**
 * @author cyl
 * @date 2020/9/16
 */
interface DownloadListener {

    fun onProgress(downloadInfo: DownloadInfo) // 下载进度

    fun onSuccess()  // 下载成功

    fun onDownloaded() // 已经下载的歌曲

    fun onFailed()  // 下载失败

    fun onPaused()  // 暂停下载

    fun onCanceled() // 取消下载
}