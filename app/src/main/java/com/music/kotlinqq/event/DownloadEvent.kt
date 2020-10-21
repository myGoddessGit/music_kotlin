package com.music.kotlinqq.event

import com.music.kotlinqq.bean.DownloadInfo

/**
 * @author cyl
 * @date 2020/9/16
 */
class DownloadEvent {

    private var downloadStatus : Int // 下载的状态
    private var downloadInfo = DownloadInfo()

    constructor(status: Int){
        this.downloadStatus = status
    }
    constructor(status: Int, downloadInfo: DownloadInfo){
        this.downloadStatus = status
        this.downloadInfo = downloadInfo
    }

    fun getDownloadStatus(): Int {
        return downloadStatus
    }

    fun getDownloadInfo(): DownloadInfo{
        return downloadInfo
    }
}